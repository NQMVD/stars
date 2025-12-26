"use client"

import type { AppData } from "@/app/page"

const LIBRARY_KEY = "stars-library"

export interface InstalledApp {
  id: string
  name: string
  developer: string
  icon: string
  category: string
  version: string
  installedVersion: string
  installTimestamp: number
  size: string
  platforms: ("windows" | "macos" | "linux")[]
}

/**
 * Get all installed apps from localStorage
 */
export function getInstalledApps(): InstalledApp[] {
  if (typeof window === "undefined") return []
  const stored = localStorage.getItem(LIBRARY_KEY)
  if (!stored) return []
  try {
    return JSON.parse(stored) as InstalledApp[]
  } catch {
    return []
  }
}

/**
 * Save installed apps to localStorage
 */
function saveInstalledApps(apps: InstalledApp[]): void {
  localStorage.setItem(LIBRARY_KEY, JSON.stringify(apps))
}

/**
 * Check if an app is installed
 */
export function isAppInstalled(appId: string): boolean {
  return getInstalledApps().some(app => app.id === appId)
}

/**
 * Install an app - returns a promise that simulates download
 */
export async function installApp(
  app: AppData, 
  onProgress?: (progress: number, status: string) => void
): Promise<InstalledApp> {
  // Simulate download/install progress
  for (let i = 0; i <= 100; i += 10) {
    await new Promise(resolve => setTimeout(resolve, 200))
    if (onProgress) {
      if (i < 50) {
        onProgress(i, `Downloading... ${i}%`)
      } else if (i < 90) {
        onProgress(i, "Installing...")
      } else {
        onProgress(i, "Finishing up...")
      }
    }
  }

  const installedApp: InstalledApp = {
    id: app.id,
    name: app.name,
    developer: app.developer,
    icon: app.icon,
    category: app.category,
    version: app.version,
    installedVersion: app.version,
    installTimestamp: Date.now(),
    size: app.size,
    platforms: app.platforms,
  }

  const apps = getInstalledApps()
  if (!apps.some(a => a.id === app.id)) {
    apps.push(installedApp)
    saveInstalledApps(apps)
  }

  return installedApp
}

/**
 * Remove an app from the library
 */
export function removeApp(appId: string): boolean {
  const apps = getInstalledApps()
  const filtered = apps.filter(app => app.id !== appId)
  if (filtered.length !== apps.length) {
    saveInstalledApps(filtered)
    return true
  }
  return false
}

/**
 * Update an app (simulates updating by bumping version)
 */
export async function updateApp(
  appId: string,
  onProgress?: (progress: number, status: string) => void
): Promise<InstalledApp | null> {
  // Simulate update progress
  for (let i = 0; i <= 100; i += 20) {
    await new Promise(resolve => setTimeout(resolve, 150))
    if (onProgress) {
      onProgress(i, i < 80 ? "Updating..." : "Finishing...")
    }
  }

  const apps = getInstalledApps()
  const index = apps.findIndex(app => app.id === appId)
  
  if (index !== -1) {
    // Bump patch version
    const currentVersion = apps[index].installedVersion
    const parts = currentVersion.split(".")
    if (parts.length === 3) {
      parts[2] = String(parseInt(parts[2]) + 1)
    } else if (parts.length === 2) {
      parts[1] = String(parseInt(parts[1]) + 1)
    }
    apps[index].installedVersion = parts.join(".")
    saveInstalledApps(apps)
    return apps[index]
  }
  
  return null
}

/**
 * Get a single installed app by ID
 */
export function getInstalledApp(appId: string): InstalledApp | undefined {
  return getInstalledApps().find(app => app.id === appId)
}
