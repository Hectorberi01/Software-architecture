#!/bin/bash
set -e

echo "=== Starting all services ==="
cd "$(dirname "$0")/.."
docker compose up -d
echo "=== Services started ==="
echo ""
echo "Frontend:   http://localhost:3000"
echo "Backend:    http://localhost:8081/api/v1/spots"
echo "phpMyAdmin: http://localhost:8888"
echo "RabbitMQ:   http://localhost:15672 (guest/guest)"
echo ""
echo "Run './scripts/logs.sh' to view logs"
