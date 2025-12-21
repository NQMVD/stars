import json
import subprocess

def get_latest_release(full_name):
    try:
        result = subprocess.run(
            ["gh", "api", f"repos/{full_name}/releases/latest"],
            capture_output=True, text=True, check=True
        )
        return json.loads(result.stdout)
    except: return None

def check_assets(release):
    if not release: return [], []
    assets = release.get("assets", [])
    qualifying = []
    platforms = set()
    exts = [".exe", ".msi", ".dmg", ".pkg", ".deb", ".rpm", ".appimage", ".flatpak"]
    for a in assets:
        name = a["name"]
        lname = name.lower()
        if any(lname.endswith(ext) for ext in exts) or ".app.zip" in lname:
            qualifying.append({"name": name, "download_url": a["browser_download_url"]})
            if ".exe" in lname or ".msi" in lname: platforms.add("Windows")
            if ".dmg" in lname or ".pkg" in lname or ".app" in lname: platforms.add("macOS")
            if any(x in lname for x in [".deb", ".rpm", ".appimage", ".flatpak"]): platforms.add("Linux")
    return qualifying, sorted(list(platforms))

candidates = [
    "dinoki-ai/osaurus", "Far-Beyond-Pulsar/Pulsar-Native", "ejbills/DockDoor",
    "rlxone/Equinox", "LinwoodDev/Butterfly", "ransome1/sleek", 
    "gaauwe/fast-forward", "HeroTools/open-whispr", "TNTwise/REAL-Video-Enhancer",
    "nook-browser/Nook", "quests-org/quests", "2mawi2/schaltwerk",
    "ArthurSonzogni/Diagon", "fontforge/fontforge", "toeverything/AFFiNE",
    "ggml-org/LlamaBarn"
]

results = []
with open("current_batch.jsonl", "r") as f:
    for line in f:
        if "|" in line: line = line.split("|", 1)[1]
        try: repo = json.loads(line)
        except: continue
        
        if repo["full_name"] in candidates:
            release = get_latest_release(repo["full_name"])
            assets, platforms = check_assets(release)
            if assets:
                results.append({
                    "full_name": repo["full_name"],
                    "description": repo["description"],
                    "language": repo["language"],
                    "html_url": repo["html_url"],
                    "stargazers_count": repo["stargazers_count"],
                    "topics": repo["topics"],
                    "release_assets": assets,
                    "supported_platforms": platforms
                })

print(json.dumps(results, indent=2))
