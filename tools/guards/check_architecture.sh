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
  firestore.rules
  firestore.indexes.json
  firebase.json
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

for removed_path in \
  app/src/main/java/com/pedilo/app/data \
  app/src/main/java/com/pedilo/app/domain \
  functions
do
  if [ -e "$removed_path" ]; then
    fail "removed legacy path exists: $removed_path"
  fi
done

for removed_symbol in \
  "Pedilo""Screen" \
  "Pedilo""ViewModel" \
  "Firebase""Pedilo""Repository" \
  "Pedilo""Repository" \
  "com.pedilo.app.""data" \
  "com.pedilo.app.""domain"
do
  if rg -n --fixed-strings "$removed_symbol" app/src/main tests >/tmp/pedilo_guard_match.txt; then
    cat /tmp/pedilo_guard_match.txt >&2
    fail "removed legacy symbol found"
  fi
done

if rg -n "collection[(][\"']orders[\"'][)].*[.](set|update|delete|add)|document[(].*orders.*[)].*[.](set|update|delete)" app/src/main >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "Android must not write directly to orders"
fi

if rg -n '"functions"[[:space:]]*:' firebase.json >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "legacy functions deploy config remains"
fi

if rg -n "isOwner|owner|customer" firestore.rules >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "firestore rules contain forbidden public ownership logic"
fi

if ! rg -n "allow create, update, delete: if false" firestore.rules >/dev/null; then
  fail "orders must reject direct client writes"
fi

echo "architecture guard passed"
