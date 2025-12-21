import json
import subprocess
import os
import csv

BATCH_SIZE = 150
REPOS_FILE = 'all_starred_repos.jsonl'
PROGRESS_FILE = 'progress_counter.txt'
OUTPUT_FILE = 'complete_desktop_apps_catalog.csv'

DESKTOP_KEYWORDS = [
    'gui', 'desktop', 'window', 'electron', 'qt', 'tk', 'wx', 'fltk', 
    'iced', 'egui', 'slint', 'tauri', 'appimage', 'flatpak', 'snap',
    'native', 'cross-platform app', 'macos app', 'windows app', 'linux app',
    'javafx', 'swing', 'wpf', 'uwp', 'winui', 'cocoa', 'gtk', 'sdl'
]

def is_potential_desktop_app(repo):
    description = (repo.get('description') or '').lower()
    name = repo.get('name', '').lower()
    topics = [t.lower() for t in (repo.get('topics') or [])]
    
    # Check if it's a library or CLI
    if 'library' in description or 'wrapper' in description or 'api' in description:
        # But it might be a GUI library, we want apps.
        pass
    
    if 'cli' in description or 'command line' in description or 'terminal' in description:
        # Might be a TUI, but task says exclude terminal/CLI tools.
        # However, some apps are TUI and Desktop. Let's be strict.
        if 'gui' not in description and 'desktop' not in description:
            return False

    combined = description + ' ' + name + ' ' + ' '.join(topics)
    for kw in DESKTOP_KEYWORDS:
        if kw in combined:
            return True
            
    # Also check language for common desktop languages
    # This might be too broad, so we rely more on keywords.
    return False

def get_progress():
    if os.path.exists(PROGRESS_FILE):
        with open(PROGRESS_FILE, 'r') as f:
            content = f.read().strip()
            if content:
                return int(content)
    return 0

def update_progress(index):
    with open(PROGRESS_FILE, 'w') as f:
        f.write(str(index))

def load_repos():
    repos = []
    if os.path.exists(REPOS_FILE):
        with open(REPOS_FILE, 'r') as f:
            for line in f:
                repos.append(json.loads(line))
    return repos

def main():
    repos = load_repos()
    total_repos = len(repos)
    start_index = get_progress()
    
    print(f"Total repos: {total_repos}")
    
    batch_start = start_index
    batch_end = min(batch_start + BATCH_SIZE, total_repos)
    
    if batch_start >= total_repos:
        print("All repos processed.")
        return

    current_batch = repos[batch_start:batch_end]
    batch_file = f'current_batch.jsonl'
    with open(batch_file, 'w') as f:
        for repo in current_batch:
            f.write(json.dumps(repo) + '\n')
            
    print(f"Prepared batch of {len(current_batch)} repos in {batch_file}")
    print(f"Indices {batch_start} to {batch_end} in total repos list.")

if __name__ == "__main__":
    main()
