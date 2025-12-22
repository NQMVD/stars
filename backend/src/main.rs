// allow unused of all sorts
#![allow(unused)]

use axum::body::Body;
use axum::http::header;
use axum::http::StatusCode;
use axum::{
    extract::{Path, State},
    response::{IntoResponse, Response},
    routing::get,
    Json, Router,
};
use chrono::DateTime;
use dotenvy::dotenv;
use polars::prelude::DataFrame;
use std::env;
use std::fmt;
use std::net::SocketAddr;
use std::sync::Arc;
use tracing_subscriber::{
    fmt::{format::Writer, time::FormatTime},
    layer::SubscriberExt,
    util::SubscriberInitExt,
};

mod db;
mod github;
mod models;

use crate::github::GithubClient;
use crate::models::GithubAsset;
use regex::Regex;

#[derive(Clone)]
struct AppState {
    df: Arc<DataFrame>,
    github_client: GithubClient,
    github_client_id: String,
    github_client_secret: String,
}

/// Retrieve and print the current wall-clock time.
#[derive(Debug, Clone, Copy, Eq, PartialEq, Default)]
pub struct JustTime;

impl FormatTime for JustTime {
    fn format_time(&self, w: &mut Writer<'_>) -> fmt::Result {
        write!(w, "{}", chrono::Local::now().format("%H:%M:%S"))
    }
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    dotenv().ok();

    if env::var("RUST_LOG_DETAIL")
        .unwrap_or_else(|_| "compact".into())
        .contains("source")
    {
        tracing_subscriber::registry()
            .with(tracing_subscriber::EnvFilter::new(
                env::var("RUST_LOG").unwrap_or_else(|_| "info".into()),
            ))
            .with(tracing_subscriber::fmt::layer().pretty())
            .init();
        tracing::debug!("Running in debug mode");
    } else {
        tracing_subscriber::registry()
            .with(tracing_subscriber::EnvFilter::new(
                env::var("RUST_LOG").unwrap_or_else(|_| "info".into()),
            ))
            .with(
                tracing_subscriber::fmt::layer()
                    .with_level(true)
                    .compact()
                    .with_timer(JustTime),
            )
            .init();
    }
    // println!("{:?}", env::var("RUST_LOG_DETAIL"));

    tracing::info!("Loading environment variables");

    let github_client_id = env::var("GITHUB_CLIENT_ID").expect("GITHUB_CLIENT_ID must be set");
    let github_client_secret =
        env::var("GITHUB_CLIENT_SECRET").expect("GITHUB_CLIENT_SECRET must be set");
    let port = env::var("PORT")
        .unwrap_or_else(|_| "4200".to_string())
        .parse::<u16>()?;
    let csv_path = env::var("CSV_PATH").expect("CSV_PATH must be set");

    tracing::info!("Loading data from {}", &csv_path);

    let df = db::init_db(&csv_path)?;
    let df = Arc::new(df);

    let github_client = GithubClient::new()?;

    let state = Arc::new(AppState {
        df,
        github_client,
        github_client_id,
        github_client_secret,
    });

    let app = Router::new()
        .route("/api/apps", get(get_apps))
        // get catalog info, file name and git commit hash
        .route("/api/catalog_info", get(get_catalog_info))
        .route("/api/apps/:id/latest", get(get_latest_version))
        .route("/api/apps/:id/download", get(download_asset))
        .route("/api/apps/:id/screenshots", get(get_screenshots))
        // version of running backend service for compatibility
        .route("/api/version", get(get_version))
        .with_state(state);

    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    tracing::info!("listening on {}", addr);
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;

    Ok(())
}

async fn get_apps(State(state): State<Arc<AppState>>) -> Result<Json<Vec<models::App>>, AppError> {
    tracing::info!("Handling get_apps");
    let apps = db::get_all_apps(&state.df)?;
    Ok(Json(apps))
}

async fn get_catalog_info(State(state): State<Arc<AppState>>) -> Result<Json<String>, AppError> {
    tracing::info!("Handling get_catalog_info");
    // INFO: use dummy data for now
    let catalog_name = "dummy_catalog_name";
    let git_commit_hash = "dummy_git_commit_hash";
    let catalog_info = format!(
        "Catalog Name: {}, Git Commit Hash: {}",
        catalog_name, git_commit_hash
    );
    Ok(Json(catalog_info))
}

