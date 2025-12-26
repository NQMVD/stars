"use client"

import type { AppData } from "@/app/page"
import { Button } from "@/components/ui/button"
import { Star, Download, Check } from "lucide-react"
import { PlatformBadges } from "@/components/platform-badges"

interface AppCardProps {
  app: AppData
  onSelect: (app: AppData) => void
  variant?: "default" | "compact" | "featured"
}

export function AppCard({ app, onSelect, variant = "default" }: AppCardProps) {
  if (variant === "featured") {
    return (
      <button
        onClick={() => onSelect(app)}
        className="group relative flex flex-col overflow-hidden border-2 border-border bg-card transition-all hover:border-primary text-left"
      >
        <div className="aspect-[2/1] w-full overflow-hidden bg-secondary border-b-2 border-border group-hover:border-primary">
          <img
            src={app.screenshots[0] || "/placeholder.svg"}
            alt={app.name}
            className="h-full w-full object-cover grayscale transition-all group-hover:grayscale-0"
          />
        </div>
        <div className="flex flex-1 items-start gap-4 p-5">
          <img src={app.icon || "/placeholder.svg"} alt={app.name} className="h-14 w-14 border-2 border-border" />
          <div className="flex-1 min-w-0">
            <h3 className="text-lg font-bold uppercase tracking-tight text-card-foreground truncate">{app.name}</h3>
            <p className="mt-1 text-sm font-medium text-muted-foreground line-clamp-2 uppercase tracking-wide">{app.shortDescription}</p>
            <div className="mt-3 flex items-center gap-2">
              <PlatformBadges platforms={app.platforms} size="sm" />
            </div>
          </div>
          <Button
            size="sm"
            variant={app.installed ? "secondary" : "default"}
            className="uppercase tracking-wider font-bold"
            onClick={(e) => {
              e.stopPropagation()
            }}
          >
            {app.installed ? (
              <>
                <Check className="mr-1 h-3 w-3" /> INSTALLED
              </>
            ) : (
              "INSTALL"
            )}
          </Button>
        </div>
      </button>
    )
  }

  if (variant === "compact") {
    return (
      <button
        onClick={() => onSelect(app)}
        className="flex items-center gap-4 border border-border bg-card p-3 text-left transition-all hover:bg-accent hover:border-primary"
      >
        <img src={app.icon || "/placeholder.svg"} alt={app.name} className="h-10 w-10 border border-border" />
        <div className="flex-1 min-w-0">
          <h4 className="font-bold uppercase tracking-tight text-card-foreground truncate">{app.name}</h4>
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{app.developer}</p>
        </div>
        <Button
          size="sm"
          variant={app.installed ? "secondary" : "default"}
          className="uppercase tracking-wider font-bold h-8"
          onClick={(e) => {
            e.stopPropagation()
          }}
        >
          {app.installed ? "OPEN" : "GET"}
        </Button>
      </button>
    )
  }

  return (
    <button
      onClick={() => onSelect(app)}
      className="group flex flex-col overflow-hidden border-2 border-border bg-card transition-all hover:border-primary text-left"
    >
      <div className="aspect-video w-full overflow-hidden bg-secondary border-b-2 border-border group-hover:border-primary">
        <img
          src={app.screenshots[0] || "/placeholder.svg"}
          alt={app.name}
          className="h-full w-full object-cover grayscale transition-all group-hover:grayscale-0"
        />
      </div>
      <div className="flex flex-1 flex-col p-4">
        <div className="flex items-start gap-3">
          <img src={app.icon || "/placeholder.svg"} alt={app.name} className="h-12 w-12 border border-border" />
          <div className="flex-1 min-w-0">
            <h3 className="font-bold uppercase tracking-tight text-card-foreground truncate">{app.name}</h3>
            <p className="text-xs font-bold uppercase tracking-wide text-muted-foreground">{app.developer}</p>
          </div>
        </div>
        <p className="mt-3 text-sm font-medium text-muted-foreground line-clamp-2 uppercase tracking-wide">{app.shortDescription}</p>
        <div className="mt-4 flex items-center justify-between border-t border-border pt-3">
          <div className="flex items-center gap-1">
            <Star className="h-3 w-3 fill-foreground text-foreground" />
            <span className="text-sm font-bold">{app.rating}</span>
          </div>
          <PlatformBadges platforms={app.platforms} size="sm" />
        </div>
        <div className="mt-3 flex items-center justify-between">
          <span className="text-sm font-bold uppercase">{app.price === "Free" ? "Free" : app.price}</span>
          <Button
            size="sm"
            variant={app.installed ? "secondary" : "default"}
            className="uppercase tracking-wider font-bold"
            onClick={(e) => {
              e.stopPropagation()
            }}
          >
            {app.installed ? (
              <>
                <Check className="mr-1 h-3 w-3" /> INSTALLED
              </>
            ) : (
              <>
                <Download className="mr-1 h-3 w-3" /> INSTALL
              </>
            )}
          </Button>
        </div>
      </div>
    </button>
  )
}
