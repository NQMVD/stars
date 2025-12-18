package com.example.appstore.service;

import com.example.appstore.model.App;
import com.example.appstore.model.GithubAsset;
import com.example.appstore.model.GithubRelease;
import com.example.appstore.service.PlatformDetector.Platform;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for downloading and installing applications.
 * Handles platform-specific installation procedures.
 */
public class InstallationService {

    private static final Logger LOG = Logger.getLogger(
        InstallationService.class.getName()
    );
    private static InstallationService instance;

    /**
     * Installation progress callback data.
     */
    public static class InstallProgress {

        public enum Stage {
            FETCHING_RELEASE,
            DOWNLOADING,
            EXTRACTING,
            INSTALLING,
            VERIFYING,
            COMPLETED,
            FAILED,
        }

        private final Stage stage;
        private final double progress; // 0.0 to 1.0
        private final String message;
        private final Throwable error;

        public InstallProgress(Stage stage, double progress, String message) {
            this.stage = stage;
            this.progress = progress;
            this.message = message;
            this.error = null;
        }

        public InstallProgress(Stage stage, String message, Throwable error) {
            this.stage = stage;
            this.progress = 0;
            this.message = message;
            this.error = error;
        }

        public Stage getStage() {
            return stage;
        }

        public double getProgress() {
            return progress;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getError() {
            return error;
        }

        public boolean isFailed() {
            return stage == Stage.FAILED;
        }

        public boolean isCompleted() {
            return stage == Stage.COMPLETED;
        }
    }

    /**
     * Result of a successful installation.
     */
    public static class InstallResult {

        private final String appId;
        private final String version;
        private final String installPath;
        private final String executablePath;
        private final long sizeBytes;

        public InstallResult(
            String appId,
            String version,
            String installPath,
            String executablePath,
            long sizeBytes
        ) {
            this.appId = appId;
            this.version = version;
            this.installPath = installPath;
            this.executablePath = executablePath;
            this.sizeBytes = sizeBytes;
        }

        public String getAppId() {
            return appId;
        }

        public String getVersion() {
            return version;
        }

        public String getInstallPath() {
            return installPath;
        }

        public String getExecutablePath() {
            return executablePath;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public String getFormattedSize() {
            if (sizeBytes < 1024) return sizeBytes + " B";
            if (sizeBytes < 1024 * 1024) return String.format(
                "%.1f KB",
                sizeBytes / 1024.0
            );
            if (sizeBytes < 1024 * 1024 * 1024) return String.format(
                "%.1f MB",
                sizeBytes / (1024.0 * 1024)
            );
            return String.format("%.2f GB", sizeBytes / (1024.0 * 1024 * 1024));
        }
    }

    private InstallationService() {
        LOG.info("[InstallationService] Initialized");
    }

    public static synchronized InstallationService getInstance() {
        if (instance == null) {
            instance = new InstallationService();
        }
        return instance;
    }

    /**
     * Install an app asynchronously.
     *
     * @param app The app to install
     * @param progressCallback Called with progress updates (can be null)
     * @return CompletableFuture with the install result
     */
    public CompletableFuture<InstallResult> installApp(
        App app,
        Consumer<InstallProgress> progressCallback
    ) {
        LOG.info(
            "[InstallationService] Starting installation for: " +
                app.getName() +
                " (id: " +
                app.getId() +
                ")"
        );
        return CompletableFuture.supplyAsync(() -> {
            try {
                return doInstall(app, progressCallback);
            } catch (Exception e) {
                LOG.log(
                    Level.SEVERE,
                    "[InstallationService] Installation failed for: " +
                        app.getName(),
                    e
                );
                reportProgress(
                    progressCallback,
                    new InstallProgress(
                        InstallProgress.Stage.FAILED,
                        "Installation failed: " + e.getMessage(),
                        e
                    )
                );
                throw new RuntimeException(e);
            }
        });
    }

    private InstallResult doInstall(
        App app,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        Platform platform = PlatformDetector.detectPlatform();
        LOG.info(
            "[InstallationService] Detected platform: " +
                PlatformDetector.getPlatformDisplayName(platform) +
                ", arch: " +
                PlatformDetector.getArchitecture()
        );

        // Stage 1: Fetch release info
        LOG.info(
            "[InstallationService] Stage 1: Fetching release info for " +
                app.getId()
        );
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.FETCHING_RELEASE,
                0.0,
                "Fetching release information..."
            )
        );

