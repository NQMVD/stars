package com.example.appstore.model;

/**
 * Response from /api/apps/:id/availability endpoint.
 * Contains platform availability and asset information.
 */
public class AppAvailability {

    private String app_id;
    private String platform;
    private boolean supported;
    private boolean has_release_assets;
    private AssetInfo best_asset;
    private String message;

    public AppAvailability() {}

    public String getAppId() {
        return app_id;
    }

    public void setAppId(String appId) {
        this.app_id = appId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public boolean hasReleaseAssets() {
        return has_release_assets;
    }

    public void setHasReleaseAssets(boolean hasReleaseAssets) {
        this.has_release_assets = hasReleaseAssets;
    }

    public AssetInfo getBestAsset() {
        return best_asset;
    }

    public void setBestAsset(AssetInfo bestAsset) {
        this.best_asset = bestAsset;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAvailable() {
        return supported && has_release_assets && best_asset != null;
    }
}
