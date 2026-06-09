# 20 - Reporte Total del Estado Actual Real del Repo

**Fecha de auditoría:** 2026-06-09  
**Repo auditado:** `/home/oem/Desktop/pedilo`  
**Documentación cerrada usada:** `/home/oem/Desktop/documentos pedilo/documentacion-generada-pedilo/`  
**Alcance:** auditoría técnica real previa a Etapa Q.  
**Acciones realizadas:** lectura de repo/documentación, ejecución de validaciones locales y creación de este informe.  
**No realizado:** implementación, deploy, commit, cambios de código, cambios sobre documentación cerrada externa, uso de Firebase producción.

## 1. Resumen ejecutivo

El repo actual contiene una app Android Kotlin/Compose con Firebase Auth, Firestore y Cloud Functions. La base no es un mock vacío: existen flujos reales V1 para usuario público, creación de pedidos, tracking, operación Admin, Local/Store y Repartidor/Driver sobre `/orders`.

El núcleo de Pedido Vivo existe en forma parcial-operativa: nacimiento por Functions, estados, `version`, `nextAllowedActions`, `responsibleRole/currentResponsibleRole`, eventos, incidencias básicas, idempotencia en nacimiento y en acciones operativas, reglas Firestore que bloquean escrituras directas de cliente sobre pedidos, y operación transaccional desde backend.

La app todavía no implementa el producto completo cerrado documentalmente. Las mayores brechas reales están en configuración persistente Admin, alta real de roles, pagos/finanzas, WhatsApp/chat/notificaciones, IA, métricas/salud, timeouts/fallbacks ejecutables, archivo/cierre financiero completo, reclamos públicos persistentes y release productivo.

**Dictamen:** **B) REPO LISTO PARA INICIAR ETAPA Q CON RIESGOS CONTROLADOS.**

No hay bloqueos críticos para iniciar Q: el repo compila, tests/guards pasan y Git está limpio. Sí hay riesgos importantes normales y ya previstos por el plan: placeholders visuales, módulos V1 parciales y dependencia Firebase sensible.

## 2. Identificación del repo

| Ítem | Estado real |
|------|-------------|
| Rama actual | `main` |
| Commit HEAD | `50c51fa92bfd761e24c05cde7bd5c7238265c397` |
| Estado Git inicial | limpio |
| Estado Git tras auditoría | untracked esperado: este archivo nuevo y carpeta nueva `documentacion-generada-pedilo/` |
| Repo raíz | `/home/oem/Desktop/pedilo` |
| Documentación cerrada | no estaba dentro del repo; fue encontrada en `/home/oem/Desktop/documentos pedilo/documentacion-generada-pedilo/` |

Estructura general detectada:

| Ruta | Contenido |
|------|-----------|
| `app/` | Android app Kotlin/Compose |
| `functions/` | Firebase Cloud Functions Node.js |
| `tests/` | 20 tests Node sobre contrato/código/reglas/guards |
| `tools/` | scripts de catálogo y guards |
| `reports/` | bitácoras técnicas previas |
| `firestore.rules` | reglas Firestore |
| `firestore.indexes.json` | índices |
| `firebase.json` | configuración Firebase Functions/Firestore |
| `.firebaserc` | proyecto default configurado: `pediloapp-e2758` |
| `app/google-services.json` | configuración Firebase Android presente localmente |

Tecnología detectada:

- Android Gradle Plugin `8.7.3`.
- Kotlin `2.0.21`.
- Jetpack Compose con Material 3.
- Firebase Android: Auth, Firestore, Functions.
- Cloud Functions v2 Node `20`, región `southamerica-east1`.
- Firebase Admin `^13.6.0`, Firebase Functions `^6.4.0`.
- Tests Node nativos con `node --test`.

Módulos detectados:

