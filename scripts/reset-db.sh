#!/bin/bash
set -e

echo "=== Resetting database (destroying all volumes) ==="
cd "$(dirname "$0")/.."
docker compose down -v
echo "=== Volumes removed. Run './scripts/run.sh' to restart with a fresh database ==="
