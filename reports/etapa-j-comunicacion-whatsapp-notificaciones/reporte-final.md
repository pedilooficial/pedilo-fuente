# Bloque J - Comunicacion, WhatsApp y Notificaciones

## Rama y HEAD inicial

- Rama inicial: `main`
- HEAD inicial: `f0e66b5d63cca0160c826791b53bcd90c0fea77b`
- Estado inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

Se implemento el contrato minimo real de comunicacion del Pedido Vivo. Los eventos operativos, reclamos, incidencias, resoluciones y cancelaciones registran comunicaciones persistentes y auditadas. Los canales reales externos quedan honestamente `disabled` cuando no existe proveedor seguro configurado.

## Archivos modificados

- `functions/index.js`
- `firestore.rules`
- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseStoreOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseDriverOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/model/StoreOrderModels.kt`
- `app/src/main/java/com/pedilo/app/core/model/DriverOrderModels.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `app/src/main/java/com/pedilo/app/ui/store/StoreApp.kt`
- `app/src/main/java/com/pedilo/app/ui/driver/DriverApp.kt`
- `tests/live_order_birth_contract.test.js`

## Archivos creados

- `tests/communication_notification_block_j.test.js`
- `reports/etapa-j-comunicacion-whatsapp-notificaciones/reporte-final.md`

## Contrato de comunicacion

Las comunicaciones quedan en `/orders/{orderId}/communications/{communicationId}` y, para reclamos, en `/public_claims/{claimId}/communications/{communicationId}`. El documento incluye `communicationId`, `orderId`, `claimId`, `incidentId`, `eventType`, `channel`, `targetRole`, `targetUserId`, `targetPhone`, `status`, `messageType`, `templateKey`, `messageBody`, timestamps, actor disparador, evento fuente y `publicSafe`.

## communicationStatus

`communicationStatus` ahora admite `received`, `pending`, `prepared`, `sent`, `failed`, `closed` y `disabled`. Los pedidos nacen con comunicacion `prepared`; eventos operativos relevantes vuelven a `prepared`; cierre entregado conserva `closed`.

## Plantillas

Se centralizaron plantillas seguras en backend para pedido recibido, aceptado, preparacion, listo, asignado, en camino, cerrado, cancelado, revision, incidencia abierta/resuelta, reclamo recibido, comunicacion fallida y validacion telefonica preparada.

## WhatsApp

No se implemento envio real porque no hay proveedor seguro configurado. El canal `whatsapp` queda registrado como `disabled`, sin `sentAt`, con motivo auditable. No se agregaron credenciales, tokens ni secretos.

## Notificaciones internas

Se registran comunicaciones internas `prepared` para Admin y, cuando corresponde, Store y Driver. Son in-app/read-only y no fingen push real.

## Exposicion por rol

- Admin ve estado de comunicacion en resumen operativo y puede leer comunicaciones por reglas.
- Store ve el estado de comunicacion de pedidos propios.
- Driver ve el estado de comunicacion de pedidos visibles/propios.
- Publico ve solo tracking seguro y reclamo recibido, sin proveedor, actor interno, UID ni fallos tecnicos crudos.

## Auditoria

Toda comunicacion preparada o deshabilitada queda persistida con canal, destino, estado, plantilla, actor, fuente, timestamp y motivo de deshabilitacion si aplica.

## Fuera de alcance

IA, automatizacion inteligente, metricas avanzadas, ranking, Google Play, deploy, seed, produccion, credenciales, secretos, proveedor externo improvisado, WhatsApp real, push/FCM real y validacion telefonica real.

## Tests agregados/modificados

- Agregado `tests/communication_notification_block_j.test.js`
- Actualizado `tests/live_order_birth_contract.test.js`

## Validaciones ejecutadas

- `node --test tests/*.test.js`
- `npm --prefix functions run build`
- `bash tools/guards/check_architecture.sh`
- `bash tools/guards/check_ui_quality.sh`
- `./gradlew assembleDebug --offline`
- `./gradlew lintDebug --offline`
- `git diff --check`

## Riesgos pendientes

No hay proveedor real seguro de WhatsApp ni canal FCM real configurado en este repo; por eso el dictamen no usa A. La lectura detallada de comunicaciones puede ampliarse en un bloque posterior con pantallas dedicadas, sin cambiar el contrato.

## Dictamen final

BLOQUE J COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Proximo bloque operativo posterior a comunicacion/notificaciones, sin produccion ni canales externos reales hasta que exista proveedor seguro.
