#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend.log"

mkdir -p "$LOG_DIR"

"$ROOT_DIR/scripts/stop-dev.sh" >/dev/null 2>&1 || true

echo "Starting backend on http://localhost:8080 ..."
(
  cd "$ROOT_DIR/backend"
  nohup mvn spring-boot:run >"$BACKEND_LOG" 2>&1 &
  echo $! >"$LOG_DIR/backend.pid"
)

for _ in {1..40}; do
  if curl -fsS "http://127.0.0.1:8080/api/game/status" >/dev/null 2>&1; then
    echo "Backend is ready."
    break
  fi
  sleep 1
done

if ! curl -fsS "http://127.0.0.1:8080/api/game/status" >/dev/null 2>&1; then
  echo "Backend failed to start. See $BACKEND_LOG"
  tail -80 "$BACKEND_LOG" || true
  exit 1
fi

echo "Starting frontend on http://localhost:8081 ..."
(
  cd "$ROOT_DIR/frontend"
  nohup npm run serve >"$FRONTEND_LOG" 2>&1 &
  echo $! >"$LOG_DIR/frontend.pid"
)

for _ in {1..40}; do
  if curl -fsS "http://127.0.0.1:8081/" >/dev/null 2>&1; then
    echo "Frontend is ready."
    break
  fi
  sleep 1
done

if ! curl -fsS "http://127.0.0.1:8081/" >/dev/null 2>&1; then
  echo "Frontend failed to start. See $FRONTEND_LOG"
  tail -80 "$FRONTEND_LOG" || true
  exit 1
fi

echo
echo "Dev servers are running:"
echo "  Backend:  http://localhost:8080"
echo "  Frontend: http://localhost:8081"
echo
echo "Logs:"
echo "  $BACKEND_LOG"
echo "  $FRONTEND_LOG"
echo
echo "Stop them with:"
echo "  $ROOT_DIR/scripts/stop-dev.sh"
