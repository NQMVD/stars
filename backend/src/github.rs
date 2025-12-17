use crate::models::GithubRelease;
use anyhow::{Context, Result};
use reqwest::{header, Client};
use tracing::{debug, instrument};

#[derive(Clone)]
pub struct GithubClient {
    client: Client,
}

impl GithubClient {
    pub fn new() -> Result<Self> {
        let mut headers = header::HeaderMap::new();
        headers.insert(
            header::USER_AGENT,
            header::HeaderValue::from_static("desktop-appstore-backend"),
        );

        // Use Basic Auth for OAuth App rate limits
        // https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting
        // "For OAuth Apps... you can use your client ID and secret to make unauthenticated calls with a higher rate limit."
        // Format: Basic base64(client_id:client_secret)
        // Actually, reqwest supports basic auth directly on the request builder.
        // But we can also just set it as a default header if we want, or per request.
        // Let's do it per request or use the client builder.
        // Wait, for OAuth apps making server-to-server calls, we usually pass client_id and client_secret as query params
        // OR use Basic Auth. Basic Auth is cleaner.

        let client = Client::builder().default_headers(headers).build()?;

        Ok(Self { client })
    }

    #[instrument(skip(self, client_id, client_secret))]
    pub async fn get_latest_release(
        &self,
        owner: &str,
        repo: &str,
        client_id: &str,
        client_secret: &str,
    ) -> Result<GithubRelease> {
        let url = format!(
            "https://api.github.com/repos/{}/{}/releases/latest",
            owner, repo
        );
        debug!("Requesting latest release from GitHub: {}", url);

        let response = self
            .client
            .get(&url)
            // .basic_auth(client_id, Some(client_secret))
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
        Ok(release)
    }

    #[instrument(skip(self, client_id, client_secret))]
    pub async fn download_asset(
        &self,
        url: &str,
        client_id: &str,
        client_secret: &str,
    ) -> Result<reqwest::Response> {
        // For assets, we might need to follow redirects. Reqwest does this by default.
        // Note: browser_download_url usually points to a location that redirects to S3/Azure.
        // If we use the API URL for the asset (e.g. /repos/:owner/:repo/releases/assets/:id), we need Accept: application/octet-stream.
        // But the user diagram says "api call to get asset".
        // Usually `browser_download_url` is a direct link (or redirect) to the binary.
        // If we want to proxy it, we just GET it.
        debug!("Downloading asset from: {}", url);

        let response = self
            .client
            .get(url)
            // We might not need auth for public assets, but if it's a private repo or we want the rate limit boost for the redirect lookup:
            // .basic_auth(client_id, Some(client_secret))
            .send()
            .await
            .context("Failed to fetch asset")?;

        if !response.status().is_success() {
            anyhow::bail!("Failed to download asset: {}", response.status());
        }

        debug!("Asset download started, status: {}", response.status());
        Ok(response)
    }

    /// Fetch the raw README content from a GitHub repository.
    /// Tries common README filenames (README.md, readme.md, README, etc.)
    #[instrument(skip(self))]
    pub async fn get_readme(&self, owner: &str, repo: &str) -> Result<String> {
        // GitHub provides a convenient API to get the README
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
        Ok(content)
    }
}
