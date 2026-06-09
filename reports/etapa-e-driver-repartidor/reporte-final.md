# Bloque E - Repartidor / Driver

## Rama y HEAD inicial

- Rama: `main`
- HEAD inicial: `613493cc405ee1c6b230b402772858488fcec5d1`
- Estado inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

- Se alineo Driver/Repartidor para operar solo pedidos disponibles tomables o pedidos propios asignados.
- Se endurecio la lectura de pedidos disponibles en adapter y reglas: responsable actual driver, sin asignacion, sin driverId y con `driver_take` en `nextAllowedActions`.
- Se filtro la lista de acciones para que pedidos disponibles muestren solo `Tomar pedido`; incidencias/cancelacion quedan visibles solo cuando el pedido es propio y el backend las habilita.
- Se enriquecio el detalle Driver con tipo de pedido, accion necesaria, version, estado operativo, incidencia activa y mensajes explicitos para capacidad/cobro/caja.
- Se mantuvo toda accion por callable `operateLiveOrder`, con `expectedVersion` y motivo cuando corresponde.

## Archivos modificados

- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseDriverOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/core/model/DriverOrderModels.kt`
- `app/src/main/java/com/pedilo/app/ui/driver/DriverApp.kt`
- `firestore.rules`

## Archivos creados

- `tests/driver_operation_alignment.test.js`
- `reports/etapa-e-driver-repartidor/reporte-final.md`

## Acceso Driver

- El acceso sigue entrando por `TeamRole.Driver -> DriverApp`.
- La resolucion de rol activo sigue en `FirebaseTeamAccessAdapter` contra `/users/{uid}`.
- Usuario inactivo, rol invalido o perfil inexistente quedan como `NoAccess`.
- La UI Driver muestra errores claros cuando no puede cargar pedidos visibles para un repartidor activo.

## Pedidos disponibles

- El adapter consulta pedidos con `responsibleRole=driver` y `assignedActorId=""`.
- La app filtra disponibles solo si tienen `DriverTake` permitido.
- El detalle solo es visible si el pedido es propio o realmente tomable por Driver.
- Firestore Rules exige `currentResponsibleRole=driver`, `driverId=""`, `assignedActorId=""` y `nextAllowedActions.hasAny(["driver_take"])` para lectura de disponibles.

## Pedidos asignados

- La lectura de asignados sigue consultando `driverId == uid`.
- Las acciones operativas asignadas se mantienen solo para pedidos propios.
- El adapter considera propio si `driverId` o `assignedActorId` coinciden con el uid actual.
- No se habilita lectura ni operacion de pedidos de otro repartidor.

## Detalle de pedido

- Muestra identidad, tipo, estado publico/operativo, local, persona, telefono, entrega, productos, total referencial, version, accion necesaria e incidencia.
- No muestra payload crudo.
- El total queda marcado como referencial y sin cobro real.

## Acciones Driver

- `Tomar pedido`, `Marcar retirado`, `Marcar entregado`, `Reportar incidencia` y `Cancelar pedido` pasan por `operateLiveOrder`.
- La UI usa `nextAllowedActions` y `expectedVersion`.
- Pedidos disponibles solo muestran `Tomar pedido`; no muestran acciones que el backend rechazaria por falta de asignacion.
- Pedido sin acciones muestra que el backend no habilita acciones para ese pedido, version o estado cerrado.

## Incidencias basicas

- Se mantiene `OpenIncident` solo si llega habilitada por backend y el pedido es propio.
- Se exige motivo operativo para incidencia y cancelacion.
- La pantalla muestra incidencia activa sin crear un flujo ampliado de incidencias.

## Capacidad

- No se implemento motor de capacidad.
- La UI marca capacidad como preparacion/no disponible y aclara que no bloquea ni asigna por cupos.

## Cobro / caja / finanzas

- No se implementaron pagos, cobros, caja, deuda, liquidaciones ni comprobantes.
- La UI marca cobro/caja como visual no persistente.
- El total del pedido se muestra solo como referencial.

## Fuera de alcance

- Pagos reales, cierre de caja, deuda, liquidaciones, comprobantes, bloqueo financiero.
- WhatsApp, chat, notificaciones, IA, metricas completas, Google Play.
- Deploy, seed, Firebase produccion, `.firebaserc`, `app/google-services.json`.
- Motor real de capacidad o asignacion automatica.

## Tests agregados/modificados

- Agregado `tests/driver_operation_alignment.test.js`.
- Cubre acceso Driver, lectura disponible/propia, detalle, ausencia de acciones falsas, callable con version, doble toma, operacion ajena, version vieja, retiro/entrega invalidos, terminal sin acciones, incidencia basica, capacidad y caja no persistentes, y compatibilidad Public/Admin/Store.

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

- `git status --short`: cambios esperados del bloque E.
- `node --test tests/*.test.js`: pasa, 26/26 archivos.
- `npm --prefix functions run build`: pasa.
- `bash tools/guards/check_architecture.sh`: pasa.
- `bash tools/guards/check_ui_quality.sh`: pasa.
- `./gradlew assembleDebug --offline`: pasa con elevacion por lock de `~/.gradle`.
- `./gradlew lintDebug --offline`: pasa.
- `git diff --check`: pasa.

## Riesgos pendientes

- Capacidad Driver sigue fuera de alcance hasta existir backend seguro.
- Cobro/caja/finanzas siguen fuera de alcance y no persisten.
- Incidencias Driver siguen en modo basico; el universo completo queda para etapa posterior.

## Dictamen final

BLOQUE E COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Bloque posterior de capacidad/incidencias/finanzas solo cuando exista contrato backend seguro especifico.
