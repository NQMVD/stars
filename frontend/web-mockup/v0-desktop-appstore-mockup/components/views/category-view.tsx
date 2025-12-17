"use client"

import type { AppData } from "@/app/page"
import { AppCard } from "@/components/app-card"
import { Button } from "@/components/ui/button"
import { ArrowLeft, SlidersHorizontal } from "lucide-react"
import { mockApps } from "@/lib/mock-data"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"

interface CategoryViewProps {
  category: string
  onAppSelect: (app: AppData) => void
  onBack: () => void
  selectedPlatform: "all" | "windows" | "macos" | "linux"
}

const categoryNames: Record<string, string> = {
  "developer-tools": "Developer Tools",
  productivity: "Productivity",
  "graphics-design": "Graphics & Design",
  games: "Games",
  "music-audio": "Music & Audio",
  video: "Video",
  utilities: "Utilities",
  security: "Security",
}

export function CategoryView({ category, onAppSelect, onBack, selectedPlatform }: CategoryViewProps) {
  const categoryApps = mockApps.filter((app) => {
    const matchesPlatform = selectedPlatform === "all" || app.platforms.includes(selectedPlatform)
    return matchesPlatform
  })

  return (
    <div className="p-8 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between border-b-2 border-border pb-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={onBack} className="rounded-none">
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-3xl font-black uppercase tracking-tighter">{categoryNames[category] || category}</h1>
            <p className="text-sm font-bold text-muted-foreground uppercase tracking-wide">{categoryApps.length} apps available</p>
          </div>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm" className="uppercase font-bold tracking-tight">
              <SlidersHorizontal className="mr-2 h-4 w-4" />
              Sort & Filter
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="rounded-none border-2">
            <DropdownMenuItem className="uppercase font-bold tracking-tight text-xs">Most Popular</DropdownMenuItem>
            <DropdownMenuItem className="uppercase font-bold tracking-tight text-xs">Highest Rated</DropdownMenuItem>
            <DropdownMenuItem className="uppercase font-bold tracking-tight text-xs">Newest</DropdownMenuItem>
            <DropdownMenuItem className="uppercase font-bold tracking-tight text-xs">Price: Low to High</DropdownMenuItem>
            <DropdownMenuItem className="uppercase font-bold tracking-tight text-xs">Price: High to Low</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* Subcategories */}
      <div className="flex flex-wrap gap-2">
        {["All", "Code Editors", "Version Control", "Databases", "APIs", "Testing"].map((sub) => (
          <Button key={sub} variant={sub === "All" ? "default" : "outline"} size="sm" className="uppercase font-bold tracking-tight text-xs">
            {sub}
          </Button>
        ))}
      </div>

      {/* Apps Grid */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {categoryApps.map((app) => (
          <AppCard key={app.id} app={app} onSelect={onAppSelect} />
        ))}
      </div>
    </div>
  )
}
