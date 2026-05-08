#!/bin/bash

set -e

ORIGEN="$HOME/Desktop/pedilo"
DESTINO_BASE="$HOME/backups/pedilo"
FECHA=$(date +"%Y-%m-%d_%H-%M-%S")
DESTINO="$DESTINO_BASE/pedilo_backup_$FECHA"
LOG="$DESTINO_BASE/ultimo_backup.log"

mkdir -p "$DESTINO"

rsync -av \
  --exclude="node_modules" \
  --exclude=".git" \
  --exclude=".env" \
  --exclude=".env.*" \
  --exclude="dist" \
  --exclude="build" \
  --exclude="coverage" \
  --exclude=".cache" \
  "$ORIGEN/" "$DESTINO/" > "$LOG"

echo "BACKUP OK"
echo "Guardado en: $DESTINO"
echo "Log: $LOG"