        GithubRelease release = ApiService.getInstance()
            .getLatestRelease(app.getId())
            .get(); // Blocking get since we're in async context

        if (release == null) {
            LOG.warning(
                "[InstallationService] No release found for: " + app.getName()
            );
            throw new RuntimeException("No release found for " + app.getName());
        }

        LOG.info(
            "[InstallationService] Found release: " +
                release.getTagName() +
                " (" +
                release.getName() +
                ")"
        );

        List<GithubAsset> assets = release.getAssets();
        if (assets == null || assets.isEmpty()) {
            LOG.warning(
                "[InstallationService] No assets in release for: " +
                    app.getName()
            );
            throw new RuntimeException(
                "No downloadable assets found for " + app.getName()
            );
        }

        LOG.info(
            "[InstallationService] Release has " + assets.size() + " assets:"
        );
        for (GithubAsset asset : assets) {
            LOG.fine(
                "[InstallationService]   - " +
                    asset.getName() +
                    " (" +
                    formatBytes(asset.getSize()) +
                    ")"
            );
        }

        // Find the best asset for this platform
        GithubAsset bestAsset = findBestAsset(assets, platform);
        if (bestAsset == null) {
            LOG.warning(
                "[InstallationService] No compatible asset found for platform: " +
                    platform
            );
            throw new RuntimeException(
                "No compatible download found for " +
                    PlatformDetector.getPlatformDisplayName(platform)
            );
        }

        LOG.info(
            "[InstallationService] Selected asset: " +
                bestAsset.getName() +
                " (" +
                formatBytes(bestAsset.getSize()) +
                ")"
        );

