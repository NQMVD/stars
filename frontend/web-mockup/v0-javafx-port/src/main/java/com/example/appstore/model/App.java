package com.example.appstore.model;

import java.time.ZonedDateTime;

/**
 * App model matching the backend API response.
 */
public class App {

    private String id;
    private String name;
    private String owner_login;
    private String owner_avatar_url;
    private long stargazers_count;
    private String category;
    private String description;
    private String created_at;
    private String updated_at;
    private boolean windows_support;
    private boolean macos_support;
    private boolean linux_support;

    public App() {}

    public App(
        String id,
        String name,
        String ownerLogin,
        String category,
        String description
    ) {
        this.id = id;
        this.name = name;
        this.owner_login = ownerLogin;
        this.category = category;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerLogin() {
        return owner_login;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.owner_login = ownerLogin;
    }

    public String getOwnerAvatarUrl() {
        return owner_avatar_url;
    }

    public void setOwnerAvatarUrl(String ownerAvatarUrl) {
        this.owner_avatar_url = ownerAvatarUrl;
    }

    public long getStargazersCount() {
        return stargazers_count;
    }

    public void setStargazersCount(long stargazersCount) {
        this.stargazers_count = stargazersCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String createdAt) {
        this.created_at = createdAt;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updated_at = updatedAt;
    }

    public boolean isWindowsSupport() {
        return windows_support;
    }

    public void setWindowsSupport(boolean windowsSupport) {
        this.windows_support = windowsSupport;
    }

    public boolean isMacosSupport() {
        return macos_support;
    }

    public void setMacosSupport(boolean macosSupport) {
        this.macos_support = macosSupport;
    }

    public boolean isLinuxSupport() {
        return linux_support;
    }

    public void setLinuxSupport(boolean linuxSupport) {
        this.linux_support = linuxSupport;
    }

    public String getFormattedStars() {
        if (stargazers_count >= 1000000) {
            return String.format("%.1fM", stargazers_count / 1000000.0);
        } else if (stargazers_count >= 1000) {
            return String.format("%.1fK", stargazers_count / 1000.0);
        } else {
            return String.valueOf(stargazers_count);
        }
    }

    public boolean isSupportedOnCurrentPlatform(String platformString) {
        switch (platformString) {
            case "windows":
                return windows_support;
            case "macos":
                return macos_support;
            case "linux_deb":
            case "linux_rpm":
            case "linux_arch":
            case "linux_generic":
                return linux_support;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return (
            "App{id='" +
            id +
            "', name='" +
            name +
            "', owner='" +
            owner_login +
            "'}"
        );
    }
}
