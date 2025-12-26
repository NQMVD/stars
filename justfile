# Backend commands
backend:
    cd backend && cargo run -r

backend-debug:
    cd backend && RUST_LOG=debug RUST_LOG_DETAIL=source cargo run -r

serve-backend:
    cd backend && cargo build --release
    pueue add \
        --group SERVICES \
        -w {{ justfile_directory() }}/backend \
        {{ justfile_directory() }}/backend/target/release/backend-service
    sleep 1
    pueue status

# Desktop client commands (PRIMARY CLIENT)
desktop-local:
    cd desktop && STARS_API_URL="http://localhost:4444" mvn javafx:run

desktop:
    cd desktop && mvn javafx:run

# Simple client (mockup, for reference)
desktop-mockup:
    cd desktop-mockup && mvn javafx:run

# Web prototype commands
web-dev:
    cd web-mockup && pnpm run dev

# Legacy compatibility (deprecated)
frontend:
    @echo "Warning: 'frontend' is deprecated. Use 'desktop' (primary) or 'desktop-mockup' instead."

gitstatus:
    git-statuses
    git-statuses web-mockup
