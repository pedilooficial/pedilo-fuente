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
  app/src/main/java/com/pedilo/app/domain
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

if rg -n "collection[(][\"']orders[\"'][)].*[.](set|update|delete|add)|document[(].*orders.*[)].*[.](set|update|delete)" app/src/main/java/com/pedilo/app/ui >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "UI must not write directly to orders"
fi

pure_core_paths=(
  app/src/main/java/com/pedilo/app/core/model
  app/src/main/java/com/pedilo/app/core/port
  app/src/main/java/com/pedilo/app/core/result
  app/src/main/java/com/pedilo/app/core/usecase
)

if [ -d app/src/main/java/com/pedilo/app/core ]; then
  if rg -n "firebase|Firebase|Firestore|Functions|com[.]google[.]firebase" "${pure_core_paths[@]}" >/tmp/pedilo_guard_match.txt; then
    cat /tmp/pedilo_guard_match.txt >&2
    fail "pure core must not import Firebase"
  fi

  if rg -n "androidx[.]compose|android[.]app|android[.]content" "${pure_core_paths[@]}" app/src/main/java/com/pedilo/app/core/firebase >/tmp/pedilo_guard_match.txt; then
    cat /tmp/pedilo_guard_match.txt >&2
    fail "core must not import Android or Compose"
  fi
fi

if ! rg -n '"source"[[:space:]]*:[[:space:]]*"functions"' firebase.json >/dev/null; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "functions deploy config must only point to the local functions source"
fi

if [ -d functions ]; then
  if ! rg -n "exports[.]createLocalOrder" functions/index.js >/dev/null; then
    fail "functions must expose the createLocalOrder callable"
  fi
  if ! rg -n "exports[.]createPlusOrder" functions/index.js >/dev/null; then
    fail "functions must expose the createPlusOrder callable"
  fi
  if ! rg -n "exports[.]getPublicOrderTracking" functions/index.js >/dev/null; then
    fail "functions must expose the getPublicOrderTracking callable"
  fi
  if rg -n "collection[(][\"'](users|roles|payments|order_tracking)[\"'][)]|whatsapp|WhatsApp|driverId" functions >/tmp/pedilo_guard_match.txt; then
    cat /tmp/pedilo_guard_match.txt >&2
    fail "public order functions must not touch users, roles, payments, WhatsApp or tracking collections"
  fi
fi

if rg -n "isOwner|owner|customer" firestore.rules >/tmp/pedilo_guard_match.txt; then
  cat /tmp/pedilo_guard_match.txt >&2
  fail "firestore rules contain forbidden public ownership logic"
fi

if ! rg -n "allow create, update, delete: if false" firestore.rules >/dev/null; then
  fail "orders must reject direct client writes"
fi

echo "architecture guard passed"