- Público: `app/src/main/java/com/pedilo/app/ui/publicuser/`.
- Admin: `app/src/main/java/com/pedilo/app/ui/admin/`.
- Store/Local: `app/src/main/java/com/pedilo/app/ui/store/`.
- Driver/Repartidor: `app/src/main/java/com/pedilo/app/ui/driver/`.
- Core: modelos, puertos, use cases, adapters Firebase en `app/src/main/java/com/pedilo/app/core/`.
- Backend: `functions/index.js`.

Señales de legacy/duplicado/placeholders:

- `adminOrderAction` convive con `operateLiveOrder`; parece acción Admin legacy/dedicada junto al motor operativo unificado.
- Admin Configuración y Alta de roles son visuales/no persistentes.
- Reclamo público en `PublicConventions.kt` usa estado local `sent`, no colección real de claims.
- `TeamRolePlaceholder` sigue existiendo como ruta intermedia, aunque Store/Driver ya tienen apps operativas.
- `tools/seed_public_catalog.js` y `tools/verify_public_catalog.js` son scripts de carga/verificación, no runtime; deben tratarse como herramientas sensibles.

## 3. Inventario general de la app

Android:

- Existe app Android real.
- `MainActivity` inicia `PublicApp()`.
- `PediloApp` es `Application` vacía.
- No se detectaron flavors ni módulos Gradle adicionales.
- Existe APK debug generable.

Kotlin / Compose:

- 78 archivos Kotlin bajo `app/src/main/java/com/pedilo/app`.
- UI Compose para público, Admin, Store y Driver.
- Material icons extended y Material 3.
- Arquitectura local con `core/model`, `core/port`, `core/usecase`, `core/firebase`, `core/runtime`.

Firebase:

- Firebase Auth usado para acceso interno.
- Firestore usado para catálogo, usuarios y pedidos.
- Functions callable usadas para creación/tracking/operación.
- `app/google-services.json` presente localmente. Es configuración sensible; no debe exponerse ni tocarse salvo necesidad explícita.

Cloud Functions:

- `createLocalOrder`.
- `createPlusOrder`.
- `getPublicOrderTracking`.
- `adminOrderAction`.
- `operateLiveOrder`.

Firestore Rules:

- Deny client writes sobre `/orders`, `/events`, `/incidents`, `/stores`, `/products`.
- Lectura pública de stores/products visibles.
- Lectura de órdenes sólo para operadores autenticados con rol válido.
- `/users` legible por propio usuario/Admin; escritura sólo Admin según rules, pero alta real desde UI no está implementada.

Tests:

- 20 archivos en `tests/`.
- Cubren flujos público/local/plus/tracking, Admin, Store, Driver, reglas, guards, contrato nacimiento, acciones backend y hardening visual.

Scripts:

- `tools/guards/check_architecture.sh`.
- `tools/guards/check_ui_quality.sh`.
- `tools/seed_public_catalog.js`.
- `tools/verify_public_catalog.js`.

Documentación:

- En repo: `README.md`, `Pedilo.concepto.md`, `reports/`.
- Documentación cerrada solicitada: fuera del repo, en `/home/oem/Desktop/documentos pedilo/documentacion-generada-pedilo/`.
- Este reporte crea la carpeta solicitada dentro del repo porque no existía.

Assets/configuraciones:

- Launchers Android XML/drawable/mipmap.
- `strings.xml`, `styles.xml`.
- `.firebaserc`, `firebase.json`, `firestore.indexes.json`, `firestore.rules`.

CI:

- No se detectó configuración CI en archivos listados (`.github/workflows`, GitLab CI, etc. no visibles en el repo auditado).

## 4. Estado real por rol

### A. Usuario público

Pantallas existentes:

- Home pública.
- Botón + (`PublicPlus.kt`).
- Tienda/catálogo (`PublicShop.kt`, `PublicShopSearch.kt`, `PublicShopSubcategory.kt`).
- Local (`PublicLocal.kt`).
- Tracking (`PublicShopTracking.kt`).
- Convenciones/reclamo visual (`PublicConventions.kt`).
- Login de equipo interno desde público (`PublicTeamAccess.kt`).

Flujos existentes:

