import json
import subprocess
import os
import re
import sys
from concurrent.futures import ThreadPoolExecutor

def get_latest_release(full_name):
    try:
        result = subprocess.run(
            ["gh", "api", f"repos/{full_name}/releases/latest"],
            capture_output=True, text=True, check=True
        )
        return json.loads(result.stdout)
    except:
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

def identify_desktop_app(repo, assets):
    description = (repo.get("description") or "").lower()
    topics = [t.lower() for t in (repo.get("topics") or [])]
    name = repo.get("name", "").lower()
    
    # If it has strong desktop assets, it is a desktop app
    strong_exts = {".dmg", ".pkg", ".appimage", ".msi", ".deb", ".rpm", ".exe"}
    has_strong_asset = any(any(asset["name"].lower().endswith(ext) for ext in strong_exts) for asset in assets)
    if has_strong_asset:
        return True

    # GUI topics
    gui_indicators = {
        "electron", "tauri", "gui", "desktop-app", "macos-app", "windows-app", 
        "linux-app", "appimage", "flatpak", "wails", "javafx", "swing", 
        "qt", "gtk", "wxwidgets", "flutter-desktop", "swiftui", "cocoa", "egui"
    }
    if any(topic in gui_indicators for topic in topics):
        return True
    
    if any(kw in name for kw in ["-gui", "-desktop", "-app"]):
        return True

    desktop_keywords = [
        "desktop app", "gui for", "native app", "menubar", "system tray", 
        "cross-platform app", "desktop client", "window manager", 
        "code editor", "text editor", "web browser", "image viewer",
        "for macos", "for windows", "for linux", "native application"
    ]
    if any(kw in description for kw in desktop_keywords):
        return True
        
    if "gtk" in description or "qt" in description or "ui" in description:
        if "library" not in description and "api" not in description:
             return True

    return False

def check_assets(release):
    if not release:
        return [], []
    
    assets = release.get("assets", [])
    qualifying_assets = []
    platforms = set()
    
    installable_exts = [".dmg", ".pkg", ".appimage", ".msi", ".deb", ".rpm", ".exe", ".zip", ".tar.gz", ".tar.xz"]
    exclude_exts = [".txt", ".md", ".json", ".sha256", ".sig", ".asc", ".sha512", ".pdb", ".yaml", ".yml", ".xml"]

    for asset in assets:
        name = asset["name"]
        url = asset["browser_download_url"]
        lname = name.lower()
        
        if any(lname.endswith(ext) for ext in exclude_exts):
            continue

        if any(lname.endswith(ext) for ext in installable_exts):
            # For zip/tar.gz, only include if they seem to be binaries (not source)
            if any(ext in lname for ext in [".zip", ".tar.gz", ".tar.xz"]):
                if any(bad in lname for bad in ["src", "source", "devel", "headers"]):
                    continue
            
            qualifying_assets.append({"name": name, "download_url": url})
            
            if any(ext in lname for ext in [".exe", ".msi"]) or "win" in lname:
                platforms.add("Windows")
            if any(ext in lname for ext in [".dmg", ".pkg"]) or ".app" in lname or "macos" in lname or "darwin" in lname:
                platforms.add("macOS")
            if any(ext in lname for ext in [".deb", ".rpm", ".appimage", ".flatpak"]) or "linux" in lname:
                platforms.add("Linux")
                
    return qualifying_assets, sorted(list(platforms))

def process_repo(line):
    try:
        if "|" in line:
            line = line.split("|", 1)[1]
        repo = json.loads(line)
    except:
        return None
        
    release = get_latest_release(repo["full_name"])
    assets, platforms = check_assets(release)
    is_desktop = identify_desktop_app(repo, assets)
    
    return {
        "full_name": repo["full_name"],
        "is_desktop_app": is_desktop,
        "description": repo.get("description"),
        "html_url": repo.get("html_url"),
        "stargazers_count": repo.get("stargazers_count"),
        "installable_assets": assets,
        "detected_platforms": platforms
    }

def main():
    with open("current_batch.jsonl", "r") as f:
        lines = f.readlines()
    
    results = []
    with ThreadPoolExecutor(max_workers=15) as executor:
        for result in executor.map(process_repo, lines):
            if result:
                results.append(result)
                
    print(json.dumps(results, indent=2))

if __name__ == "__main__":
    main()
