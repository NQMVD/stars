use crate::cache::{CacheManager, CachedRelease};
use crate::models::GithubRelease;
use anyhow::{Context, Result};
use reqwest::{header, Client};
use serde::Serialize;
use tracing::{debug, instrument};

#[derive(Clone)]
pub struct GithubClient {
    client: Client,
    cache: CacheManager,
}

impl GithubClient {
    pub fn new(cache: CacheManager) -> Result<Self> {
        let mut headers = header::HeaderMap::new();
        headers.insert(
            header::USER_AGENT,
            header::HeaderValue::from_static("desktop-appstore-backend"),
        );

        let client = Client::builder().default_headers(headers).build()?;

        Ok(Self { client, cache })
    }

    #[instrument(skip(self, client_id, client_secret))]
    pub async fn get_latest_release(
        &self,
        owner: &str,
        repo: &str,
        client_id: &str,
        client_secret: &str,
    ) -> Result<GithubRelease> {
        let cache_key = format!("{}/{}", owner, repo);

        if let Some(cached) = self.cache.release_cache.get(&cache_key).await {
            debug!("Cache hit for release: {}", cache_key);
            let release: GithubRelease = serde_json::from_slice(&cached.data)?;
            return Ok(release);
        }

        debug!("Cache miss for release: {}, fetching from GitHub", cache_key);

        let url = format!(
            "https://api.github.com/repos/{}/{}/releases/latest",
            owner, repo
        );
        debug!("Requesting latest release from GitHub: {}", url);

        let response = self
            .client
            .get(&url)
            .send()
            .await
            .context("Failed to send request to GitHub")?;

        debug!("GitHub response status: {}", response.status());

        if !response.status().is_success() {
            let status = response.status();
            let text = response.text().await.unwrap_or_default();
            anyhow::bail!("GitHub API error: {} - {}", status, text);
        }

        let release = response
            .json::<GithubRelease>()
            .await
            .context("Failed to parse GitHub response")?;
        debug!("Successfully parsed release: {}", release.tag_name);

        let data = serde_json::to_vec(&release)?;
        let cached_release = CachedRelease {
            data,
            cached_at: chrono::Utc::now(),
        };
        self.cache
            .release_cache
            .insert(cache_key, cached_release)
            .await;

        Ok(release)
    }

    #[instrument(skip(self, client_id, client_secret))]
    pub async fn download_asset(
        &self,
        url: &str,
        client_id: &str,
        client_secret: &str,
    ) -> Result<reqwest::Response> {
        debug!("Downloading asset from: {}", url);

        let response = self
            .client
            .get(url)
            .send()
            .await
            .context("Failed to fetch asset")?;

        if !response.status().is_success() {
            anyhow::bail!("Failed to download asset: {}", response.status());
        }

        debug!("Asset download started, status: {}", response.status());
        Ok(response)
    }

    #[instrument(skip(self))]
    pub async fn get_readme(&self, owner: &str, repo: &str) -> Result<String> {
        let cache_key = format!("{}/{}/readme", owner, repo);

        if let Some(cached) = self.cache.readme_cache.get(&cache_key).await {
            debug!("Cache hit for README: {}", cache_key);
            return Ok(cached.content.clone());
        }

        debug!("Cache miss for README: {}, fetching from GitHub", cache_key);

        let url = format!("https://api.github.com/repos/{}/{}/readme", owner, repo);
        debug!("Fetching README from: {}", url);

        let response = self
            .client
            .get(&url)
            .header("Accept", "application/vnd.github.raw+json")
            .send()
            .await
            .context("Failed to fetch README from GitHub")?;

        if !response.status().is_success() {
            let status = response.status();
            let text = response.text().await.unwrap_or_default();
            anyhow::bail!("GitHub API error fetching README: {} - {}", status, text);
        }

        let content = response
            .text()
            .await
            .context("Failed to read README content")?;
        debug!("Successfully fetched README ({} bytes)", content.len());

        let cached_readme = crate::cache::CachedReadme {
            content: content.clone(),
            cached_at: chrono::Utc::now(),
        };
        self.cache.readme_cache.insert(cache_key, cached_readme).await;

        Ok(content)
    }
}
