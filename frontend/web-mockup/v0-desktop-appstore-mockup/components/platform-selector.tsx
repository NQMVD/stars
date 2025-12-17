"use client"

import { cn } from "@/lib/utils"
import { Monitor, Apple, Terminal } from "lucide-react"

interface PlatformSelectorProps {
  selected: "all" | "windows" | "macos" | "linux"
  onChange: (platform: "all" | "windows" | "macos" | "linux") => void
}

const platforms = [
  { id: "all", label: "All", icon: null },
  { id: "windows", label: "Windows", icon: Monitor },
  { id: "macos", label: "macOS", icon: Apple },
  { id: "linux", label: "Linux", icon: Terminal },
] as const

export function PlatformSelector({ selected, onChange }: PlatformSelectorProps) {
  return (
    <div className="flex items-center rounded-lg border border-border bg-secondary p-1">
      {platforms.map((platform) => (
        <button
          key={platform.id}
          onClick={() => onChange(platform.id)}
          className={cn(
            "flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm transition-colors",
            selected === platform.id
              ? "bg-background text-foreground shadow-sm"
              : "text-muted-foreground hover:text-foreground",
          )}
        >
          {platform.icon && <platform.icon className="h-3.5 w-3.5" />}
          <span>{platform.label}</span>
        </button>
      ))}
    </div>
  )
}
