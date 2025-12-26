"use client"

import type React from "react"
import { useState, useEffect } from "react"

import type { AppData } from "@/app/page"
import { Button } from "@/components/ui/button"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"
import { Download, Check, RefreshCw, Trash2, Loader2 } from "lucide-react"
import { getInstalledApps, removeApp, updateApp, type InstalledApp } from "@/lib/library-store"

interface LibraryViewProps {
  onAppSelect: (app: AppData) => void
  selectedPlatform: "all" | "windows" | "macos" | "linux"
}

export function LibraryView({ onAppSelect, selectedPlatform }: LibraryViewProps) {
  const [installedApps, setInstalledApps] = useState<InstalledApp[]>([])
  const [isLoading, setIsLoading] = useState(true)

  // Load installed apps from localStorage
  useEffect(() => {
    setInstalledApps(getInstalledApps())
    setIsLoading(false)
  }, [])

  const refreshLibrary = () => {
    setInstalledApps(getInstalledApps())
  }

  const filteredApps = installedApps.filter(
    (app) => selectedPlatform === "all" || app.platforms.includes(selectedPlatform)
  )

  return (
    <div className="p-8 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between border-b-2 border-border pb-6">
        <div>
          <h1 className="text-3xl font-black uppercase tracking-tighter">My Library</h1>
          <p className="text-sm font-bold text-muted-foreground uppercase tracking-wide">{installedApps.length} apps installed</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={refreshLibrary} className="uppercase font-bold tracking-tight">
            <RefreshCw className="mr-2 h-4 w-4" />
            Refresh
          </Button>
        </div>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="installed" className="w-full">
        <TabsList className="w-full justify-start rounded-none bg-transparent border-b-2 border-border p-0 h-auto gap-8 mb-6">
          <TabsTrigger value="installed" className="rounded-none border-b-4 border-transparent px-0 py-3 font-bold uppercase tracking-wider text-muted-foreground data-[state=active]:border-primary data-[state=active]:text-foreground data-[state=active]:bg-transparent shadow-none">Installed</TabsTrigger>
          <TabsTrigger value="purchases" className="rounded-none border-b-4 border-transparent px-0 py-3 font-bold uppercase tracking-wider text-muted-foreground data-[state=active]:border-primary data-[state=active]:text-foreground data-[state=active]:bg-transparent shadow-none">Purchases</TabsTrigger>
          <TabsTrigger value="wishlist" className="rounded-none border-b-4 border-transparent px-0 py-3 font-bold uppercase tracking-wider text-muted-foreground data-[state=active]:border-primary data-[state=active]:text-foreground data-[state=active]:bg-transparent shadow-none">Wishlist</TabsTrigger>
        </TabsList>

        <TabsContent value="installed" className="mt-0">
          {isLoading ? (
            <div className="flex items-center justify-center py-16">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : filteredApps.length > 0 ? (
            <div className="space-y-4">
              {filteredApps.map((app) => (
                <LibraryAppItem
                  key={app.id}
                  app={app}
                  onRemove={() => {
                    removeApp(app.id)
                    refreshLibrary()
                  }}
                  onUpdate={async () => {
                    await updateApp(app.id)
                    refreshLibrary()
                  }}
                />
              ))}
            </div>
          ) : (
            <EmptyState icon={Download} title="No apps installed" description="Apps you install will appear here" />
          )}
        </TabsContent>

        <TabsContent value="purchases" className="mt-0">
          <EmptyState icon={Check} title="No purchases yet" description="Apps you purchase will appear here" />
        </TabsContent>

        <TabsContent value="wishlist" className="mt-0">
          <EmptyState
            icon={Download}
            title="Your wishlist is empty"
            description="Save apps to your wishlist to find them later"
          />
        </TabsContent>
      </Tabs>
    </div>
  )
}

function LibraryAppItem({
  app,
  onRemove,
  onUpdate,
}: {
  app: InstalledApp
  onRemove: () => void
  onUpdate: () => Promise<void>
}) {
  const [isUpdating, setIsUpdating] = useState(false)
  const [isRemoving, setIsRemoving] = useState(false)

  const handleUpdate = async () => {
    setIsUpdating(true)
    await onUpdate()
    setIsUpdating(false)
  }

  const handleRemove = () => {
    setIsRemoving(true)
    setTimeout(() => {
      onRemove()
    }, 300)
  }

  return (
    <div className="flex items-center gap-6 border-2 border-border bg-card p-6 transition-all hover:border-primary group">
      <div className="flex flex-1 items-center gap-6 text-left">
        <img src={app.icon || "/placeholder.svg"} alt={app.name} className="h-16 w-16 border border-border" />
        <div className="flex-1 min-w-0">
          <h3 className="text-lg font-bold uppercase tracking-tight">{app.name}</h3>
          <p className="text-sm font-bold text-muted-foreground uppercase tracking-wide">{app.developer}</p>
          <div className="flex items-center gap-3 mt-2">
            <span className="text-xs font-medium uppercase tracking-wider text-muted-foreground bg-secondary px-2 py-0.5">v{app.installedVersion}</span>
            <span className="text-xs text-muted-foreground">•</span>
            <span className="text-xs font-medium uppercase tracking-wider text-muted-foreground">{app.size}</span>
            <span className="text-xs text-muted-foreground">•</span>
            <span className="text-xs font-bold uppercase tracking-wider text-green-600">Installed</span>
          </div>
        </div>
      </div>
      <div className="flex items-center gap-3">
        <Button size="sm" variant="default" onClick={handleUpdate} disabled={isUpdating} className="uppercase font-bold tracking-tight">
          {isUpdating ? (
            <>
              <Loader2 className="mr-1 h-3 w-3 animate-spin" />
              Updating...
            </>
          ) : (
            <>
              <RefreshCw className="mr-1 h-3 w-3" />
              Update
            </>
          )}
        </Button>
        <Button size="sm" variant="secondary" className="uppercase font-bold tracking-tight">
          Open
        </Button>
        <Button
          size="sm"
          variant="ghost"
          className="text-muted-foreground hover:text-destructive hover:bg-destructive/10 uppercase font-bold tracking-tight rounded-none"
          onClick={handleRemove}
          disabled={isRemoving}
        >
          {isRemoving ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Trash2 className="h-4 w-4" />
          )}
        </Button>
      </div>
    </div>
  )
}

function EmptyState({
  icon: Icon,
  title,
  description,
}: {
  icon: React.ComponentType<{ className?: string }>
  title: string
  description: string
}) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <Icon className="h-12 w-12 text-muted-foreground mb-4" />
      <h2 className="text-lg font-semibold">{title}</h2>
      <p className="text-sm text-muted-foreground mt-1">{description}</p>
    </div>
  )
}
