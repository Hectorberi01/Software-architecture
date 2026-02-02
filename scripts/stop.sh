#!/bin/bash
set -e

echo "=== Stopping all services ==="
cd "$(dirname "$0")/.."
docker compose down
echo "=== Services stopped ==="
