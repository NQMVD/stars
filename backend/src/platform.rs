use crate::models::{GithubAsset, Platform};
use tracing::debug;

pub fn detect_platform() -> Platform {
    let os = std::env::consts::OS.to_lowercase();
    let arch = std::env::consts::ARCH.to_string();

    debug!("Detecting platform - OS: {}, Arch: {}", os, arch);

    if os.contains("windows") {
        Platform::Windows
    } else if os.contains("mac") || os.contains("darwin") {
        Platform::Macos
    } else if os.contains("linux") || os.contains("nix") || os.contains("nux") {
        detect_linux_distribution()
    } else {
        debug!("Unknown OS: {}, defaulting to LINUX_GENERIC", os);
        Platform::LinuxGeneric
    }
}

fn detect_linux_distribution() -> Platform {
    if let Ok(os_release) = std::fs::read_to_string("/etc/os-release") {
        let lower_release = os_release.to_lowercase();

        if lower_release.contains("ubuntu")
            || lower_release.contains("debian")
            || lower_release.contains("pop!_os")
            || lower_release.contains("mint")
            || lower_release.contains("elementary")
        {
            debug!("Detected Debian-based Linux distribution");
            return Platform::LinuxDeb;
        } else if lower_release.contains("arch")
            || lower_release.contains("manjaro")
            || lower_release.contains("endeavouros")
        {
            debug!("Detected Arch-based Linux distribution");
            return Platform::LinuxArch;
        } else if lower_release.contains("fedora")
            || lower_release.contains("rhel")
            || lower_release.contains("centos")
            || lower_release.contains("rocky")
            || lower_release.contains("alma")
            || lower_release.contains("opensuse")
        {
            debug!("Detected RPM-based Linux distribution");
            return Platform::LinuxRpm;
        }
    }

    debug!("Unknown Linux distribution, using generic");
    Platform::LinuxGeneric
}

pub fn get_architecture() -> String {
    let arch = std::env::consts::ARCH.to_lowercase();

    if arch.contains("amd64") || arch.contains("x86_64") {
        "x64".to_string()
    } else if arch.contains("aarch64") || arch.contains("arm64") {
        "arm64".to_string()
    } else if arch.contains("x86") || arch.contains("i386") || arch.contains("i686") {
        "x86".to_string()
    } else {
        arch
    }
}

pub fn is_asset_for_platform(asset_name: &str, platform: &Platform) -> bool {
    let name = asset_name.to_lowercase();
    let arch = get_architecture();

    if name.ends_with(".tar.gz")
        && (name.contains("source") || name.contains("src"))
    {
        return false;
    }

    match platform {
        Platform::Windows => {
            (name.ends_with(".exe") || name.ends_with(".msi") || name.ends_with(".zip"))
                && is_arch_compatible(&name, &arch)
        }
        Platform::Macos => {
            (name.ends_with(".dmg")
                || name.ends_with(".pkg")
                || name.ends_with(".zip")
                || name.ends_with(".app.tar.gz"))
                && is_arch_compatible(&name, &arch)
        }
        Platform::LinuxDeb => {
            if name.ends_with(".deb") && is_arch_compatible(&name, &arch) {
                return true;
            }
            name.ends_with(".appimage") && is_arch_compatible(&name, &arch)
        }
        Platform::LinuxRpm => {
            if name.ends_with(".rpm") && is_arch_compatible(&name, &arch) {
                return true;
            }
            name.ends_with(".appimage") && is_arch_compatible(&name, &arch)
        }
        Platform::LinuxArch => {
            if name.ends_with(".pkg.tar.zst") || name.ends_with(".pkg.tar.xz") {
                return is_arch_compatible(&name, &arch);
            }
            name.ends_with(".appimage") && is_arch_compatible(&name, &arch)
        }
        Platform::LinuxGeneric => {
            if name.ends_with(".appimage") {
                return is_arch_compatible(&name, &arch);
            }
            name.ends_with(".tar.gz")
                && (name.contains("linux") || name.contains("nux"))
                && !name.contains("source")
                && is_arch_compatible(&name, &arch)
        }
    }
}

fn is_arch_compatible(name: &str, arch: &str) -> bool {
    let has_arch_in_name = name.contains("x64")
        || name.contains("x86_64")
        || name.contains("amd64")
        || name.contains("arm64")
        || name.contains("aarch64")
        || name.contains("x86")
        || name.contains("i386")
        || name.contains("i686")
        || name.contains("universal");

    if !has_arch_in_name {
        return true;
    }

    match arch {
        "x64" => name.contains("x64")
            || name.contains("x86_64")
            || name.contains("amd64")
            || name.contains("universal"),
        "arm64" => name.contains("arm64")
            || name.contains("aarch64")
            || name.contains("universal"),
        "x86" => name.contains("x86")
            || name.contains("i386")
            || name.contains("i686")
            || name.contains("win32"),
        _ => true,
    }
}

pub fn get_asset_priority(asset_name: &str, platform: &Platform) -> u32 {
    let name = asset_name.to_lowercase();

    match platform {
        Platform::Windows => {
            if name.ends_with(".msi") {
                return 10;
            }
            if name.ends_with(".exe") && !name.contains("portable") {
                return 8;
            }
            if name.ends_with(".exe") && name.contains("portable") {
                return 5;
            }
            if name.ends_with(".zip") {
                return 3;
            }
        }
        Platform::Macos => {
            if name.ends_with(".dmg") {
                return 10;
            }
            if name.ends_with(".pkg") {
                return 8;
            }
            if name.ends_with(".zip") {
                return 4;
            }
        }
        Platform::LinuxDeb => {
            if name.ends_with(".deb") {
                return 10;
            }
            if name.ends_with(".appimage") {
                return 7;
            }
            if name.ends_with(".tar.gz") {
                return 3;
            }
        }
        Platform::LinuxRpm => {
            if name.ends_with(".rpm") {
                return 10;
            }
            if name.ends_with(".appimage") {
                return 7;
            }
            if name.ends_with(".tar.gz") {
                return 3;
            }
        }
        Platform::LinuxArch => {
            if name.ends_with(".pkg.tar.zst") || name.ends_with(".pkg.tar.xz") {
                return 10;
            }
            if name.ends_with(".appimage") {
                return 8;
            }
            if name.ends_with(".tar.gz") {
                return 3;
            }
        }
        Platform::LinuxGeneric => {
            if name.ends_with(".appimage") {
                return 10;
            }
            if name.ends_with(".tar.gz") && name.contains("linux") {
                return 5;
            }
        }
    }

    0
}

pub fn find_best_asset<'a>(assets: &'a [GithubAsset], platform: &Platform) -> Option<&'a GithubAsset> {
    assets
        .iter()
        .filter(|asset| is_asset_for_platform(&asset.name, platform))
        .max_by_key(|asset| get_asset_priority(&asset.name, platform))
}

pub fn get_platform_display_name(platform: &Platform) -> String {
    match platform {
        Platform::Windows => "Windows".to_string(),
        Platform::Macos => "macOS".to_string(),
        Platform::LinuxDeb => "Linux (Debian/Ubuntu)".to_string(),
        Platform::LinuxRpm => "Linux (Fedora/RHEL)".to_string(),
        Platform::LinuxArch => "Linux (Arch)".to_string(),
        Platform::LinuxGeneric => "Linux".to_string(),
    }
}

pub fn is_linux_platform(platform: &Platform) -> bool {
    matches!(
        platform,
        Platform::LinuxDeb | Platform::LinuxRpm | Platform::LinuxArch | Platform::LinuxGeneric
    )
}
