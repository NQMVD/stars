#!/bin/bash

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | xargs)
fi

if [ -z "$DATABASE_URL" ]; then
    echo "DATABASE_URL is not set in .env"
    exit 1
fi

# Extract host and port from DATABASE_URL
# Format: postgres://user:pass@host:port/db
HOST=$(echo $DATABASE_URL | sed -E 's/.*@([^:/]+).*/\1/')
PORT=$(echo $DATABASE_URL | sed -E 's/.*:([0-9]+)\/.*/\1/')

if [ -z "$PORT" ]; then
    PORT=5432
fi

echo "Debugging connection to $HOST:$PORT..."

# Check DNS resolution
echo "1. Checking DNS resolution..."
host $HOST || nslookup $HOST

# Check TCP connectivity
echo -e "\n2. Checking TCP connectivity (nc)..."
if command -v nc >/dev/null 2>&1; then
    echo "Trying default (likely IPv6 if available)..."
    nc -zv -w 5 $HOST $PORT
    
    echo "Trying IPv4 explicitly..."
    nc -4 -zv -w 5 $HOST $PORT
else
    echo "nc (netcat) not found, skipping TCP check."
fi

echo -e "\n2b. Checking Pooler Port (6543)..."
if command -v nc >/dev/null 2>&1; then
    nc -zv -w 5 $HOST 6543
fi

# Check with psql if available
echo -e "\n3. Checking with psql (if available)..."
if command -v psql >/dev/null 2>&1; then
    psql "$DATABASE_URL" -c "SELECT 1"
else
    echo "psql not found, skipping psql check."
fi
