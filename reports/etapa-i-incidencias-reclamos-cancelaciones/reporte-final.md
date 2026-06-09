# Bloque I - Incidencias, Reclamos y Cancelaciones

## Rama y HEAD inicial

- Rama inicial: `main`
- HEAD inicial: `a62cf3a94ff7f4ad16d78808ad23707e20b286a5`
- Estado inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

Se cerró el flujo mínimo real para incidencias operativas, reclamos públicos persistentes y cancelaciones auditadas. El backend mantiene la autoridad mediante callables, `expectedVersion`, acciones permitidas por estado y roles activos. No se tocó producción, `.firebaserc`, `google-services.json`, deploy ni seed.

## Archivos modificados

- `functions/index.js`
- `firestore.rules`
- `app/src/main/java/com/pedilo/app/core/runtime/PublicRuntime.kt`
- `app/src/main/java/com/pedilo/app/ui/publicuser/PublicConventions.kt`
- `tests/core_foundation.test.js`
- `tests/public_user_flow_alignment.test.js`

## Archivos creados

- `app/src/main/java/com/pedilo/app/core/firebase/FirebasePublicClaimAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/model/PublicClaimDraft.kt`
- `app/src/main/java/com/pedilo/app/core/model/PublicClaimReceipt.kt`
- `app/src/main/java/com/pedilo/app/core/port/PublicClaimPort.kt`
- `app/src/main/java/com/pedilo/app/core/usecase/SubmitPublicClaimUseCase.kt`
- `tests/incidents_claims_cancellations_block_i.test.js`
- `reports/etapa-i-incidencias-reclamos-cancelaciones/reporte-final.md`

## Contrato de incidencia

Las incidencias operativas persisten en `/orders/{orderId}/incidents/{incidentId}` con `incidentId`, `orderId`, `status`, `type`, `reason`, `description`, `sourceRole`, `sourceActorId`, timestamps, impacto público, impacto operativo, prioridad, acción vinculada y estado operativo previo. La resolución agrega `resolvedAt`, `resolvedByRole`, `resolvedByActorId` y `resolutionNote`.

## Reclamo público

Se agregó `submitPublicClaim`, que valida tracking opcional, nombre/contacto, motivo, descripción y tipo. Persiste reclamos en `/public_claims`, crea evento propio y, si el tracking existe, deja vínculo mínimo en `/orders/{orderId}/claims` sin mutar Pedido Vivo ni revelar existencia del pedido al público.

## Cancelaciones

Las cancelaciones por callable guardan motivo, actor, rol, estado previo, estado operativo previo, estado financiero al cancelar, estado público, archivo previo, evento y nota/revisión financiera mínima si hay transferencia, pago declarado o cobro al recibir.

## Store

Store conserva acciones propias por `operateLiveOrder`, solo con pedidos propios, rol activo, `expectedVersion`, `nextAllowedActions` y motivo para incidencia/cancelación.

## Driver

Driver conserva acciones propias/asignadas por `operateLiveOrder`, rol activo, `expectedVersion`, `nextAllowedActions` y motivo para incidencia/cancelación.

## Admin

Admin puede abrir y resolver incidencias persistentes, cancelar con motivo y dejar auditoría. La resolución actualiza el documento de incidencia activo cuando existe `activeIncidentId`.

## Exposición pública

Tracking muestra mensajes seguros: revisión operativa, cancelado/cerrado y reclamo recibido. No expone UIDs, actores internos, eventos, auditoría, notas internas, deuda, caja ni conflicto operativo.

## Auditoría

Quedan rastros en `events`, `incidents`, `claims` y `public_claims/{claimId}/events` con actor/rol/fuente/motivo/estados previos y siguientes/timestamp según corresponda.

## Impacto financiero mínimo

No se borra información financiera. Si una cancelación toca cobro al recibir, transferencia declarada, pago declarado o estado financiero sensible, queda `financialReviewRequired` y `financialReviewNote` sin conciliación, banco, caja avanzada ni devolución automática.

## Fuera de alcance

WhatsApp real, chat, notificaciones, IA, métricas avanzadas, scoring, resolución automática, pagos externos, validación bancaria, caja avanzada, Google Play, producción, deploy y seed.

## Tests agregados/modificados

- Agregado `tests/incidents_claims_cancellations_block_i.test.js`
- Actualizado `tests/core_foundation.test.js`
- Actualizado `tests/public_user_flow_alignment.test.js`

## Validaciones ejecutadas

- `node --test tests/*.test.js`
- `npm --prefix functions run build`
- `bash tools/guards/check_architecture.sh`
- `bash tools/guards/check_ui_quality.sh`
- `./gradlew assembleDebug --offline`
- `./gradlew lintDebug --offline`
- `git diff --check`

## Riesgos pendientes

No quedan riesgos bloqueantes del Bloque I. La lista Admin dedicada de reclamos públicos queda mínima por reglas/colección y puede ampliarse en un bloque posterior sin mezclar reclamo público con incidencia operativa.

## Dictamen final

BLOQUE I COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Próximo bloque permitido

Próximo bloque operativo posterior a incidencias/reclamos/cancelaciones, manteniendo fuera producción y módulos avanzados.
