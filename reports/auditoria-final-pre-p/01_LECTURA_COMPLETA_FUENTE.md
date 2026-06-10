# 01 - Lectura completa de fuente

## Archivos leidos completos

- `functions/index.js`
- `firestore.rules`
- `firebase.json`
- `README.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- 83 archivos Kotlin bajo `app/src/main/java`
- 33 archivos `tests/*.test.js`
- 3 guards bajo `tools/guards`
- `tools/seed_public_catalog.js`
- `tools/verify_public_catalog.js`
- `functions/package.json`
- `tools/package.json`

## Lectura por areas

- Backend: se leyo el archivo completo de Functions por segmentos, incluyendo exports, payloads, finanzas, comunicacion, IA, health, tracking, claims, idempotencia, roles y acciones.
- Rules: se leyo completo `firestore.rules`.
- Android core: se leyeron adapters Firebase, modelos, ports, use cases, runtime y validadores.
- UI publica: se leyeron Home, tienda/local, Boton +, tracking, reclamos, inputs, tema y team access.
- UI Admin: se leyo `AdminApp.kt`, componentes, datos de operacion y roles.
- UI Store/Driver: se leyeron pantallas, acciones, labels de comunicacion, finanzas y ayudas operativas.
- Tests/guards: se leyeron los tests existentes y se agrego un test final de auditoria pre-P.

## Archivos leidos parcialmente

- Reportes previos bajo `reports/`: leidos parcialmente solo para contraste historico, sin usarlos como prueba final.
- Assets binarios PNG/JAR: inventariados, no leidos semanticamente.
- `functions/package-lock.json` y `tools/package-lock.json`: revisados por riesgo de providers/secrets, no auditados linea por linea como logica de producto.

## Archivos no leidos y motivo

- `.git/*`: metadata Git, excluida.
- `.gradle/*`, `build/*`, `app/build/*`: cache/salida generada.
- `functions/node_modules/*`, `node_modules/*`: dependencias externas.
- `app/google-services.json`: sensible local, no tracked; no se abrio ni modifico.
- `local.properties`: sensible local, no tracked; no se abrio ni modifico.

## Archivos con riesgo

- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `app/src/main/java/com/pedilo/app/ui/store/StoreApp.kt`
- `app/src/main/java/com/pedilo/app/ui/driver/DriverApp.kt`

Riesgo detectado: label `sent` de comunicacion decia "Enviada por canal real", que podia sonar a envio externo real. Fue corregido a "Registrada como enviada; verificar canal".

## Archivos que contradicen reportes previos

- No se detectaron contradicciones funcionales contra los reportes previos.
- Se detecto una mejora de copy pre-P no cubierta por los reportes: evitar que `sent` prometa canal real.
