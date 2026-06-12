#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

stop_matching() {
  local pattern="$1"
  local pids
  pids="$(pgrep -f "$pattern" || true)"

  if [[ -n "$pids" ]]; then
    echo "$pids" | xargs -r kill
  fi
}

stop_matching "$ROOT_DIR/backend/target/classes.*cn.edu.whut.sept.zuul.ZuulApplication"
stop_matching "node $ROOT_DIR/frontend/node_modules/.bin/vue-cli-service serve"

sleep 1

echo "Stopped dev servers for $ROOT_DIR"
