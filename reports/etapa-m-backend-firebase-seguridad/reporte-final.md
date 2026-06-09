# Etapa M - Backend Firebase Seguridad

## Linea base

- Rama inicial: main.
- HEAD inicial: 18caf074b561f8675f24c3f379de68e6912b9ba6.
- Estado Git inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

Se endurecio la base backend/Firebase del Pedido Vivo sin implementar modulos nuevos ni cambiar flujos de producto. Las acciones operativas siguen pasando por Cloud Functions, las escrituras directas de clientes sobre pedidos/eventos/incidencias siguen cerradas y la lectura operativa ahora depende de perfiles internos activos.

## Archivos modificados

- `functions/index.js`
- `firestore.rules`
- `tests/admin_operational_actions.test.js`
- `tests/operational_order_actions_backend.test.js`

## Archivos creados

- `tests/backend_firebase_security_hardening.test.js`
- `reports/etapa-m-backend-firebase-seguridad/reporte-final.md`

## Endurecimiento en Cloud Functions

- `adminOrderAction` ahora usa `requireAdminActor` y exige usuario autenticado, existente, rol `admin` y `active == true`.
- `operateLiveOrder` ahora exige usuario autenticado, existente, rol en `admin/store/driver` y `active == true`.
- Se centralizo el conjunto de roles operativos validos en `OPERATIONAL_ROLES`.
- Los payloads operativos rechazan `orderId` no seguro antes de operar.
- Tracking publico valida formato `PDL-...` y evita placeholders antes de consultar.
- Telefono publico se alineo a la validacion de UI: 8 a 15 digitos, con `+` opcional solo al inicio.
- Items de pedidos locales rechazan `productId` placeholder.
- Se preservaron transacciones, versionado por `expectedVersion`, idempotencia por `actionId`, eventos/incidencias creados desde backend y errores controlados con `HttpsError`.
- Se corrigio la auditoria de incidencias Admin para usar `actor.uid` despues del refactor de autenticacion.

## Endurecimiento en Firestore Rules

- Se agrego `operatorActive()` sobre `/users/{uid}.active == true`.
- `isAdmin()` ahora requiere rol Admin y perfil activo.
- `isOperator()` ahora requiere rol operativo valido y perfil activo.
- `/orders` mantiene lectura solo por `canReadOrder(resource.data)` y escrituras directas cerradas.
- `/orders/{orderId}/events` e `/orders/{orderId}/incidents` mantienen lectura ligada al pedido visible por rol y escritura cerrada.
- Catalogo publico sigue siendo solo lectura y solo para stores/productos visibles y disponibles.

## Validaciones por rol

- Admin valido: puede pasar por backend si existe y esta activo.
- Store valido: puede operar solo su pedido y si existe/esta activo.
- Driver valido: puede leer/operar disponibles o asignados segun reglas/validadores actuales y si existe/esta activo.
- Usuario inactivo: rechazado en Functions y excluido de lectura operativa por rules.
- Rol invalido: rechazado por Functions y rules.
- Usuario inexistente: rechazado por Functions y rules.
- Publico sin login: no escribe ni lee pedidos internos; consulta tracking solo por callable con respuesta publica filtrada.

## Tests agregados/modificados

- Agregado `tests/backend_firebase_security_hardening.test.js`.
- Actualizado `tests/admin_operational_actions.test.js`.
- Actualizado `tests/operational_order_actions_backend.test.js`.

## Validaciones ejecutadas

- `git status --short`
- `node --test tests/*.test.js`
- `npm --prefix functions run build`
- `bash tools/guards/check_architecture.sh`
- `bash tools/guards/check_ui_quality.sh`
- `./gradlew assembleDebug --offline`
- `./gradlew lintDebug --offline`
- `git diff --check`

## Resultado de validaciones

- `node --test tests/*.test.js`: OK, 22/22 archivos de test.
- `npm --prefix functions run build`: OK.
- `bash tools/guards/check_architecture.sh`: OK.
- `bash tools/guards/check_ui_quality.sh`: OK.
- `./gradlew assembleDebug --offline`: OK con ejecucion fuera del sandbox para cache local Gradle.
- `./gradlew lintDebug --offline`: OK con ejecucion fuera del sandbox para cache local Gradle.
- `git diff --check`: OK.

## Proteccion contra produccion accidental

- `.firebaserc`: revisado, no modificado.
- `app/google-services.json`: revisado en existencia, no modificado.
- `tools/seed_public_catalog.js`: no ejecutado.
- `tools/verify_public_catalog.js`: no ejecutado.
- Comandos de deploy: no ejecutados.
- Comandos seguros usados: tests Node, build local de Functions, guards locales, build/lint Android offline.
- Comandos peligrosos evitados: deploy, seed, escritura sobre configuracion sensible o produccion.

## Fuera de alcance

- Pagos reales.
- Cierre de caja.
- WhatsApp real.
- Chat real.
- Notificaciones reales.
- IA.
- Metricas reales.
- Google Play.
- Deploy.
- Seed.
- Cambios de producto o rediseño.

## Riesgos pendientes

- No se ejecutaron pruebas contra emulador Firestore real; las pruebas actuales son estaticas/de unidad sobre reglas y contratos.
- El endurecimiento `active == true` requiere que perfiles internos reales tengan ese campo correctamente poblado en entornos no productivos.

## Dictamen final

BLOQUE M COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Continuar con el siguiente bloque funcional planificado sobre la base backend/Firebase ya endurecida, sin deploy ni seed hasta que se autorice explicitamente.
