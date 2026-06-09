# Baseline real V1 - Pédilo

**Fecha:** 2026-06-09  
**Rama inicial:** `main`  
**HEAD inicial:** `50c51fa92bfd761e24c05cde7bd5c7238265c397`  
**Propósito:** registrar qué parte del repo actual es base V1 real y debe conservarse para continuar desarrollo por etapas.

## Base real conservada

| Área | Estado actual real | Base segura a conservar |
|------|--------------------|--------------------------|
| Usuario público | App Compose inicia en `PublicApp`; home, Botón +, Tienda, Local, tracking y login interno existen. | Conservar navegación pública y flujos conectados a Firebase. |
| Creación de pedidos | `createLocalOrder` y `createPlusOrder` crean documentos en `/orders` vía Cloud Functions. | Conservar nacimiento backend como única escritura pública de pedidos. |
| Tracking público | `getPublicOrderTracking` consulta `/orders` por `trackingNumber` o `publicOrderNumber`. | Conservar tracking público real y sin login público. |
| Admin operativo | Admin lee pedidos, observa snapshots, consulta detalle/eventos y opera vía callable. | Conservar mesa operativa V1; no confundir con configuración real. |
| Store/Local operativo | Store observa pedidos propios por `storeId == uid` y ejecuta acciones permitidas. | Conservar operación V1 de local sobre pedido vivo. |
| Driver/Repartidor operativo | Driver ve disponibles/asignados y ejecuta tomar, retirar, entregar, incidencia/cancelación según permisos. | Conservar operación V1 de repartidor sobre pedido vivo. |
| Pedido operativo V1 | `/orders` contiene estado, ejes declarativos, versión, responsable, acciones permitidas, snapshots, eventos e incidencias. | Conservar contrato actual; no reescribir estados ni transiciones. |
| Firestore Rules | Bloquean escrituras directas de cliente sobre pedidos/eventos/incidencias y limitan lecturas por rol. | Conservar deny-client-write y permisos por rol. |
| Tests/guards/build | Suite Node, guards, Functions build, Android build y lint disponibles. | Conservar como validación mínima de continuidad. |

## Cloud Functions existentes

| Function | Rol en baseline |
|----------|-----------------|
| `createLocalOrder` | Crea pedido de local validando store/productos visibles. |
| `createPlusOrder` | Crea pedido Botón + para compra directa o retiro/envío. |
| `getPublicOrderTracking` | Devuelve tracking público por número visible. |
| `adminOrderAction` | Acción Admin dedicada/legacy; conservar sin ampliar. |
| `operateLiveOrder` | Motor operativo unificado para Admin, Store y Driver. |

## Firestore y colecciones usadas

- `/users/{uid}`: perfiles internos y rol operativo.
- `/stores/{storeId}`: locales visibles.
- `/stores/{storeId}/products/{productId}`: productos visibles/disponibles.
- `/orders/{orderId}`: pedido vivo principal.
- `/orders/{orderId}/events/{eventId}`: eventos operativos.
- `/orders/{orderId}/incidents/{incidentId}`: incidencias operativas V1.

## Validaciones disponibles

- `node --test tests/*.test.js`
- `npm --prefix functions run build`
- `bash tools/guards/check_architecture.sh`
- `bash tools/guards/check_ui_quality.sh`
- `./gradlew assembleDebug --offline`
- `./gradlew lintDebug --offline`
- `git diff --check`

## Decisión de baseline

La base V1 real queda conservada como punto seguro: usuario público, pedidos públicos, tracking, Admin operativo parcial, Store, Driver, Functions, Rules y tests/guards. Los módulos incompletos deben seguir marcados como parciales o placeholders hasta su etapa correspondiente.
