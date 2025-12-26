package com.example.appstore.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Detects the current platform and Linux distribution.
 * Used for selecting the appropriate release asset to download.
 */
public class PlatformDetector {

    private static final Logger LOG = LogManager.getLogger(
        PlatformDetector.class
    );

    public enum Platform {
        WINDOWS,
        MACOS,
        LINUX_DEB, // Ubuntu, Debian, etc.
        LINUX_RPM, // Fedora, RHEL, CentOS
        LINUX_ARCH, // Arch, Manjaro
        LINUX_GENERIC, // AppImage, tar.gz fallback
    }

    private static Platform cachedPlatform = null;

    /**
     * Detect the current platform.
     */
    public static Platform detectPlatform() {
        if (cachedPlatform != null) {
            return cachedPlatform;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch");
        LOG.debug("Detecting platform - OS: {}, Arch: {}", os, arch);

        if (os.contains("win")) {
            cachedPlatform = Platform.WINDOWS;
        } else if (os.contains("mac")) {
            cachedPlatform = Platform.MACOS;
        } else if (
            os.contains("linux") || os.contains("nix") || os.contains("nux")
        ) {
            cachedPlatform = detectLinuxDistribution();
        } else {
            LOG.warn("Unknown OS: {}, defaulting to LINUX_GENERIC", os);
            cachedPlatform = Platform.LINUX_GENERIC;
        }

        LOG.info(
            "Detected platform: {}",
            getPlatformDisplayName(cachedPlatform)
        );
        return cachedPlatform;
    }

    /**
     * Detect Linux distribution from /etc/os-release.
     */
    private static Platform detectLinuxDistribution() {
        try {
            String osRelease = executeCommand("cat", "/etc/os-release");
            String lowerRelease = osRelease.toLowerCase();
            LOG.debug(
                "/etc/os-release contents (first 200 chars): {}",
                osRelease.substring(0, Math.min(200, osRelease.length()))
            );

            if (
                lowerRelease.contains("ubuntu") ||
                lowerRelease.contains("debian") ||
                lowerRelease.contains("pop!_os") ||
                lowerRelease.contains("mint") ||
                lowerRelease.contains("elementary")
            ) {
                LOG.info("Detected Debian-based Linux distribution");
                return Platform.LINUX_DEB;
            } else if (
                lowerRelease.contains("arch") ||
                lowerRelease.contains("manjaro") ||
                lowerRelease.contains("endeavouros")
            ) {
                LOG.info("Detected Arch-based Linux distribution");
                return Platform.LINUX_ARCH;
            } else if (
                lowerRelease.contains("fedora") ||
                lowerRelease.contains("rhel") ||
                lowerRelease.contains("centos") ||
                lowerRelease.contains("rocky") ||
                lowerRelease.contains("alma") ||
                lowerRelease.contains("opensuse")
            ) {
                LOG.info("Detected RPM-based Linux distribution");
                return Platform.LINUX_RPM;
            }
        } catch (Exception e) {
            LOG.warn(
                "Failed to detect Linux distribution: {}",
                e.getMessage(),
                e
            );
        }

        LOG.info("Unknown Linux distribution, using generic");
        return Platform.LINUX_GENERIC;
    }

    /**
     * Get the architecture (amd64, arm64, x86_64, etc.)
     */
    public static String getArchitecture() {
        String arch = System.getProperty("os.arch").toLowerCase();

        if (arch.contains("amd64") || arch.contains("x86_64")) {
            return "x64";
        } else if (arch.contains("aarch64") || arch.contains("arm64")) {
            return "arm64";
        } else if (
            arch.contains("x86") ||
            arch.contains("i386") ||
            arch.contains("i686")
        ) {
            return "x86";
        }

        return arch;
    }

    /**
     * Check if an asset name is suitable for the current platform.
     */
    public static boolean isAssetForPlatform(
        String assetName,
        Platform platform
    ) {
        String name = assetName.toLowerCase();
        String arch = getArchitecture();

        // Filter out source archives
        if (
            name.endsWith(".tar.gz") &&
            (name.contains("source") || name.contains("src"))
        ) {
            return false;
        }

        switch (platform) {
            case WINDOWS:
                return (
                    (name.endsWith(".exe") ||
                        name.endsWith(".msi") ||
                        name.endsWith(".zip")) &&
                    (name.contains("win") || name.contains("windows")) &&
                    isArchCompatible(name, arch)
                );
            case MACOS:
                return (
                    (name.endsWith(".dmg") ||
                        name.endsWith(".pkg") ||
                        name.endsWith(".zip") ||
                        name.endsWith(".app.tar.gz")) &&
                    (name.contains("mac") ||
                        name.contains("darwin") ||
                        name.contains("osx")) &&
                    isArchCompatible(name, arch)
                );
            case LINUX_DEB:
                if (name.endsWith(".deb") && isArchCompatible(name, arch)) {
                    return true;
                }
                // Fall through to AppImage as alternative
                return (
                    name.endsWith(".appimage") && isArchCompatible(name, arch)
                );
            case LINUX_RPM:
                if (name.endsWith(".rpm") && isArchCompatible(name, arch)) {
                    return true;
                }
                return (
                    name.endsWith(".appimage") && isArchCompatible(name, arch)
                );
            case LINUX_ARCH:
                // Arch uses pacman but many projects only provide AUR, so prefer AppImage
                if (
                    name.endsWith(".pkg.tar.zst") ||
                    name.endsWith(".pkg.tar.xz")
                ) {
                    return isArchCompatible(name, arch);
                }
                return (
                    name.endsWith(".appimage") && isArchCompatible(name, arch)
                );
            case LINUX_GENERIC:
                // Prefer AppImage, then tar.gz
                if (name.endsWith(".appimage")) {
                    return isArchCompatible(name, arch);
                }
                return (
                    name.endsWith(".tar.gz") &&
                    (name.contains("linux") || name.contains("nux")) &&
                    !name.contains("source") &&
                    isArchCompatible(name, arch)
                );
            default:
                return false;
        }
    }

    /**
     * Check architecture compatibility in asset name.
     */
    private static boolean isArchCompatible(String name, String arch) {
        // If no architecture is specified in the name, assume it's compatible
        boolean hasArchInName =
            name.contains("x64") ||
            name.contains("x86_64") ||
            name.contains("amd64") ||
            name.contains("arm64") ||
            name.contains("aarch64") ||
            name.contains("x86") ||
            name.contains("i386") ||
            name.contains("i686") ||
            name.contains("universal");

        if (!hasArchInName) {
            return true;
        }

        // Check specific architecture matches
        if ("x64".equals(arch)) {
            return (
                name.contains("x64") ||
                name.contains("x86_64") ||
                name.contains("amd64") ||
                name.contains("universal")
            );
        } else if ("arm64".equals(arch)) {
            return (
                name.contains("arm64") ||
                name.contains("aarch64") ||
                name.contains("universal")
            );
        } else if ("x86".equals(arch)) {
            return (
                name.contains("x86") ||
                name.contains("i386") ||
                name.contains("i686") ||
                name.contains("win32")
            );
        }

        return true;
    }

    /**
     * Get the priority of an asset (higher is better).
     * Used to select the best asset when multiple are available.
     */
    public static int getAssetPriority(String assetName, Platform platform) {
        String name = assetName.toLowerCase();

        switch (platform) {
            case WINDOWS:
                if (name.endsWith(".msi")) return 10;
                if (
                    name.endsWith(".exe") && !name.contains("portable")
                ) return 8;
                if (
                    name.endsWith(".exe") && name.contains("portable")
                ) return 5;
                if (name.endsWith(".zip")) return 3;
                break;
            case MACOS:
                if (name.endsWith(".dmg")) return 10;
                if (name.endsWith(".pkg")) return 8;
                if (name.endsWith(".app.tar.gz")) return 6;
                if (name.endsWith(".zip")) return 4;
                break;
            case LINUX_DEB:
                if (name.endsWith(".deb")) return 10;
                if (name.endsWith(".appimage")) return 7;
                if (name.endsWith(".tar.gz")) return 3;
                break;
            case LINUX_RPM:
                if (name.endsWith(".rpm")) return 10;
                if (name.endsWith(".appimage")) return 7;
                if (name.endsWith(".tar.gz")) return 3;
                break;
            case LINUX_ARCH:
                if (
                    name.endsWith(".pkg.tar.zst") ||
                    name.endsWith(".pkg.tar.xz")
                ) return 10;
                if (name.endsWith(".appimage")) return 8;
                if (name.endsWith(".tar.gz")) return 3;
                break;
            case LINUX_GENERIC:
                if (name.endsWith(".appimage")) return 10;
                if (
                    name.endsWith(".tar.gz") && name.contains("linux")
                ) return 5;
                break;
        }

        return 0;
    }

    /**
     * Get the installation directory for the platform.
     */
    public static String getInstallDirectory(Platform platform) {
        String userHome = System.getProperty("user.home");

        switch (platform) {
            case WINDOWS:
                String appData = System.getenv("LOCALAPPDATA");
                return appData != null
                    ? appData + "\\Stars\\Apps"
                    : userHome + "\\AppData\\Local\\Stars\\Apps";
            case MACOS:
                return "/Applications";
            case LINUX_DEB:
            case LINUX_RPM:
                // System packages install to /usr/bin, /opt, etc.
                // For user-managed apps, use ~/.local
                return userHome + "/.local/share/applications";
            case LINUX_ARCH:
            case LINUX_GENERIC:
                return userHome + "/.local/bin";
            default:
                return userHome + "/.stars/apps";
        }
    }

    /**
     * Get the download directory.
     */
    public static String getDownloadDirectory() {
        String userHome = System.getProperty("user.home");
        return userHome + "/.stars/downloads";
    }

    private static String executeCommand(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            )
        ) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Get a human-readable platform name.
     */
    public static String getPlatformDisplayName(Platform platform) {
        switch (platform) {
            case WINDOWS:
                return "Windows";
            case MACOS:
                return "macOS";
            case LINUX_DEB:
                return "Linux (Debian/Ubuntu)";
            case LINUX_RPM:
                return "Linux (Fedora/RHEL)";
            case LINUX_ARCH:
                return "Linux (Arch)";
            case LINUX_GENERIC:
                return "Linux";
            default:
                return "Unknown";
        }
    }
}
