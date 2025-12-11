package com.example.appstore.model;

/**
 * Data class for an installed app in the library.
 */
public class InstalledApp {

    private String id;
    private String name;
    private String developer;
    private String category;
    private String installedVersion;
    private long installTimestamp;
    private String size;

    public InstalledApp() {}

    public InstalledApp(
        String id,
        String name,
        String developer,
        String category,
        String installedVersion,
        long installTimestamp,
        String size
    ) {
        this.id = id;
        this.name = name;
        this.developer = developer;
        this.category = category;
        this.installedVersion = installedVersion;
        this.installTimestamp = installTimestamp;
        this.size = size;
    }

    // Getters and setters
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

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

    public long getInstallTimestamp() {
        return installTimestamp;
    }

    public void setInstallTimestamp(long installTimestamp) {
        this.installTimestamp = installTimestamp;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Create a new InstalledApp with an updated version.
     */
    public InstalledApp withUpdatedVersion(String newVersion) {
        return new InstalledApp(
            this.id,
            this.name,
            this.developer,
            this.category,
            newVersion,
            this.installTimestamp,
            this.size
        );
    }

    @Override
    public String toString() {
        return (
            "InstalledApp{id='" +
            id +
            "', name='" +
            name +
            "', version='" +
            installedVersion +
            "'}"
        );
    }
}
