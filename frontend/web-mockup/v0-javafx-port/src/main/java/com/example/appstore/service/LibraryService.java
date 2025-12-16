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

/**
 * Service for managing the user's app library with JSON persistence.
 * Library data is stored in ~/.stars/library-v0.json
 */
public class LibraryService {

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
        } catch (IOException e) {
            System.err.println(
                "Failed to create library directory: " + e.getMessage()
            );
        }

        loadLibrary();
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
            installedApps = new ArrayList<>();
            return;
        }

        try (Reader reader = new FileReader(libraryPath.toFile())) {
            Type listType = new TypeToken<List<InstalledApp>>() {}.getType();
            List<InstalledApp> loaded = gson.fromJson(reader, listType);
            installedApps = loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load library: " + e.getMessage());
            installedApps = new ArrayList<>();
        }
    }

    /**
     * Save the library to JSON file.
     */
    public void saveLibrary() {
        try (Writer writer = new FileWriter(libraryPath.toFile())) {
            gson.toJson(installedApps, writer);
        } catch (IOException e) {
            System.err.println("Failed to save library: " + e.getMessage());
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
        saveLibrary();
    }

    /**
     * Remove an app from the library by ID.
     */
    public boolean removeApp(String id) {
        boolean removed = installedApps.removeIf(app -> app.getId().equals(id));
        if (removed) {
            saveLibrary();
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
                saveLibrary();
                return true;
            }
        }
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
