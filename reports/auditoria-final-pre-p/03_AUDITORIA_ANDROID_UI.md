# 03 - Auditoria Android / UI

## Android core

- Modelos coinciden con campos backend principales: status, operationalStatus, financialStatus, communicationStatus, incidentStatus, archiveStatus, activeIncident, nextAllowedActions, currentResponsibleRole, assignedActorId, driverId, storeId, AI y health.
- Ports/use cases separan public, Admin, Store, Driver y TeamAccess.
- Adapters publicos usan callables: `createLocalOrder`, `createPlusOrder`, `getPublicOrderTracking`, `submitPublicClaim`.
- Admin usa lectura Firestore para pedidos autorizados por rules y callables para acciones/health.
- Store filtra por `storeId == auth.uid` y llama `operateLiveOrder`.
- Driver observa disponibles/asignados y llama `operateLiveOrder`.
- No se encontraron escrituras directas Android a `/orders`.

## UI publica

- Home, tienda/local, Boton +, tracking y reclamos leidos.
- Pedido local exige carrito, datos reales, telefono valido, direccion y pago.
- Boton + compra/retiro exige datos minimos y telefono valido.
- Tracking publico llama callable y no expone internos.
- Reclamo publico llama callable y usa mensaje seguro.
- No se encontro copy de WhatsApp enviado, pago confirmado, IA real, produccion lista o Play listo.

## UI Admin

- Operacion real de pedidos usa datos reales y acciones por backend.
- Detalle expone version, acciones disponibles, eventos, finanzas, comunicacion, IA y health.
- Configuracion y Alta roles permanecen visuales/no persistentes y el copy lo indica.
- No se encontraron botones peligrosos sin confirmacion para acciones operativas.

## UI Store

- Lectura propia por `storeId`.
- Acciones reales con `expectedVersion`.
- Productos/stock, solicitud de repartidor y finanzas avanzadas indican que no persisten/no aplican.
- Comunicacion y ayuda operativa son seguras.

## UI Driver

- Lectura de disponibles/asignados.
- Acciones reales con `expectedVersion`.
- Cobro/caja avanzada se muestra como no persistente fuera de bloque.
- Ayuda operativa no expone prompt, score ni proveedor externo.

## Correccion aplicada

- En Admin, Store y Driver, el label para `communicationStatus == "sent"` paso de "Enviada por canal real" a "Registrada como enviada; verificar canal".

## Hallazgos UI

- Criticos: ninguno.
- Medios: copy potencialmente falso de comunicacion `sent`, corregido.
- Menores: no hay tests JVM/UI instrumentados; se mantiene cobertura por tests Node/source y build/lint Android.