- Catálogo leído desde Firestore: `/stores` visibles y `/stores/{storeId}/products` visibles/disponibles.
- Pedido local creado vía `createLocalOrder`.
- Pedido Botón + creado vía `createPlusOrder`.
- Tracking público vía `getPublicOrderTracking`.
- Validaciones cliente para nombre, teléfono, dirección, productos, placeholder values.
- Validaciones backend equivalentes para payloads públicos.

Pedidos reales o mock:

- La creación de pedidos es real vía Cloud Functions y persiste en `/orders`.
- Catálogo depende de datos reales/seed en Firestore.
- Reclamo público no es real: sólo marca `sent` en UI.

Tracking:

- Real por callable y consulta a `/orders` por `trackingNumber` o `publicOrderNumber`.
- Devuelve estado público, resumen, tipo, cierre.

Botón +:

- Real para `buy` y `pickup_shipping`.
- Crea pedido con source `public_plus_buy` o `public_plus_pickup_shipping`.
- No tiene IA ni WhatsApp real.

Tienda/Local/carrito/ticket:

- Tienda y local están conectados a catálogo read-only.
- Carrito y confirmación existen en UI.
- Ticket público devuelve número/estado/store desde Function.

Qué funciona:

- Crear pedidos públicos.
- Consultar tracking.
- Validar inputs básicos.
- Navegar catálogo/local/Botón +.

Qué es placeholder o visual:

- Reclamos posteriores.
- Textos de WhatsApp como campo de contacto, sin validación WhatsApp real.
- Rutas/estados informativos no respaldados por módulos completos de comunicación/claims.

Qué está incompleto:

- Cancelación pública por estado.
- Reclamos persistentes.
- Validación de teléfono por WhatsApp.
- Chat/notificaciones.
- Pago real y comprobantes.

### B. Admin

Login:

- Real vía Firebase Auth.
- Rol resuelto desde `/users/{uid}` por `FirebaseTeamAccessAdapter`.

Operación/pedidos/intervención/auditoría:

- Admin lee pedidos reales desde `/orders`.
- Observa órdenes con snapshot listener.
- Lee detalle y eventos.
- Ejecuta acciones por callable `operateLiveOrder`.
- También existe callable `adminOrderAction` para acciones Admin dedicadas.
- Eventos se guardan en `/orders/{id}/events`.
- Incidencias Admin se guardan en `/orders/{id}/incidents` para marcar incidencia.

Configuración/alta de roles:

- Pantallas extensas existen.
- Son visuales/no persistentes; el propio README y UI indican que no modifican configuración ni roles reales.

Qué funciona:

- Mesa operativa real sobre pedidos.
- Clasificación visual/operativa de pedidos.
- Acciones como intervenir, abrir/resolver incidencia, cancelar, forzar estados permitidos según backend.

Qué es placeholder:

- Configuración persistente.
- Alta de roles/vinculaciones reales.
- Métricas/salud/configuración de canales.

Qué falta:

- CRUD real `/users` desde Admin.
- Config versionada en Firestore.
- Auditoría administrativa completa consultable como módulo.
- Panel de salud real.
- Gestión financiera real.

### C. Local / Store

Pantallas:

- `StoreApp.kt` existe y ya no es placeholder.
- Listado de pedidos propios.
- Detalle de pedido.
- Acciones operativas permitidas.

Pedidos entrantes:

- Store observa `/orders` con `storeId == auth.currentUser.uid`.
- Esto implica acoplamiento fuerte: el `storeId` del pedido debe coincidir con UID del usuario store.

Aceptar/rechazar/preparación:

- Acciones reales vía `operateLiveOrder`: aceptar, rechazar, marcar preparación, marcar listo, abrir incidencia.

Productos/stock:

- Catálogo público existe como Firestore read-only.
- No existe módulo Store para administrar productos, variantes, extras o stock.

Finanzas:

- No existe cierre financiero ni caja local.
- Sólo se muestran/importan importes simples cuando están en pedido.

Qué funciona:

- Operación V1 del pedido propio.
- Lectura de pedidos propios.
- Transiciones Store básicas.

