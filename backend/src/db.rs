use crate::models::App;
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
    let created_ats = df.column("created_at")?.str()?;
    let updated_ats = df.column("updated_at")?.str()?;

    let mut apps = Vec::with_capacity(df.height());

    for i in 0..df.height() {
        // We use repo_name as ID (slug) and name for now, based on previous logic
        let repo_name = repo_names.get(i).unwrap_or_default();
        let owner_login = owner_logins.get(i).unwrap_or_default();

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

#[instrument(skip(df))]
pub fn get_app(df: &DataFrame, app_id: &str) -> Result<Option<App>> {
    debug!("Querying app: {}", app_id);

    // Filter DataFrame
    let mask = df.column("repo_name")?.str()?.equal(app_id);
    let filtered = df.filter(&mask)?;

    if filtered.height() == 0 {
        return Ok(None);
    }

    // Should be unique, take first
    let repo_names = filtered.column("repo_name")?.str()?;
    let owner_logins = filtered.column("owner_login")?.str()?;
    let created_ats = filtered.column("created_at")?.str()?;
    let updated_ats = filtered.column("updated_at")?.str()?;

    let i = 0;
    let repo_name = repo_names.get(i).unwrap_or_default();
    let owner_login = owner_logins.get(i).unwrap_or_default();

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

    Ok(Some(App {
        id: repo_name.to_string(),
        name: repo_name.to_string(),
        owner_login: owner_login.to_string(),
        created_at,
        updated_at,
    }))
}
