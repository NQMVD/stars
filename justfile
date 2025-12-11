
backend:
    cd backend && cargo run -r

frontend:
    cd frontend/web-mockup/v0-javafx-port && mvn javafx:run

decoup-frontend:
    cd frontend/client && mvn javafx:run
    
