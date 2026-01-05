use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use std::str::FromStr;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct App {
    pub id: String,
    pub name: String,
    pub owner_login: String,
    pub created_at: Option<DateTime<Utc>>,
    pub updated_at: Option<DateTime<Utc>>,
    pub windows_support: bool,
    pub macos_support: bool,
    pub linux_support: bool,
}

#[derive(Debug, Serialize, Deserialize, Clone, PartialEq)]
pub enum Platform {
    #[serde(rename = "windows")]
    Windows,
    #[serde(rename = "macos")]
    Macos,
    #[serde(rename = "linux_deb")]
    LinuxDeb,
    #[serde(rename = "linux_rpm")]
    LinuxRpm,
    #[serde(rename = "linux_arch")]
    LinuxArch,
    #[serde(rename = "linux_generic")]
    LinuxGeneric,
}

impl FromStr for Platform {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_lowercase().as_str() {
            "windows" => Ok(Platform::Windows),
            "macos" => Ok(Platform::Macos),
            "linux_deb" => Ok(Platform::LinuxDeb),
            "linux_rpm" => Ok(Platform::LinuxRpm),
            "linux_arch" => Ok(Platform::LinuxArch),
            "linux_generic" => Ok(Platform::LinuxGeneric),
            _ => Err(format!("Unknown platform: {}", s)),
        }
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct AppAvailability {
    pub app_id: String,
    pub platform: Platform,
    pub supported: bool,
    pub has_release_assets: bool,
    pub best_asset: Option<AssetInfo>,
    pub message: Option<String>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct AssetInfo {
    pub name: String,
    pub browser_download_url: String,
    pub size: i64,
    pub content_type: String,
    pub priority: u32,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct PlatformReleaseInfo {
    pub tag_name: String,
    pub name: Option<String>,
    pub body: Option<String>,
    pub published_at: Option<DateTime<Utc>>,
    pub platform: Platform,
    pub available: bool,
    pub asset: Option<AssetInfo>,
    pub assets: Vec<GithubAsset>,
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
