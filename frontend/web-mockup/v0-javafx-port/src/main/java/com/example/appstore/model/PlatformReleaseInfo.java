package com.example.appstore.model;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Response from /api/apps/:id/release-info endpoint.
 * Contains release info with platform-specific asset details.
 */
public class PlatformReleaseInfo {

    private String tag_name;
    private String name;
    private String body;
    private String published_at;
    private String platform;
    private boolean available;
    private AssetInfo asset;
    private List<GithubAsset> assets;

    public PlatformReleaseInfo() {}

    public String getTagName() {
        return tag_name;
    }

    public void setTagName(String tagName) {
        this.tag_name = tagName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPublishedAt() {
        return published_at;
    }

    public void setPublishedAt(String publishedAt) {
        this.published_at = publishedAt;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public AssetInfo getAsset() {
        return asset;
    }

    public void setAsset(AssetInfo asset) {
        this.asset = asset;
    }

    public List<GithubAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<GithubAsset> assets) {
        this.assets = assets;
    }

    public GithubAsset getBestGithubAsset() {
        if (asset == null) {
            return null;
        }
        if (assets == null || assets.isEmpty()) {
            return null;
        }
        return assets.stream()
            .filter(a -> a.getName().equals(asset.getName()))
            .findFirst()
            .orElse(null);
    }
}
