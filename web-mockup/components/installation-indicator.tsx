"use client"

import { useState, useEffect } from "react"
import { Download, ArrowRightLeft, Package, CheckCircle2, Pause, Play, X } from "lucide-react"
import { cn } from "@/lib/utils"

type InstallStep = 1 | 2 | 3 | 4
type InstallStatus = "processing" | "paused" | "completed" | "error"

interface InstallationIndicatorProps {
  appName: string
  currentStep: InstallStep
  status: InstallStatus
}

const steps = [
  { id: 1, label: "Downloading", description: "Backend downloads assets", icon: Download },
  { id: 2, label: "Transferring", description: "Transfer assets to client", icon: ArrowRightLeft },
  { id: 3, label: "Installing", description: "Client installs assets", icon: Package },
  { id: 4, label: "Verifying", description: "Client verifies installation", icon: CheckCircle2 },
]

export function InstallationIndicator({
  appName,
  currentStep: initialStep,
  status: initialStatus,
}: InstallationIndicatorProps) {
  const [currentStep, setCurrentStep] = useState<InstallStep>(initialStep)
  const [status, setStatus] = useState<InstallStatus>(initialStatus)
  const [isPaused, setIsPaused] = useState(false)

  // Demo: Auto-advance steps for preview
  useEffect(() => {
    if (status !== "processing" || isPaused) return

    const interval = setInterval(() => {
      setCurrentStep((prev) => {
        if (prev >= 4) {
          setStatus("completed")
          return 4
        }
        return (prev + 1) as InstallStep
      })
    }, 3000)

    return () => clearInterval(interval)
  }, [status, isPaused])

  const currentStepData = steps.find((s) => s.id === currentStep) || steps[0]
  const StepIcon = currentStepData.icon

  const getStatusText = () => {
    if (status === "completed") return "COMPLETED"
    if (status === "error") return "ERROR"
    if (isPaused) return "PAUSED"
    return currentStepData.label
  }

  const handlePauseResume = () => {
    setIsPaused(!isPaused)
  }

  const handleCancel = () => {
    setStatus("processing")
    setCurrentStep(1)
    setIsPaused(false)
  }

  return (
    <div className="rounded-lg border border-border bg-card p-3">
      <div className="flex items-center gap-2.5">
        {/* Icon */}
        <div
          className={cn(
            "flex h-8 w-8 shrink-0 items-center justify-center rounded-full",
            status === "completed"
              ? "bg-emerald-500/20 text-emerald-400"
              : status === "error"
                ? "bg-red-500/20 text-red-400"
                : "bg-blue-500/20 text-blue-400",
          )}
        >
          <StepIcon className="h-4 w-4" />
        </div>

        {/* Status Text */}
        <div className="flex-1 min-w-0">
          <p className="text-xs font-medium text-foreground truncate">{appName}</p>
          <p
            className={cn(
              "text-[11px] font-medium",
              status === "completed"
                ? "text-emerald-400"
                : status === "error"
                  ? "text-red-400"
                  : isPaused
                    ? "text-amber-400"
                    : "text-muted-foreground",
            )}
          >
            {getStatusText()}
          </p>
        </div>

        {/* Control Button */}
        {status !== "completed" && (
          <button
            onClick={handlePauseResume}
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-secondary hover:bg-secondary/80 transition-colors"
          >
            {isPaused ? (
              <Play className="h-3.5 w-3.5 text-foreground" />
            ) : (
              <Pause className="h-3.5 w-3.5 text-foreground" />
            )}
          </button>
        )}

        {status === "completed" && (
          <button
            onClick={handleCancel}
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-secondary hover:bg-secondary/80 transition-colors"
          >
            <X className="h-3.5 w-3.5 text-foreground" />
          </button>
        )}
      </div>

      {/* Step Progress Indicator */}
      <div className="mt-2 flex items-center gap-1.5">
        {steps.map((step) => (
          <div
            key={step.id}
            className={cn(
              "h-1.5 flex-1 rounded-full transition-all duration-300",
              step.id < currentStep
                ? "bg-blue-500"
                : step.id === currentStep
                  ? status === "completed"
                    ? "bg-emerald-500"
                    : status === "error"
                      ? "bg-red-500"
                      : isPaused
                        ? "bg-amber-500"
                        : "bg-blue-500"
                  : "bg-secondary",
            )}
          />
        ))}
      </div>
    </div>
  )
}
