"use client"

import type React from "react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Switch } from "@/components/ui/switch"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Folder, HardDrive, Bell, Download, Shield, Monitor, Globe } from "lucide-react"

export function SettingsView() {
  return (
    <div className="p-8 max-w-4xl mx-auto">
      <h1 className="text-3xl font-black uppercase tracking-tighter mb-8 pb-4 border-b-2 border-border">Settings</h1>

      <div className="space-y-12">
        {/* General */}
        <SettingsSection title="General" icon={Monitor}>
          <SettingsRow label="Launch at startup" description="Automatically start AppVault when you log in">
            <Switch defaultChecked />
          </SettingsRow>
          <SettingsRow label="Minimize to tray" description="Keep running in background when closed">
            <Switch defaultChecked />
          </SettingsRow>
          <SettingsRow label="Language" description="Choose your preferred language">
            <Select defaultValue="en">
              <SelectTrigger className="w-40 uppercase font-bold tracking-tight">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="en" className="uppercase font-bold tracking-tight">English</SelectItem>
                <SelectItem value="es" className="uppercase font-bold tracking-tight">Español</SelectItem>
                <SelectItem value="fr" className="uppercase font-bold tracking-tight">Français</SelectItem>
                <SelectItem value="de" className="uppercase font-bold tracking-tight">Deutsch</SelectItem>
                <SelectItem value="ja" className="uppercase font-bold tracking-tight">日本語</SelectItem>
              </SelectContent>
            </Select>
          </SettingsRow>
        </SettingsSection>

        {/* Downloads */}
        <SettingsSection title="Downloads" icon={Download}>
          <SettingsRow label="Download location" description="Where apps are downloaded and installed">
            <div className="flex items-center gap-2">
              <Input value="/Applications" readOnly className="w-64 bg-input font-mono text-sm" />
              <Button variant="outline" size="icon" className="rounded-none">
                <Folder className="h-4 w-4" />
              </Button>
            </div>
          </SettingsRow>
          <SettingsRow label="Simultaneous downloads" description="Maximum number of concurrent downloads">
            <Select defaultValue="3">
              <SelectTrigger className="w-24 uppercase font-bold tracking-tight">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="1">1</SelectItem>
                <SelectItem value="2">2</SelectItem>
                <SelectItem value="3">3</SelectItem>
                <SelectItem value="5">5</SelectItem>
              </SelectContent>
            </Select>
          </SettingsRow>
          <SettingsRow
            label="Pause downloads on metered connection"
            description="Save bandwidth on limited connections"
          >
            <Switch />
          </SettingsRow>
        </SettingsSection>

        {/* Updates */}
        <SettingsSection title="Updates" icon={HardDrive}>
          <SettingsRow label="Automatic updates" description="Automatically update apps in the background">
            <Switch defaultChecked />
          </SettingsRow>
          <SettingsRow label="Check frequency" description="How often to check for updates">
            <Select defaultValue="daily">
              <SelectTrigger className="w-40 uppercase font-bold tracking-tight">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="hourly" className="uppercase font-bold tracking-tight">Hourly</SelectItem>
                <SelectItem value="daily" className="uppercase font-bold tracking-tight">Daily</SelectItem>
                <SelectItem value="weekly" className="uppercase font-bold tracking-tight">Weekly</SelectItem>
                <SelectItem value="manual" className="uppercase font-bold tracking-tight">Manual</SelectItem>
              </SelectContent>
            </Select>
          </SettingsRow>
          <SettingsRow label="Include beta updates" description="Receive early access to new features">
            <Switch />
          </SettingsRow>
        </SettingsSection>

        {/* Notifications */}
        <SettingsSection title="Notifications" icon={Bell}>
          <SettingsRow label="Enable notifications" description="Receive alerts for updates and downloads">
            <Switch defaultChecked />
          </SettingsRow>
          <SettingsRow label="Update notifications" description="Notify when app updates are available">
            <Switch defaultChecked />
          </SettingsRow>
          <SettingsRow label="Download complete notifications" description="Notify when downloads finish">
            <Switch defaultChecked />
          </SettingsRow>
          <SettingsRow label="Sound" description="Play sound for notifications">
            <Switch />
          </SettingsRow>
        </SettingsSection>

        {/* Privacy & Security */}
        <SettingsSection title="Privacy & Security" icon={Shield}>
          <SettingsRow label="Verify app signatures" description="Only install apps with valid signatures">
            <Switch defaultChecked />
          </SettingsRow>
          <SettingsRow label="Send usage statistics" description="Help improve AppVault with anonymous data">
            <Switch />
          </SettingsRow>
          <SettingsRow label="Clear cache" description="Remove temporary files and cached data">
            <Button variant="outline" size="sm" className="uppercase font-bold tracking-tight">
              Clear Cache
            </Button>
          </SettingsRow>
        </SettingsSection>

        {/* Network */}
        <SettingsSection title="Network" icon={Globe}>
          <SettingsRow label="Use proxy" description="Connect through a proxy server">
            <Switch />
          </SettingsRow>
          <SettingsRow label="Bandwidth limit" description="Maximum download speed (0 for unlimited)">
            <div className="flex items-center gap-2">
              <Input type="number" defaultValue="0" className="w-24 bg-input font-bold" />
              <span className="text-sm font-bold uppercase text-muted-foreground">MB/s</span>
            </div>
          </SettingsRow>
        </SettingsSection>

        {/* Footer */}
        <div className="pt-8 flex items-center justify-between text-sm text-muted-foreground border-t-2 border-border mt-8">
          <div>
            <p className="font-bold uppercase tracking-wide">AppVault v1.0.0</p>
            <p className="text-xs uppercase tracking-wide mt-1">© 2025 AppVault. All rights reserved.</p>
          </div>
          <div className="flex gap-6 uppercase font-bold tracking-tight text-xs">
            <button className="hover:text-primary transition-colors">Terms of Service</button>
            <button className="hover:text-primary transition-colors">Privacy Policy</button>
            <button className="hover:text-primary transition-colors">Licenses</button>
          </div>
        </div>
      </div>
    </div>
  )
}

function SettingsSection({
  title,
  icon: Icon,
  children,
}: {
  title: string
  icon: React.ComponentType<{ className?: string }>
  children: React.ReactNode
}) {
  return (
    <section>
      <div className="flex items-center gap-3 mb-6 border-b-2 border-border pb-2 w-fit pr-8">
        <Icon className="h-5 w-5 text-primary" />
        <h2 className="text-lg font-bold uppercase tracking-tight">{title}</h2>
      </div>
      <div className="space-y-4">{children}</div>
    </section>
  )
}

function SettingsRow({
  label,
  description,
  children,
}: {
  label: string
  description: string
  children: React.ReactNode
}) {
  return (
    <div className="flex items-center justify-between border-2 border-border bg-card p-5 hover:border-muted-foreground/50 transition-colors">
      <div>
        <Label className="text-sm font-bold uppercase tracking-wide">{label}</Label>
        <p className="text-xs font-medium text-muted-foreground mt-1 uppercase tracking-wide">{description}</p>
      </div>
      {children}
    </div>
  )
}
