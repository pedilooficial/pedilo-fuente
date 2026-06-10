# Bloque L - Metricas / Auditoria / Salud del sistema

## Baseline

- Rama inicial: `main`
- HEAD inicial: `4348aed62797787d9aa441202eaae6d295172b88`
- Estado Git inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

Se implemento un baseline interno de observabilidad calculada para Admin. El bloque lee senales existentes de Pedido Vivo, eventos, incidencias, reclamos, comunicaciones, decisiones asistidas y finanzas; resume estado operativo; detecta incoherencias basicas; expone modulos deshabilitados/preparados/no implementados; y muestra un tablero Admin informativo sin ejecutar correcciones ni mutaciones operativas.

## Archivos modificados

- `functions/index.js`
- `app/src/main/java/com/pedilo/app/core/model/AdminOrderReadModels.kt`
- `app/src/main/java/com/pedilo/app/core/port/AdminOrdersPort.kt`
- `app/src/main/java/com/pedilo/app/core/usecase/GetAdminOperationOrdersUseCase.kt`
- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseAdminOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`

## Archivos creados

- `tests/operational_health_block_l.test.js`
- `reports/etapa-l-metricas-auditoria-salud/reporte-final.md`

## Metricas operativas

- Pedidos vivos, cerrados, cancelados y pendientes de revision.
- Pedidos con atencion Admin, incidencias abiertas y fallos de comunicacion.
- Comunicaciones prepared, disabled y failed.
- Revision financiera pendiente, cobros contra entrega, transferencias declaradas y pagos declarados sin confirmar.
- Sugerencias IA pendientes, aceptadas, rechazadas y no aplicables.
- Reclamos publicos recibidos, vinculados y no vinculados.
- Acciones pendientes agrupadas por rol.

## Auditoria transversal

- Resumen calculado desde `orders/{orderId}/events`, `incidents`, `claims`, `communications`, `ai_decisions` y `public_claims`.
- Ultimos eventos criticos resumidos para Admin.
- Sin exposicion publica de auditoria interna.
- Sin persistencia nueva de auditoria ni health.

## Salud del Pedido Vivo

Se agregaron alertas no destructivas para:

- pedido terminal con `archiveStatus == live`;
- pedido activo sin `nextAllowedActions`;
- `activeIncident` incoherente con `incidentStatus`;
- revision financiera sin motivo;
- `communicationStatus == failed` sin comunicacion fallida;
- `aiRequiresHumanReview` sin `ai_decision`;
- pedido activo sin responsable;
- pedido driver sin `driverId` cuando corresponde;
- pedido local sin `storeId`;
- importes de cobro y total invalidos.

## Salud de comunicacion

- WhatsApp y Push/FCM quedan como `disabled` por falta de proveedor real.
- Se resumen prepared, disabled, failed y pedidos con comunicacion fallida.
- No se envia WhatsApp real, push real ni mensajes externos.

## Salud financiera

- Se resumen cobros contra entrega, transferencias declaradas, pagos declarados no confirmados, revision financiera y cobros pendientes.
- No se implementa banco, caja avanzada, conciliacion ni confirmacion automatica de pagos.

## Salud de incidencias, reclamos y cancelaciones

- Se resumen incidencias abiertas, resueltas y sin resolver.
- Se resumen reclamos publicos vinculados y no vinculados.
- Cancelaciones quedan como conteo de pedidos terminales/cancelados; no se agrego ranking ni analitica avanzada.

## Salud IA asistida

- Se resume el motor `deterministic_rules_v1`.
- Proveedor externo queda `disabled`.
- Se cuentan sugerencias pendientes, aceptadas, rechazadas, no aplicables y riesgos altos/criticos.
- No se ejecutan sugerencias ni acciones criticas.

## Tablero Admin

- Se agrego panel `Salud interna` en Operacion Admin.
- Muestra estado general, pedidos vivos, atencion requerida, comunicacion, finanzas, incidencias, IA pendiente, modulos, consistencia, auditoria transversal y ultimos eventos criticos.
- No agrega botones de correccion ni promete produccion.

## Estado de modulos

- WhatsApp: `disabled`.
- Push/FCM: `disabled`.
- IA externa: `disabled`.
- Caja avanzada: `not_implemented`.
- Banco/pasarela: `not_implemented`.
- Google Play: `not_ready`.
- Produccion: `not_ready`.
- Hardening carga: `pending_o`.

## Auditoria de seguridad

- `getOperationalHealth` exige Admin activo por backend.
- `/orders` sigue sin escritura directa cliente.
- Subcolecciones criticas mantienen escritura directa denegada por rules.
- No se agregaron lecturas publicas nuevas.
- Store/Driver/Public no reciben el tablero global.

## Fuera de alcance

- Produccion, deploy, seed y Google Play.
- Monitoreo externo, alertas externas, BI, dashboards comerciales y ranking.
- Hardening profundo, pruebas de estres y carga.
- WhatsApp real, push real, IA externa, banco, pasarela, caja avanzada y contabilidad avanzada.
- Correcciones automaticas, cancelaciones automaticas, cierre automatico de incidencias o confirmacion automatica de pagos.

## Tests agregados/modificados

- Agregado `tests/operational_health_block_l.test.js`.
- No se bajo exigencia de tests existentes.

## Validaciones ejecutadas

- `node --test tests/*.test.js`: paso.
- `npm --prefix functions run build`: paso.
- `bash tools/guards/check_architecture.sh`: paso.
- `bash tools/guards/check_ui_quality.sh`: paso.
- `./gradlew assembleDebug --offline`: paso.
- `./gradlew lintDebug --offline`: paso.
- `git diff --check`: paso.

## Riesgos pendientes

- No hay monitoreo productivo externo.
- No hay stress tests ni hardening de carga.
- No hay proveedores reales de WhatsApp, push o IA externa.
- No hay banco, pasarela ni caja avanzada.
- Produccion y Google Play siguen no listos.

## Dictamen final

BLOQUE L COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Bloque M/O posterior segun roadmap: hardening, carga, preparacion productiva o el siguiente bloque definido por el plan.
