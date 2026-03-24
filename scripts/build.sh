#!/usr/bin/env bash
set -euo pipefail

echo "[build] backend"
(cd backend && mvn -q -DskipTests package)

echo "[build] frontend"
(cd frontend && npm install && npm run build)
