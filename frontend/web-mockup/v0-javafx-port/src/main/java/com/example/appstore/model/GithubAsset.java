package com.example.appstore.model;

/**
 * GitHub asset model for downloadable files.
 */
public class GithubAsset {
    private String name;
    private String browser_download_url;
    private long size;
    private String content_type;
    
    public GithubAsset() {}
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBrowserDownloadUrl() { return browser_download_url; }
    public void setBrowserDownloadUrl(String url) { this.browser_download_url = url; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public String getContentType() { return content_type; }
    public void setContentType(String contentType) { this.content_type = contentType; }
}
