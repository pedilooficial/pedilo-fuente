# Admin uso real — cierre Pédilo

**Fecha:** 2026-06-10  
**Rama inicial:** `work`  
**HEAD inicial real:** `b6a5ec58f862176e756987345d8f2bc25d1b8395`  
**HEAD esperado por instrucción:** `eab50ff317c3ee89366a2c09b15aa4bf5acd2c10` no coincidía con el entorno; se trabajó sobre el HEAD real verificado.  
**Estado Git inicial:** limpio.

## Documentación leída

- `documentacion-generada-pedilo/00-maestros/`
- `documentacion-generada-pedilo/02-pedido-vivo/`
- `documentacion-generada-pedilo/06-admin/`
- `documentacion-generada-pedilo/07-pagos-tarifas-finanzas/`
- `documentacion-generada-pedilo/09-incidencias-reclamos-cancelaciones/`
- `documentacion-generada-pedilo/10-comunicacion/`
- `documentacion-generada-pedilo/11-ia/`
- `documentacion-generada-pedilo/12-metricas-auditoria-salud/`
- `documentacion-generada-pedilo/13-backend-firebase-seguridad/`
- `documentacion-generada-pedilo/14-android-ui-calidad/`
- `documentacion-generada-pedilo/17-cierre-final/`
- `documentacion-generada-pedilo/18-plan-implementacion/`

## Matriz usada

- `reports/cierre-app-real/00_MATRIZ_CUMPLIMIENTO_DOCUMENTACION.md`

## Faltantes Admin detectados

1. Configuración Admin seguía representada como revisión visual/heredada en lugar de superficie persistente.
2. Alta/roles seguía comunicando preparación visual o no aplicación, lo que podía aparentar flujo sin persistencia.
3. Admin no tenía una superficie directa para leer usuarios reales de `/users` y persistir cambios de rol/estado desde UI.
4. Faltaban funciones backend auditadas para cambios Admin de configuración y accesos existentes.
5. Firestore Rules no cubrían explícitamente lectura Admin de `admin_config` ni subcolecciones de auditoría de config/accesos.
6. Tests seguían defendiendo herramientas visuales no persistentes; se actualizaron para exigir superficies reales o bloqueos explícitos.

## Correcciones aplicadas

- Se agregó lectura en vivo de configuración Admin y usuarios de equipo al puerto/use case/adaptador Admin.
- Se reemplazó el root Admin de Configuración por `AdminRealConfigurationScreen`, con flags persistentes y mensajes humanos de guardado/error.
- Se reemplazó el root Admin de Roles por `AdminRealRoleAccessScreen`, con lectura real de `/users`, cambio persistente de rol y activación/desactivación de usuarios existentes.
- Se bloqueó explícitamente la creación de cuentas nuevas porque no hay flujo documental de invitación/Auth ni credenciales definido.
- Se agregaron callables `adminUpdateTeamUser` y `adminUpdateConfig`, protegidas por `requireAdminActor`, con auditoría en subcolecciones.
- Se mantuvieron las escrituras directas a `/orders` cerradas; Admin sigue operando pedidos mediante `operateLiveOrder` y `expectedVersion`.
- Se ajustaron Rules para `admin_config/{configId}/events` y `users/{userId}/access_events` como lectura Admin y escritura backend-only.
- Se actualizaron tests para exigir persistencia real en Configuración/Roles y ausencia de botones visuales falsos.

## Archivos modificados

- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseAdminOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/model/AdminOrderReadModels.kt`
- `app/src/main/java/com/pedilo/app/core/port/AdminOrdersPort.kt`
- `app/src/main/java/com/pedilo/app/core/usecase/GetAdminOperationOrdersUseCase.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `firestore.rules`
- `functions/index.js`
- `tests/admin_operation_alignment.test.js`
- `tests/admin_visual_shell.test.js`

## Archivos creados

- `reports/cierre-app-real/admin/ADMIN_USO_REAL_CIERRE.md`

## Qué quedó real

- Admin puede entrar y ver operación real existente.
- Admin puede abrir pedidos, ver detalle, estados, responsable, finanzas mínimas, comunicación, IA asistida, incidencias y auditoría visible cuando existan en el pedido.
- Admin ejecuta acciones de pedido reales sólo si vienen en `nextAllowedActions` y con `expectedVersion`.
- Admin ve configuración real persistente para modos operativos básicos (`maintenanceMode`, `rainMode`, `saturationMode`, `emergencyMode`, `publicOrderingEnabled`).
- Admin ve usuarios reales existentes desde `/users` y puede activar/desactivar o cambiar rol mediante backend callable auditada.
- Configuración y roles ya no se presentan en el root como pantallas de desarrollo o confirmaciones sin efecto.
- Cambios de config/acceso quedan auditados en subcolecciones internas.

## Qué quedó bloqueado por decisión externa o alcance documental no definido

- Creación de cuentas Auth nuevas desde Admin: bloqueada visualmente porque falta flujo documental de invitación/Auth, credenciales iniciales y política de alta segura.
- Proveedores externos reales de mensajería, cobro, push e IA externa: no se inventaron ni activaron.
- Google Play, deploy productivo, AAB firmado y Firebase producción: no se tocaron.

## Tests agregados/modificados

- Modificados: `tests/admin_operation_alignment.test.js`.
- Modificados: `tests/admin_visual_shell.test.js`.
- No se eliminaron tests.

## Validaciones ejecutadas

| Comando | Resultado |
|---|---|
| `git status --short` | OK antes de cambios; al final limpio tras commit. |
| `node --test tests/*.test.js` | OK: 222 tests pasan. |
| `npm --prefix functions run build` | OK: `node --check index.js`. |
| `bash tools/guards/check_architecture.sh` | OK. |
| `bash tools/guards/check_ui_quality.sh` | OK. |
| `bash tools/guards/check_no_production_release.sh` | OK. |
| `./gradlew assembleDebug --offline` | No ejecutable en este entorno: Gradle/Kotlin falla antes de compilar por Java `25.0.2`. |
| `./gradlew lintDebug --offline` | No ejecutable en este entorno: Gradle/Kotlin falla antes de compilar por Java `25.0.2`. |
| `git diff --check` | OK. |

## Riesgos restantes

1. No se pudo compilar Android en este contenedor por incompatibilidad de Java 25 con el stack Gradle/Kotlin instalado; requiere JDK compatible para certificación Android real.
2. Admin queda real para operación, configuración básica y accesos existentes, pero creación de cuentas nuevas sigue bloqueada hasta definir invitación/Auth.
3. Finanzas completas, proveedores externos y módulos de pago/caja siguen limitados al baseline ya documentado; no se inventaron integraciones externas.
4. La validación final en celulares reales sigue siendo necesaria antes de piloto operativo.

## Dictamen final

**B) ADMIN AVANZADO, PERO NO CERRADO POR BLOQUEOS REALES.**

Motivo: las superficies Admin falsas más críticas fueron reemplazadas por operación/persistencia real o bloqueo explícito, y los tests/guards Node pasan. No corresponde dictamen A porque `assembleDebug` y `lintDebug` no pudieron ejecutarse por Java 25.0.2 del entorno, y la creación real de cuentas nuevas queda bloqueada hasta decisión/flujo externo de Auth.
