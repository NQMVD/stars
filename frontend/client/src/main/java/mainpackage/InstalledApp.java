package mainpackage;

/**
 * Data record for an installed app in the library.
 */
public record InstalledApp(
        String title,
        String author,
        String category,
        String installedVersion,
        long installTimestamp,
        String color,
        String svgPath,
        String rating
) {
    /**
     * Create an InstalledApp from an AppData with a version string.
     */
    public static InstalledApp fromAppData(AppData app, String version) {
        return new InstalledApp(
                app.title(),
                app.author(),
                app.category(),
                version,
                System.currentTimeMillis(),
                app.color(),
                app.svgPath(),
                app.rating()
        );
    }

    /**
     * Create a new InstalledApp with an updated version.
     */
    public InstalledApp withUpdatedVersion(String newVersion) {
        return new InstalledApp(
                this.title,
                this.author,
                this.category,
                newVersion,
                this.installTimestamp,
                this.color,
                this.svgPath,
                this.rating
        );
    }
}
