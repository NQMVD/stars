"use client"

import type React from "react"

import { cn } from "@/lib/utils"
import type { ViewType } from "@/app/page"
import {
  Home,
  Grid3X3,
  Download,
  Settings,
  Gamepad2,
  Code,
  Palette,
  Music,
  Video,
  FileText,
  Shield,
  Briefcase,
  RefreshCw,
} from "lucide-react"
import { InstallationIndicator } from "./installation-indicator"

interface SidebarProps {
  currentView: ViewType
  onViewChange: (view: ViewType) => void
  onCategorySelect: (category: string) => void
}

const categories = [
  { id: "developer-tools", name: "Developer Tools", icon: Code },
  { id: "productivity", name: "Productivity", icon: Briefcase },
  { id: "graphics-design", name: "Graphics & Design", icon: Palette },
  { id: "games", name: "Games", icon: Gamepad2 },
  { id: "music-audio", name: "Music & Audio", icon: Music },
  { id: "video", name: "Video", icon: Video },
  { id: "utilities", name: "Utilities", icon: FileText },
  { id: "security", name: "Security", icon: Shield },
]

export function Sidebar({ currentView, onViewChange, onCategorySelect }: SidebarProps) {
  return (
    <aside className="flex w-64 flex-col border-r border-border bg-sidebar">
      {/* Logo */}
      <div className="flex h-16 items-center gap-3 border-b border-border px-6">
        <div className="flex h-8 w-8 items-center justify-center bg-primary text-primary-foreground">
          <Grid3X3 className="h-5 w-5" />
        </div>
        <span className="text-xl font-bold uppercase tracking-tighter">AppVault</span>
      </div>

      {/* Main Navigation */}
      <nav className="flex-1 overflow-y-auto px-4 py-6">
        <div className="space-y-1">
          <SidebarItem
            icon={Home}
            label="Discover"
            active={currentView === "discover"}
            onClick={() => onViewChange("discover")}
          />
          <SidebarItem
            icon={Download}
            label="Library"
            active={currentView === "library"}
            onClick={() => onViewChange("library")}
          />
          <SidebarItem
            icon={RefreshCw}
            label="Updates"
            active={currentView === "updates"}
            onClick={() => onViewChange("updates")}
            badge={3}
          />
          <SidebarItem
            icon={Settings}
            label="Settings"
            active={currentView === "settings"}
            onClick={() => onViewChange("settings")}
          />
        </div>

        {/* Categories */}
        <div className="mt-10">
          <h3 className="mb-4 px-2 text-sm font-bold uppercase tracking-widest text-foreground">Categories</h3>
          <div className="space-y-0.5">
            {categories.map((category) => (
              <SidebarItem
                key={category.id}
                icon={category.icon}
                label={category.name}
                active={currentView === "category"}
                onClick={() => onCategorySelect(category.id)}
              />
            ))}
          </div>
        </div>
      </nav>

      {/* Installation Indicator */}
      <div className="border-t border-border p-4">
        <InstallationIndicator appName="Visual Studio Code" currentStep={1} status="processing" />
      </div>
    </aside>
  )
}

interface SidebarItemProps {
  icon: React.ComponentType<{ className?: string }>
  label: string
  active?: boolean
  onClick?: () => void
  badge?: number
}

function SidebarItem({ icon: Icon, label, active, onClick, badge }: SidebarItemProps) {
  return (
    <button
      onClick={onClick}
      className={cn(
        "group flex w-full items-center gap-3 px-2 py-2.5 text-sm transition-colors border-l-2",
        active
          ? "border-primary bg-accent/10 text-primary font-bold tracking-tight"
          : "border-transparent text-muted-foreground hover:bg-accent/5 hover:text-foreground"
      )}
    >
      <Icon className={cn("h-4 w-4", active && "stroke-[2.5px]")} />
      <span className={cn("flex-1 text-left uppercase tracking-tight", active ? "font-bold" : "font-medium")}>{label}</span>
      {badge && (
        <span className={cn(
          "flex h-5 min-w-5 items-center justify-center text-xs font-bold",
          active ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"
        )}>
          {badge}
        </span>
      )}
    </button>
  )
}
