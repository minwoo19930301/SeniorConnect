#!/usr/bin/env bash
# Wire this clone to shared .githooks/ so every developer (including admins)
# cannot push straight to main/master without intentionally overriding.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -d .git ]]; then
  echo "error: run from a git clone of SeniorConnect" >&2
  exit 1
fi

if [[ ! -f .githooks/pre-push ]]; then
  echo "error: missing .githooks/pre-push" >&2
  exit 1
fi

chmod +x .githooks/pre-push
git config core.hooksPath .githooks

echo "Installed git hooks for this clone."
echo "  core.hooksPath = $(git config --get core.hooksPath)"
echo "  pre-push will block direct pushes to: main master"
echo ""
echo "Other developers should run this after clone:"
echo "  ./scripts/install-dev-hooks.sh"
