#!/bin/bash
set -e

echo "=== Building all Docker images ==="
cd "$(dirname "$0")/.."
docker compose build
echo "=== Build complete ==="
