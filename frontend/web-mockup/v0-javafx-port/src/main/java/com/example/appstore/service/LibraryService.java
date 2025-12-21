package com.example.appstore.service;

import com.example.appstore.model.InstalledApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service for managing the user's app library with JSON persistence.
 * Library data is stored in ~/.stars/library-v0.json
 */
public class LibraryService {

    private static final Logger LOG = LogManager.getLogger(
        LibraryService.class
    );
    private static final String LIBRARY_DIR = ".stars";
    private static final String LIBRARY_FILE = "library-v0.json";

    private static LibraryService instance;

    private final Path libraryPath;
    private final Gson gson;
    private List<InstalledApp> installedApps;

    public LibraryService() {
        String userHome = System.getProperty("user.home");
        Path starsDir = Paths.get(userHome, LIBRARY_DIR);
        this.libraryPath = starsDir.resolve(LIBRARY_FILE);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.installedApps = new ArrayList<>();

        // Ensure directory exists
        try {
            Files.createDirectories(starsDir);
            LOG.debug("Library directory ensured: {}", starsDir);
        } catch (IOException e) {
            LOG.error("Failed to create library directory: {}", starsDir, e);
        }

        loadLibrary();
        LOG.info("LibraryService initialized, library path: {}", libraryPath);
    }

    public static LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    /**
     * Load the library from JSON file.
     */
    public void loadLibrary() {
        if (!Files.exists(libraryPath)) {
            LOG.info(
                "Library file does not exist, starting with empty library: {}",
                libraryPath
            );
            installedApps = new ArrayList<>();
            return;
        }

        try (Reader reader = new FileReader(libraryPath.toFile())) {
            Type listType = new TypeToken<List<InstalledApp>>() {}.getType();
            List<InstalledApp> loaded = gson.fromJson(reader, listType);
            installedApps = loaded != null ? loaded : new ArrayList<>();
            LOG.info(
                "Loaded {} installed apps from library: {}",
                installedApps.size(),
                libraryPath
            );
        } catch (IOException e) {
            LOG.error(
                "Failed to load library from {}: {}",
                libraryPath,
                e.getMessage(),
                e
            );
            installedApps = new ArrayList<>();
        }
    }

    /**
     * Save the library to JSON file.
     */
    public void saveLibrary() {
        try (Writer writer = new FileWriter(libraryPath.toFile())) {
            gson.toJson(installedApps, writer);
            LOG.debug(
                "Saved {} installed apps to library: {}",
                installedApps.size(),
                libraryPath
            );
        } catch (IOException e) {
            LOG.error(
                "Failed to save library to {}: {}",
                libraryPath,
                e.getMessage(),
                e
            );
        }
    }

    /**
     * Install an app to the library (legacy, no paths).
     */
    public void installApp(
        String id,
        String name,
        String developer,
        String category,
        String version,
        String size
    ) {
        installApp(id, name, developer, category, version, size, null, null);
    }

    /**
     * Install an app to the library with installation paths.
     */
    public void installApp(
        String id,
        String name,
        String developer,
        String category,
        String version,
        String size,
        String installPath,
        String executablePath
    ) {
        if (isInstalled(id)) {
            LOG.debug("App {} ({}) is already installed, skipping", name, id);
            return; // Already installed
        }

        InstalledApp installed = new InstalledApp(
            id,
            name,
            developer,
            category,
            version,
            System.currentTimeMillis(),
            size,
            installPath,
            executablePath
        );
        installedApps.add(installed);
        LOG.info(
            "Added app to library: {} (id: {}, version: {})",
            name,
            id,
            version
        );
        saveLibrary();
    }

    /**
     * Remove an app from the library by ID.
     */
    public boolean removeApp(String id) {
        boolean removed = installedApps.removeIf(app -> app.getId().equals(id));
        if (removed) {
            LOG.info("Removed app from library: {}", id);
            saveLibrary();
        } else {
            LOG.debug("App not found in library: {}", id);
        }
        return removed;
    }

    /**
     * Check if an app is installed.
     */
    public boolean isInstalled(String id) {
        return installedApps.stream().anyMatch(app -> app.getId().equals(id));
    }

    /**
     * Get an installed app by ID.
     */
    public Optional<InstalledApp> getInstalledApp(String id) {
        return installedApps
            .stream()
            .filter(app -> app.getId().equals(id))
            .findFirst();
    }

    /**
     * Update an app (simulates updating by bumping the version).
     */
    public boolean updateApp(String id) {
        for (int i = 0; i < installedApps.size(); i++) {
            InstalledApp app = installedApps.get(i);
            if (app.getId().equals(id)) {
                // Bump version
                String currentVersion = app.getInstalledVersion();
                String newVersion = bumpVersion(currentVersion);
                installedApps.set(i, app.withUpdatedVersion(newVersion));
                LOG.info(
                    "Updated app version: {} (id: {}) from {} to {}",
                    app.getName(),
                    id,
                    currentVersion,
                    newVersion
                );
                saveLibrary();
                return true;
            }
        }
        LOG.warn("App not found for update: {}", id);
        return false;
    }

    /**
     * Get all installed apps.
     */
    public List<InstalledApp> getInstalledApps() {
        return new ArrayList<>(installedApps);
    }

    /**
     * Get the count of installed apps.
     */
    public int getInstalledCount() {
        return installedApps.size();
    }

    /**
     * Bump a semantic version string (e.g., "1.0.0" -> "1.0.1").
     */
    private String bumpVersion(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                int last = Integer.parseInt(parts[parts.length - 1]) + 1;
                parts[parts.length - 1] = String.valueOf(last);
                return String.join(".", parts);
            }
        } catch (NumberFormatException e) {
            // Fall through
        }
        return version + ".1";
    }
}
