"use client"

import type React from "react"

import { Search } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { PlatformSelector } from "@/components/platform-selector"

interface HeaderProps {
  onSearch: (query: string) => void
  searchQuery: string
  setSearchQuery: (query: string) => void
  selectedPlatform: "all" | "windows" | "macos" | "linux"
  onPlatformChange: (platform: "all" | "windows" | "macos" | "linux") => void
}

export function Header({ onSearch, searchQuery, setSearchQuery, selectedPlatform, onPlatformChange }: HeaderProps) {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSearch(searchQuery)
  }

  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-background px-8">
      <form onSubmit={handleSubmit} className="relative w-full max-w-lg">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-primary" />
        <Input
          type="search"
          placeholder="SEARCH APPS..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="h-10 border-2 border-border pl-10 text-sm font-bold uppercase tracking-tight placeholder:text-muted-foreground focus-visible:ring-0 focus-visible:border-primary"
        />
      </form>

      <div className="flex items-center gap-6">
        <PlatformSelector selected={selectedPlatform} onChange={onPlatformChange} />
        <Button variant="outline" size="sm" className="h-10 border-2 font-bold uppercase tracking-wider">
          Feedback
        </Button>
      </div>
    </header>
  )
}