async fn get_latest_version(
    State(state): State<Arc<AppState>>,
    Path(app_id): Path<String>,
) -> Result<Json<serde_json::Value>, AppError> {
    tracing::info!("Handling get_latest_version for app_id: {}", app_id);

    // 1. Get App metadata
    tracing::debug!("Fetching app metadata from DataFrame");
    let app = db::get_app(&state.df, &app_id)?.ok_or_else(|| {
        tracing::warn!("App {} not found in DB", app_id);
        AppError::NotFound(format!("App {} not found", app_id))
    })?;
    tracing::debug!("Found app: {:?}", app.name);

    // 2. Fetch latest release from GitHub
    // No caching for now as per plan
    tracing::debug!(
        "Fetching latest release from GitHub for {}/{}",
        app.owner_login,
        app.name
    );
    let gh_release = state
        .github_client
        .get_latest_release(
            &app.owner_login,
            &app.name,
            &state.github_client_id,
            &state.github_client_secret,
        )
        .await?;
    tracing::debug!("Got release: {}", gh_release.tag_name);

    let release_json = serde_json::to_value(&gh_release).unwrap();
    Ok(Json(release_json))
}

async fn download_asset(
    State(state): State<Arc<AppState>>,
    Path(app_id): Path<String>,
) -> Result<Response, AppError> {
    tracing::info!("Handling download_asset for app_id: {}", app_id);

    // 1. Get App metadata
    tracing::debug!("Fetching app metadata from DataFrame");
    let app = db::get_app(&state.df, &app_id)?.ok_or_else(|| {
        tracing::warn!("App {} not found in DB", app_id);
        AppError::NotFound(format!("App {} not found", app_id))
    })?;

    // 2. Get latest release info from GitHub
    tracing::debug!("Fetching latest release info from GitHub to find asset");
    let gh_release = state
        .github_client
        .get_latest_release(
            &app.owner_login,
            &app.name,
            &state.github_client_id,
            &state.github_client_secret,
        )
        .await?;

    // 3. Find the asset
    let asset = gh_release.assets.first().ok_or_else(|| {
        tracing::warn!("No assets found in release {}", gh_release.tag_name);
        AppError::NotFound("No assets found in release".to_string())
    })?;
    tracing::info!("Found asset: {} (size: {} bytes)", asset.name, asset.size);

    // 4. Stream it
    tracing::info!(
        "Starting download stream from: {}",
        asset.browser_download_url
    );
    let resp = state
        .github_client
        .download_asset(
            &asset.browser_download_url,
            &state.github_client_id,
            &state.github_client_secret,
        )
        .await?;
    tracing::info!("Got response from GitHub asset download: {}", resp.status());

    let content_type = resp
        .headers()
        .get(reqwest::header::CONTENT_TYPE)
        .and_then(|v| v.to_str().ok())
        .unwrap_or("application/octet-stream")
        .to_string();

    let content_length = resp
        .headers()
        .get(reqwest::header::CONTENT_LENGTH)
        .and_then(|v| v.to_str().ok())
        .map(|v| v.to_string());

    let stream = Body::from_stream(resp.bytes_stream());

    let mut builder = Response::builder().header(header::CONTENT_TYPE, content_type);

    if let Some(len) = content_length {
        builder = builder.header(header::CONTENT_LENGTH, len);
    }

    builder = builder.header(
        header::CONTENT_DISPOSITION,
        format!("attachment; filename=\"{}\"", asset.name),
    );

    Ok(builder.body(stream).unwrap())
}

/// Get screenshots (images) extracted from the app's README file.
/// Parses markdown image syntax: ![alt](url) and HTML img tags.
async fn get_screenshots(
    State(state): State<Arc<AppState>>,
    Path(app_id): Path<String>,
) -> Result<Json<Vec<String>>, AppError> {
    tracing::info!("Handling get_screenshots for app_id: {}", app_id);

    // 1. Get App metadata
    let app = db::get_app(&state.df, &app_id)?.ok_or_else(|| {
        tracing::warn!("App {} not found in DB", app_id);
        AppError::NotFound(format!("App {} not found", app_id))
    })?;

    // 2. Fetch README from GitHub
    let readme_content = match state
        .github_client
        .get_readme(&app.owner_login, &app.name)
        .await
    {
        Ok(content) => content,
        Err(e) => {
            tracing::warn!("Failed to fetch README for {}: {}", app_id, e);
            // Return empty list if README not found
            return Ok(Json(Vec::new()));
        }
    };

    // 3. Extract image URLs from markdown
    let image_urls = extract_images_from_markdown(&readme_content, &app.owner_login, &app.name);
    tracing::info!("Found {} images in README for {}", image_urls.len(), app_id);
    tracing::debug!("Image URLs: {:?}", image_urls);

    Ok(Json(image_urls))
}