Qué es placeholder/incompleto:

- Gestión de productos/stock.
- Configuración local.
- Finanzas.
- Solicitud explícita `store_driver_request` no existe.

### D. Repartidor / Driver

Pantallas:

- `DriverApp.kt` existe y ya no es placeholder.
- Lista pedidos disponibles/asignados.
- Detalle.
- Acciones operativas.

Disponibles/asignados:

- Disponibles: `responsibleRole == "driver"` y `assignedActorId == ""`.
- Asignados: `driverId == uid`.

Tomar/retiro/entrega:

- Acciones reales vía `operateLiveOrder`: tomar pedido, marcar retirado, marcar entregado, abrir incidencia, cancelar.

Problemas:

- Puede abrir incidencia.
- Resolución de incidencia queda restringida por backend a Admin en la práctica; la UI Driver filtra acciones propias.

Finanzas/cierre:

- No existe caja/recaudación/deuda/bloqueo financiero.

Qué funciona:

- Tomar pedido listo para retiro.
- Marcar retiro y entrega.
- Ver datos de contacto/dirección si tiene permiso de lectura.

Qué falta:

- Cierre de caja.
- Cobro en entrega.
- Capacidad configurable.
- Bloqueo financiero.
- Finanzas del repartidor.

## 5. Estado real del Pedido Vivo

Modelo actual:

- Colección principal: `/orders`.
- Subcolecciones: `/orders/{id}/events`, `/orders/{id}/incidents`.
- Campos de nacimiento: `orderType`, `source`, `status`, `publicStatus`, `operationalStatus`, `financialStatus`, `communicationStatus`, `incidentStatus`, `archiveStatus`, `currentResponsibleRole`, `responsibleRole`, `assignedActorId`, `assignedActorRole`, `driverId`, `priority`, `needsAttention`, `activeIncident`, `adminReviewed`, `nextAllowedActions`, `liveSnapshot`, `initialSnapshot`, `timeoutPolicy`, `fallbackPolicy`, `version`, `idempotencyKey`, `trackingNumber`, `publicOrderNumber`.

Estados existentes:

- Inicial: `created`.
- Operativos: `accepted`, `preparing`, `ready_for_pickup`, `assigned_to_driver`, `picked_up`, `delivered`.
- Terminales reconocidos: `cancelled`, `canceled`, `delivered`, `closed`, `archived`.
- Admin forceable: `created`, `preparing`, `on_the_way`, `delivered`, `under_review`.

Transiciones reales:

- Local acepta/rechaza.
- Local marca preparando/listo.
- Driver toma/retira/entrega.
- Admin/store/driver cancelan según permisos.
- Admin/store/driver abren incidencia según permisos.
- Admin resuelve incidencia/interviene.

Eventos:

- Nacimiento crea evento inicial.
- Acciones operativas crean evento con tipo, summary, actor, estados previo/siguiente, versiones y resultado.
- `lastOperationEvent` se actualiza en pedido.

Incidencias:

- `OPEN_INCIDENT` crea documento en `/incidents`.
- `MARK_INCIDENT` Admin también crea incidencia.
- No hay flujo documental completo de tipos/tiempos/reclamos públicos.

`responsibleRole` / `nextAllowedActions`:

- Existen y gobiernan visibilidad/acciones.
- `currentResponsibleRole` también existe.
- `nextAllowedActions` se recalcula en backend.

Auditoría:

- Existe auditoría mínima por eventos.
- No existe módulo Admin completo de auditoría/salud, ni export, ni panel de eventos global.

Snapshots:

- `liveSnapshot` e `initialSnapshot` existen.
- Snapshot financiero/tarifario completo no existe; pricing actual es subtotal/total/paymentMethod simple.

Idempotencia:

- Nacimiento usa doc id por hash estable de payload.
- Acción operativa usa `actionId` hash y `tx.create(eventRef)`; si existe, devuelve resultado idempotente.

Concurrencia:

