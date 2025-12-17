use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct App {
    pub id: String,
    pub name: String,
    pub owner_login: String,
    pub created_at: Option<DateTime<Utc>>,
    pub updated_at: Option<DateTime<Utc>>,
}

// GitHub API Models

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct GithubRelease {
    pub tag_name: String,
    pub name: Option<String>,
    pub body: Option<String>,
    pub published_at: Option<DateTime<Utc>>,
    pub assets: Vec<GithubAsset>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct GithubAsset {
    pub name: String,
    pub browser_download_url: String,
    pub size: i64,
    pub content_type: String,
}
