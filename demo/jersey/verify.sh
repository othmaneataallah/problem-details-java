#!/usr/bin/env bash
# Verifies the Jersey demo endpoint by endpoint. The app must be running
# (see README.md). Exits non-zero on the first mismatch.
set -u

BASE="${BASE_URL:-http://localhost:8081}"
BODY="$(mktemp)"
trap 'rm -f "$BODY"' EXIT

fail() {
  echo "FAIL: $1"
  exit 1
}

echo "Waiting for $BASE ..."
for _ in $(seq 1 30); do
  curl -sf -o /dev/null "$BASE/demo/ok" 2>/dev/null && break
  sleep 1
done
curl -sf -o /dev/null "$BASE/demo/ok" 2>/dev/null || fail "app is not reachable at $BASE"

check() { # name method path accept expected_status [expected content-type] [body fragments...]
  local name="$1" method="$2" path="$3" accept="$4" expected_status="$5"
  local expected_type="$6"
  shift 6
  local status
  status="$(curl -s -o "$BODY" -w "%{http_code}" -X "$method" -H "Accept: $accept" "$BASE$path")"
  [ "$status" = "$expected_status" ] \
    || fail "$name: status $status, expected $expected_status ($(cat "$BODY"))"
  if [ -n "$expected_type" ]; then
    curl -s -o /dev/null -D - -X "$method" -H "Accept: $accept" "$BASE$path" \
      | grep -qiF "content-type: $expected_type" \
      || fail "$name: missing content type $expected_type"
  fi
  for fragment in "$@"; do
    grep -qF "$fragment" "$BODY" \
      || fail "$name: body lacks '$fragment' ($(cat "$BODY"))"
  done
  echo "ok: $name"
}

JSON="application/problem+json"
XML="application/problem+xml"

check "control case" GET /demo/ok "text/plain" 200 "" "ok"
check "returned problem as JSON" GET /demo/credit "$JSON" 200 "$JSON" \
  '"type":"https://example.com/probs/out-of-credit"' \
  '"status":403' \
  '"balance":30'
check "returned problem as XML" GET /demo/credit "$XML" 200 "$XML" \
  'xmlns="urn:ietf:rfc:7807"' \
  '<balance>30</balance>'
check "thrown problem as JSON" GET /demo/fail "$JSON" 403 "$JSON" \
  '"status":403' \
  '"balance":30'
check "thrown problem stays JSON for XML Accept" GET /demo/fail "$XML" 403 "$JSON" \
  '"status":403'
check "statusless fallback" GET /demo/boom "$JSON" 500 "$JSON" \
  '"title":"Boom."'
check "validation as JSON" GET /demo/validate "$JSON" 200 "$JSON" \
  '"status":422' \
  '"pointer":"#/age"'
check "validation as XML" GET /demo/validate "$XML" 200 "$XML" \
  '<errors><i><detail>must be a positive integer</detail>'
check "registry listing" GET /demo/types "$JSON" 200 "" \
  '"type":"about:blank"' \
  '"type":"https://example.com/probs/out-of-credit"'

echo "All demo checks passed."
