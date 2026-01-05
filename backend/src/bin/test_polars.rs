use polars::prelude::*;
use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct App {
    pub id: String,
    pub name: String,
    pub owner_login: String,
    pub created_at: Option<DateTime<Utc>>,
    pub updated_at: Option<DateTime<Utc>>,
    pub last_release_at: Option<DateTime<Utc>>,
    pub windows_support: bool,
    pub macos_support: bool,
    pub linux_support: bool,
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let file_path = "desktop_apps_export.csv";
    let df = CsvReadOptions::default()
        .try_into_reader_with_file_path(Some(file_path.into()))?
        .finish()?;

    println!("Loaded DF with shape: {:?}", df.shape());
    println!("Columns: {:?}", df.get_column_names());

    // We want to map:
    // repo_name -> id
    // repo_name -> name
    // owner_login -> owner_login
    // created_at -> created_at
    // updated_at -> updated_at

    let repo_names = df.column("repo_name")?.str()?;
    let owner_logins = df.column("owner_login")?.str()?;
    let created_ats = df.column("created_at")?.str()?;
    let updated_ats = df.column("updated_at")?.str()?;
    let last_release_ats = df.column("last_release_at")?.str()?;
    let windows_support = df.column("windows_support")?.bool()?;
    let macos_support = df.column("macos_support")?.bool()?;
    let linux_support = df.column("linux_support")?.bool()?;

    let mut apps = Vec::new();

    for i in 0..df.height() {
        let repo_name = repo_names.get(i).unwrap();
        let owner_login = owner_logins.get(i).unwrap();
        let created_at_str = created_ats.get(i);
        let updated_at_str = updated_ats.get(i);
        let last_release_at_str = last_release_ats.get(i);

        let created_at = created_at_str.and_then(|s| DateTime::parse_from_rfc3339(s).ok().map(|dt| dt.with_timezone(&Utc)));
        let updated_at = updated_at_str.and_then(|s| DateTime::parse_from_rfc3339(s).ok().map(|dt| dt.with_timezone(&Utc)));
        let last_release_at = last_release_at_str.and_then(|s| DateTime::parse_from_rfc3339(s).ok().map(|dt| dt.with_timezone(&Utc)));

        let app = App {
            id: repo_name.to_string(),
            name: repo_name.to_string(),
            owner_login: owner_login.to_string(),
            created_at,
            updated_at,
            last_release_at,
            windows_support: windows_support.get(i).unwrap_or(false),
            macos_support: macos_support.get(i).unwrap_or(false),
            linux_support: linux_support.get(i).unwrap_or(false),
        };
        apps.push(app);
    }

    println!("Parsed {} apps", apps.len());
    if let Some(first) = apps.first() {
        println!("First app: {:?}", first);
    }

    Ok(())
}
