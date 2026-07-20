#!/usr/bin/env bash
# Hourly PR checker for SeniorConnect, intended to run from macOS crontab.
# Uses local Grok CLI (headless), NOT Grok's built-in scheduler.
#
# Install (2 days, every hour, then self-remove):
#   ./scripts/install-mac-pr-check-cron.sh
#
# Uninstall early:
#   ./scripts/uninstall-mac-pr-check-cron.sh

set -euo pipefail

REPO="${SENIORCONNECT_REPO:-minwoo19930301/SeniorConnect}"
REPO_DIR="${SENIORCONNECT_DIR:-$HOME/SeniorConnect}"
GROK_BIN="${GROK_BIN:-$HOME/.grok/bin/grok}"
STATE_DIR="${SENIORCONNECT_CRON_STATE:-$HOME/.grok/cron}"
EXPIRES_FILE="${STATE_DIR}/seniorconnect-pr-check.expires"
LOG_DIR="${SENIORCONNECT_PR_LOG_DIR:-$HOME/.grok/logs/seniorconnect-pr-check}"
MARKER="# seniorconnect-pr-check"

mkdir -p "$LOG_DIR" "$STATE_DIR"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
LOG_FILE="${LOG_DIR}/${STAMP}.log"
SUMMARY_FILE="${LOG_DIR}/latest-summary.md"

log() {
  printf '%s %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$*" | tee -a "$LOG_FILE"
}

remove_cron_entry() {
  if ! command -v crontab >/dev/null 2>&1; then
    return 0
  fi
  local current
  current="$(crontab -l 2>/dev/null || true)"
  if [[ -z "$current" ]]; then
    return 0
  fi
  if printf '%s\n' "$current" | grep -Fq "$MARKER"; then
    printf '%s\n' "$current" | grep -Fv "$MARKER" | crontab - || true
    log "Removed crontab entry ($MARKER)."
  fi
  rm -f "$EXPIRES_FILE" "$STATE_DIR/seniorconnect-pr-check.sh" 2>/dev/null || true
}

# Expire after the install window (default 2 days).
if [[ -f "$EXPIRES_FILE" ]]; then
  expires="$(tr -d '[:space:]' <"$EXPIRES_FILE" || true)"
  now="$(date +%s)"
  if [[ -n "$expires" && "$now" -ge "$expires" ]]; then
    log "PR check window expired (expires=$expires). Uninstalling cron entry."
    remove_cron_entry
    exit 0
  fi
fi

export PATH="/usr/local/bin:/opt/homebrew/bin:$HOME/.grok/bin:$PATH"
export GH_HOST="${GH_HOST:-github.com}"

if [[ ! -x "$GROK_BIN" ]]; then
  log "ERROR: grok binary not found/executable at $GROK_BIN"
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  log "ERROR: gh CLI not found in PATH"
  exit 1
fi

if [[ ! -d "$REPO_DIR/.git" ]]; then
  log "ERROR: repo dir missing: $REPO_DIR"
  exit 1
fi

cd "$REPO_DIR"

log "Starting PR check for $REPO"

PR_JSON="$(gh pr list --repo "$REPO" --state open --json number,title,author,url,headRefName,isDraft,reviewDecision,statusCheckRollup,updatedAt 2>&1)" || {
  log "ERROR: gh pr list failed: $PR_JSON"
  exit 1
}

PR_COUNT="$(printf '%s' "$PR_JSON" | python3 -c 'import json,sys; print(len(json.load(sys.stdin)))' 2>/dev/null || echo 0)"
log "Open PR count: $PR_COUNT"
printf '%s\n' "$PR_JSON" >"${LOG_DIR}/${STAMP}.prs.json"

if [[ "$PR_COUNT" == "0" ]]; then
  {
    echo "# SeniorConnect PR check — ${STAMP}"
    echo
    echo "No open pull requests on \`$REPO\`."
  } >"$SUMMARY_FILE"
  log "No open PRs. Wrote $SUMMARY_FILE"
  exit 0
fi

PROMPT_FILE="$(mktemp)"
trap 'rm -f "$PROMPT_FILE"' EXIT

cat >"$PROMPT_FILE" <<EOF
You are an hourly PR checker for the GitHub repo ${REPO}.

Working directory: ${REPO_DIR}
Open PR JSON from \`gh pr list\` is below. Use gh (read-only by default) if you need more detail on a single PR (checks, files, review state).

Goals for this run:
1. Summarize every open PR in plain language (author, branch, draft?, reviewDecision, CI/check health).
2. Flag anything that needs human attention: failing checks, merge conflicts, stale PRs (>24h without update), missing reviews, or policy risks (direct main work, real PII, claiming unimplemented features).
3. Suggest the single next action for each PR (e.g. "wait for CI", "request review from X", "rebase", "fix validate job").
4. Do NOT merge, push, force-push, or approve PRs.
5. Do NOT post GitHub comments/reviews unless a PR has clearly failing CI or a merge-blocking problem AND no similar bot note was posted in the last 6 hours. Prefer writing the report only.
6. Keep the final answer under ~40 lines, markdown, with a short top summary then a bullet per PR.

Open PRs JSON:
${PR_JSON}
EOF

# Bounded, non-interactive Grok run. Prefer tools needed for PR inspection.
set +e
"$GROK_BIN" -p "$(cat "$PROMPT_FILE")" \
  --cwd "$REPO_DIR" \
  --yolo \
  --no-auto-update \
  --max-turns 12 \
  --tools "run_terminal_cmd,read_file,grep,list_dir" \
  --output-format plain \
  >"${LOG_DIR}/${STAMP}.grok.out" \
  2>"${LOG_DIR}/${STAMP}.grok.err"
status=$?
set -e

if [[ "$status" -ne 0 ]]; then
  log "ERROR: grok exited $status (see ${STAMP}.grok.err)"
  {
    echo "# SeniorConnect PR check — ${STAMP}"
    echo
    echo "Grok run failed (exit $status). See \`${STAMP}.grok.err\`."
    echo
    echo "## Raw open PRs"
    echo '```json'
    echo "$PR_JSON"
    echo '```'
  } >"$SUMMARY_FILE"
  exit "$status"
fi

{
  echo "# SeniorConnect PR check — ${STAMP}"
  echo
  cat "${LOG_DIR}/${STAMP}.grok.out"
} >"$SUMMARY_FILE"

log "Wrote summary: $SUMMARY_FILE"
exit 0