- `operateLiveOrder` usa transacción y exige `expectedVersion`.
- `adminOrderAction` también usa transacción y admite `expectedVersion`, pero el adapter Admin legacy no envía `expectedVersion`; la ruta live sí lo hace.

Timeouts/fallbacks:

- Existen `timeoutPolicy` y `fallbackPolicy` iniciales declarativos.
- No hay worker/scheduler que ejecute timeouts ni escaladas.

Cierre/archivo:

- Entrega marca `archiveStatus: "archived"` y `communicationStatus: "closed"`.
- No hay cierre financiero, archivo histórico separado ni proceso de archivado.

Qué está real:

- Contrato V1, estado vivo, acciones, eventos, incidencias básicas, idempotencia, concurrencia.

Qué está parcial:

- Ejes financiero/comunicación/incidencia/archivo.
- Snapshots.
- Auditoría.
- Admin legacy junto a operación live.

Qué falta:

- Motor autónomo de timeouts/fallbacks.
- Estados financieros vivos.
- Cierre de caja.
- Reclamos públicos.
- Archivo final completo.
- Salud/alertas de pedidos trabados.

## 6. Backend / Firebase / Seguridad

Cloud Functions existentes:

| Function | Estado |
|----------|--------|
| `createLocalOrder` | real, pública callable, valida local/productos visibles |
| `createPlusOrder` | real, pública callable, valida payload |
| `getPublicOrderTracking` | real, pública callable |
| `adminOrderAction` | real pero parece camino Admin dedicado/legacy |
| `operateLiveOrder` | real, unificada para Admin/Store/Driver |

Estructura de colecciones usada:

- `/users/{uid}`.
- `/stores/{storeId}`.
- `/stores/{storeId}/products/{productId}`.
- `/orders/{orderId}`.
- `/orders/{orderId}/events/{eventId}`.
- `/orders/{orderId}/incidents/{incidentId}`.

Validaciones backend:

- Payload público limpio y anti-placeholder.
- Teléfono por regex básica, no WhatsApp real.
- Store/product visible/available.
- Roles operativos activos desde `/users`.
- Permisos por rol en `validateLiveActor`.
- Versionado esperado en acciones live.

Transacciones:

- Creación de pedido usa transacción.
- Acciones Admin/live usan transacciones.

Permisos por rol:

- Rules permiten lectura a operadores según Admin, storeId, driverId o driver disponible.
- Escritura directa cliente bloqueada para órdenes/eventos/incidencias.

Escritura desde cliente:

- Pedidos se crean/operan por Functions.
- Catálogo/stores/products no se escriben desde cliente.
- `/users` writable por Admin según rules, pero no hay UI CRUD real.

Puntos inseguros/riesgos:

- `.firebaserc` apunta a proyecto real default `pediloapp-e2758`; cualquier deploy accidental sería de riesgo, aunque no se ejecutó deploy.
- `app/google-services.json` está presente; tratar como configuración sensible.
- No se detecta configuración de emuladores en app/functions.
- Store identity depende de que UID de usuario coincida con `storeId`.
- Admin adapter puede leer todos los pedidos; correcto para rol Admin, sensible si rules/roles están mal poblados.

Emulators:

- No se detectó `useEmulator`, `connectFirestoreEmulator`, `connectFunctionsEmulator` ni configuración explícita de emuladores.

Tests backend:

- Node tests validan estructura de `functions/index.js`, reglas y contratos.
- No son tests de integración contra emulador Firestore/Functions real.

Uso de producción:

- No se usó producción durante auditoría.
- El repo contiene proyecto Firebase default y config Android, por lo que el riesgo de usar producción existe si se ejecutan scripts/deploy sin entorno controlado.

## 7. Pagos / Finanzas

Existe:

- `PaymentMethod` en core.
- Métodos wire: `cash`, `card`, `transfer`, vacío.
- `paymentMethod`, `subtotal`, `total` en pedidos locales.
- En plus: `paymentMethod`, `amount`.
- `financialStatus: "pending_review"` al nacimiento.

No existe:

