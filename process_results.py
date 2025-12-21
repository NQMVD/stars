import json
import csv
import os

OUTPUT_FILE = 'complete_desktop_apps_catalog.csv'

def save_results(results):
    file_exists = os.path.exists(OUTPUT_FILE)
    
    # Define columns based on the original schema
    columns = [
        'full_name', 'description', 'language', 'html_url', 
        'stargazers_count', 'topics', 'release_assets', 
        'windows_support', 'macos_support', 'linux_support', 'platform_notes'
    ]
    
    with open(OUTPUT_FILE, 'a', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=columns)
        if not file_exists:
            writer.writeheader()
        
        for app in results:
            # Map subagent output to CSV columns, with fallback for different keys
            supported_platforms = app.get('supported_platforms') or app.get('detected_platforms') or []
            release_assets = app.get('release_assets') or app.get('installable_assets') or []
            
            row = {
                'full_name': app.get('full_name'),
                'description': app.get('description'),
                'language': app.get('language'),
                'html_url': app.get('html_url'),
                'stargazers_count': app.get('stargazers_count'),
                'topics': json.dumps(app.get('topics', [])),
                'release_assets': json.dumps(release_assets),
                'windows_support': 'Windows' in supported_platforms,
                'macos_support': 'macOS' in supported_platforms or 'MacOS' in supported_platforms,
                'linux_support': 'Linux' in supported_platforms,
                'platform_notes': f"Supported platforms: {', '.join(supported_platforms)}"
            }
            writer.writerow(row)

if __name__ == "__main__":
    # This script will be called with JSON results from the subagent
    import sys
    if len(sys.argv) > 1:
        with open(sys.argv[1], 'r') as f:
            data = json.load(f)
            save_results(data)
