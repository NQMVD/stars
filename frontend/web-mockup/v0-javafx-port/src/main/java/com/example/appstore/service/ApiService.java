package com.example.appstore.service;

import com.example.appstore.model.App;
import com.example.appstore.model.GithubRelease;
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

/**
 * Service for communicating with the backend API.
 * Uses Java 11+ HttpClient for async HTTP requests.
 */
public class ApiService {

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
        // Default to localhost, can be configured
        this.baseUrl = System.getProperty(
            "api.baseUrl",
            "http://localhost:4200"
        );
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get all apps from the API.
     */
    public CompletableFuture<List<App>> getApps() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/apps"))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .<List<App>>thenApply(response -> {
                if (response.statusCode() == 200) {
                    Type listType = new TypeToken<List<App>>() {}.getType();
                    List<App> apps = gson.fromJson(response.body(), listType);
                    return apps != null ? apps : new ArrayList<>();
                }
                System.err.println("API error: " + response.statusCode());
                return new ArrayList<>();
            })
            .exceptionally(e -> {
                System.err.println("Failed to fetch apps: " + e.getMessage());
                return new ArrayList<>();
            });
    }

    /**
     * Get featured apps from the API.
     */
    public CompletableFuture<List<App>> getFeaturedApps() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/apps/featured"))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .<List<App>>thenApply(response -> {
                if (response.statusCode() == 200) {
                    Type listType = new TypeToken<List<App>>() {}.getType();
                    List<App> apps = gson.fromJson(response.body(), listType);
                    return apps != null ? apps : new ArrayList<>();
                }
                System.err.println("API error: " + response.statusCode());
                return new ArrayList<>();
            })
            .exceptionally(e -> {
                System.err.println(
                    "Failed to fetch featured apps: " + e.getMessage()
                );
                return new ArrayList<>();
            });
    }

    /**
     * Get apps filtered by category.
     */
    public CompletableFuture<List<App>> getAppsByCategory(String category) {
        // For now, filter client-side since backend doesn't have query param yet
        return getApps().thenApply(apps -> {
            if (category == null || category.isEmpty()) {
                return apps;
            }
            List<App> filtered = new ArrayList<>();
            for (App app : apps) {
                if (category.equalsIgnoreCase(app.getCategory())) {
                    filtered.add(app);
                }
            }
            return filtered;
        });
    }

    /**
     * Get latest release info for an app.
     */
    public CompletableFuture<GithubRelease> getLatestRelease(String appId) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/apps/" + appId + "/latest"))
            .GET()
            .build();

        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    return gson.fromJson(response.body(), GithubRelease.class);
                }
                System.err.println(
                    "API error getting release: " + response.statusCode()
                );
                return null;
            })
            .exceptionally(e -> {
                System.err.println(
                    "Failed to fetch release: " + e.getMessage()
                );
                return null;
            });
    }

    /**
     * Get screenshots (images from README) for an app.
     */
    public CompletableFuture<List<String>> getScreenshots(String appId) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/apps/" + appId + "/screenshots"))
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
                    return urls != null ? urls : new ArrayList<>();
                }
                System.err.println(
                    "API error getting screenshots: " + response.statusCode()
                );
                return new ArrayList<>();
            })
            .exceptionally(e -> {
                System.err.println(
                    "Failed to fetch screenshots: " + e.getMessage()
                );
                return new ArrayList<>();
            });
    }

    /**
     * Get download URL for an app.
     */
    public String getDownloadUrl(String appId) {
        return baseUrl + "/api/apps/" + appId + "/download";
    }
}
