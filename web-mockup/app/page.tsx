"use client"

import { useState } from "react"
import { Sidebar } from "@/components/sidebar"
import { Header } from "@/components/header"
import { DiscoverView } from "@/components/views/discover-view"
import { CategoryView } from "@/components/views/category-view"
import { SearchView } from "@/components/views/search-view"
import { LibraryView } from "@/components/views/library-view"
import { SettingsView } from "@/components/views/settings-view"
import { AppDetailView } from "@/components/views/app-detail-view"
import { UpdatesView } from "@/components/views/updates-view"

export type ViewType = "discover" | "category" | "search" | "library" | "settings" | "app-detail" | "updates"

export interface AppData {
  id: string
  name: string
  developer: string
  icon: string
  description: string
  shortDescription: string
  category: string
  rating: number
  reviews: number
  downloads: string
  size: string
  version: string
  platforms: ("windows" | "macos" | "linux")[]
  screenshots: string[]
  price: string
  installed?: boolean
  hasUpdate?: boolean
}

export default function AppStore() {
  const [currentView, setCurrentView] = useState<ViewType>("discover")
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedApp, setSelectedApp] = useState<AppData | null>(null)
  const [selectedPlatform, setSelectedPlatform] = useState<"all" | "windows" | "macos" | "linux">("all")

  const handleCategorySelect = (category: string) => {
    setSelectedCategory(category)
    setCurrentView("category")
  }

  const handleSearch = (query: string) => {
    setSearchQuery(query)
    if (query.trim()) {
      setCurrentView("search")
    }
  }

  const handleAppSelect = (app: AppData) => {
    setSelectedApp(app)
    setCurrentView("app-detail")
  }

  const handleBack = () => {
    setCurrentView("discover")
    setSelectedApp(null)
  }

  const renderView = () => {
    switch (currentView) {
      case "discover":
        return (
          <DiscoverView
            onCategorySelect={handleCategorySelect}
            onAppSelect={handleAppSelect}
            selectedPlatform={selectedPlatform}
          />
        )
      case "category":
        return (
          <CategoryView
            category={selectedCategory!}
            onAppSelect={handleAppSelect}
            onBack={() => setCurrentView("discover")}
            selectedPlatform={selectedPlatform}
          />
        )
      case "search":
        return <SearchView query={searchQuery} onAppSelect={handleAppSelect} selectedPlatform={selectedPlatform} />
      case "library":
        return <LibraryView onAppSelect={handleAppSelect} selectedPlatform={selectedPlatform} />
      case "updates":
        return <UpdatesView onAppSelect={handleAppSelect} />
      case "settings":
        return <SettingsView />
      case "app-detail":
        return <AppDetailView app={selectedApp!} onBack={handleBack} />
      default:
        return null
    }
  }

  return (
    <div className="flex h-full w-full bg-background">
      <Sidebar currentView={currentView} onViewChange={setCurrentView} onCategorySelect={handleCategorySelect} />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header
          onSearch={handleSearch}
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          selectedPlatform={selectedPlatform}
          onPlatformChange={setSelectedPlatform}
        />
        <main className="flex-1 overflow-y-auto">{renderView()}</main>
      </div>
    </div>
  )
}
