use crate::models::{App, PaginatedAppsResponse};
use anyhow::{Context, Result};
use chrono::{DateTime, Utc};
use polars::prelude::*;
use std::sync::Arc;
use tracing::{debug, instrument};

#[instrument]
pub fn init_db(file_path: &str) -> Result<DataFrame> {
    debug!("Initializing DB from CSV: {}", file_path);
    let df = CsvReadOptions::default()
        .try_into_reader_with_file_path(Some(file_path.into()))?
        .finish()
        .context("Failed to read CSV file")?;

    debug!("Loaded {} apps", df.height());
    Ok(df)
}

#[instrument(skip(df))]
pub fn get_all_apps(df: &DataFrame) -> Result<Vec<App>> {
    debug!("Querying all apps from DataFrame");

    let repo_names = df.column("repo_name")?.str()?;
    let owner_logins = df.column("owner_login")?.str()?;
    let owner_avatar_urls = df.column("owner_avatar_url")?.str()?;
    let stargazers_counts = df.column("stargazers_count")?.i64()?;
    let created_ats = df.column("created_at")?.str()?;
    let updated_ats = df.column("updated_at")?.str()?;
    let last_release_ats = df.column("last_release_at")?.str()?;
    let windows_support = df.column("windows_support")?.bool()?;
    let macos_support = df.column("macos_support")?.bool()?;
    let linux_support = df.column("linux_support")?.bool()?;

    let mut apps = Vec::with_capacity(df.height());

    for i in 0..df.height() {
        let repo_name = repo_names.get(i).unwrap_or_default();
        let owner_login = owner_logins.get(i).unwrap_or_default();
        let owner_avatar_url = owner_avatar_urls.get(i).map(|s| s.to_string());

        let created_at = created_ats.get(i).and_then(|s| {
            DateTime::parse_from_rfc3339(s)
                .ok()
                .map(|dt| dt.with_timezone(&Utc))
        });

        let updated_at = updated_ats.get(i).and_then(|s| {
            DateTime::parse_from_rfc3339(s)
                .ok()
                .map(|dt| dt.with_timezone(&Utc))
        });

        let last_release_at = last_release_ats.get(i).and_then(|s| {
            DateTime::parse_from_rfc3339(s)
                .ok()
                .map(|dt| dt.with_timezone(&Utc))
        });

        apps.push(App {
            id: repo_name.to_string(),
            name: repo_name.to_string(),
            owner_login: owner_login.to_string(),
            owner_avatar_url,
            stargazers_count: stargazers_counts.get(i).unwrap_or(0),
            created_at,
            updated_at,
            last_release_at,
            windows_support: windows_support.get(i).unwrap_or(false),
            macos_support: macos_support.get(i).unwrap_or(false),
            linux_support: linux_support.get(i).unwrap_or(false),
        });
    }

    Ok(apps)
}

#[instrument(skip(df))]
pub fn get_apps_paginated(df: &DataFrame, page: usize, page_size: usize) -> Result<PaginatedAppsResponse> {
    debug!("Querying apps paginated - page: {}, page_size: {}", page, page_size);

    let all_apps = get_all_apps(df)?;
    let total = all_apps.len();

    if total == 0 {
        return Ok(PaginatedAppsResponse {
            apps: Vec::new(),
            total: 0,
            page,
            page_size,
            total_pages: 0,
            has_next: false,
            has_previous: false,
        });
    }

    let total_pages = (total + page_size - 1) / page_size;
    let page = page.max(1).min(total_pages);
    let start = (page - 1) * page_size;
    let end = start + page_size;
    let apps: Vec<App> = all_apps[start..end.min(total)].to_vec();

    Ok(PaginatedAppsResponse {
        apps,
        total,
        page,
        page_size,
        total_pages,
        has_next: page < total_pages,
        has_previous: page > 1,
    })
}

#[instrument(skip(df))]
pub fn get_app(df: &DataFrame, app_id: &str) -> Result<Option<App>> {
    debug!("Querying app: {}", app_id);

    let mask = df.column("repo_name")?.str()?.equal(app_id);
    let filtered = df.filter(&mask)?;

    if filtered.height() == 0 {
        return Ok(None);
    }

    let repo_names = filtered.column("repo_name")?.str()?;
    let owner_logins = filtered.column("owner_login")?.str()?;
    let owner_avatar_urls = filtered.column("owner_avatar_url")?.str()?;
    let stargazers_counts = filtered.column("stargazers_count")?.i64()?;
    let created_ats = filtered.column("created_at")?.str()?;
    let updated_ats = filtered.column("updated_at")?.str()?;
    let last_release_ats = filtered.column("last_release_at")?.str()?;
    let windows_support = filtered.column("windows_support")?.bool()?;
    let macos_support = filtered.column("macos_support")?.bool()?;
    let linux_support = filtered.column("linux_support")?.bool()?;

    let i = 0;
    let repo_name = repo_names.get(i).unwrap_or_default();
    let owner_login = owner_logins.get(i).unwrap_or_default();
    let owner_avatar_url = owner_avatar_urls.get(i).map(|s| s.to_string());

    let created_at = created_ats.get(i).and_then(|s| {
        DateTime::parse_from_rfc3339(s)
            .ok()
            .map(|dt| dt.with_timezone(&Utc))
    });

    let updated_at = updated_ats.get(i).and_then(|s| {
        DateTime::parse_from_rfc3339(s)
            .ok()
            .map(|dt| dt.with_timezone(&Utc))
    });

    let last_release_at = last_release_ats.get(i).and_then(|s| {
        DateTime::parse_from_rfc3339(s)
            .ok()
            .map(|dt| dt.with_timezone(&Utc))
    });

    Ok(Some(App {
        id: repo_name.to_string(),
        name: repo_name.to_string(),
        owner_login: owner_login.to_string(),
        owner_avatar_url,
        stargazers_count: stargazers_counts.get(i).unwrap_or(0),
        created_at,
        updated_at,
        last_release_at,
        windows_support: windows_support.get(i).unwrap_or(false),
        macos_support: macos_support.get(i).unwrap_or(false),
        linux_support: linux_support.get(i).unwrap_or(false),
    }))
}
