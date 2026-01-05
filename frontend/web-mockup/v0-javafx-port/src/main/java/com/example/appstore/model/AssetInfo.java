package com.example.appstore.model;

/**
 * Asset information from the platform-aware API endpoints.
 */
public class AssetInfo {

    private String name;
    private String browser_download_url;
    private long size;
    private String content_type;
    private int priority;

    public AssetInfo() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrowserDownloadUrl() {
        return browser_download_url;
    }

    public void setBrowserDownloadUrl(String url) {
        this.browser_download_url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return content_type;
    }

    public void setContentType(String contentType) {
        this.content_type = contentType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
