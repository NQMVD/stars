"use client"

import type { AppData } from "@/app/page"
import { AppCard } from "@/components/app-card"
import { Button } from "@/components/ui/button"
import { SlidersHorizontal, Search } from "lucide-react"
import { mockApps } from "@/lib/mock-data"

interface SearchViewProps {
  query: string
  onAppSelect: (app: AppData) => void
  selectedPlatform: "all" | "windows" | "macos" | "linux"
}

export function SearchView({ query, onAppSelect, selectedPlatform }: SearchViewProps) {
  const results = mockApps.filter((app) => {
    const matchesQuery =
      app.name.toLowerCase().includes(query.toLowerCase()) ||
      app.description.toLowerCase().includes(query.toLowerCase()) ||
      app.developer.toLowerCase().includes(query.toLowerCase())
    const matchesPlatform = selectedPlatform === "all" || app.platforms.includes(selectedPlatform)
    return matchesQuery && matchesPlatform
  })

  return (
    <div className="p-8 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between border-b-2 border-border pb-6">
        <div>
          <div className="flex items-center gap-2 text-muted-foreground uppercase font-bold tracking-wider text-xs">
            <Search className="h-4 w-4" />
            <span>Search results for</span>
          </div>
          <h1 className="text-3xl font-black uppercase tracking-tighter mt-1">"{query}"</h1>
          <p className="text-sm font-bold text-muted-foreground uppercase tracking-wide mt-1">{results.length} results found</p>
        </div>
        <Button variant="outline" size="sm" className="uppercase font-bold tracking-tight">
          <SlidersHorizontal className="mr-2 h-4 w-4" />
          Filters
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-2">
        <Button variant="secondary" size="sm" className="uppercase font-bold tracking-tight text-xs">
          All Results
        </Button>
        <Button variant="outline" size="sm" className="uppercase font-bold tracking-tight text-xs">
          Free
        </Button>
        <Button variant="outline" size="sm" className="uppercase font-bold tracking-tight text-xs">
          Paid
        </Button>
        <Button variant="outline" size="sm" className="uppercase font-bold tracking-tight text-xs">
          4+ Stars
        </Button>
      </div>

      {/* Results */}
      {results.length > 0 ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {results.map((app) => (
            <AppCard key={app.id} app={app} onSelect={onAppSelect} />
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-24 text-center border-2 border-dashed border-border">
          <Search className="h-12 w-12 text-muted-foreground mb-4" />
          <h2 className="text-xl font-bold uppercase tracking-tight">No results found</h2>
          <p className="text-sm font-medium text-muted-foreground mt-1 uppercase tracking-wide">Try adjusting your search or filter criteria</p>
        </div>
      )}
    </div>
  )
}
