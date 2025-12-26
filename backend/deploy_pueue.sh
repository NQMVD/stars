#!/bin/bash
set -e

# Build the project
echo "Building backend-service..."
cargo build --release

# Get the absolute path to the binary
BINARY_PATH="$(pwd)/target/release/backend-service"
WORKING_DIR="$(pwd)"

# Add to pueue
echo "Adding to pueue..."
pueue add --label "desktop-appstore-backend" --working-directory "$WORKING_DIR" "$BINARY_PATH"

echo "Deployment task added to pueue!"
