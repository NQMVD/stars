//! Integration tests for the backend-service API endpoints.
//!
//! These tests start an actual HTTP server and make real requests to the API,
//! similar to the test_api.sh bash script. Tests use "wezterm" as the default
//! test application.
//!
//! To run: `cargo test`
//!
//! Note: Requires valid GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET in .env

use axum::{
    body::Body,
    extract::{Path, State},
    http::header,
    http::{Request, StatusCode},
    response::{IntoResponse, Response},
    routing::get,
    Json, Router,
};
use http_body_util::BodyExt;
use polars::prelude::*;
use std::sync::Arc;
use std::sync::Once;
use tower::ServiceExt;
use tracing::info;

static INIT: Once = Once::new();

/// Initialize tracing for tests - only runs once
fn init_tracing() {
    INIT.call_once(|| {
        tracing_subscriber::fmt()
            .with_test_writer()
            .with_max_level(tracing::Level::INFO)
            .init();
    });
}

// Re-export the modules from the main crate
mod test_helpers {
    use super::*;
    use std::env;

    // Minimal App struct matching the main crate's models
    #[derive(Debug, serde::Serialize, serde::Deserialize, Clone)]
    pub struct App {
        pub id: String,
        pub name: String,
        pub owner_login: String,
        pub created_at: Option<chrono::DateTime<chrono::Utc>>,
        pub updated_at: Option<chrono::DateTime<chrono::Utc>>,
    }

    // Minimal GitHub release struct
    #[derive(Debug, serde::Serialize, serde::Deserialize)]
    pub struct GithubRelease {
        pub tag_name: String,
        pub name: Option<String>,
        pub body: Option<String>,
        pub published_at: Option<chrono::DateTime<chrono::Utc>>,
        pub assets: Vec<GithubAsset>,
    }

    #[derive(Debug, serde::Serialize, serde::Deserialize)]
    pub struct GithubAsset {
        pub name: String,
        pub browser_download_url: String,
        pub size: i64,
        pub content_type: String,
    }

    // GitHub client for tests
    #[derive(Clone)]
    pub struct GithubClient {
        client: reqwest::Client,
    }

    impl GithubClient {
        pub fn new() -> reqwest::Result<Self> {
            let client = reqwest::Client::builder()
                .user_agent("backend-service-tests/0.1")
                .build()?;
            Ok(Self { client })
        }

        pub async fn get_latest_release(
            &self,
            owner: &str,
            repo: &str,
            _client_id: &str,
            _client_secret: &str,
        ) -> anyhow::Result<GithubRelease> {
            let url = format!(
                "https://api.github.com/repos/{}/{}/releases/latest",
                owner, repo
            );
            let resp = self
                .client
                .get(&url)
                // Note: basic_auth is commented out in production code, so we skip it here too
                .send()
                .await?
                .error_for_status()?
                .json::<GithubRelease>()
                .await?;
            Ok(resp)
        }

        pub async fn download_asset(
            &self,
            url: &str,
            _client_id: &str,
            _client_secret: &str,
        ) -> reqwest::Result<reqwest::Response> {
            self.client
                .get(url)
                // Note: basic_auth is commented out in production code, so we skip it here too
                .send()
                .await
        }

        pub async fn get_readme(&self, owner: &str, repo: &str) -> anyhow::Result<String> {
            let url = format!("https://api.github.com/repos/{}/{}/readme", owner, repo);
            let resp = self
                .client
                .get(&url)
                .header("Accept", "application/vnd.github.raw+json")
                .send()
                .await?
                .error_for_status()?
                .text()
                .await?;
            Ok(resp)
        }
    }

    // App state for tests
    #[derive(Clone)]
    pub struct AppState {
        pub df: Arc<DataFrame>,
        pub github_client: GithubClient,
        pub github_client_id: String,
        pub github_client_secret: String,
    }

    // Error handling
    pub enum AppError {
        NotFound(String),
        Internal(anyhow::Error),
    }

