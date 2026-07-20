#!/usr/bin/env bash
# Remove the SeniorConnect hourly Grok PR-check crontab entry early.
set -euo pipefail

STATE_DIR="${SENIORCONNECT_CRON_STATE:-$HOME/.grok/cron}"
MARKER="# seniorconnect-pr-check"

existing="$(crontab -l 2>/dev/null || true)"
if [[ -z "$existing" ]]; then
  echo "No crontab for this user."
else
  printf '%s\n' "$existing" | grep -Fv "$MARKER" | crontab - || true
  echo "Removed any crontab lines containing: $MARKER"
fi

rm -f \
  "$STATE_DIR/seniorconnect-pr-check.expires" \
  "$STATE_DIR/seniorconnect-pr-check.sh" 2>/dev/null || true

echo "Current crontab:"
crontab -l 2>/dev/null || echo "(empty)"
