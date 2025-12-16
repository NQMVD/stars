# Agent Instructions

## Commands
- **Backend (Rust):** `cd backend && cargo run -r` | `cargo test` | `cargo check`
- **Frontend (JavaFX):** `cd frontend/client && mvn javafx:run` | `mvn test`
- **Web Mockup:** `cd frontend/web-mockup/v0-desktop-appstore-mockup` -> `pnpm dev` | `pnpm build` | `pnpm lint`
- **Shortcuts:** Use `just` (see `justfile` in root)

## Code Style & Conventions
- **Rust:** Use `axum` for API, `anyhow` for main, custom `AppError` enum for handlers. `tracing` for logs.
- **Java:** JavaFX + AtlantaFX (PrimerDark). `mainpackage` namespace. Log4j2. FXML for views.
- **Next.js:** Next 16, React 19, Tailwind, Radix UI. TypeScript. Use `pnpm`.
- **General:**
  - **Path:** Resolve absolute paths from project root.
  - **Verification:** Run project-specific lint/test after changes.
  - **Formatting:** Follow existing patterns (standard `rustfmt`, Java conventions).
