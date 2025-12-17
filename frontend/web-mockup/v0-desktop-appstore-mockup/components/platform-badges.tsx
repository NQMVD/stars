import { cn } from "@/lib/utils"
import { Monitor, Apple, Terminal } from "lucide-react"

interface PlatformBadgesProps {
  platforms: ("windows" | "macos" | "linux")[]
  size?: "sm" | "md"
}

const platformConfig = {
  windows: { icon: Monitor, label: "Windows", color: "text-blue-400" },
  macos: { icon: Apple, label: "macOS", color: "text-gray-400" },
  linux: { icon: Terminal, label: "Linux", color: "text-orange-400" },
}

export function PlatformBadges({ platforms, size = "md" }: PlatformBadgesProps) {
  return (
    <div className="flex items-center gap-1.5">
      {platforms.map((platform) => {
        const config = platformConfig[platform]
        const Icon = config.icon
        return (
          <div
            key={platform}
            className={cn(
              "flex items-center justify-center rounded-md bg-secondary",
              size === "sm" ? "h-5 w-5" : "h-6 w-6",
            )}
            title={config.label}
          >
            <Icon className={cn(config.color, size === "sm" ? "h-3 w-3" : "h-3.5 w-3.5")} />
          </div>
        )
      })}
    </div>
  )
}