- Pago real.
- Pasarela.
- Validación de transferencia.
- Comprobantes.
- Tarifa base configurable.
- Modo lluvia.
- Zona extra.
- Cierre de caja repartidor.
- Deuda.
- Bloqueo financiero.
- Snapshot financiero completo/versionado.
- Estados financieros vivos más allá de `pending_review`.

Riesgo:

- El pedido puede cerrarse como entregado sin cierre financiero ni caja.

## 8. Comunicación / WhatsApp / Chat / Notificaciones

Existe:

- Campos de teléfono/WhatsApp en UI pública.
- Validación sintáctica de teléfono.
- `communicationStatus` inicial `received` y cierre `closed` al entregar.
- Secciones visuales Admin relacionadas a comunicación/notificaciones.

No existe:

- WhatsApp real/API.
- Validación de teléfono por WhatsApp.
- Cola de mensajes.
- Mensajes automáticos reales.
- Chat interno por pedido.
- FCM/notificaciones push.
- Fallbacks operativos de comunicación.

Riesgo:

- El producto documental presupone comunicación operativa, pero el repo actual sólo conserva datos/copy visual y campos declarativos.

## 9. IA

Existe:

- No se detectó invocación real a IA, LLM, OpenAI u otro proveedor.
- No hay permisos de IA ni capacidad de modificar estado.

Estado:

- IA no implementada.
- Por ahora no representa riesgo de autoridad indebida porque no existe runtime.

Falta:

- Asistencia controlada sin autoridad.
- Pipeline de sugerencias.
- Auditoría de sugerencias.
- Límites explícitos de permisos.

## 10. Métricas / Auditoría / Salud

Existe:

- Eventos por pedido.
- `lastOperationEvent`.
- Estados de atención/prioridad.
- Tests/guards locales.
- UI Admin con categorías visuales de métricas/auditoría/salud.

No existe:

- Agregación de métricas.
- Panel real de salud backend.
- Alertas.
- Detector de pedidos trabados.
- Métricas de latencia/error.
- Dashboard de colas, WhatsApp, IA, notificaciones.
- Logging estructurado de negocio más allá de documentos de eventos.

Riesgo:

- Sin L no hay visibilidad operacional para producción o carga alta.

## 11. Tests y validaciones ejecutadas

| Comando | Resultado |
|---------|-----------|
| `git branch --show-current` | `main` |
| `git rev-parse HEAD` | `50c51fa92bfd761e24c05cde7bd5c7238265c397` |
| `git status --short` inicial | limpio |
| `node --test tests/*.test.js` | OK: 20/20 tests pasan |
| `bash tools/guards/check_architecture.sh` | OK: `architecture guard passed` |
| `bash tools/guards/check_ui_quality.sh` | OK: `ui quality guard passed` |
| `npm --prefix functions run build` | OK: `node --check index.js` |
| `./gradlew assembleDebug --offline` | OK: build successful, 37 tareas |
| `./gradlew lintDebug --offline` | OK: build successful, reporte en `app/build/reports/lint-results-debug.html` |

Advertencias/incidencias:

- Primer intento de `./gradlew assembleDebug --offline` falló por sandbox: Gradle intentó escribir lock en `/home/oem/.gradle/...gradle-8.9-bin.zip.lck`, fuera del filesystem writable. Se reejecutó con permiso escalado local; pasó.
- `lintDebug` también requirió ejecución escalada por la misma razón de Gradle cache/locks.
- No se ejecutaron deploys.
- No se ejecutaron scripts seed/verify contra Firebase.
- No se ejecutaron emuladores porque no hay configuración explícita detectada y el pedido prohibió tocar producción.

## 12. Comparación contra brechas BR-01 a BR-23