/// Extract image URLs from markdown content.
/// Supports:
/// - Markdown syntax: ![alt text](url)
/// - HTML img tags: <img src="url" ...>
/// - Relative URLs are converted to raw GitHub URLs
fn extract_images_from_markdown(content: &str, owner: &str, repo: &str) -> Vec<String> {
    let mut urls = Vec::new();

    // Pattern for markdown images: ![alt](url)
    let md_pattern = Regex::new(r"!\[[^\]]*\]\(([^)]+)\)").unwrap();

    // Pattern for HTML img tags: <img ... src="url" ...>
    let html_pattern = Regex::new(r#"<img[^>]+src=["']([^"']+)["']"#).unwrap();

    // Extract markdown image URLs
    for cap in md_pattern.captures_iter(content) {
        if let Some(url) = cap.get(1) {
            let url_str = url.as_str().trim();
            // Handle markdown titles: "url title" -> "url"
            let url_clean = url_str.split_whitespace().next().unwrap_or(url_str);
            let resolved = resolve_github_url(url_clean, owner, repo);
            if is_image_url(&resolved) {
                urls.push(resolved);
            }
        }
    }

    // Extract HTML img src URLs
    for cap in html_pattern.captures_iter(content) {
        if let Some(url) = cap.get(1) {
            let url_str = url.as_str().trim();
            let resolved = resolve_github_url(url_str, owner, repo);
            if is_image_url(&resolved) && !urls.contains(&resolved) {
                urls.push(resolved);
            }
        }
    }

    urls
}

/// Resolve relative URLs to absolute GitHub raw URLs
fn resolve_github_url(url: &str, owner: &str, repo: &str) -> String {
    // Already absolute URL
    if url.starts_with("http://") || url.starts_with("https://") {
        // Fix GitHub blob URLs to raw URLs to avoid redirects and HTML pages
        if url.contains("github.com") && url.contains("/blob/") {
            return url
                .replace("github.com", "raw.githubusercontent.com")
                .replace("/blob/", "/");
        }
        return url.to_string();
    }

    // Remove leading ./ or /
    let path = url.trim_start_matches("./").trim_start_matches('/');

    // Convert to raw GitHub URL (default branch)
    format!(
        "https://raw.githubusercontent.com/{}/{}/HEAD/{}",
        owner, repo, path
    )
}

/// Check if a URL looks like an image (by extension or known image hosts)
fn is_image_url(url: &str) -> bool {
    let lower = url.to_lowercase();

    // Common image extensions
    let image_extensions = [
        ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".bmp", ".ico",
    ];
    if image_extensions.iter().any(|ext| lower.contains(ext)) {
        return true;
    }

    // Known image hosting services
    let image_hosts = [
        "imgur.com",
        "i.imgur.com",
        "raw.githubusercontent.com",
        "user-images.githubusercontent.com",
        "camo.githubusercontent.com",
        "github.com/user-attachments",
    ];
    if image_hosts.iter().any(|host| lower.contains(host)) {
        return true;
    }

    false
}

#[tracing::instrument]
async fn get_version() -> Result<Response, AppError> {
    let version = env!("CARGO_PKG_VERSION");
    Ok(Response::builder()
        .status(StatusCode::OK)
        .header(header::CONTENT_TYPE, "text/plain")
        .body(Body::from(version))
        .unwrap())
}

enum AppError {
    NotFound(String),
    Internal(anyhow::Error),
}

impl IntoResponse for AppError {
    fn into_response(self) -> Response {
        let (status, message) = match self {
            AppError::NotFound(msg) => (StatusCode::NOT_FOUND, msg),
            AppError::Internal(err) => {
                tracing::error!("Internal error: {:#}", err);
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    "Internal server error".to_string(),
                )
            }
        };

        (status, Json(serde_json::json!({ "error": message }))).into_response()
    }
}

impl<E> From<E> for AppError
where
    E: Into<anyhow::Error>,
{
    fn from(err: E) -> AppError {
        AppError::Internal(err.into())
    }
}
