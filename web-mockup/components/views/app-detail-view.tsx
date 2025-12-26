"use client"

import type { AppData } from "@/app/page"
import { Button } from "@/components/ui/button"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"
import { PlatformBadges } from "@/components/platform-badges"
import {
  ArrowLeft,
  Star,
  Download,
  ExternalLink,
  Shield,
  Check,
  ChevronLeft,
  ChevronRight,
  Heart,
  Share2,
  Flag,
  Loader2,
} from "lucide-react"
import { useState, useEffect } from "react"
import { isAppInstalled, installApp, removeApp, getInstalledApp } from "@/lib/library-store"

interface AppDetailViewProps {
  app: AppData
  onBack: () => void
}

export function AppDetailView({ app, onBack }: AppDetailViewProps) {
  const [currentScreenshot, setCurrentScreenshot] = useState(0)
  const [installed, setInstalled] = useState(false)
  const [installing, setInstalling] = useState(false)
  const [installProgress, setInstallProgress] = useState(0)
  const [installStatus, setInstallStatus] = useState("")
  const [installedVersion, setInstalledVersion] = useState("")

  // Check install status on mount and when app changes
  useEffect(() => {
    const isInstalled = isAppInstalled(app.id)
    setInstalled(isInstalled)
    if (isInstalled) {
      const installedApp = getInstalledApp(app.id)
      if (installedApp) {
        setInstalledVersion(installedApp.installedVersion)
      }
    }
  }, [app.id])

  const handleInstall = async () => {
    setInstalling(true)
    setInstallProgress(0)
    await installApp(app, (progress, status) => {
      setInstallProgress(progress)
      setInstallStatus(status)
    })
    setInstalled(true)
    setInstalledVersion(app.version)
    setInstalling(false)
    setInstallStatus("")
  }

  const handleUninstall = () => {
    removeApp(app.id)
    setInstalled(false)
    setInstalledVersion("")
  }

  return (
    <div className="p-8 max-w-6xl mx-auto space-y-8">
      {/* Back Button */}
      <Button variant="ghost" size="sm" onClick={onBack} className="uppercase font-bold tracking-tight pl-0 hover:pl-2 transition-all">
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back to Discover
      </Button>

      {/* Header */}
      <div className="flex gap-8 items-start">
        <img src={app.icon || "/placeholder.svg"} alt={app.name} className="h-32 w-32 border-2 border-border" />
        <div className="flex-1">
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-4xl font-black uppercase tracking-tighter leading-none">{app.name}</h1>
              <p className="text-xl text-muted-foreground font-medium uppercase tracking-wide mt-1">{app.developer}</p>
            </div>
            <div className="flex gap-2">
              <Button variant="outline" size="icon" className="border-2">
                <Heart className="h-4 w-4" />
              </Button>
              <Button variant="outline" size="icon" className="border-2">
                <Share2 className="h-4 w-4" />
              </Button>
            </div>
          </div>
          <div className="flex items-center gap-6 mt-6 border-y-2 border-border py-4">
            <div className="flex items-center gap-2">
              <Star className="h-5 w-5 fill-primary text-primary" />
              <span className="font-bold text-lg">{app.rating}</span>
              <span className="text-sm font-medium text-muted-foreground uppercase">({app.reviews.toLocaleString()} REVIEWS)</span>
            </div>
            <div className="h-6 w-px bg-border" />
            <span className="text-sm font-bold uppercase">{app.downloads} DOWNLOADS</span>
            <div className="h-6 w-px bg-border" />
            <PlatformBadges platforms={app.platforms} />
          </div>
          <div className="flex items-center gap-4 mt-6">
            {installing ? (
              <div className="flex flex-col gap-2 min-w-64">
                <div className="flex items-center gap-2">
                  <Loader2 className="h-4 w-4 animate-spin text-primary" />
                  <span className="text-sm font-bold uppercase tracking-wide">{installStatus}</span>
                </div>
                <div className="h-4 border-2 border-border bg-secondary overflow-hidden">
                  <div
                    className="h-full bg-primary transition-all duration-200"
                    style={{ width: `${installProgress}%` }}
                  />
                </div>
              </div>
            ) : installed ? (
              <>
                <Button size="lg" className="min-w-40 bg-primary text-primary-foreground font-bold uppercase tracking-wider hover:bg-primary/90">
                  <Check className="mr-2 h-5 w-5" /> Open
                </Button>
                <Button variant="outline" size="lg" onClick={handleUninstall} className="uppercase font-bold tracking-wider border-2 hover:bg-destructive hover:text-destructive-foreground hover:border-destructive">
                  Uninstall
                </Button>
                {installedVersion && (
                  <span className="text-sm font-mono text-muted-foreground">v{installedVersion}</span>
                )}
              </>
            ) : (
              <Button size="lg" className="min-w-40 h-12 text-base font-bold uppercase tracking-wider bg-primary text-primary-foreground hover:bg-primary/90" onClick={handleInstall}>
                <Download className="mr-2 h-5 w-5" />
                {app.price === "Free" ? "Install" : `Buy ${app.price}`}
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Screenshots Carousel */}
      <div className="relative group">
        <div className="overflow-hidden border-2 border-border bg-secondary">
          <div
            className="flex transition-transform duration-300"
            style={{ transform: `translateX(-${currentScreenshot * 100}%)` }}
          >
            {app.screenshots.map((screenshot, index) => (
              <div key={index} className="w-full flex-shrink-0">
                <img
                  src={screenshot || "/placeholder.svg"}
                  alt={`${app.name} screenshot ${index + 1}`}
                  className="w-full aspect-video object-cover"
                />
              </div>
            ))}
          </div>
        </div>
        {app.screenshots.length > 1 && (
          <>
            <Button
              variant="default"
              size="icon"
              className="absolute left-0 top-1/2 -translate-y-1/2 rounded-none h-12 w-12 opacity-0 group-hover:opacity-100 transition-opacity"
              onClick={() => setCurrentScreenshot((prev) => (prev === 0 ? app.screenshots.length - 1 : prev - 1))}
            >
              <ChevronLeft className="h-6 w-6" />
            </Button>
            <Button
              variant="default"
              size="icon"
              className="absolute right-0 top-1/2 -translate-y-1/2 rounded-none h-12 w-12 opacity-0 group-hover:opacity-100 transition-opacity"
              onClick={() => setCurrentScreenshot((prev) => (prev === app.screenshots.length - 1 ? 0 : prev + 1))}
            >
              <ChevronRight className="h-6 w-6" />
            </Button>
            <div className="flex gap-0 border-t-2 border-border mt-4 overflow-x-auto pb-2">
              {app.screenshots.map((screenshot, index) => (
                <button
                  key={index}
                  onClick={() => setCurrentScreenshot(index)}
                  className={`relative h-20 w-36 flex-shrink-0 grayscale hover:grayscale-0 transition-all border-r-2 border-border ${index === currentScreenshot ? "grayscale-0 border-b-4 border-b-primary" : "opacity-50"
                    }`}
                >
                  <img src={screenshot || "/placeholder.svg"} className="h-full w-full object-cover" alt="" />
                </button>
              ))}
            </div>
          </>
        )}
      </div>

      {/* Tabs */}
      <Tabs defaultValue="overview" className="w-full">
        <TabsList className="w-full justify-start rounded-none bg-transparent border-b-2 border-border p-0 h-auto gap-8">
          {["Overview", "Reviews", "Changelog", "Permissions"].map((tab) => (
            <TabsTrigger
              key={tab}
              value={tab.toLowerCase()}
              className="rounded-none border-b-4 border-transparent px-0 py-3 font-bold uppercase tracking-wider text-muted-foreground data-[state=active]:border-primary data-[state=active]:text-foreground data-[state=active]:bg-transparent shadow-none"
            >
              {tab}
            </TabsTrigger>
          ))}
        </TabsList>

        <TabsContent value="overview" className="mt-8 space-y-8">
          {/* Description */}
          <section>
            <h2 className="text-xl font-bold uppercase tracking-tight mb-4 flex items-center gap-2">
              <div className="w-2 h-6 bg-primary" /> About this app
            </h2>
            <p className="text-lg leading-relaxed max-w-4xl">{app.description}</p>
          </section>

          {/* Info Grid */}
          <section className="grid grid-cols-2 md:grid-cols-4 border-2 border-border">
            <InfoCard label="Version" value={app.version} />
            <InfoCard label="Size" value={app.size} />
            <InfoCard label="Category" value={app.category} />
            <InfoCard label="Updated" value="Dec 1, 2025" />
          </section>

          {/* Highlights */}
          <section>
            <h2 className="text-xl font-bold uppercase tracking-tight mb-4 flex items-center gap-2">
              <div className="w-2 h-6 bg-primary" /> Highlights
            </h2>
            <div className="grid gap-4 sm:grid-cols-2">
              {[
                "Cross-platform synchronization",
                "Regular security updates",
                "Offline functionality",
                "Dark mode support",
              ].map((highlight, index) => (
                <div key={index} className="flex items-center gap-3 border-2 border-border p-4 hover:border-primary transition-colors bg-card">
                  <div className="h-6 w-6 flex items-center justify-center bg-primary text-primary-foreground">
                    <Check className="h-4 w-4" />
                  </div>
                  <span className="font-medium uppercase tracking-wide">{highlight}</span>
                </div>
              ))}
            </div>
          </section>

          {/* Developer Info */}
          <section className="flex items-center justify-between border-2 border-border bg-secondary/10 p-6">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center bg-primary text-primary-foreground font-bold text-xl">
                {app.developer[0]}
              </div>
              <div>
                <p className="font-bold text-lg uppercase tracking-tight">{app.developer}</p>
                <p className="text-sm font-medium text-muted-foreground uppercase tracking-wider">Verified Publisher</p>
              </div>
            </div>
            <div className="flex gap-4">
              <Button variant="outline" size="sm" className="border-2 font-bold uppercase">
                <ExternalLink className="mr-2 h-4 w-4" />
                Website
              </Button>
              <Button variant="outline" size="sm" className="border-2 font-bold uppercase">
                <Flag className="mr-2 h-4 w-4" />
                Report
              </Button>
            </div>
          </section>
        </TabsContent>

        <TabsContent value="reviews" className="mt-8 space-y-6">
          {/* Rating Summary */}
          <div className="flex gap-12 border-2 border-border p-8 bg-card">
            <div className="text-center min-w-40">
              <div className="text-6xl font-black tracking-tighter">{app.rating}</div>
              <div className="flex justify-center mt-3 gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Star
                    key={star}
                    className={`h-5 w-5 ${star <= Math.round(app.rating) ? "fill-primary text-primary" : "text-muted-foreground/30"
                      }`}
                  />
                ))}
              </div>
              <p className="text-sm font-bold uppercase tracking-wide text-muted-foreground mt-2">{app.reviews.toLocaleString()} reviews</p>
            </div>
            <div className="flex-1 space-y-3 pt-2">
              {[5, 4, 3, 2, 1].map((stars) => (
                <div key={stars} className="flex items-center gap-4">
                  <span className="text-sm font-bold w-4">{stars}</span>
                  <div className="flex-1 h-4 bg-secondary border border-border">
                    <div
                      className="h-full bg-primary"
                      style={{
                        width: `${stars === 5 ? 65 : stars === 4 ? 20 : stars === 3 ? 10 : 3}%`,
                      }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Reviews List */}
          <div className="space-y-6">
            {[
              {
                author: "Alex M.",
                rating: 5,
                date: "Nov 28, 2025",
                content:
                  "Absolutely love this app! It has completely transformed my workflow. The cross-platform sync is seamless.",
              },
              {
                author: "Sarah K.",
                rating: 4,
                date: "Nov 25, 2025",
                content: "Great app overall. Would love to see more customization options in future updates.",
              },
            ].map((review, index) => (
              <div key={index} className="border-b-2 border-border pb-6 space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 bg-primary text-primary-foreground flex items-center justify-center font-bold">
                      {review.author[0]}
                    </div>
                    <span className="font-bold uppercase tracking-wide">{review.author}</span>
                  </div>
                  <span className="text-sm font-mono text-muted-foreground">{review.date}</span>
                </div>
                <div className="flex gap-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                      key={star}
                      className={`h-4 w-4 ${star <= review.rating ? "fill-primary text-primary" : "text-muted-foreground/30"
                        }`}
                    />
                  ))}
                </div>
                <p className="text-lg leading-relaxed">{review.content}</p>
              </div>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="changelog" className="mt-8 space-y-6">
          {[
            {
              version: app.version,
              date: "Dec 1, 2025",
              changes: ["Added dark mode support", "Performance improvements", "Bug fixes and stability improvements"],
            },
            {
              version: "1.4.0",
              date: "Nov 15, 2025",
              changes: ["New feature: Cross-platform sync", "Improved user interface", "Fixed crash on startup"],
            },
          ].map((release, index) => (
            <div key={index} className="border-2 border-border p-6 bg-card">
              <div className="flex items-center justify-between mb-6 border-b-2 border-secondary pb-4">
                <span className="text-xl font-bold uppercase">Version {release.version}</span>
                <span className="text-sm font-mono text-muted-foreground">{release.date}</span>
              </div>
              <ul className="space-y-3">
                {release.changes.map((change, changeIndex) => (
                  <li key={changeIndex} className="text-base flex items-start gap-4">
                    <div className="h-1.5 w-1.5 bg-primary mt-2" />
                    {change}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </TabsContent>

        <TabsContent value="permissions" className="mt-8 space-y-6">
          <div className="border-2 border-green-500/20 bg-green-500/5 p-6 flex items-start gap-4">
            <Shield className="h-8 w-8 text-green-600 mt-1" />
            <div>
              <h3 className="font-bold text-lg uppercase tracking-tight text-green-700 dark:text-green-400">Verified Publisher</h3>
              <p className="text-green-800 dark:text-green-200 mt-1">
                This app is from a verified developer and has passed our security checks.
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="font-bold uppercase text-lg">This app may access:</h3>
            <div className="grid gap-4 sm:grid-cols-2">
              {[
                { name: "File System", description: "Read and write files" },
                { name: "Network", description: "Connect to the internet" },
                { name: "Notifications", description: "Show system notifications" },
              ].map((permission, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between border-2 border-border p-4 bg-card"
                >
                  <div>
                    <p className="font-bold uppercase tracking-wide text-sm">{permission.name}</p>
                    <p className="text-xs text-muted-foreground mt-1">{permission.description}</p>
                  </div>
                  <Check className="h-5 w-5 text-primary" />
                </div>
              ))}
            </div>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}

function InfoCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="border-r-2 last:border-r-0 border-border p-4 text-center">
      <p className="text-xs font-bold uppercase tracking-widest text-muted-foreground mb-1">{label}</p>
      <p className="font-bold text-lg">{value}</p>
    </div>
  )
}