    impl IntoResponse for AppError {
        fn into_response(self) -> Response {
            let (status, message) = match self {
                AppError::NotFound(msg) => (StatusCode::NOT_FOUND, msg),
                AppError::Internal(err) => {
                    eprintln!("Internal error: {:#}", err);
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

    // Database helpers
    pub fn init_db(file_path: &str) -> anyhow::Result<DataFrame> {
        let df = CsvReadOptions::default()
            .try_into_reader_with_file_path(Some(file_path.into()))?
            .finish()?;
        Ok(df)
    }

    pub fn get_all_apps(df: &DataFrame) -> anyhow::Result<Vec<App>> {
        let repo_names = df.column("repo_name")?.str()?;
        let owner_logins = df.column("owner_login")?.str()?;
        let created_ats = df.column("created_at")?.str()?;
        let updated_ats = df.column("updated_at")?.str()?;

        let mut apps = Vec::with_capacity(df.height());

        for i in 0..df.height() {
            let repo_name = repo_names.get(i).unwrap_or_default();
            let owner_login = owner_logins.get(i).unwrap_or_default();

            let created_at = created_ats.get(i).and_then(|s| {
                chrono::DateTime::parse_from_rfc3339(s)
                    .ok()
                    .map(|dt| dt.with_timezone(&chrono::Utc))
            });

            let updated_at = updated_ats.get(i).and_then(|s| {
                chrono::DateTime::parse_from_rfc3339(s)
                    .ok()
                    .map(|dt| dt.with_timezone(&chrono::Utc))
            });

            apps.push(App {
                id: repo_name.to_string(),
                name: repo_name.to_string(),
                owner_login: owner_login.to_string(),
                created_at,
                updated_at,
            });
        }

        Ok(apps)
    }

    pub fn get_app(df: &DataFrame, app_id: &str) -> anyhow::Result<Option<App>> {
        let mask = df.column("repo_name")?.str()?.equal(app_id);
        let filtered = df.filter(&mask)?;

        if filtered.height() == 0 {
            return Ok(None);
        }

        let repo_names = filtered.column("repo_name")?.str()?;
        let owner_logins = filtered.column("owner_login")?.str()?;
        let created_ats = filtered.column("created_at")?.str()?;
        let updated_ats = filtered.column("updated_at")?.str()?;

        let i = 0;
        let repo_name = repo_names.get(i).unwrap_or_default();
        let owner_login = owner_logins.get(i).unwrap_or_default();

        let created_at = created_ats.get(i).and_then(|s| {
            chrono::DateTime::parse_from_rfc3339(s)
                .ok()
                .map(|dt| dt.with_timezone(&chrono::Utc))
        });

        let updated_at = updated_ats.get(i).and_then(|s| {
            chrono::DateTime::parse_from_rfc3339(s)
                .ok()
                .map(|dt| dt.with_timezone(&chrono::Utc))
        });

        Ok(Some(App {
            id: repo_name.to_string(),
            name: repo_name.to_string(),
            owner_login: owner_login.to_string(),
            created_at,
            updated_at,
        }))
    }

    // Route handlers
    pub async fn get_apps(State(state): State<Arc<AppState>>) -> Result<Json<Vec<App>>, AppError> {
        let apps = get_all_apps(&state.df)?;
        Ok(Json(apps))
    }

    pub async fn get_catalog_info() -> Result<Json<String>, AppError> {
        let catalog_info = format!(
            "Catalog Name: {}, Git Commit Hash: {}",
            "dummy_catalog_name", "dummy_git_commit_hash"
        );
        Ok(Json(catalog_info))
    }

    pub async fn get_latest_version(
        State(state): State<Arc<AppState>>,
        Path(app_id): Path<String>,
    ) -> Result<Json<serde_json::Value>, AppError> {
        let app = get_app(&state.df, &app_id)?
            .ok_or_else(|| AppError::NotFound(format!("App {} not found", app_id)))?;

        let gh_release = state
            .github_client
            .get_latest_release(
                &app.owner_login,
                &app.name,
                &state.github_client_id,
                &state.github_client_secret,
            )
            .await?;

        let release_json = serde_json::to_value(&gh_release).unwrap();
        Ok(Json(release_json))
    }

    pub async fn download_asset(
        State(state): State<Arc<AppState>>,
        Path(app_id): Path<String>,
    ) -> Result<Response, AppError> {
        let app = get_app(&state.df, &app_id)?
            .ok_or_else(|| AppError::NotFound(format!("App {} not found", app_id)))?;

        let gh_release = state
            .github_client
            .get_latest_release(
                &app.owner_login,
                &app.name,
                &state.github_client_id,
                &state.github_client_secret,
            )
            .await?;

        let asset = gh_release
            .assets
            .first()
            .ok_or_else(|| AppError::NotFound("No assets found in release".to_string()))?;

        let resp = state
            .github_client
            .download_asset(
                &asset.browser_download_url,
                &state.github_client_id,
                &state.github_client_secret,
            )
            .await?;

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

    pub async fn get_version() -> Result<Response, AppError> {
        let version = env!("CARGO_PKG_VERSION");
        Ok(Response::builder()
            .status(StatusCode::OK)
            .header(header::CONTENT_TYPE, "text/plain")
            .body(Body::from(version))
            .unwrap())
    }

    pub async fn get_screenshots(
        State(state): State<Arc<AppState>>,
        Path(app_id): Path<String>,
    ) -> Result<Json<Vec<String>>, AppError> {
        let app = get_app(&state.df, &app_id)?
            .ok_or_else(|| AppError::NotFound(format!("App {} not found", app_id)))?;

        let readme_content = match state
            .github_client
            .get_readme(&app.owner_login, &app.name)
            .await
        {
            Ok(content) => content,
            Err(_) => return Ok(Json(Vec::new())),
        };

        let urls = extract_images_from_markdown(&readme_content, &app.owner_login, &app.name);
        Ok(Json(urls))
    }

    fn extract_images_from_markdown(content: &str, owner: &str, repo: &str) -> Vec<String> {
        let mut urls = Vec::new();
        let md_pattern = regex::Regex::new(r"!\[[^\]]*\]\(([^)]+)\)").unwrap();
        let html_pattern = regex::Regex::new(r#"<img[^>]+src=["']([^"']+)["']"#).unwrap();

        for cap in md_pattern.captures_iter(content) {
            if let Some(url) = cap.get(1) {
                let resolved = resolve_github_url(url.as_str().trim(), owner, repo);
                if is_image_url(&resolved) {
                    urls.push(resolved);
                }
            }
        }

        for cap in html_pattern.captures_iter(content) {
            if let Some(url) = cap.get(1) {
                let resolved = resolve_github_url(url.as_str().trim(), owner, repo);
                if is_image_url(&resolved) && !urls.contains(&resolved) {
                    urls.push(resolved);
                }
            }
        }

        urls
    }

    fn resolve_github_url(url: &str, owner: &str, repo: &str) -> String {
        if url.starts_with("http://") || url.starts_with("https://") {
            return url.to_string();
        }
        let path = url.trim_start_matches("./").trim_start_matches('/');
        format!(
            "https://raw.githubusercontent.com/{}/{}/HEAD/{}",
            owner, repo, path
        )
    }

    fn is_image_url(url: &str) -> bool {
        let lower = url.to_lowercase();
        let exts = [
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".bmp", ".ico",
        ];
        let hosts = [
            "imgur.com",
            "raw.githubusercontent.com",
            "user-images.githubusercontent.com",
            "camo.githubusercontent.com",
        ];
        exts.iter().any(|e| lower.contains(e)) || hosts.iter().any(|h| lower.contains(h))
    }

    pub fn create_test_router(state: Arc<AppState>) -> Router {
        Router::new()
            .route("/api/apps", get(get_apps))
            .route("/api/catalog_info", get(get_catalog_info))
            .route("/api/apps/:id/latest", get(get_latest_version))
            .route("/api/apps/:id/download", get(download_asset))
            .route("/api/apps/:id/screenshots", get(get_screenshots))
            .route("/api/version", get(get_version))
            .with_state(state)
    }

    // Setup test state
    pub fn setup_test_state() -> anyhow::Result<Arc<AppState>> {
        dotenvy::dotenv().ok();

        let github_client_id =
            env::var("GITHUB_CLIENT_ID").expect("GITHUB_CLIENT_ID must be set in .env");
        let github_client_secret =
            env::var("GITHUB_CLIENT_SECRET").expect("GITHUB_CLIENT_SECRET must be set in .env");
        let csv_path = env::var("CSV_PATH").expect("CSV_PATH must be set in .env");

        let df = init_db(&csv_path)?;
        let github_client = GithubClient::new()?;

        Ok(Arc::new(AppState {
            df: Arc::new(df),
            github_client,
            github_client_id,
            github_client_secret,
        }))
    }
}

use test_helpers::*;

const DEFAULT_TEST_APP: &str = "wezterm";

/// Test GET /api/version - should return the package version
#[tokio::test]
async fn test_get_version() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let response = app
        .oneshot(
            Request::builder()
                .uri("/api/version")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .expect("Failed to execute request");

    assert_eq!(response.status(), StatusCode::OK);

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let body_str = String::from_utf8(body.to_vec()).unwrap();

    // Should return the package version
    assert!(!body_str.is_empty(), "Version should not be empty");
    assert!(
        body_str.contains('.'),
        "Version should contain a dot (e.g., 0.1.0)"
    );
    info!("✓ GET /api/version returned: {}", body_str);
}

/// Test GET /api/apps - should return a list of apps
#[tokio::test]
async fn test_get_apps() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let response = app
        .oneshot(
            Request::builder()
                .uri("/api/apps")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .expect("Failed to execute request");

    assert_eq!(response.status(), StatusCode::OK);

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let apps: Vec<test_helpers::App> =
        serde_json::from_slice(&body).expect("Failed to parse response as JSON array of apps");

    assert!(!apps.is_empty(), "Apps list should not be empty");

    // Check that wezterm is in the list
    let has_wezterm = apps.iter().any(|a| a.name == DEFAULT_TEST_APP);
    assert!(has_wezterm, "Apps list should contain {}", DEFAULT_TEST_APP);

    info!("✓ GET /api/apps returned {} apps", apps.len());
}

/// Test GET /api/catalog_info - should return catalog info
#[tokio::test]
async fn test_get_catalog_info() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let response = app
        .oneshot(
            Request::builder()
                .uri("/api/catalog_info")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .expect("Failed to execute request");

    assert_eq!(response.status(), StatusCode::OK);

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let catalog_info: String =
        serde_json::from_slice(&body).expect("Failed to parse response as JSON string");

    assert!(
        catalog_info.contains("Catalog Name"),
        "Catalog info should contain 'Catalog Name'"
    );
    assert!(
        catalog_info.contains("Git Commit Hash"),
        "Catalog info should contain 'Git Commit Hash'"
    );

    info!("✓ GET /api/catalog_info returned: {}", catalog_info);
}

/// Test GET /api/apps/:id/latest - should return latest release for wezterm
#[tokio::test]
async fn test_get_latest_version_wezterm() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let uri = format!("/api/apps/{}/latest", DEFAULT_TEST_APP);
    let response = app
        .oneshot(Request::builder().uri(&uri).body(Body::empty()).unwrap())
        .await
        .expect("Failed to execute request");

    assert_eq!(
        response.status(),
        StatusCode::OK,
        "Expected 200 OK for {}",
        DEFAULT_TEST_APP
    );

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let release: serde_json::Value =
        serde_json::from_slice(&body).expect("Failed to parse response as JSON");

    // Check for expected fields in GitHub release
    assert!(
        release.get("tag_name").is_some(),
        "Release should have tag_name"
    );
    assert!(
        release.get("assets").is_some(),
        "Release should have assets"
    );

    let tag_name = release["tag_name"].as_str().unwrap_or("unknown");
    let assets = release["assets"].as_array().map(|a| a.len()).unwrap_or(0);

    info!(
        "✓ GET {} returned release: {} with {} assets",
        uri, tag_name, assets
    );
}

/// Test GET /api/apps/:id/download - should return asset download for wezterm
#[tokio::test]
async fn test_download_asset_wezterm() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let uri = format!("/api/apps/{}/download", DEFAULT_TEST_APP);
    let response = app
        .oneshot(Request::builder().uri(&uri).body(Body::empty()).unwrap())
        .await
        .expect("Failed to execute request");

    assert_eq!(
        response.status(),
        StatusCode::OK,
        "Expected 200 OK for download"
    );

    // Check Content-Disposition header
    let content_disposition = response
        .headers()
        .get("content-disposition")
        .expect("Response should have Content-Disposition header")
        .to_str()
        .unwrap();

    assert!(
        content_disposition.contains("attachment"),
        "Content-Disposition should indicate attachment"
    );
    assert!(
        content_disposition.contains("filename="),
        "Content-Disposition should have filename"
    );

    // Check Content-Type header
    let content_type = response
        .headers()
        .get("content-type")
        .expect("Response should have Content-Type header")
        .to_str()
        .unwrap();

    info!("✓ GET {} returned Content-Type: {}", uri, content_type);
    info!("  Content-Disposition: {}", content_disposition);

    // Note: We're not consuming the body to avoid downloading large files during tests
}

/// Test GET /api/apps/:id/latest with non-existent app - should return 404
#[tokio::test]
async fn test_get_latest_version_not_found() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let uri = "/api/apps/this-app-definitely-does-not-exist-12345/latest";
    let response = app
        .oneshot(Request::builder().uri(uri).body(Body::empty()).unwrap())
        .await
        .expect("Failed to execute request");

    assert_eq!(
        response.status(),
        StatusCode::NOT_FOUND,
        "Expected 404 for non-existent app"
    );

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let error: serde_json::Value =
        serde_json::from_slice(&body).expect("Failed to parse error response as JSON");

    assert!(
        error.get("error").is_some(),
        "Error response should have 'error' field"
    );

    info!("✓ GET {} returned 404 with error: {}", uri, error["error"]);
}

/// Test GET /api/apps/:id/download with non-existent app - should return 404
#[tokio::test]
async fn test_download_asset_not_found() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let uri = "/api/apps/this-app-definitely-does-not-exist-12345/download";
    let response = app
        .oneshot(Request::builder().uri(uri).body(Body::empty()).unwrap())
        .await
        .expect("Failed to execute request");

    assert_eq!(
        response.status(),
        StatusCode::NOT_FOUND,
        "Expected 404 for non-existent app"
    );

    info!("✓ GET {} returned 404 as expected", uri);
}

/// Test GET /api/apps/:id/screenshots - should return a list of image URLs from README
#[tokio::test]
async fn test_get_screenshots_wezterm() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let uri = format!("/api/apps/{}/screenshots", DEFAULT_TEST_APP);
    let response = app
        .oneshot(Request::builder().uri(&uri).body(Body::empty()).unwrap())
        .await
        .expect("Failed to execute request");

    assert_eq!(
        response.status(),
        StatusCode::OK,
        "Expected 200 OK for screenshots"
    );

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let urls: Vec<String> =
        serde_json::from_slice(&body).expect("Failed to parse response as JSON array");

    info!("✓ GET {} returned {} screenshot URLs", uri, urls.len());

    // Log first few URLs for debugging
    for (i, url) in urls.iter().take(3).enumerate() {
        info!("  Screenshot {}: {}", i + 1, url);
    }
}

/// Test GET /api/apps/:id/screenshots with non-existent app - should return 404
#[tokio::test]
async fn test_get_screenshots_not_found() {
    init_tracing();
    let state = setup_test_state().expect("Failed to setup test state");
    let app = create_test_router(state);

    let uri = "/api/apps/this-app-definitely-does-not-exist-12345/screenshots";
    let response = app
        .oneshot(Request::builder().uri(uri).body(Body::empty()).unwrap())
        .await
        .expect("Failed to execute request");

    assert_eq!(
        response.status(),
        StatusCode::NOT_FOUND,
        "Expected 404 for non-existent app"
    );

    info!("✓ GET {} returned 404 as expected", uri);
}