| Brecha | Estado repo actual | Archivos relacionados | Riesgo | Etapa |
|--------|--------------------|----------------------|--------|-------|
| BR-01 `store_driver_request` | No cubierto. No existe 4º tipo operativo específico. | `functions/index.js`, `StoreApp.kt`, `LiveOrderContract.kt` | Local sin solicitud documentada de repartidor. | D |
| BR-02 5 ejes completos | Parcial. Existen campos financiero/comunicación/incidencia/archivo, pero sin motor completo. | `functions/index.js`, `AdminOrderReadModels.kt`, adapters | Estados/cierre incompletos si se opera más allá de V1. | B |
| BR-03 timeouts/fallbacks | Parcial declarativo. No hay scheduler/worker. | `functions/index.js` | Pedidos trabados sin salida automática. | B |
| BR-04 snapshots modos/tarifas | Parcial. `liveSnapshot` existe, pricing simple. | `functions/index.js` | Disputa de precios/tarifas futuras. | G |
| BR-05 Admin Config persistente | No cubierto. UI visual. | `AdminApp.kt` | Config no gobierna backend. | F |
| BR-06 Alta roles CRUD | No cubierto. UI visual/no persistente. | `AdminApp.kt`, `RoleAccessData.kt`, `FirebaseTeamAccessAdapter.kt` | No operar equipo real desde Admin. | F |
| BR-07 cancelación pública | No cubierto. | `PublicConventions.kt`, `functions/index.js` | Cliente sin salida regulada. | C |
| BR-08 reclamos backend | No cubierto para público. UI marca `sent`. | `PublicConventions.kt` | Reclamos falsos/no auditados. | I |
| BR-09 validación WhatsApp | No cubierto. Sólo teléfono básico. | `PublicInputs.kt`, `PublicLocal.kt`, `PublicPlus.kt`, `functions/index.js` | Teléfono no validado operacionalmente. | J |
| BR-10 pagos/cobro entrega | No cubierto salvo campos simples. | `PaymentMethod.kt`, `functions/index.js`, adapters | Entrega sin cierre financiero. | G |
| BR-11 cierre caja repartidor | No cubierto. | `DriverApp.kt`, `functions/index.js` | Deuda/recaudación sin control. | G |
| BR-12 capacidad repartidor | No cubierto. | `FirebaseDriverOrdersAdapter.kt`, `functions/index.js` | Sobrecarga/doble toma no modelada por capacidad. | E |
| BR-13 WhatsApp/chat/notificaciones | No cubierto. | `AdminApp.kt`, UI pública | Comunicación manual only. | J |
| BR-14 IA controlada | No cubierto. | Sin runtime detectado | Sin asistencia IA; sin riesgo de autoridad por ahora. | K |
| BR-15 métricas/salud | No cubierto real. Visual parcial Admin. | `AdminApp.kt`, `events` | Admin sin visibilidad sistémica. | L |
| BR-16 modos lluvia/saturación/mantenimiento | No cubierto real. Visual/config parcial. | `AdminApp.kt` | Tarifas/modos no gobiernan pedidos. | F/G |
| BR-17 variantes/extras/stock | Parcial mínimo. Productos simples con `available`. | `FirebasePublicCatalogAdapter.kt`, `functions/index.js` | Catálogo limitado. | D |
| BR-18 incidencias completas | Parcial V1. | `functions/index.js`, Admin/Store/Driver adapters | Excepciones sin tiempos/tipos completos. | I |
| BR-19 placeholders producción | Parcial: identificados, aún activos visualmente. | `AdminApp.kt`, `PublicConventions.kt`, `tools/seed_public_catalog.js` | Riesgo de demo/visual en producción. | Q/O |
| BR-20 carga 1000 pedidos | No cubierto. | Tests actuales | Riesgo de rendimiento desconocido. | O |
| BR-21 JUnit Android | No cubierto. No se detectaron tests JUnit Android. | `app/` | Regresión UI no cubierta por JUnit. | O |
| BR-22 release certificado | Parcial debug. `assembleDebug` OK; no AAB firmado. | Gradle config | Publicación insegura/incompleta. | P |
| BR-23 privacidad legal | No técnico en repo. Decisión externa/documental. | Docs cerradas | Bloquea Play, no Q. | P |

## 13. Riesgos antes de implementar Etapa Q

