# Reporte final - Bloque B Pedido Vivo

**Fecha:** 2026-06-09  
**Rama inicial:** `main`  
**HEAD inicial real:** `871ca77c2ec818b821a38cb82f69022dff9d05aa`  
**HEAD esperado en pedido:** `db36a3afabe909598a95d108e3413fd2ae2d96f2`  

Nota: el HEAD esperado era ancestro del HEAD inicial real. El commit adicional inicial (`871ca77`) agregaba documentación local del reporte total y no modificaba código funcional.

## Resumen de cambios

Se completó una alineación acotada del núcleo Pedido Vivo existente, sin implementar módulos posteriores ni cambiar producto fuera del pedido.

El bloque endurece:

- contrato declarativo de estados del Pedido Vivo;
- timeout/fallback como políticas explícitamente declarativas y no ejecutables;
- snapshots básicos normalizados en nacimiento;
- estado vivo leído por backend con campos completos del contrato;
- validación explícita de transiciones críticas;
- responsabilidad actual por rol/actor;
- evento operativo con auditoría mínima uniforme;
- tests específicos de bloque B.

## Archivos modificados

- `functions/index.js`
- `app/src/main/java/com/pedilo/app/core/model/LiveOrderContract.kt`
- `tests/live_order_birth_contract.test.js`

## Archivos creados

- `tests/live_order_core_alignment.test.js`
- `reports/etapa-b-pedido-vivo/reporte-final.md`

## Qué se alineó

### Modelo del Pedido Vivo

`liveOrderState` ahora expone de forma consistente campos del contrato operativo:

- `id`, `orderType`, `source`;
- `status`, `publicStatus`, `operationalStatus`;
- `financialStatus`, `communicationStatus`, `incidentStatus`, `archiveStatus`;
- `responsibleRole`, `currentResponsibleRole`;
- `assignedActorId`, `assignedActorRole`, `driverId`, `storeId`;
- `trackingNumber`, `publicOrderNumber`;
- `version`, `priority`, `needsAttention`;
- `nextAllowedActions`;
- `createdAt`, `updatedAt`, `lastOperationEvent`;
- `initialSnapshot`, `liveSnapshot`;
- `timeoutPolicy`, `fallbackPolicy`.

### Estados

Se agregó `LIVE_ORDER_STATES` como contrato interno declarativo para:

- iniciales;
- operativos;
- terminales;
- financieros;
- comunicación;
- incidencia;
- archivo.

En Kotlin se amplió el vocabulario declarativo compatible:

- `LiveOrderCommunicationStatus.Closed("closed")`;
- `LiveOrderIncidentStatus.Open("open")`;
- `LiveOrderIncidentStatus.Resolved("resolved")`.

### Transiciones válidas

Se agregó `validateLiveTransition` para impedir:

- preparación sin aceptación previa;
- listo sin preparación previa;
- toma de driver sin pedido listo para retiro;
- retiro sin asignación previa;
- entrega sin retiro previo;
- acciones sobre pedido terminal.

### Responsabilidad actual

Se reforzó `validateLiveActor` con control de responsabilidad actual, preservando compatibilidad V1:

- Local puede aceptar/rechazar pedido recién creado.
- Local no puede ejecutar acciones normales cuando el responsable actual ya pasó a Driver/Admin, salvo cancelar o abrir incidencia.
- Driver sólo actúa cuando `currentResponsibleRole == "driver"` y respeta asignación.

### Versionado, concurrencia e idempotencia

Se conservó:

- `expectedVersion` obligatorio en acciones live;
- rechazo por versión vieja;
- transacción Firestore;
- `actionId` idempotente por acción;
- retorno idempotente si el evento ya existe.

No se cambiaron wire names ni payloads.

### Eventos y auditoría mínima

Se agregó `liveOrderEvent` para crear eventos operativos con:

- tipo de acción;
- actor y rol;
- estado anterior/nuevo;
- versión anterior/nueva;
- motivo;
- resultado;
- auditoría mínima de responsable y archivo.

