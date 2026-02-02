#!/bin/bash
set -e

echo "=== Running backend tests ==="
cd "$(dirname "$0")/.."
docker run --rm \
  -v "$(pwd)/backend:/app" \
  -w /app \
  maven:3.9-eclipse-temurin-21 \
  mvn clean test
echo "=== Tests complete ==="
