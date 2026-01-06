# Stars (AppVault) - Project Context

## Project Overview
**Stars** (also referred to as **AppVault**) is an open-source, cross-platform desktop application store and catalog. It provides a centralized interface for discovering, installing, and managing desktop applications that are hosted on GitHub.

### Mission Statement
To democratize desktop software distribution by providing a beautiful, unified interface for open-source applications, bypassing traditional closed app stores while maintaining a high-quality user experience and automated installation processes across Windows, macOS, and Linux.

---

## Core Approaches & Philosophy

### 1. GitHub as the Source of Truth
Instead of hosting its own binaries, Stars leverages the existing GitHub ecosystem. It uses the GitHub Release API to:
- Fetch metadata (versions, release notes, star counts).
- Discover platform-specific assets (DMGs, EXEs, DEBs, etc.).
- Extract screenshots and documentation directly from project READMEs.
- Stream downloads directly from GitHub's infrastructure.

### 2. Native Cross-Platform Installation
Stars aims to provide a "one-click" installation experience for all major operating systems.
- **macOS:** Handles DMG mounting, `.app` bundle extraction, and moving to `/Applications`.
- **Windows:** Supports executing MSI and EXE installers silently, as well as portable executables.
- **Linux:** Handles DEB/RPM packages and `.tar.gz`/`.zip` archives with auto-detection of executable bundles.

### 3. Decoupled, Hybrid Architecture
The project is structured as a monorepo with a clear separation of concerns:
- **High-Performance Backend:** A Rust service (Axum) handles heavy lifting like GitHub API communication, CSV catalog processing (via Polars), and asset streaming.
- **Native Client:** A JavaFX-based frontend provides a smooth, cross-platform UI that feels like a native desktop application.
- **Design-First Prototyping:** A parallel Next.js mockup serves as the visual and UX reference for the JavaFX implementation.

### 4. Data-Driven Catalog
The app catalog is currently managed via a CSV-based database (`desktop_apps_export.csv`), allowing for easy updates and community contributions without the overhead of a complex database cluster during early development.

---

## Technical Stack

### Backend (`/backend`)
- **Language:** Rust (2021 edition)
- **Web Framework:** Axum (Tokio-based)
- **Data Processing:** Polars (High-performance CSV/DataFrame handling)
- **HTTP Client:** Reqwest (with streaming support)
- **Observability:** Tracing & Tracing-Subscriber

### Primary Frontend (`/frontend/web-mockup/v0-javafx-port`)
- **Language:** Java 17+ / JavaFX 21
- **Icons:** Ikonli (Feather icons)
- **Architecture:** MVVM-like with custom View/Controller logic
- **Communication:** Async `CompletableFuture` based API calls to the Rust backend

### Reference/Mockup Frontend (`/frontend/web-mockup/v0-desktop-appstore-mockup`)
- **Tech:** Next.js, React, Tailwind CSS (Used for UI/UX prototyping)

---

## Project Structure

```text
.
├── backend/                # Rust Axum service
│   ├── src/                # Backend logic (GitHub integration, API routes)
│   └── statui.toml         # Backend monitoring configuration
├── frontend/
│   ├── client/             # Legacy standalone JavaFX client (standalone)
│   └── web-mockup/
│       ├── v0-javafx-port/ # PRIMARY JavaFX client (API-connected)
│       └── v0-...-mockup/  # Next.js design prototype
├── desktop_apps_export.csv # The application database
├── justfile                # Command runner for common tasks
├── SCRATCHPAD.md           # Active development notes and logs
└── TODO.md                 # Current task list
```

---

## Development Priorities & Patterns

### Current Focus
- **Library Persistence:** Transitioning from in-memory state to local JSON/database persistence for installed apps.
- **Installation Robustness:** Improving error handling for varied GitHub release structures (e.g., missing assets, unusual naming).
- **Caching:** Implementing backend caching for GitHub metadata and README-extracted assets to respect rate limits.

### Conventions
- **Command Runner:** Use `just` for most operations (`just backend`, `just frontend`).
- **Logs:** Backend uses compact tracing; Frontend logs to console via standard Java logging.
- **Environment:** Requires `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` for GitHub API access (if rate limits are an issue).

---

## Related Documentation
- `float_project_state.txt`: Detailed technical status and component breakdown.
- `cross-platform-installation-procedures.md`: Detailed research on platform-specific installers.
- `SCRATCHPAD.md`: History of recent fixes and identified problems.
