"use client"

import type { AppData } from "@/app/page"
import { Button } from "@/components/ui/button"
import { RefreshCw, Download, Check, ArrowRight } from "lucide-react"
import { mockApps } from "@/lib/mock-data"
import { PlatformBadges } from "@/components/platform-badges"

interface UpdatesViewProps {
  onAppSelect: (app: AppData) => void
}

export function UpdatesView({ onAppSelect }: UpdatesViewProps) {
  const appsWithUpdates = mockApps.filter((app) => app.hasUpdate)

  return (
    <div className="p-8 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between border-b-2 border-border pb-6">
        <div>
          <h1 className="text-3xl font-black uppercase tracking-tighter">Updates</h1>
          <p className="text-sm font-bold text-muted-foreground uppercase tracking-wide">{appsWithUpdates.length} updates available</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" size="sm" className="uppercase font-bold tracking-tight">
            <RefreshCw className="mr-2 h-4 w-4" />
            Refresh
          </Button>
          {appsWithUpdates.length > 0 && (
            <Button size="sm" className="uppercase font-bold tracking-tight">
              <Download className="mr-2 h-4 w-4" />
              Update All
            </Button>
          )}
        </div>
      </div>

      {/* Updates List */}
      {appsWithUpdates.length > 0 ? (
        <div className="space-y-4">
          {appsWithUpdates.map((app) => (
            <div key={app.id} className="flex items-center gap-6 border-2 border-border bg-card p-6 transition-all hover:border-primary">
              <button onClick={() => onAppSelect(app)} className="flex flex-1 items-center gap-6 text-left group">
                <img src={app.icon || "/placeholder.svg"} alt={app.name} className="h-16 w-16 border border-border group-hover:border-primary transition-colors" />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3">
                    <h3 className="text-xl font-bold uppercase tracking-tight group-hover:text-primary transition-colors">{app.name}</h3>
                    <PlatformBadges platforms={app.platforms} size="sm" />
                  </div>
                  <p className="text-sm font-bold text-muted-foreground uppercase tracking-wide">{app.developer}</p>
                  <div className="flex items-center gap-3 mt-2 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                    <span className="bg-secondary px-2 py-0.5 text-foreground">v{app.version}</span>
                    <ArrowRight className="h-3 w-3" />
                    <span className="bg-primary/10 text-primary px-2 py-0.5 font-bold">v2.0.0</span>
                    <span>•</span>
                    <span>{app.size}</span>
                  </div>
                </div>
              </button>
              <div className="flex flex-col items-end gap-3 text-right">
                <Button size="sm" className="uppercase font-bold tracking-tight">
                  <Download className="mr-1 h-3 w-3" />
                  Update
                </Button>
                <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Released 2 days ago</span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-24 text-center border-2 border-dashed border-border p-8">
          <Check className="h-12 w-12 text-green-500 mb-4" />
          <h2 className="text-xl font-bold uppercase tracking-tight">All apps are up to date</h2>
          <p className="text-sm font-medium text-muted-foreground mt-1 uppercase tracking-wide">Check back later for new updates</p>
        </div>
      )}

      {/* Update History */}
      <section className="mt-12">
        <h2 className="text-xl font-black uppercase tracking-tighter mb-6 pb-2 border-b-2 border-border">Recently Updated</h2>
        <div className="space-y-3">
          {mockApps.slice(0, 3).map((app) => (
            <button
              key={app.id}
              onClick={() => onAppSelect(app)}
              className="flex w-full items-center gap-4 border-2 border-border bg-card p-4 text-left transition-all hover:border-primary group"
            >
              <img src={app.icon || "/placeholder.svg"} alt={app.name} className="h-12 w-12 border border-border" />
              <div className="flex-1 min-w-0">
                <h4 className="font-bold uppercase tracking-tight group-hover:text-primary transition-colors">{app.name}</h4>
                <p className="text-xs font-bold uppercase tracking-wide text-muted-foreground">Updated to v{app.version} • 3 days ago</p>
              </div>
              <Check className="h-5 w-5 text-green-500" />
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}
