# stars / AppVault

A cross-platform desktop application store powered by GitHub releases.

## Structure

- `backend/` - Rust API server (Axum)
- `desktop/` - Primary JavaFX desktop client (Java 21, with API integration)
- `desktop-mockup/` - Simple JavaFX client (for reference)
- `web-mockup/` - Next.js web prototype
- `docs/` - Documentation

## Quick Start

### Backend
```bash
just backend
```

### Desktop Clients
```bash
just desktop-local   # Primary client, local development (localhost:4444)
just desktop         # Primary client, production backend
just desktop-mockup  # Simple client (for reference)
```

### Web Prototype
```bash
just web-dev
```

## Documentation

See `docs/` for detailed documentation.
