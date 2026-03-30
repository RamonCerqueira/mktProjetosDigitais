#!/usr/bin/env bash
set -euo pipefail

echo "[build] frontend fullstack"
(cd frontend && npm install && npm run build)
