# PR-only workflow (all contributors, including admins)

Direct pushes to `main` / `master` are not part of the team process. Everyone—including the repo owner—opens a pull request.

## Why

- GitHub branch protection already requires a PR (+ review) on `main` (`enforce_admins` is on).
- Local git hooks fail fast before a rejected push.
- CI (`Validate harness`) runs on pull requests.

## One-time setup after clone (Mac or Windows)

```bash
git clone https://github.com/minwoo19930301/SeniorConnect.git
cd SeniorConnect
./scripts/install-dev-hooks.sh
```

This sets `core.hooksPath=.githooks` for **this clone only** and installs a `pre-push` hook that blocks direct pushes to `main` / `master`.

## Daily flow

```bash
git switch -c your-topic-branch
# ... edit, commit ...
npm test
git push -u origin HEAD
gh pr create --fill
```

After review + green checks, merge via GitHub UI (or `gh pr merge`).

## Emergency override (local only)

```bash
ALLOW_DIRECT_PUSH=1 git push
```

GitHub branch protection may still reject the push. Do not use this for normal work.

## Mac-only: hourly Grok PR checker (2 days)

This uses **macOS crontab + local `grok` CLI**, not Grok’s built-in scheduler.

```bash
# install: every hour for 2 days, then self-removes
./scripts/install-mac-pr-check-cron.sh

# optional smoke test
SENIORCONNECT_DIR="$PWD" /bin/bash ~/.grok/cron/seniorconnect-pr-check.sh

# uninstall early
./scripts/uninstall-mac-pr-check-cron.sh
```

Outputs:

| Path | Purpose |
|------|---------|
| `~/.grok/logs/seniorconnect-pr-check/latest-summary.md` | Latest report |
| `~/.grok/logs/seniorconnect-pr-check/*.log` | Run logs |
| `~/.grok/cron/seniorconnect-pr-check.expires` | Unix time when cron self-removes |

Requirements on the Mac that runs cron: logged-in `gh`, working `grok` auth (`XAI_API_KEY` or prior `grok login`), and a local clone path (default `~/SeniorConnect`).
