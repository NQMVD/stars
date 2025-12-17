
backend:
    cd backend && cargo run -r

frontend:
    cd frontend/web-mockup/v0-javafx-port && \
        mvn javafx:run \
        --enable-native-access=javafx.web \
        --enable-native-access=javafx.graphics \
        2>&1 | tee client.log

web-mockup:
    cd frontend/web-mockup/v0-desktop-appstore-mockup && pnpm run dev

decoup-frontend:
    cd frontend/client && mvn javafx:run

gitstatus:
    git-statuses
    git-statuses frontend/web-mockup/v0-desktop-appstore-mockup