El evento inicial ahora conserva `previousVersion`, `nextVersion`, `result` y `audit`.

### Snapshots básicos

`initialSnapshot` y `liveSnapshot` nacen con:

- `schemaVersion`;
- `orderType`;
- `source`;
- `trackingNumber`;
- `publicSummary`;
- `payload` original.

No se implementaron tarifas avanzadas, caja ni finanzas completas.

### Timeouts y fallbacks

Se dejó contrato declarativo claro:

- `mode: "declarative"`;
- `executable: false`;
- nota explícita de que no hay scheduler en Bloque B.

No se creó scheduler ni infraestructura nueva.

### Cierre y archivo operativo mínimo

El cierre operativo conserva:

- pedidos terminales sin acciones permitidas;
- `archiveStatus: "archived"`;
- `communicationStatus: "closed"` al entregar;
- tracking público seguro sin detalles internos en pedidos cerrados.

## Compatibilidad mantenida

Se mantuvo compatibilidad con:

- creación de pedido local;
- creación de pedido Botón +;
- tracking público;
- Admin operativo;
- Store/Local operativo;
- Driver/Repartidor operativo;
- eventos;
- incidencias básicas;
- reglas Firestore existentes;
- wire names actuales.

## Fuera de alcance

No se implementó:

- pagos reales;
- cierre de caja;
- WhatsApp real;
- chat real;
- notificaciones reales;
- IA;
- métricas reales;
- Google Play;
- scheduler real de timeouts/fallbacks;
- archivo histórico separado;
- deploy;
- seed;
- cambios en `.firebaserc` o `app/google-services.json`.

## Tests agregados/modificados

Agregado:

- `tests/live_order_core_alignment.test.js`

Modificado:

- `tests/live_order_birth_contract.test.js`

Cobertura nueva:

- contrato de estados B;
- timeout/fallback declarativo;
- campos requeridos de nacimiento;
- snapshots normalizados;
- preparación sin aceptación rechazada;
- toma driver antes de listo rechazada;
- flujo Local acepta/prepara/listo;
- Driver toma/retira/entrega;
- doble toma/actor incorrecto rechazado por asignación;
- entrega sin retiro rechazada;
- pedido terminal sin acciones;
- incidencia mueve responsabilidad a Admin;
- evento con auditoría mínima;
- tracking cerrado sin datos internos.

## Validaciones ejecutadas

| Comando | Resultado |
|---------|-----------|
| `node --test tests/live_order_birth_contract.test.js tests/live_order_end_to_end_flow.test.js tests/live_order_core_alignment.test.js tests/operational_order_actions_backend.test.js` | OK |
| `node --test tests/*.test.js` | OK: 21/21 tests pasan |
| `npm --prefix functions run build` | OK: `node --check index.js` |
| `bash tools/guards/check_architecture.sh` | OK: `architecture guard passed` |
| `bash tools/guards/check_ui_quality.sh` | OK: `ui quality guard passed` |
| `./gradlew assembleDebug --offline` | OK con ejecución local escalada por lock/cache de Gradle fuera del workspace; `BUILD SUCCESSFUL`. |
| `./gradlew lintDebug --offline` | OK con ejecución local escalada por lock/cache de Gradle fuera del workspace; `BUILD SUCCESSFUL`. |
| `git diff --check` | OK sin errores. |

## Riesgos pendientes

- Timeouts/fallbacks siguen declarativos; ejecución real queda para etapa posterior si se define infraestructura segura.
- No hay cierre financiero ni caja, por alcance.
- No hay emuladores configurados explícitamente.
- Admin conserva convivencia entre operación real y secciones visuales de módulos futuros.
- `adminOrderAction` legacy/dedicado sigue existiendo junto a `operateLiveOrder`; no se removió por compatibilidad.

## Dictamen final

**B) BLOQUE B COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.**

## Próximo bloque permitido

Luego de validaciones verdes y commit, continuar con el siguiente bloque planificado dependiente de B, sin tocar módulos posteriores fuera de su etapa.
