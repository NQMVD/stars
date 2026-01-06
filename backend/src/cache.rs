use moka::future::Cache;
use std::sync::Arc;
use std::time::Duration;

#[derive(Clone)]
pub struct CacheManager {
    pub release_cache: Arc<Cache<String, CachedRelease>>,
    pub readme_cache: Arc<Cache<String, CachedReadme>>,
}

impl CacheManager {
    pub fn new(
        release_cache_size: u64,
        release_ttl_secs: u64,
        readme_cache_size: u64,
        readme_ttl_secs: u64,
    ) -> Self {
        let release_cache = Arc::new(
            Cache::builder()
                .max_capacity(release_cache_size)
                .time_to_live(Duration::from_secs(release_ttl_secs))
                .build(),
        );

        let readme_cache = Arc::new(
            Cache::builder()
                .max_capacity(readme_cache_size)
                .time_to_live(Duration::from_secs(readme_ttl_secs))
                .build(),
        );

        Self {
            release_cache,
            readme_cache,
        }
    }

    pub fn default() -> Self {
        Self::new(
            1000,  // 1000 release entries
            300,   // 5 minutes TTL for releases
            500,   // 500 readme entries
            600,   // 10 minutes TTL for READMEs (change less frequently)
        )
    }
}

#[derive(Clone, Debug)]
pub struct CachedRelease {
    pub data: Vec<u8>,
    pub cached_at: chrono::DateTime<chrono::Utc>,
}

#[derive(Clone, Debug)]
pub struct CachedReadme {
    pub content: String,
    pub cached_at: chrono::DateTime<chrono::Utc>,
}
