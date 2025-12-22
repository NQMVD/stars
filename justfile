backend:
    cd backend && cargo run -r

backend-debug:
    cd backend && RUST_LOG=debug RUST_LOG_DETAIL=source cargo run -r

serve-backend:
    cd backend && cargo build --release
    pueue add \
        --group SERVICES \
        -w /root/repos/stars/backend/ \
        /root/repos/stars/backend/target/release/backend-service
    sleep 1
    pueue status

frontend-local:
    cd frontend/web-mockup/v0-javafx-port && \
        STARS_API_URL="http://localhost:4444" mvn javafx:run -Dapi.baseUrl=http://localhost:4444 -Dexec.args="--localhost"

frontend:
    cd frontend/web-mockup/v0-javafx-port && \
        mvn javafx:run

web-mockup:
    cd frontend/web-mockup/v0-desktop-appstore-mockup && pnpm run dev

decoup-frontend:
    cd frontend/client && mvn javafx:run

gitstatus:
    git-statuses
    git-statuses frontend/web-mockup/v0-desktop-appstore-mockup
