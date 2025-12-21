import json
import subprocess
import os
import re
from concurrent.futures import ThreadPoolExecutor

def get_latest_release(full_name):
    try:
        result = subprocess.run(
            ["gh", "api", f"repos/{full_name}/releases/latest"],
            capture_output=True, text=True, check=True
        )
        return json.loads(result.stdout)
    except subprocess.CalledProcessError:
        try:
            result = subprocess.run(
                ["gh", "api", f"repos/{full_name}/releases"],
                capture_output=True, text=True, check=True
            )
            releases = json.loads(result.stdout)
            if releases:
                return releases[0]
        except:
            return None
        return None
    except Exception:
        return None

def is_desktop_app(repo):
    description = (repo.get("description") or "").lower()
    topics = [t.lower() for t in (repo.get("topics") or [])]
    name = repo.get("name", "").lower()
    homepage = (repo.get("homepage") or "").lower()
    
    # Exclude obvious non-apps
    if any(t in topics for t in ["tui", "terminal-ui", "cli", "command-line", "library", "api", "sdk", "framework"]):
        if not any(t in topics for t in ["gui", "desktop", "electron", "tauri", "game"]):
            if "desktop" not in description and "gui " not in description:
                return False

    gui_indicators = {
        "electron", "tauri", "gpui", "gui", "desktop-app", "macos-app", 
        "windows-app", "linux-app", "appimage", "flatpak", "wails", 
        "maui", "avalonia", "javafx", "swing", "compose-desktop", 
        "qt", "gtk", "wxwidgets", "flutter-desktop", "menubar-app", "tray-app",
        "swiftui", "uikit", "cocoa", "egui", "imgui", "slint", "libadwaita", "gtk4",
        "winui", "winappsdk", "xaml", "wpf"
    }
    if any(topic in gui_indicators or any(gi in topic for gi in ["gtk", "qt", "electron", "tauri"]) for topic in topics):
        return True
        
    desktop_keywords = [
        "desktop app", "gui for", "native app", "menubar", "system tray", 
        "cross-platform app", "desktop client", "window manager", 
        "code editor", "text editor", "web browser", "image viewer",
        "for macos", "for windows", "for linux", "native application",
        "client for", "player for", "viewer for", "editor for",
        "desktop application", "gui application", "mind-mapping", "web-browser",
        "tiling window manager", "terminal emulator"
    ]
    
    if any(kw in description for kw in desktop_keywords):
        if not any(ex in description for ex in ["library for", "cli for", "api for", "framework for", "sdk for"]):
            return True

    if re.search(r'\b(app|application)\b', description) or "app" in topics or name.endswith("-app") or ".app" in homepage:
        exclude_indicators = ["cli", "command-line", "library", "sdk", "api", "framework", "template", "boilerplate"]
        if any(ex in description for ex in exclude_indicators) or any(ex in topics for ex in exclude_indicators):
            if any(kw in description for kw in ["gui", "desktop", "native", "visual", "game"]):
                return True
            return False
        return True

    if "desktop" in topics or "game" in topics:
        return True

    return False

def check_assets(release, repo_is_desktop=False):
    if not release:
        return [], [], False
    
    assets = release.get("assets", [])
    qualifying_assets = []
    platforms = set()
    
    strong_exts = [".dmg", ".pkg", ".appimage", ".msi", ".flatpak", ".deb", ".rpm", ".exe", ".jar"]
    
    has_strong_asset = False
    
    for asset in assets:
        name = asset["name"]
        url = asset["browser_download_url"]
        lname = name.lower()
        
        if any(lname.endswith(ext) for ext in [".txt", ".md", ".json", ".sha256", ".sig", ".asc", ".sha512", ".pdb", ".yaml", ".yml", ".checksums", ".pem", ".sig"]):
            continue

        is_qualifying = False
        if any(lname.endswith(ext) for ext in strong_exts):
            if lname.endswith(".exe") and asset.get("size", 0) < 500000 and not repo_is_desktop:
                continue
            if lname.endswith(".jar") and asset.get("size", 0) < 1000000 and not repo_is_desktop:
                continue
            is_qualifying = True
            if not lname.endswith(".jar"): # jar is somewhat weak indicator of desktop app by itself
                has_strong_asset = True
        elif any(kw in lname for kw in ["macos", "win32", "win64", "windows", "linux-x64", "linux-arm64", "apple-darwin", "universal"]):
            if any(lname.endswith(ext) for ext in [".zip", ".tar.gz", ".tgz", ".tar.xz", ".app.zip"]):
                if repo_is_desktop or asset.get("size", 0) > 3000000:
                    is_qualifying = True
            elif "." not in lname or (repo_is_desktop and asset.get("size", 0) > 3000000):
                 is_qualifying = True
        elif repo_is_desktop and any(lname.endswith(ext) for ext in [".zip", ".tar.gz", ".tgz", ".tar.xz"]):
            if not any(bad in lname for bad in ["src", "source", "devtools"]):
                is_qualifying = True

        if is_qualifying:
            qualifying_assets.append({"name": name, "download_url": url})
            
            if any(ext in lname for ext in [".exe", ".msi"]) or "win" in lname:
                platforms.add("Windows")
            if any(ext in lname for ext in [".dmg", ".pkg"]) or ".app" in lname or "macos" in lname or "darwin" in lname:
                platforms.add("macOS")
            if any(ext in lname for ext in [".deb", ".rpm", ".appimage", ".flatpak"]) or "linux" in lname:
                platforms.add("Linux")
            if lname.endswith(".jar") and not platforms:
                platforms.add("Cross-platform")
                
    return qualifying_assets, sorted(list(platforms)), has_strong_asset

def process_repo(line):
    try:
        if "|" in line:
            line = line.split("|", 1)[1]
        repo = json.loads(line)
    except Exception:
        return None
        
    is_desktop = is_desktop_app(repo)
    
    # If it looks like a desktop app or is popular, check releases
    if is_desktop or repo.get("stargazers_count", 0) > 1000:
        release = get_latest_release(repo["full_name"])
        if not release:
            return None
            
        qualifying_assets, platforms, has_strong_asset = check_assets(release, repo_is_desktop=is_desktop)
        
        include = False
        if qualifying_assets:
            if is_desktop:
                include = True
            elif has_strong_asset:
                description = (repo.get("description") or "").lower()
                topics = [t.lower() for t in (repo.get("topics") or [])]
                cli_indicators = ["command-line", "cli tool", "terminal tool", "cli for", "interface for the command line", "shell script", "shell tool", "git server"]
                if not any(ind in description for ind in cli_indicators) and not any(ind in topics for ind in ["cli", "tui"]):
                    include = True
        
        if include:
            return {
                "full_name": repo["full_name"],
                "description": repo["description"],
                "language": repo["language"],
                "html_url": repo["html_url"],
                "stargazers_count": repo["stargazers_count"],
                "topics": repo["topics"],
                "release_assets": qualifying_assets,
                "supported_platforms": platforms
            }
    return None

def main():
    lines = []
    if os.path.exists("current_batch.jsonl"):
        with open("current_batch.jsonl", "r") as f:
            lines = f.readlines()
    
    results = []
    with ThreadPoolExecutor(max_workers=20) as executor:
        for result in executor.map(process_repo, lines):
            if result:
                results.append(result)
                
    print(json.dumps(results, indent=2))

if __name__ == "__main__":
    main()
