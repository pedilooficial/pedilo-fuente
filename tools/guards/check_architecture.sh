#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

fail() {
  echo "architecture guard failed: $1" >&2
  exit 1
}

architecture_paths=(
  app/src/main
  app/build.gradle.kts
  functions/src
  firestore.rules
  firestore.indexes.json
  README.md
)

for pattern in \
  "UserRole"".Customer" \
  "Customer" \
  "customer" \
  "customer""Id" \
  "owner" \
  "isOwner" \
  "signIn""Anonymously" \
  "id""Token" \
  "TODO" \
  "FIXME" \
  "mock" \
  "Mock" \
  ".collection(\"orders\").add" \
  ".collection(\"orders\").document" \
  "Firebase""Firestore.getInstance().collection(\"orders\")"
do
  if rg -n --fixed-strings "$pattern" "${architecture_paths[@]}" >/tmp/pedilo_guard_match.txt; then
    cat /tmp/pedilo_guard_match.txt >&2
    fail "forbidden pattern found: $pattern"
  fi
done

if rg -n "collection[(][\"']orders[\"'][)].*[.](set|update|delete|add)|document[(].*orders.*[)].*[.](set|update|delete)" app/src/main >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "Android must not write directly to orders"
fi

create_order_block="$(awk '
  /export const createOrder/ {inside=1}
  inside {print}
  inside && /^\}\);/ {inside=0}
' functions/src/index.ts)"

if printf '%s\n' "$create_order_block" | rg -n "request[.]auth|requireAuth" >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "createOrder must not depend on auth"
fi

if ! rg -n --fixed-strings 'actorRole: "public"' functions/src >/dev/null; then
  fail "public createOrder event actor is missing"
fi

if ! rg -n --fixed-strings 'type: "order_created"' functions/src >/dev/null; then
  fail "initial order event is missing"
fi

if ! rg -n --fixed-strings 'status: "created"' functions/src >/dev/null; then
  fail "initial order status is missing"
fi

if rg -n "isOwner|owner|customer" firestore.rules >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "firestore rules contain forbidden public ownership logic"
fi

if ! rg -n "allow create, update, delete: if false" firestore.rules >/dev/null; then
  fail "orders must reject direct client writes"
fi

echo "architecture guard passed"
