import json
import subprocess
import os
import re

def get_latest_release(full_name):
    try:
        result = subprocess.run(
            ["gh", "api", f"repos/{full_name}/releases/latest"],
            capture_output=True, text=True, check=True
        )
        return json.loads(result.stdout)
    except subprocess.CalledProcessError:
        return None
    except Exception:
        return None

def is_desktop_app(repo):
    description = (repo.get("description") or "").lower()
    topics = [t.lower() for t in (repo.get("topics") or [])]
    name = repo.get("name", "").lower()
    
    # High confidence signals
    if any(sig in topics for sig in ["electron", "tauri", "gpui", "gui", "desktop-app", "macos-app", "windows-app", "linux-app", "appimage", "flatpak"]):
        return True
        
    desktop_keywords = [
        "gui", "desktop", "electron", "tauri", "native", "macos", "windows", "linux", 
        "application", "qt", "gtk", "flutter", "swift", "gpui", "viewer", "editor", 
        "browser", "menubar", "tray", "cross-platform"
    ]
    
    # If "app" is a whole word
    if re.search(r'\bapp\b', description) or re.search(r'\bapp\b', " ".join(topics)):
        if "library" not in description and "cli" not in description:
            return True

    if any(kw in description for kw in desktop_keywords) or any(kw in topics for kw in desktop_keywords):
        # Exclude typical non-app things unless they also have "app" or "gui"
        exclude_keywords = ["library", "sdk", "api", "framework", "plugin", "extension", "theme"]
        if any(ex in description for ex in exclude_keywords):
            if "gui" in description or "desktop" in description or "native app" in description:
                return True
            return False
        return True
    
    return False

def check_assets(release):
    if not release:
        return [], []
    
    assets = release.get("assets", [])
    qualifying_assets = []
    platforms = set()
    
    # Extensions for desktop installers/executables
    desktop_exts = [".exe", ".msi", ".dmg", ".pkg", ".deb", ".rpm", ".AppImage", ".flatpak", ".pacman"]
    
    for asset in assets:
        name = asset["name"]
        url = asset["browser_download_url"]
        
        # Check extension
        if any(name.endswith(ext) for ext in desktop_exts) or ".app.zip" in name:
            qualifying_assets.append({"name": name, "download_url": url})
            
            lname = name.lower()
            if any(ext in lname for ext in [".exe", ".msi"]):
                platforms.add("Windows")
            if any(ext in lname for ext in [".dmg", ".pkg"]) or ".app" in lname:
                platforms.add("macOS")
            if any(ext in lname for ext in [".deb", ".rpm", ".appimage", ".flatpak"]):
                platforms.add("Linux")
                
    return qualifying_assets, sorted(list(platforms))

results = []
with open("current_batch.jsonl", "r") as f:
    for line in f:
        if not line.strip():
            continue
        try:
            if "|" in line:
                line = line.split("|", 1)[1]
            repo = json.loads(line)
        except Exception as e:
            continue
            
        # Even if not sure, if it has a release, we'll check the assets
        # To be safe and thorough, we can check all repos that have "has_downloads": True 
        # but 150 API calls might be slow. Let's stick to a slightly broader filter.
        
        if is_desktop_app(repo) or repo.get("stargazers_count", 0) > 1000:
            release = get_latest_release(repo["full_name"])
            qualifying_assets, platforms = check_assets(release)
            
            if qualifying_assets:
                results.append({
                    "full_name": repo["full_name"],
                    "description": repo["description"],
                    "language": repo["language"],
                    "html_url": repo["html_url"],
                    "stargazers_count": repo["stargazers_count"],
                    "topics": repo["topics"],
                    "release_assets": qualifying_assets,
                    "supported_platforms": platforms
                })

print(json.dumps(results, indent=2))
