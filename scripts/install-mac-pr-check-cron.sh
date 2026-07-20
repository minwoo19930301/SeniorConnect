#!/usr/bin/env bash
# Install a macOS user crontab entry that runs the Grok PR checker every hour
# for 2 days, then self-removes (via the script's expiry check).
#
# Does NOT use Grok's built-in scheduler — only Mac crontab + local grok CLI.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STATE_DIR="${SENIORCONNECT_CRON_STATE:-$HOME/.grok/cron}"
LOG_DIR="${SENIORCONNECT_PR_LOG_DIR:-$HOME/.grok/logs/seniorconnect-pr-check}"
INSTALLED_SCRIPT="${STATE_DIR}/seniorconnect-pr-check.sh"
EXPIRES_FILE="${STATE_DIR}/seniorconnect-pr-check.expires"
MARKER="# seniorconnect-pr-check"
DURATION_DAYS="${DURATION_DAYS:-2}"
REPO_DIR="${SENIORCONNECT_DIR:-$ROOT}"
GROK_BIN="${GROK_BIN:-$HOME/.grok/bin/grok}"

mkdir -p "$STATE_DIR" "$LOG_DIR"

if [[ ! -f "$ROOT/scripts/pr-check-with-grok.sh" ]]; then
  echo "error: missing $ROOT/scripts/pr-check-with-grok.sh" >&2
  exit 1
fi

if [[ ! -x "$GROK_BIN" ]]; then
  echo "error: grok not executable at $GROK_BIN" >&2
  echo "Install/login to Grok first so headless auth works under cron." >&2
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "error: gh CLI required" >&2
  exit 1
fi

# Copy a stable path for cron (survives branch switches/worktrees).
cp "$ROOT/scripts/pr-check-with-grok.sh" "$INSTALLED_SCRIPT"
chmod +x "$INSTALLED_SCRIPT"
# Keep the source script executable in the repo too.
chmod +x "$ROOT/scripts/pr-check-with-grok.sh" \
  "$ROOT/scripts/install-mac-pr-check-cron.sh" \
  "$ROOT/scripts/uninstall-mac-pr-check-cron.sh" \
  "$ROOT/scripts/install-dev-hooks.sh" \
  "$ROOT/.githooks/pre-push" 2>/dev/null || true

expires="$(( $(date +%s) + DURATION_DAYS * 24 * 3600 ))"
printf '%s\n' "$expires" >"$EXPIRES_FILE"
expires_human="$(date -r "$expires" '+%Y-%m-%d %H:%M:%S %Z' 2>/dev/null || date -d "@$expires" 2>/dev/null || echo "$expires")"

# Hourly at minute 7 to avoid thundering herds on the hour.
CRON_LINE="7 * * * * SENIORCONNECT_DIR=\"${REPO_DIR}\" GROK_BIN=\"${GROK_BIN}\" /bin/bash \"${INSTALLED_SCRIPT}\" >> \"${LOG_DIR}/cron-stdout.log\" 2>&1 ${MARKER}"

existing="$(crontab -l 2>/dev/null || true)"
# Drop any previous marker lines, then append.
filtered="$(printf '%s\n' "$existing" | grep -Fv "$MARKER" || true)"
{
  printf '%s\n' "$filtered"
  # Ensure trailing newline before append when filtered empty
  [[ -n "$filtered" && "$filtered" != *$'\n' ]] || true
  printf '%s\n' "$CRON_LINE"
} | sed '/^$/N;/^\n$/D' | crontab -

echo "Installed Mac crontab PR checker (Grok CLI, not Grok scheduler)."
echo "  Script:   $INSTALLED_SCRIPT"
echo "  Repo dir: $REPO_DIR"
echo "  Grok:     $GROK_BIN"
echo "  Schedule: every hour at minute 7"
echo "  Expires:  $expires_human (after ~${DURATION_DAYS} days self-removes)"
echo "  Logs:     $LOG_DIR/"
echo "  Summary:  $LOG_DIR/latest-summary.md"
echo ""
echo "Current crontab:"
crontab -l
echo ""
echo "Run once now (optional smoke test):"
echo "  SENIORCONNECT_DIR=\"$REPO_DIR\" /bin/bash \"$INSTALLED_SCRIPT\""
echo "Uninstall early:"
echo "  $ROOT/scripts/uninstall-mac-pr-check-cron.sh"
