# 00 - Manifiesto de fuente auditada

## Baseline

- Rama: `main`
- HEAD inicial: `8a71504dce0e53026b472edafc4654914f03ac20`
- Estado Git inicial: limpio.

## Total detectado

- Total de archivos detectados por el comando de inventario, excluyendo `.git`, `.gradle`, `build`, `app/build`, `functions/node_modules` y `node_modules`: 185.

## Configuracion raiz

- `.firebaserc`
- `.gitignore`
- `Pedilo.concepto.md`
- `README.md`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `firebase.json`
- `firestore.indexes.json`

## Android app

- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/pedilo/app/MainActivity.kt`
- `app/src/main/java/com/pedilo/app/PediloApp.kt`

## Android core

- 58 archivos bajo `app/src/main/java/com/pedilo/app/core`.
- Incluye modelos, ports, use cases, runtime y adapters Firebase.

## UI publica

- 16 archivos bajo `app/src/main/java/com/pedilo/app/ui/publicuser`.
- Incluye Home, Local, Boton +, tienda, tracking, reclamos e ingreso de equipo.

## UI Admin

- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/components/AdminComponents.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/operation/AdminOperationNavigation.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/operation/OperationData.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/roles/RoleAccessData.kt`

## UI Store

- `app/src/main/java/com/pedilo/app/ui/store/StoreApp.kt`

## UI Driver

- `app/src/main/java/com/pedilo/app/ui/driver/DriverApp.kt`

## Backend Functions

- `functions/index.js`
- `functions/package.json`
- `functions/package-lock.json`

## Firestore Rules

- `firestore.rules`
- `firestore.indexes.json`

## Tests

- 33 archivos `tests/*.test.js` luego de agregar `tests/final_pre_p_source_audit.test.js`.

## Guards

- `tools/guards/check_architecture.sh`
- `tools/guards/check_ui_quality.sh`
- `tools/guards/check_no_production_release.sh`

## Scripts

- `tools/seed_public_catalog.js`
- `tools/verify_public_catalog.js`
- `gradlew`
- `gradlew.bat`

## Assets

- Recursos Android bajo `app/src/main/res`, incluyendo launcher, estilos, strings y `pedilo_logo_mark.png`.
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

## Reports

- 26 reportes previos detectados bajo `reports/` antes de crear esta carpeta.
- Usados solo como contraste historico, no como fuente de certificacion.

## Archivos sensibles/configuracion

- `.firebaserc`: tracked; no modificado.
- `app/google-services.json`: presente localmente, no tracked; no modificado.
- `local.properties`: presente localmente, no tracked; no modificado.
- `functions/package-lock.json` y `tools/package-lock.json`: auditados como dependencias, no modificados.

## Excluidos por build/cache/dependencias

- `.git/*`: metadata Git.
- `.gradle/*`: cache local Gradle.
- `build/*`: salida de build raiz.
- `app/build/*`: salida de build Android.
- `functions/node_modules/*`: dependencias instaladas.
- `node_modules/*`: dependencias instaladas si existieran.

## Archivos que requieren lectura completa

- `functions/index.js`
- `firestore.rules`
- `firebase.json`
- `README.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- todos los `.kt` bajo `app/src/main/java`
- todos los `.js` bajo `tests`
- todos los `.sh` bajo `tools/guards`
- `tools/seed_public_catalog.js`
- `tools/verify_public_catalog.js`
