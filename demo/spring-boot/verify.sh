#!/usr/bin/env bash
# Verifies the demo app endpoint by endpoint. The app must be running
# (see README.md). Exits non-zero on the first mismatch.
set -u

BASE="${BASE_URL:-http://localhost:8080}"
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

check() { # name path expected_status [expected content-type fragment] [expected body fragments...]
  local name="$1" path="$2" expected_status="$3"
  local expected_type="$4"
  shift 4
  local status
  status="$(curl -s -o "$BODY" -w "%{http_code}" "$BASE$path")"
  [ "$status" = "$expected_status" ] \
    || fail "$name: status $status, expected $expected_status ($(cat "$BODY"))"
  if [ -n "$expected_type" ]; then
    curl -s -o /dev/null -D - "$BASE$path" \
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

check "control case" /demo/ok 200 "" '"message":"ok"'
check "full problem" /demo/credit 403 "$JSON" \
  '"type":"https://example.com/probs/out-of-credit"' \
  '"status":403' \
  '"balance":30' \
  '"instance":"https://example.com/instances/1"'
check "instance from path" /demo/missing 404 "$JSON" \
  '"status":404' \
  '"instance":"/demo/missing"'
check "statusless fallback" /demo/boom 500 "$JSON" \
  '"title":"Boom."'
check "validation errors" "/demo/validate?age=-5" 422 "$JSON" \
  '"type":"https://example.net/validation-error"' \
  '"status":422' \
  '"pointer":"#/age"'
check "valid input" "/demo/validate?age=30" 200 "" '"message":"valid"'
check "registry listing" /demo/types 200 "" \
  '"type":"about:blank"' \
  '"type":"https://example.com/probs/out-of-credit"'

echo "All demo checks passed."