Riesgos de tocar el repo:

- Tests/guards son parte del contrato vivo; cualquier cambio en nombres, copy operativo o arquitectura puede romperlos.
- `functions/index.js` concentra mucha lógica crítica; cambios amplios pueden romper nacimiento, roles y operación.
- `AdminApp.kt` es grande y mezcla operación real con secciones visuales; alto riesgo de confundir shell con backend real.
- Store/Driver dependen de `nextAllowedActions` y `expectedVersion`; romper wire names rompe operación.
- Rules bloquean escrituras directas; cualquier implementación debe pasar por Functions o ajustar Rules con tests.

Zonas frágiles:

- `functions/index.js`.
- `firestore.rules`.
- `FirebaseAdminOrdersAdapter.kt`, `FirebaseStoreOrdersAdapter.kt`, `FirebaseDriverOrdersAdapter.kt`.
- `AdminApp.kt`.
- `PublicConventions.kt` por reclamo visual.
- `tools/guards/`.

Código legacy/duplicados:

- `adminOrderAction` vs `operateLiveOrder`.
- `AdminOrderOperations.kt` local vs cálculo backend de acciones.
- Placeholders/rutas de transición de roles.

Archivos que no deben tocarse sin motivo explícito:

- Documentación cerrada externa en `/home/oem/Desktop/documentos pedilo/documentacion-generada-pedilo/`.
- `.firebaserc`.
- `app/google-services.json`.
- `firestore.rules` sin tests.
- `tools/seed_public_catalog.js` si no hay entorno controlado.
- `functions/index.js` sin suite completa.

Dependencias ocultas:

- UID de Firebase Auth usado como `storeId`/`driverId`.
- Proyecto default Firebase configurado.
- Gradle depende de cache local para `--offline`.
- Tests inspeccionan texto/código con regex; refactors cosméticos pueden fallar.

Riesgo Firebase:

- Alto si se ejecutan deploy/seed contra default.
- Controlado para Q si se mantiene auditoría/local sin deploy.

Riesgo Android:

- Build debug pasa.
- No hay suite JUnit/Instrumented real.
- MainActivity sólo lanza público; roles internos dependen del login/navegación dentro de `PublicApp`.

Riesgo de romper usuario público:

- Medio. Público ya crea pedidos reales por Functions; cambios en validación, payload o catalog adapters pueden romper el flujo central.

## 14. Dictamen técnico final

**B) REPO LISTO PARA INICIAR ETAPA Q CON RIESGOS CONTROLADOS.**

Justificación:

- Git estaba limpio antes de crear este reporte.
- HEAD coincide con el plan documental de referencia.
- Tests Node, guards, Functions syntax, Android build y lint pasan.
- Las brechas importantes son reales, pero coinciden con el plan de implementación y no impiden comenzar Q.
- No se detectó bloqueo crítico que obligue a resolver feature antes de Q.
- No debe usarse opción A porque hay riesgos importantes: placeholders visuales, Firebase default, ausencia de emuladores, módulos financieros/comunicación/IA/salud no implementados.
- No debe usarse opción C porque las ausencias principales son brechas normales ya previstas por el plan.

## 15. Primera acción recomendada para Etapa Q

Crear la certificación Q formal partiendo de este baseline:

1. Congelar inventario técnico Q1 con este HEAD y resultados de validación.
2. Catalogar explícitamente placeholders Q6: Admin Configuración, Alta de roles, reclamo público, scripts seed y rutas de transición.
3. Definir matriz conservar/ajustar/reemplazar para `functions/index.js`, `AdminApp.kt`, `PublicConventions.kt`, adapters operativos, rules y guards.
4. Mantener como validación obligatoria de Q: `node --test tests/*.test.js`, ambos guards, `npm --prefix functions run build`, `./gradlew assembleDebug --offline` y `./gradlew lintDebug --offline`.
5. No avanzar a B hasta que Q deje claro qué piezas V1 se conservan y cuáles se aislan antes de tocar Pedido Vivo.
