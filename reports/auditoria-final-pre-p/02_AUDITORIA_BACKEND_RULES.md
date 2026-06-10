# 02 - Auditoria Backend / Rules

## Backend Functions

Exports reales auditados:

- `createLocalOrder`
- `createPlusOrder`
- `getPublicOrderTracking`
- `submitPublicClaim`
- `adminOrderAction`
- `resolveAssistedDecision`
- `getOperationalHealth`
- `operateLiveOrder`

## Verificaciones backend

- Pedidos publicos nacen por callable, no por escritura directa Android.
- `createLocalOrder` valida store visible, productos visibles/disponibles, payload, telefono, placeholders y finanzas.
- `createPlusOrder` valida compra y retiro/envio, placeholders, telefono, pago y monto.
- `getPublicOrderTracking` normaliza tracking, valida formato y devuelve respuesta publica segura.
- `submitPublicClaim` crea `public_claims`, eventos, comunicaciones prepared/disabled y vincula claim a pedido sin mutar Pedido Vivo.
- `adminOrderAction` exige Admin activo, `expectedVersion`, acciones permitidas y motivo donde corresponde.
- `operateLiveOrder` exige rol activo, `expectedVersion`, actor autorizado, transicion permitida, `nextAllowedActions` e idempotencia por `actionId`.
- `resolveAssistedDecision` audita resolucion de sugerencia sin mutar estados operativos del pedido.
- `getOperationalHealth` exige Admin activo y calcula health sin escribir.

## Finanzas

- Efectivo queda como `collect_on_delivery`.
- Transferencia queda declarada pendiente.
- Pago declarado queda no confirmado.
- Tarjeta/pasarela real no disponible y se rechaza.
- Cancelaciones con impacto financiero marcan `financialReviewRequired`.

## Comunicacion

- WhatsApp y push quedan disabled sin proveedor.
- `sentAt` queda `null` en comunicaciones generadas.
- No se encontro envio real por FCM, WhatsApp, Twilio, Meta Graph ni proveedor externo.

## IA asistida

- Motor deterministico local.
- Proveedor externo disabled.
- Sugerencias no ejecutan acciones criticas.
- Admin puede resolver sugerencia como auditoria.

## Health

- Health es read model calculado Admin-only.
- Detecta inconsistencias y no corrige automaticamente.
- Modulos externos se muestran disabled/not_ready/not_implemented.

## Firestore Rules

Verificado:

- `/users`: lectura propia/Admin, writes Admin-only.
- `/stores`: lectura publica solo visible, writes bloqueados.
- `/stores/{storeId}/products`: lectura publica solo store visible y producto visible/available, writes bloqueados.
- `/orders`: lectura por `canReadOrder`, create/update/delete bloqueados.
- `/orders/{id}/events`: lectura por orden visible, write bloqueado.
- `/orders/{id}/incidents`: lectura por orden visible, write bloqueado.
- `/orders/{id}/claims`: lectura Admin-only, write bloqueado.
- `/orders/{id}/communications`: lectura por orden visible, write bloqueado.
- `/orders/{id}/ai_decisions`: lectura por orden visible, write bloqueado.
- `/public_claims`: lectura Admin-only, writes bloqueados.
- subcolecciones de `public_claims`: lectura Admin-only, writes bloqueados.

## Hallazgos backend/rules

- Criticos: ninguno.
- Medios: ninguno.
- Menores: copy de UI asociado a comunicacion `sent`, corregido fuera de Functions/Rules.
