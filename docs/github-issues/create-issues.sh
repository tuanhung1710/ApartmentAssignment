#!/usr/bin/env bash
# Create GitHub issues for Vibe feedback (Apartment module)
# Usage:
#   gh auth login
#   bash docs/github-issues/create-issues.sh
# Or:
#   export GH_TOKEN=...
#   bash docs/github-issues/create-issues.sh

set -euo pipefail
REPO="${GITHUB_REPO:-tuanhung1710/ApartmentAssignment}"
DIR="$(cd "$(dirname "$0")" && pwd)"

create_one() {
  local file="$1"
  local title body
  title=$(grep -m1 '^## Title' -A1 "$file" | tail -n1 | sed 's/^[[:space:]]*//')
  # body = file without first Title section header lines for cleaner issue — use full file after Title
  body=$(awk 'BEGIN{p=0} /^## Body/{p=1; next} p{print}' "$file")
  labels=$(grep -m1 '^## Labels' -A1 "$file" | tail -n1 | tr -d '`' | sed 's/^[[:space:]]*//')

  echo "Creating: $title"

  if command -v gh >/dev/null 2>&1; then
    # shellcheck disable=SC2086
    local label_args=()
    IFS=',' read -ra parts <<< "$labels"
    for lab in "${parts[@]}"; do
      lab=$(echo "$lab" | xargs)
      [ -n "$lab" ] && label_args+=(--label "$lab")
    done
    # Labels may not exist on repo — create without labels if fail
    if ! gh issue create --repo "$REPO" --title "$title" --body "$body" "${label_args[@]}" 2>/dev/null; then
      gh issue create --repo "$REPO" --title "$title" --body "$body"
    fi
  elif [ -n "${GH_TOKEN:-}" ] || [ -n "${GITHUB_TOKEN:-}" ]; then
    TOKEN="${GH_TOKEN:-$GITHUB_TOKEN}"
    # JSON escape via python if available
    if command -v python >/dev/null 2>&1 || command -v python3 >/dev/null 2>&1; then
      PY=$(command -v python3 || command -v python)
      payload=$("$PY" -c "import json,sys; print(json.dumps({'title':sys.argv[1],'body':sys.argv[2]}))" "$title" "$body")
      curl -sS -X POST \
        -H "Authorization: token $TOKEN" \
        -H "Accept: application/vnd.github+json" \
        "https://api.github.com/repos/$REPO/issues" \
        -d "$payload" | "$PY" -c "import sys,json; d=json.load(sys.stdin); print(d.get('html_url', d))"
    else
      echo "Need gh or python for API create" >&2
      exit 1
    fi
  else
    echo "ERROR: cài gh (gh auth login) hoặc set GH_TOKEN" >&2
    exit 1
  fi
}

for f in \
  "$DIR/01-bug-assign-owner-fail.md" \
  "$DIR/02-owner-change-should-not-keep-stale-household-expectation.md" \
  "$DIR/03-assign-tenant-does-not-update-household-members.md" \
  "$DIR/04-missing-history-on-assign-owner-tenant.md" \
  "$DIR/05-remove-member-should-hard-delete-from-household.md" \
  "$DIR/06-cleanup-redundant-comments.md"
do
  create_one "$f"
  echo "---"
done

echo "Done."
