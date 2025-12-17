import type React from "react"
import type { Metadata, Viewport } from "next"
import { Analytics } from "@vercel/analytics/next"
import "./globals.css"

export const metadata: Metadata = {
  title: "Desktop App Store - Cross-Platform Apps",
  description: "Install desktop applications on Linux, macOS, and Windows from a single unified store.",
  generator: 'v0.app'
}

export const viewport: Viewport = {
  themeColor: "#ffffff",
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en" className="dark h-full" suppressHydrationWarning>
      <body className="font-sans antialiased h-full overflow-hidden bg-background">
        {children}
        <Analytics />
      </body>
    </html>
  )
}
