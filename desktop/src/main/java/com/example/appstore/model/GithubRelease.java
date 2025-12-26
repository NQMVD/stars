package com.example.appstore.model;

import java.util.List;

/**
 * GitHub release model from /api/apps/:id/latest endpoint.
 */
public class GithubRelease {

    private String tag_name;
    private String name;
    private String body;
    private String published_at;
    private List<GithubAsset> assets;

    public GithubRelease() {}

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

    public List<GithubAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<GithubAsset> assets) {
        this.assets = assets;
    }
}
