#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

fail() {
  echo "ui quality guard failed: $1" >&2
  exit 1
}

ui_paths=(app/src/main/java/com/pedilo/app)

if rg -n "TODO|FIXME|Pedido listo para enviar|Cliente|Customer|customer|signInAnonymously" "${ui_paths[@]}" >/tmp/pedilo_ui_guard_match.txt; then
  cat /tmp/pedilo_ui_guard_match.txt >&2
  fail "legacy or unresolved UI text found"
fi

if rg -n "Color[(]0x" app/src/main/java/com/pedilo/app --glob '!app/src/main/java/com/pedilo/app/ui/theme/Color.kt' >/tmp/pedilo_ui_guard_match.txt; then
  cat /tmp/pedilo_ui_guard_match.txt >&2
  fail "hardcoded color outside theme"
fi

if rg -n "OutlinedTextField" app/src/main/java/com/pedilo/app/ui --glob '!app/src/main/java/com/pedilo/app/ui/components/PediloTextField.kt' >/tmp/pedilo_ui_guard_match.txt; then
  cat /tmp/pedilo_ui_guard_match.txt >&2
  fail "raw text field outside shared component"
fi

if find app/src/main/java/com/pedilo/app/ui/components -type f -printf '%f\n' | sort | uniq -d | rg . >/tmp/pedilo_ui_guard_match.txt; then
  cat /tmp/pedilo_ui_guard_match.txt >&2
  fail "duplicated component file names"
fi

if find app/src/main/java/com/pedilo/app/ui/theme -type f -printf '%f\n' | sort | uniq -d | rg . >/tmp/pedilo_ui_guard_match.txt; then
  cat /tmp/pedilo_ui_guard_match.txt >&2
  fail "duplicated theme file names"
fi

echo "ui quality guard passed"
