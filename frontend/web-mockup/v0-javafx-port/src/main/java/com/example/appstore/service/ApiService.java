package com.example.appstore.service;

import com.example.appstore.model.App;
import com.example.appstore.model.AppAvailability;
import com.example.appstore.model.AssetInfo;
import com.example.appstore.model.GithubRelease;
import com.example.appstore.model.PaginatedAppsResponse;
import com.example.appstore.model.PlatformReleaseInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service for communicating with the backend API.
 * Uses Java 11+ HttpClient for async HTTP requests.
 */
public class ApiService {

    private static final Logger LOG = LogManager.getLogger(ApiService.class);
    private static ApiService instance;
    private final HttpClient httpClient;
    private final Gson gson;
    private String baseUrl;

    private ApiService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        this.gson = new Gson();
        this.baseUrl = System.getProperty("api.baseUrl");
        // this.baseUrl = "http://stars.stardive.space";
        LOG.info("ApiService initialized with base URL: {}", baseUrl);
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public void setBaseUrl(String baseUrl) {
        LOG.info("Changing API base URL from {} to {}", this.baseUrl, baseUrl);
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get paginated apps from the API.
     *
     * @param page The page number (1-indexed)
     * @param pageSize The number of apps per page
     * @return CompletableFuture containing PaginatedAppsResponse
     */
    public CompletableFuture<PaginatedAppsResponse> getAppsPaginated(int page, int pageSize) {
        String url = baseUrl + "/api/apps/paged?page=" + page + "&page_size=" + pageSize;
        LOG.debug("Fetching paginated apps from: {} (page: {}, pageSize: {})", url, page, pageSize);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    PaginatedAppsResponse result = gson.fromJson(
                        response.body(),
                        PaginatedAppsResponse.class
                    );
                    if (result != null) {
                        LOG.info(
                            "Successfully fetched {} apps (page {}/{}, total: {})",
                            result.getApps().size(),
                            result.getPage(),
                            result.getTotalPages(),
                            result.getTotal()
                        );
                        return result;
                    }
                    LOG.warn("Received null paginated response");
                    return createEmptyPaginatedResponse();
                }
                LOG.error(
                    "API request failed with status code: {} for URL: {}",
                    response.statusCode(),
                    url
                );
                return createEmptyPaginatedResponse();
            })
            .exceptionally(e -> {
                LOG.error(
                    "Failed to fetch paginated apps from {}: {}",
                    url,
                    e.getMessage(),
                    e
                );
                return createEmptyPaginatedResponse();
            });
    }

    private PaginatedAppsResponse createEmptyPaginatedResponse() {
        PaginatedAppsResponse response = new PaginatedAppsResponse();
        response.setApps(new ArrayList<>());
        response.setTotal(0);
        response.setPage(1);
        response.setPageSize(20);
        response.setTotalPages(0);
        response.setHasNext(false);
        response.setHasPrevious(false);
        return response;
    }

    /**
     * Get all apps from the API.
     */
    public CompletableFuture<List<App>> getApps() {
        String url = baseUrl + "/api/apps";
        LOG.debug("Fetching apps from: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .<List<App>>thenApply(response -> {
                if (response.statusCode() == 200) {
                    Type listType = new TypeToken<List<App>>() {}.getType();
                    List<App> apps = gson.fromJson(response.body(), listType);
                    int count = apps != null ? apps.size() : 0;
                    LOG.info("Successfully fetched {} apps from API", count);
                    return apps != null ? apps : new ArrayList<>();
                }
                LOG.error(
                    "API request failed with status code: {} for URL: {}",
                    response.statusCode(),
                    url
                );
                return new ArrayList<>();
            })
            .exceptionally(e -> {
                LOG.error(
                    "Failed to fetch apps from {}: {}",
                    url,
                    e.getMessage(),
                    e
                );
                return new ArrayList<>();
            });
    }

    /**
     * Get featured apps from the API.
     */
    public CompletableFuture<List<App>> getFeaturedApps() {
        String url = baseUrl + "/api/apps/featured";
        LOG.debug("Fetching featured apps from: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .<List<App>>thenApply(response -> {
                if (response.statusCode() == 200) {
                    Type listType = new TypeToken<List<App>>() {}.getType();
                    List<App> apps = gson.fromJson(response.body(), listType);
                    int count = apps != null ? apps.size() : 0;
                    LOG.info(
                        "Successfully fetched {} featured apps from API",
                        count
                    );
                    return apps != null ? apps : new ArrayList<>();
                }
                LOG.error(
                    "API request failed with status code: {} for URL: {}",
                    response.statusCode(),
                    url
                );
                return new ArrayList<>();
            })
            .exceptionally(e -> {
                LOG.error(
                    "Failed to fetch featured apps from {}: {}",
                    url,
                    e.getMessage(),
                    e
                );
                return new ArrayList<>();
            });
    }

    /**
     * Get apps filtered by category.
     */
    public CompletableFuture<List<App>> getAppsByCategory(String category) {
        LOG.debug("Filtering apps by category: {}", category);
        // For now, filter client-side since backend doesn't have query param yet
        return getApps().thenApply(apps -> {
            if (category == null || category.isEmpty()) {
                LOG.debug("No category filter specified, returning all apps");
                return apps;
            }
            List<App> filtered = new ArrayList<>();
            for (App app : apps) {
                if (category.equalsIgnoreCase(app.getCategory())) {
                    filtered.add(app);
                }
            }
            LOG.info(
                "Filtered {} apps by category '{}' from {} total apps",
                filtered.size(),
                category,
                apps.size()
            );
            return filtered;
        });
    }

    /**
     * Get latest release info for an app.
     */
    public CompletableFuture<GithubRelease> getLatestRelease(String appId) {
        String url = baseUrl + "/api/apps/" + appId + "/latest";
        LOG.debug("Fetching latest release for app: {} from: {}", appId, url);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    GithubRelease release = gson.fromJson(
                        response.body(),
                        GithubRelease.class
                    );
                    if (release != null) {
                        LOG.info(
                            "Successfully fetched release for app {}: {} ({})",
                            appId,
                            release.getTagName(),
                            release.getName()
                        );
                    } else {
                        LOG.warn(
                            "Received null release data for app: {}",
                            appId
                        );
                    }
                    return release;
                }
                LOG.error(
                    "API request failed with status code: {} for URL: {} (app: {})",
                    response.statusCode(),
                    url,
                    appId
                );
                return null;
            })
            .exceptionally(e -> {
                LOG.error(
                    "Failed to fetch release for app {} from {}: {}",
                    appId,
                    url,
                    e.getMessage(),
                    e
                );
                return null;
            });
    }

    /**
     * Get screenshots (images from README) for an app.
     */
    public CompletableFuture<List<String>> getScreenshots(String appId) {
        String url = baseUrl + "/api/apps/" + appId + "/screenshots";
        LOG.debug("Fetching screenshots for app: {} from: {}", appId, url);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .<List<String>>thenApply(response -> {
                if (response.statusCode() == 200) {
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> urls = gson.fromJson(
                        response.body(),
                        listType
                    );
                    int count = urls != null ? urls.size() : 0;
                    LOG.info(
                        "Successfully fetched {} screenshots for app: {}",
                        count,
                        appId
                    );
                    return urls != null ? urls : new ArrayList<>();
                }
                LOG.error(
                    "API request failed with status code: {} for URL: {} (app: {})",
                    response.statusCode(),
                    url,
                    appId
                );
                return new ArrayList<>();
            })
            .exceptionally(e -> {
                LOG.error(
                    "Failed to fetch screenshots for app {} from {}: {}",
                    appId,
                    url,
                    e.getMessage(),
                    e
                );
                return new ArrayList<>();
            });
    }

    /**
     * Get download URL for an app.
     */
    public String getDownloadUrl(String appId) {
        String url = baseUrl + "/api/apps/" + appId + "/download";
        LOG.debug("Generated download URL for app {}: {}", appId, url);
        return url;
    }

    /**
     * Get app availability for a specific platform.
     * This is the primary method to check if an app can be installed on the current platform.
     *
     * @param appId The app ID (repo_name)
     * @param platform The platform (windows, macos, linux_deb, linux_rpm, linux_arch, linux_generic)
     * @return CompletableFuture containing AppAvailability with platform support and asset info
     */
    public CompletableFuture<AppAvailability> getAppAvailability(String appId, String platform) {
        String url = baseUrl + "/api/apps/" + appId + "/availability?platform=" + platform;
        LOG.debug("Fetching availability for app: {} on platform: {} from: {}", appId, platform, url);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    AppAvailability availability = gson.fromJson(
                        response.body(),
                        AppAvailability.class
                    );
                    if (availability != null) {
                        LOG.info(
                            "App {} availability for {}: supported={}, hasAssets={}, available={}",
                            appId,
                            platform,
                            availability.isSupported(),
                            availability.hasReleaseAssets(),
                            availability.isAvailable()
                        );
                        return availability;
                    }
                    LOG.warn("Received null availability data for app: {}", appId);
                    return createUnavailableAvailability(appId, platform, "No response from server");
                }
                LOG.error(
                    "API request failed with status code: {} for URL: {} (app: {}, platform: {})",
                    response.statusCode(),
                    url,
                    appId,
                    platform
                );
                return createUnavailableAvailability(appId, platform, "API error: " + response.statusCode());
            })
            .exceptionally(e -> {
                LOG.error(
                    "Failed to fetch availability for app {} on platform {} from {}: {}",
                    appId,
                    platform,
                    url,
                    e.getMessage(),
                    e
                );
                return createUnavailableAvailability(appId, platform, "Connection error: " + e.getMessage());
            });
    }

    private AppAvailability createUnavailableAvailability(String appId, String platform, String reason) {
        AppAvailability availability = new AppAvailability();
        availability.setAppId(appId);
        availability.setPlatform(platform);
        availability.setSupported(false);
        availability.setHasReleaseAssets(false);
        availability.setBestAsset(null);
        availability.setMessage(reason);
        return availability;
    }

    /**
     * Get platform-aware release information.
     * Returns release info with the best compatible asset for the specified platform.
     *
     * @param appId The app ID (repo_name)
     * @param platform The platform (windows, macos, linux_deb, linux_rpm, linux_arch, linux_generic)
     * @return CompletableFuture containing PlatformReleaseInfo
     */
    public CompletableFuture<PlatformReleaseInfo> getReleaseInfo(String appId, String platform) {
        String url = baseUrl + "/api/apps/" + appId + "/release-info?platform=" + platform;
        LOG.debug("Fetching release info for app: {} on platform: {} from: {}", appId, platform, url);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    PlatformReleaseInfo releaseInfo = gson.fromJson(
                        response.body(),
                        PlatformReleaseInfo.class
                    );
                    if (releaseInfo != null) {
                        LOG.info(
                            "Release info for {} on {}: tag={}, available={}",
                            appId,
                            platform,
                            releaseInfo.getTagName(),
                            releaseInfo.isAvailable()
                        );
                        return releaseInfo;
                    }
                    LOG.warn("Received null release info for app: {}", appId);
                    return null;
                }
                LOG.error(
                    "API request failed with status code: {} for URL: {} (app: {}, platform: {})",
                    response.statusCode(),
                    url,
                    appId,
                    platform
                );
                return null;
            })
            .exceptionally(e -> {
                LOG.error(
                    "Failed to fetch release info for app {} on platform {} from {}: {}",
                    appId,
                    platform,
                    url,
                    e.getMessage(),
                    e
                );
                return null;
            });
    }

    /**
     * Download a specific asset by name.
     *
     * @param appId The app ID (repo_name)
     * @param assetName The name of the asset to download
     * @return The direct download URL
     */
    public String getAssetDownloadUrl(String appId, String assetName) {
        String url = baseUrl + "/api/apps/" + appId + "/download/" + assetName;
        LOG.debug("Generated asset download URL for app {} asset {}: {}", appId, assetName, url);
        return url;
    }

    /**
     * Convenience method to get the best download URL for an app on the current platform.
     * This uses the availability endpoint to get the best asset and returns its download URL.
     *
     * @param appId The app ID (repo_name)
     * @param platform The current platform
     * @return CompletableFuture containing the download URL or null if not available
     */
    public CompletableFuture<String> getBestDownloadUrl(String appId, String platform) {
        return getAppAvailability(appId, platform)
            .thenApply(availability -> {
                if (availability != null && availability.getBestAsset() != null) {
                    return availability.getBestAsset().getBrowserDownloadUrl();
                }
                return getDownloadUrl(appId);
            });
    }
}
