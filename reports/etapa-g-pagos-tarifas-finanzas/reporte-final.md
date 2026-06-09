# Bloque G - Pagos / Tarifas / Finanzas

## Rama y HEAD inicial

- Rama: `main`
- HEAD inicial: `bb4f3ed1994a1de202bba3e55651d903ea2f1fb9`
- Estado inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

- Se implemento contrato financiero minimo persistente dentro del Pedido Vivo.
- Se normalizaron metodo de pago, estado financiero, subtotal, total, monto a cobrar y snapshot financiero.
- Se rechazo tarjeta por no existir pasarela externa activa.
- Se expuso informacion financiera minima y segura para Publico, Admin, Store y Driver.
- Caja, deuda, rendicion, bloqueo financiero, comprobantes y conciliacion bancaria quedaron explicitamente fuera de alcance.

## Archivos modificados

- `functions/index.js`
- `app/src/main/java/com/pedilo/app/core/model/LiveOrderContract.kt`
- `app/src/main/java/com/pedilo/app/core/model/AdminOrderReadModels.kt`
- `app/src/main/java/com/pedilo/app/core/model/StoreOrderModels.kt`
- `app/src/main/java/com/pedilo/app/core/model/DriverOrderModels.kt`
- `app/src/main/java/com/pedilo/app/core/model/PublicTrackingState.kt`
- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseAdminOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseStoreOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseDriverOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/firebase/FirebasePublicTrackingAdapter.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `app/src/main/java/com/pedilo/app/ui/store/StoreApp.kt`
- `app/src/main/java/com/pedilo/app/ui/driver/DriverApp.kt`
- `app/src/main/java/com/pedilo/app/ui/publicuser/PublicShopTracking.kt`
- `tests/live_order_core_alignment.test.js`
- `tests/public_tracking_flow.test.js`
- `tests/driver_operation_alignment.test.js`
- `tests/store_operation_alignment.test.js`

## Archivos creados

- `tests/financial_workflow_baseline.test.js`
- `reports/etapa-g-pagos-tarifas-finanzas/reporte-final.md`

## Contrato financiero

- Se agregaron estados financieros minimos: `pending_review`, `pending_payment`, `collect_on_delivery`, `transfer_declared_pending`, `paid_declared`, `confirmed_internal`, `rejected`, `disputed`, `settlement_pending`, `settled`.
- Se persisten `paymentMethod`, `subtotal`, `deliveryFee`, `extraFees`, `discounts`, `total`, `amountToCollect`, `collectedAmount`, `collectionRequired`, `cashResponsibleRole`, `cashResponsibleActorId`, `financialSnapshot`, `financialUpdatedAt` y `financialNotes`.
- El estado financiero no reemplaza el estado operativo del Pedido Vivo.

## Tarifas / snapshot

- Pedido local calcula subtotal desde productos visibles y disponibles.
- Boton + parsea el importe publico a centavos y lo guarda como subtotal/total.
- `deliveryFee`, extras y descuentos quedan en cero/listas vacias hasta tener base segura.
- `financialSnapshot` queda persistido al crear el pedido y no depende de calculo UI posterior.
- Montos negativos o no enteros se rechazan.

## Metodos de pago

- `cash` genera `collect_on_delivery`, `collectionRequired=true` y `amountToCollect=total`.
- `transfer` genera `transfer_declared_pending` y nota de no validacion bancaria.
- `already_paid` genera `paid_declared`, sin confirmacion externa.
- `card` se rechaza porque no hay pasarela activa.
- Metodo desconocido se rechaza.

## Cobro Driver

- Driver ve total, metodo, estado financiero y monto a cobrar si `collectionRequired=true`.
- El responsable de cobro minimo queda como rol `driver`, sin actor asignado hasta operacion posterior.
- No se registra cobro real ni cierre financiero al entregar en este bloque.

## Caja / recaudacion / deuda / bloqueo

- No se implemento caja real, deuda, rendicion ni bloqueo financiero persistente.
- Driver y Store lo muestran como fuera de alcance/no persistente.
- No se bloquea toma de pedidos por finanzas porque no existe regla persistente segura.

## Admin

- Admin ve estado financiero, metodo, total, monto a cobrar, cobro requerido, responsable de cobro y nota financiera.
- No puede aprobar banco, conciliacion ni comprobantes reales.

## Store

- Store ve metodo, estado financiero, total y cobro al recibir si aplica.
- No ve caja/deuda/rendicion del Driver.

## Usuario publico

- Tracking publico muestra total publico, etiqueta de pago y mensaje de cobro seguro.
- No expone deuda, caja, rendicion, auditoria, responsable interno ni snapshot financiero.
- Transferencia se muestra como declarada/pendiente, no validada automaticamente.

## Fuera de alcance

- Pasarela externa, Mercado Pago, Stripe, MODO, banco, validacion bancaria, OCR, IA.
- WhatsApp, chat, notificaciones, metricas avanzadas, contabilidad completa, facturacion fiscal, Google Play.
- Deploy, seed, Firebase produccion, `.firebaserc`, `app/google-services.json`.
- Caja/recaudacion real, deuda, bloqueo financiero y comprobantes aprobados.

## Tests agregados/modificados

- Agregado `tests/financial_workflow_baseline.test.js`.
- Actualizados `tests/live_order_core_alignment.test.js`, `tests/public_tracking_flow.test.js`, `tests/driver_operation_alignment.test.js`, `tests/store_operation_alignment.test.js`.

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

- `git status --short`: cambios esperados del bloque G.
- `node --test tests/*.test.js`: pasa, 27/27 archivos.
- `npm --prefix functions run build`: pasa.
- `bash tools/guards/check_architecture.sh`: pasa.
- `bash tools/guards/check_ui_quality.sh`: pasa.
- `./gradlew assembleDebug --offline`: pasa con elevacion por lock de `~/.gradle`.
- `./gradlew lintDebug --offline`: pasa.
- `git diff --check`: pasa.

## Riesgos pendientes

- No hay pasarela externa ni validacion bancaria real.
- Caja, deuda, rendicion y bloqueo financiero quedan pendientes hasta tener contrato backend especifico.
- Comprobantes reales y conciliacion quedan fuera de alcance.

## Dictamen final

BLOQUE G COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Bloque posterior de caja/rendicion/bloqueo financiero o comprobantes, solo con contrato backend seguro especifico.