        // Stage 2: Download
        LOG.info(
            "[InstallationService] Stage 2: Downloading " + bestAsset.getName()
        );
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.DOWNLOADING,
                0.0,
                "Downloading " + bestAsset.getName() + "..."
            )
        );

        long downloadStart = System.currentTimeMillis();
        Path downloadPath = downloadAsset(bestAsset, progress -> {
            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.DOWNLOADING,
                    progress,
                    String.format("Downloading... %.0f%%", progress * 100)
                )
            );
        });
        long downloadTime = System.currentTimeMillis() - downloadStart;
        LOG.info(
            "[InstallationService] Download completed in " +
                downloadTime +
                "ms, saved to: " +
                downloadPath
        );

        // Stage 3: Extract/Install
        LOG.info(
            "[InstallationService] Stage 3: Installing from " +
                bestAsset.getName()
        );
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.EXTRACTING,
                0.0,
                "Extracting..."
            )
        );

        long installStart = System.currentTimeMillis();
        Path installPath = installAsset(
            app,
            downloadPath,
            bestAsset.getName(),
            platform,
            progressCallback
        );
        long installTime = System.currentTimeMillis() - installStart;
        LOG.info(
            "[InstallationService] Installation completed in " +
                installTime +
                "ms, installed to: " +
                installPath
        );

        // Stage 4: Verify
        LOG.info("[InstallationService] Stage 4: Verifying installation");
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.VERIFYING,
                0.0,
                "Verifying installation..."
            )
        );

        String executablePath = findExecutable(
            installPath,
            app.getName(),
            platform
        );
        LOG.info("[InstallationService] Found executable: " + executablePath);

        // Cleanup download
        try {
            Files.deleteIfExists(downloadPath);
            LOG.fine(
                "[InstallationService] Cleaned up download file: " +
                    downloadPath
            );
        } catch (IOException e) {
            LOG.warning(
                "[InstallationService] Failed to cleanup download: " +
                    e.getMessage()
            );
        }

        // Stage 5: Complete
        LOG.info(
            "[InstallationService] Stage 5: Installation complete for " +
                app.getName()
        );
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.COMPLETED,
                1.0,
                "Installation complete!"
            )
        );

        return new InstallResult(
            app.getId(),
            release.getTagName(),
            installPath.toString(),
            executablePath,
            bestAsset.getSize()
        );
    }

    /**
     * Find the best asset for the given platform.
     */
    private GithubAsset findBestAsset(
        List<GithubAsset> assets,
        Platform platform
    ) {
        return assets
            .stream()
            .filter(asset ->
                PlatformDetector.isAssetForPlatform(asset.getName(), platform)
            )
            .max(
                Comparator.comparingInt(asset ->
                    PlatformDetector.getAssetPriority(asset.getName(), platform)
                )
            )
            .orElse(null);
    }

    /**
     * Download an asset to the downloads directory.
     */
    private Path downloadAsset(
        GithubAsset asset,
        Consumer<Double> progressCallback
    ) throws IOException {
        String downloadDir = PlatformDetector.getDownloadDirectory();
        Files.createDirectories(Paths.get(downloadDir));

        Path downloadPath = Paths.get(downloadDir, asset.getName());

        URL url = new URL(asset.getBrowserDownloadUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/octet-stream");
        connection.setInstanceFollowRedirects(true);

        // Handle redirects manually if needed
        int status = connection.getResponseCode();
        if (
            status == HttpURLConnection.HTTP_MOVED_TEMP ||
            status == HttpURLConnection.HTTP_MOVED_PERM ||
            status == HttpURLConnection.HTTP_SEE_OTHER ||
            status == 307 ||
            status == 308
        ) {
            String redirectUrl = connection.getHeaderField("Location");
            connection = (HttpURLConnection) new URL(
                redirectUrl
            ).openConnection();
        }

        long totalSize = connection.getContentLengthLong();
        if (totalSize <= 0) {
            totalSize = asset.getSize();
        }

        try (
            InputStream in = connection.getInputStream();
            OutputStream out = Files.newOutputStream(downloadPath)
        ) {
            byte[] buffer = new byte[8192];
            long downloaded = 0;
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                downloaded += bytesRead;

                if (totalSize > 0 && progressCallback != null) {
                    progressCallback.accept((double) downloaded / totalSize);
                }
            }
        }

        return downloadPath;
    }

    /**
     * Install the downloaded asset based on platform and file type.
     */
    private Path installAsset(
        App app,
        Path downloadPath,
        String assetName,
        Platform platform,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        String fileName = assetName.toLowerCase();
        String appName = sanitizeAppName(app.getName());

        switch (platform) {
            case MACOS:
                return installMacOS(
                    downloadPath,
                    fileName,
                    appName,
                    progressCallback
                );
            case WINDOWS:
                return installWindows(
                    downloadPath,
                    fileName,
                    appName,
                    progressCallback
                );
            case LINUX_DEB:
            case LINUX_RPM:
            case LINUX_ARCH:
            case LINUX_GENERIC:
                return installLinux(
                    downloadPath,
                    fileName,
                    appName,
                    platform,
                    progressCallback
                );
            default:
                throw new RuntimeException("Unsupported platform: " + platform);
        }
    }

    /**
     * macOS installation.
     */
    private Path installMacOS(
        Path downloadPath,
        String fileName,
        String appName,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        if (fileName.endsWith(".dmg")) {
            return installDMG(downloadPath, appName, progressCallback);
        } else if (fileName.endsWith(".zip")) {
            return extractZipMacOS(downloadPath, progressCallback);
        } else if (
            fileName.endsWith(".app.tar.gz") || fileName.endsWith(".tar.gz")
        ) {
            return extractTarGzMacOS(downloadPath, progressCallback);
        } else if (fileName.endsWith(".pkg")) {
            return installPKG(downloadPath, progressCallback);
        }
        throw new RuntimeException("Unsupported macOS installer: " + fileName);
    }

    /**
     * Extract a ZIP file on macOS.
     * Finds the .app bundle inside and copies it directly to /Applications.
     */
    private Path extractZipMacOS(
        Path zipPath,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.EXTRACTING,
                0.3,
                "Extracting archive..."
            )
        );

        // Extract to a temporary directory first
        Path tempDir = Files.createTempDirectory("stars-install-");
        LOG.info(
            "[InstallationService] Extracting zip to temp dir: " + tempDir
        );

        try {
            // Use unzip command for better compatibility
            ProcessBuilder pb = new ProcessBuilder(
                "unzip",
                "-q",
                zipPath.toString(),
                "-d",
                tempDir.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException(
                    "Failed to extract zip, exit code: " + exitCode
                );
            }

            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.5,
                    "Finding application..."
                )
            );

            // Find the .app bundle (may be nested)
            Path appBundle = findAppBundle(tempDir);
            if (appBundle == null) {
                throw new RuntimeException(
                    "No .app bundle found in zip archive"
                );
            }

            LOG.info("[InstallationService] Found .app bundle: " + appBundle);

            // Copy to /Applications directly
            Path targetPath = Paths.get(
                "/Applications",
                appBundle.getFileName().toString()
            );

            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.7,
                    "Copying to Applications..."
                )
            );

            // Remove existing installation if present
            if (Files.exists(targetPath)) {
                LOG.info(
                    "[InstallationService] Removing existing installation: " +
                        targetPath
                );
                deleteDirectory(targetPath);
            }

            // Copy the app bundle
            copyDirectory(appBundle, targetPath);
            LOG.info("[InstallationService] Installed to: " + targetPath);

            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.9,
                    "Finalizing..."
                )
            );

            return targetPath;
        } finally {
            // Cleanup temp directory
            try {
                deleteDirectory(tempDir);
            } catch (IOException e) {
                LOG.warning(
                    "[InstallationService] Failed to cleanup temp dir: " +
                        e.getMessage()
                );
            }
        }
    }

    /**
     * Extract a tar.gz file on macOS.
     * Finds the .app bundle inside and copies it directly to /Applications.
     */
    private Path extractTarGzMacOS(
        Path tarGzPath,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.EXTRACTING,
                0.3,
                "Extracting archive..."
            )
        );

        // Extract to a temporary directory first
        Path tempDir = Files.createTempDirectory("stars-install-");
        LOG.info(
            "[InstallationService] Extracting tar.gz to temp dir: " + tempDir
        );

        try {
            ProcessBuilder pb = new ProcessBuilder(
                "tar",
                "-xzf",
                tarGzPath.toString(),
                "-C",
                tempDir.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException(
                    "Failed to extract tar.gz, exit code: " + exitCode
                );
            }

            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.5,
                    "Finding application..."
                )
            );

            // Find the .app bundle (may be nested)
            Path appBundle = findAppBundle(tempDir);
            if (appBundle == null) {
                throw new RuntimeException(
                    "No .app bundle found in tar.gz archive"
                );
            }

            LOG.info("[InstallationService] Found .app bundle: " + appBundle);

            // Copy to /Applications directly
            Path targetPath = Paths.get(
                "/Applications",
                appBundle.getFileName().toString()
            );

            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.7,
                    "Copying to Applications..."
                )
            );

            // Remove existing installation if present
            if (Files.exists(targetPath)) {
                LOG.info(
                    "[InstallationService] Removing existing installation: " +
                        targetPath
                );
                deleteDirectory(targetPath);
            }

            // Copy the app bundle
            copyDirectory(appBundle, targetPath);
            LOG.info("[InstallationService] Installed to: " + targetPath);

            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.9,
                    "Finalizing..."
                )
            );

            return targetPath;
        } finally {
            // Cleanup temp directory
            try {
                deleteDirectory(tempDir);
            } catch (IOException e) {
                LOG.warning(
                    "[InstallationService] Failed to cleanup temp dir: " +
                        e.getMessage()
                );
            }
        }
    }

    /**
     * Find a .app bundle in a directory (searches up to 3 levels deep).
     */
    private Path findAppBundle(Path directory) throws IOException {
        try (var stream = Files.walk(directory, 3)) {
            return stream
                .filter(p -> p.getFileName().toString().endsWith(".app"))
                .filter(Files::isDirectory)
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Install a DMG file on macOS.
     */
    private Path installDMG(
        Path dmgPath,
        String appName,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.INSTALLING,
                0.2,
                "Mounting disk image..."
            )
        );

        // Mount the DMG
        ProcessBuilder mount = new ProcessBuilder(
            "hdiutil",
            "attach",
            dmgPath.toString(),
            "-nobrowse"
        );
        Process mountProcess = mount.start();

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(mountProcess.getInputStream())
        );

        String line;
        String mountPoint = null;
        while ((line = reader.readLine()) != null) {
            if (line.contains("/Volumes/")) {
                // Extract mount point from the line
                String[] parts = line.split("\t");
                for (String part : parts) {
                    if (part.trim().startsWith("/Volumes/")) {
                        mountPoint = part.trim();
                        break;
                    }
                }
            }
        }

        int exitCode = mountProcess.waitFor();
        if (exitCode != 0 || mountPoint == null) {
            throw new RuntimeException("Failed to mount DMG");
        }

        try {
            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.5,
                    "Copying application..."
                )
            );

            // Find .app in the mounted volume
            File volumeDir = new File(mountPoint);
            File[] contents = volumeDir.listFiles();
            File appBundle = null;

            if (contents != null) {
                for (File file : contents) {
                    if (file.isDirectory() && file.getName().endsWith(".app")) {
                        appBundle = file;
                        break;
                    }
                }
            }

            if (appBundle == null) {
                throw new RuntimeException("No .app bundle found in DMG");
            }

            // Copy to /Applications
            Path targetPath = Paths.get("/Applications", appBundle.getName());

            // Remove existing installation if present
            if (Files.exists(targetPath)) {
                deleteDirectory(targetPath);
            }

            // Copy the app bundle
            copyDirectory(appBundle.toPath(), targetPath);

            reportProgress(
                progressCallback,
                new InstallProgress(
                    InstallProgress.Stage.INSTALLING,
                    0.9,
                    "Finalizing..."
                )
            );

            return targetPath;
        } finally {
            // Unmount the DMG
            new ProcessBuilder("hdiutil", "detach", mountPoint, "-quiet")
                .start()
                .waitFor();
        }
    }

    /**
     * Install a PKG file on macOS.
     */
    private Path installPKG(
        Path pkgPath,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.INSTALLING,
                0.3,
                "Running installer..."
            )
        );

        // PKG installation requires user interaction or sudo
        // For now, we'll open it with the system installer
        ProcessBuilder pb = new ProcessBuilder("open", pkgPath.toString());
        pb.start();

        // Return a placeholder path since PKG installs to various locations
        return Paths.get("/Applications");
    }

    /**
     * Windows installation.
     */
    private Path installWindows(
        Path downloadPath,
        String fileName,
        String appName,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        String installDir = PlatformDetector.getInstallDirectory(
            Platform.WINDOWS
        );
        Path appDir = Paths.get(installDir, appName);
        Files.createDirectories(appDir);

        if (fileName.endsWith(".msi")) {
            return installMSI(downloadPath, appDir, progressCallback);
        } else if (fileName.endsWith(".exe")) {
            // For portable EXEs, just copy to the install directory
            // For installer EXEs, we'd need to run them
            if (
                fileName.contains("portable") || fileName.contains("standalone")
            ) {
                Path target = appDir.resolve(downloadPath.getFileName());
                Files.copy(
                    downloadPath,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
                );
                return target;
            } else {
                // Run the installer silently
                return runExeInstaller(downloadPath, appDir, progressCallback);
            }
        } else if (fileName.endsWith(".zip")) {
            return extractZip(downloadPath, appDir, appName);
        }

        throw new RuntimeException(
            "Unsupported Windows installer: " + fileName
        );
    }

    /**
     * Install MSI on Windows.
     */
    private Path installMSI(
        Path msiPath,
        Path targetDir,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.INSTALLING,
                0.3,
                "Running Windows Installer..."
            )
        );

        ProcessBuilder pb = new ProcessBuilder(
            "msiexec",
            "/i",
            msiPath.toString(),
            "/quiet",
            "/norestart",
            "TARGETDIR=" + targetDir.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(
                "MSI installation failed with code: " + exitCode
            );
        }

        return targetDir;
    }

    /**
     * Run an EXE installer on Windows.
     */
    private Path runExeInstaller(
        Path exePath,
        Path targetDir,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.INSTALLING,
                0.3,
                "Running installer..."
            )
        );

        // Try common silent install flags
        ProcessBuilder pb = new ProcessBuilder(
            exePath.toString(),
            "/S",
            "/D=" + targetDir.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // Fall back to just copying the EXE
            Path target = targetDir.resolve(exePath.getFileName());
            Files.copy(exePath, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        }

        return targetDir;
    }

    /**
     * Linux installation.
     */
    private Path installLinux(
        Path downloadPath,
        String fileName,
        String appName,
        Platform platform,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        if (fileName.endsWith(".appimage")) {
            return installAppImage(downloadPath, appName);
        } else if (fileName.endsWith(".deb")) {
            return installDeb(downloadPath, progressCallback);
        } else if (fileName.endsWith(".rpm")) {
            return installRpm(downloadPath, progressCallback);
        } else if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
            String installDir = PlatformDetector.getInstallDirectory(platform);
            return extractTarGz(downloadPath, Paths.get(installDir), appName);
        } else if (fileName.endsWith(".zip")) {
            String installDir = PlatformDetector.getInstallDirectory(platform);
            return extractZip(downloadPath, Paths.get(installDir), appName);
        }

        throw new RuntimeException("Unsupported Linux installer: " + fileName);
    }

    /**
     * Install AppImage on Linux.
     */
    private Path installAppImage(Path appImagePath, String appName)
        throws Exception {
        String installDir = PlatformDetector.getInstallDirectory(
            Platform.LINUX_GENERIC
        );
        Files.createDirectories(Paths.get(installDir));

        Path targetPath = Paths.get(installDir, appName + ".AppImage");
        Files.copy(
            appImagePath,
            targetPath,
            StandardCopyOption.REPLACE_EXISTING
        );

        // Make executable
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(
            targetPath
        );
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(targetPath, perms);

        return targetPath;
    }

    /**
     * Install DEB package on Linux.
     */
    private Path installDeb(
        Path debPath,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.INSTALLING,
                0.3,
                "Installing package..."
            )
        );

        // Try dpkg first (may require sudo)
        ProcessBuilder pb = new ProcessBuilder(
            "pkexec",
            "dpkg",
            "-i",
            debPath.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // dpkg failed, maybe missing dependencies - try apt
            pb = new ProcessBuilder("pkexec", "apt", "install", "-f", "-y");
            pb.start().waitFor();
        }

        // check if the package was installed successfully
        if (exitCode != 0) {
            LOG.log(Level.SEVERE, "Failed to install package: " + debPath);
            throw new Exception("Failed to install package");
        }

        // Find the installed package's binary path
        // This is a simplified approach; you might need a more robust method
        // to find the binary path based on the package name
        // (e.g., using `dpkg -L <package-name>`)

        // DEB packages typically install to /usr/bin or /opt
        return Paths.get("/usr/bin"); // NOAH <-- this doesnt work on linux
    }

    /**
     * Install RPM package on Linux.
     */
    private Path installRpm(
        Path rpmPath,
        Consumer<InstallProgress> progressCallback
    ) throws Exception {
        reportProgress(
            progressCallback,
            new InstallProgress(
                InstallProgress.Stage.INSTALLING,
                0.3,
                "Installing package..."
            )
        );

        ProcessBuilder pb = new ProcessBuilder(
            "pkexec",
            "rpm",
            "-i",
            rpmPath.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // Try dnf/yum for dependency resolution
            pb = new ProcessBuilder(
                "pkexec",
                "dnf",
                "install",
                "-y",
                rpmPath.toString()
            );
            pb.start().waitFor();
        }

        return Paths.get("/usr/bin");
    }

    /**
     * Extract a ZIP file.
     */
    private Path extractZip(Path zipPath, Path targetDir, String appName)
        throws IOException {
        Path appDir = targetDir.resolve(appName);
        Files.createDirectories(appDir);

        try (
            ZipInputStream zis = new ZipInputStream(
                Files.newInputStream(zipPath)
            )
        ) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = appDir.resolve(entry.getName());

                // Security check - prevent zip slip
                if (!entryPath.normalize().startsWith(appDir.normalize())) {
                    throw new IOException(
                        "Invalid zip entry: " + entry.getName()
                    );
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(
                        zis,
                        entryPath,
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        }

        return appDir;
    }

    /**
     * Extract a tar.gz file.
     */
    private Path extractTarGz(Path tarGzPath, Path targetDir, String appName)
        throws Exception {
        Path appDir = targetDir.resolve(appName);
        Files.createDirectories(appDir);

        ProcessBuilder pb = new ProcessBuilder(
            "tar",
            "-xzf",
            tarGzPath.toString(),
            "-C",
            appDir.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to extract tar.gz");
        }

        // Make all files in bin directories executable
        makeExecutableRecursive(appDir);

        return appDir;
    }

    /**
     * Find the executable in the installed app.
     */
    private String findExecutable(
        Path installPath,
        String appName,
        Platform platform
    ) {
        if (!Files.exists(installPath)) {
            return null;
        }

        try {
            // For macOS .app bundles - already directly an app bundle
            if (installPath.toString().endsWith(".app")) {
                return installPath.toString();
            }

            // For AppImages
            if (installPath.toString().endsWith(".AppImage")) {
                return installPath.toString();
            }

            // On macOS, search for .app bundle in the install path (for legacy installs)
            if (platform == Platform.MACOS) {
                Path appBundle = findAppBundle(installPath);
                if (appBundle != null) {
                    return appBundle.toString();
                }
            }

            // Search for executable
            String sanitizedName = sanitizeAppName(appName).toLowerCase();

            try (var stream = Files.walk(installPath, 3)) {
                return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        // Match app name or common executable patterns
                        if (
                            name.equals(sanitizedName) ||
                            name.equals(sanitizedName + ".exe") ||
                            name.startsWith(sanitizedName)
                        ) {
                            // Check if executable
                            return Files.isExecutable(p);
                        }
                        return false;
                    })
                    .map(Path::toString)
                    .findFirst()
                    .orElse(installPath.toString());
            }
        } catch (IOException e) {
            return installPath.toString();
        }
    }

    /**
     * Launch an installed application.
     */
    public void launchApp(String executablePath) throws IOException {
        Platform platform = PlatformDetector.detectPlatform();
        LOG.info(
            "[InstallationService] Launching app: " +
                executablePath +
                " on platform: " +
                platform
        );

        ProcessBuilder pb;
        if (platform == Platform.MACOS) {
            if (executablePath.endsWith(".app")) {
                LOG.fine(
                    "[InstallationService] Using 'open' command for .app bundle"
                );
                pb = new ProcessBuilder("open", executablePath);
            } else {
                LOG.fine("[InstallationService] Executing directly");
                pb = new ProcessBuilder(executablePath);
            }
        } else if (platform == Platform.WINDOWS) {
            LOG.fine("[InstallationService] Using 'cmd /c start' for Windows");
            pb = new ProcessBuilder("cmd", "/c", "start", "", executablePath);
        } else {
            // Linux
            LOG.fine("[InstallationService] Executing directly on Linux");
            pb = new ProcessBuilder(executablePath);
        }

        pb.redirectErrorStream(true);
        Process process = pb.start();
        LOG.info(
            "[InstallationService] App launched successfully, PID: " +
                process.pid()
        );
    }

    /**
     * Uninstall an application.
     */
    public boolean uninstallApp(String installPath) {
        LOG.info("[InstallationService] Uninstalling app at: " + installPath);
        try {
            Path path = Paths.get(installPath);
            if (Files.exists(path)) {
                deleteDirectory(path);
                LOG.info(
                    "[InstallationService] Successfully uninstalled: " +
                        installPath
                );
                return true;
            } else {
                LOG.warning(
                    "[InstallationService] Install path does not exist: " +
                        installPath
                );
            }
        } catch (IOException e) {
            LOG.log(
                Level.SEVERE,
                "[InstallationService] Failed to uninstall: " + installPath,
                e
            );
        }
        return false;
    }

    // Utility methods

    private void reportProgress(
        Consumer<InstallProgress> callback,
        InstallProgress progress
    ) {
        if (callback != null) {
            callback.accept(progress);
        }
    }

    private String sanitizeAppName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(
            "%.1f KB",
            bytes / 1024.0
        );
        if (bytes < 1024 * 1024 * 1024) return String.format(
            "%.1f MB",
            bytes / (1024.0 * 1024)
        );
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(
            source,
            new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(
                    Path dir,
                    java.nio.file.attribute.BasicFileAttributes attrs
                ) throws IOException {
                    Path targetDir = target.resolve(source.relativize(dir));
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(
                    Path file,
                    java.nio.file.attribute.BasicFileAttributes attrs
                ) throws IOException {
                    Path targetFile = target.resolve(source.relativize(file));
                    Files.copy(
                        file,
                        targetFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                    );
                    return FileVisitResult.CONTINUE;
                }
            }
        );
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;

        Files.walkFileTree(
            path,
            new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(
                    Path file,
                    java.nio.file.attribute.BasicFileAttributes attrs
                ) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(
                    Path dir,
                    IOException exc
                ) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            }
        );
    }

    private void makeExecutableRecursive(Path dir) throws IOException {
        if (!Files.exists(dir)) return;

        try {
            Files.walkFileTree(
                dir,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(
                        Path file,
                        java.nio.file.attribute.BasicFileAttributes attrs
                    ) throws IOException {
                        try {
                            Set<PosixFilePermission> perms =
                                Files.getPosixFilePermissions(file);
                            perms.add(PosixFilePermission.OWNER_EXECUTE);
                            Files.setPosixFilePermissions(file, perms);
                        } catch (UnsupportedOperationException e) {
                            // Windows doesn't support POSIX permissions
                        }
                        return FileVisitResult.CONTINUE;
                    }
                }
            );
        } catch (UnsupportedOperationException e) {
            // Windows
        }
    }
}
