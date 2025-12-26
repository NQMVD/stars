"use client"

import type { AppData } from "@/app/page"
import { AppCard } from "@/components/app-card"
import { Button } from "@/components/ui/button"
import { ChevronRight, TrendingUp, Sparkles, Clock } from "lucide-react"
import { mockApps } from "@/lib/mock-data"

interface DiscoverViewProps {
  onCategorySelect: (category: string) => void
  onAppSelect: (app: AppData) => void
  selectedPlatform: "all" | "windows" | "macos" | "linux"
}

export function DiscoverView({ onCategorySelect, onAppSelect, selectedPlatform }: DiscoverViewProps) {
  const filteredApps =
    selectedPlatform === "all" ? mockApps : mockApps.filter((app) => app.platforms.includes(selectedPlatform))

  const featuredApps = filteredApps.slice(0, 3)
  const trendingApps = filteredApps.slice(3, 9)
  const newApps = filteredApps.slice(9, 15)
  const editorsPicks = filteredApps.slice(0, 6)

  return (
    <div className="p-8 space-y-12">
      {/* Hero Section */}
      <section>
        <div className="flex items-center justify-between mb-6 border-b-2 border-border pb-2">
          <div className="flex items-center gap-3">
            <Sparkles className="h-6 w-6 text-primary" />
            <h2 className="text-2xl font-bold uppercase tracking-tighter">Featured</h2>
          </div>
          <Button variant="ghost" size="sm" className="text-muted-foreground uppercase font-bold tracking-tight">
            See All <ChevronRight className="ml-1 h-4 w-4" />
          </Button>
        </div>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {featuredApps.map((app) => (
            <AppCard key={app.id} app={app} onSelect={onAppSelect} variant="featured" />
          ))}
        </div>
      </section>

      {/* Trending Section */}
      <section>
        <div className="flex items-center justify-between mb-6 border-b-2 border-border pb-2">
          <div className="flex items-center gap-3">
            <TrendingUp className="h-6 w-6 text-primary" />
            <h2 className="text-2xl font-bold uppercase tracking-tighter">Trending</h2>
          </div>
          <Button variant="ghost" size="sm" className="text-muted-foreground uppercase font-bold tracking-tight">
            See All <ChevronRight className="ml-1 h-4 w-4" />
          </Button>
        </div>
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {trendingApps.map((app) => (
            <AppCard key={app.id} app={app} onSelect={onAppSelect} />
          ))}
        </div>
      </section>

      {/* New & Updated */}
      <section>
        <div className="flex items-center justify-between mb-6 border-b-2 border-border pb-2">
          <div className="flex items-center gap-3">
            <Clock className="h-6 w-6 text-primary" />
            <h2 className="text-2xl font-bold uppercase tracking-tighter">New & Updated</h2>
          </div>
          <Button variant="ghost" size="sm" className="text-muted-foreground uppercase font-bold tracking-tight">
            See All <ChevronRight className="ml-1 h-4 w-4" />
          </Button>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {newApps.map((app) => (
            <AppCard key={app.id} app={app} onSelect={onAppSelect} variant="compact" />
          ))}
        </div>
      </section>

      {/* Categories Grid */}
      <section>
        <h2 className="text-2xl font-bold uppercase tracking-tighter mb-6 border-b-2 border-border pb-2">Browse Categories</h2>
        <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
          {[
            { id: "developer-tools", name: "Developer Tools", count: 234 },
            { id: "productivity", name: "Productivity", count: 567 },
            { id: "graphics-design", name: "Graphics & Design", count: 189 },
            { id: "games", name: "Games", count: 1234 },
            { id: "music-audio", name: "Music & Audio", count: 345 },
            { id: "video", name: "Video", count: 278 },
            { id: "utilities", name: "Utilities", count: 456 },
            { id: "security", name: "Security", count: 123 },
          ].map((category) => (
            <button
              key={category.id}
              onClick={() => onCategorySelect(category.id)}
              className="flex items-center justify-between border-2 border-border bg-card p-4 transition-all hover:bg-primary hover:text-primary-foreground hover:border-primary group"
            >
              <span className="font-bold uppercase tracking-tight">{category.name}</span>
              <span className="text-sm font-medium group-hover:text-primary-foreground/80">{category.count}</span>
            </button>
          ))}
        </div>
      </section>

      {/* Editors' Picks */}
      <section>
        <div className="flex items-center justify-between mb-6 border-b-2 border-border pb-2">
          <h2 className="text-2xl font-bold uppercase tracking-tighter">Editors' Picks</h2>
          <Button variant="ghost" size="sm" className="text-muted-foreground uppercase font-bold tracking-tight">
            See All <ChevronRight className="ml-1 h-4 w-4" />
          </Button>
        </div>
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {editorsPicks.map((app) => (
            <AppCard key={app.id} app={app} onSelect={onAppSelect} />
          ))}
        </div>
      </section>
    </div>
  )
}
