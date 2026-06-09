# Reporte final - baseline seguro V1

**Fecha:** 2026-06-09  
**Rama inicial:** `main`  
**HEAD inicial:** `50c51fa92bfd761e24c05cde7bd5c7238265c397`

## Estado Git inicial

`git status --short` inicial:

```text
?? documentacion-generada-pedilo/
```

El único elemento inicial no trackeado era documentación/reporte local generado previamente. Según la regla del bloque, se lista como excluido del alcance y no bloquea.

## Archivos excluidos del alcance

- `documentacion-generada-pedilo/`  
  Motivo: documentación local generada previamente; no es código fuente funcional y no se modifica en este bloque.

## Archivos creados

- `reports/etapa-q-baseline-seguro/01-baseline-real-v1.md`
- `reports/etapa-q-baseline-seguro/02-matriz-real-parcial-placeholder.md`
- `reports/etapa-q-baseline-seguro/03-zonas-sensibles-no-tocar.md`
- `reports/etapa-q-baseline-seguro/04-reporte-final.md`

## Archivos modificados

- Ningún archivo existente fue modificado.

## Limpiezas aplicadas

- No se aplicaron limpiezas de código.
- Motivo: no se detectó una limpieza menor imprescindible que justificara tocar zonas funcionales. Se priorizó conservar intacta la base V1 real.

## Módulos reales conservados

- Usuario público V1.
- Creación pública de pedidos por Cloud Functions.
- Tracking público.
- Admin operativo V1 parcial.
- Store/Local operativo V1.
- Driver/Repartidor operativo V1.
- Pedido operativo V1 sobre `/orders`.
- Firestore Rules actuales.
- Tests, guards, build y lint existentes.

## Módulos parciales marcados

- Pedido Vivo: ejes financiero/comunicación/incidencia/archivo parciales.
- Botón +: real para crear pedidos, sin IA/WhatsApp/finanzas completas.
- Tienda/Local público: catálogo y pedido reales, sin administración de stock/variantes.
- Admin operación: real/parcial; convive con configuración visual.
- Store/Driver: operación real V1 sin finanzas/capacidad/cierre.
- Pagos/finanzas: campos simples, sin sistema financiero.
- Comunicación: campos/estado declarativo, sin canales reales.
- Métricas/salud: eventos mínimos y UI visual, sin observabilidad real.

## Placeholders identificados

- Admin Configuración.
- Admin Alta de roles.
- Reclamos públicos.
- Secciones visuales de comunicación/notificaciones.
- Secciones visuales de métricas/salud.
- Rutas intermedias de roles.
- Scripts seed/verificación como herramientas sensibles, no runtime.

## Zonas sensibles no tocadas

- `functions/index.js`
- `firestore.rules`
- `app/src/main/java/com/pedilo/app/core/firebase/`
- Modelos y acciones del pedido.
- `nextAllowedActions`
- `expectedVersion`
- Eventos e incidencias.
- UI pública funcional.
- Admin operativo.
- Store/Local operativo.
- Driver/Repartidor operativo.
- `tools/guards/`
- `tools/seed_public_catalog.js`
- `tools/verify_public_catalog.js`
- `.firebaserc`
- `app/google-services.json`

## Validaciones ejecutadas

| Comando | Resultado |
|---------|-----------|
| `git status --short` | OK. Sólo muestra `?? documentacion-generada-pedilo/`, excluido del alcance por ser documentación local generada previamente. Los reportes nuevos están bajo `reports/*`, ruta ignorada por `.gitignore`. |
| `node --test tests/*.test.js` | OK: 20/20 tests pasan. |
| `npm --prefix functions run build` | OK: `node --check index.js`. |
| `bash tools/guards/check_architecture.sh` | OK: `architecture guard passed`. |
| `bash tools/guards/check_ui_quality.sh` | OK: `ui quality guard passed`. |
| `./gradlew assembleDebug --offline` | OK con ejecución local escalada por lock/cache de Gradle fuera del workspace; `BUILD SUCCESSFUL`. |
| `./gradlew lintDebug --offline` | OK con ejecución local escalada por lock/cache de Gradle fuera del workspace; `BUILD SUCCESSFUL`. |
| `git diff --check` | OK sin errores. |

## Riesgos controlados

- Placeholders explícitamente marcados como visuales/parciales/no implementados.
- Zonas sensibles registradas como no tocar.
- Base V1 real documentada como conservar.
- Configuración Firebase local marcada como sensible.
- Scripts Firebase marcados como no runtime/no ejecutar sin entorno controlado.

## Riesgos pendientes

- No hay emuladores configurados explícitamente.
- No hay tests JUnit Android detectados.
- Admin Configuración y Alta de roles siguen siendo visuales.
- Reclamos públicos no persisten en backend.
- Pagos/finanzas, WhatsApp/chat/notificaciones, IA y métricas reales no están implementados.
- `.firebaserc` apunta a un proyecto Firebase default y requiere disciplina de no deploy.

## Commit

Realizado con mensaje:

```text
Establish safe V1 baseline for staged development
```

Nota: `reports/*` está ignorado por `.gitignore`, por eso estos reportes se agregan explícitamente al índice sin modificar reglas de tracking. La documentación previa `documentacion-generada-pedilo/` queda excluida del alcance y fuera del commit.

## Dictamen final

**B) BASELINE V1 SEGURO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.**

## Próximo bloque técnico permitido

Continuar con el siguiente bloque técnico por etapas sobre Etapa Q/Baseline, manteniendo fuera de alcance producción, deploy, seed, pagos, WhatsApp, IA, métricas reales y cambios funcionales del Pedido Vivo hasta que exista etapa específica.
