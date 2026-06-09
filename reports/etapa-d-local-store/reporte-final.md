# Etapa D - Local / Store

## Linea base

- Rama inicial: main.
- HEAD inicial: b2a4aabc46105363070e7678fb6b5b6dfd5d6fe1.
- Estado Git inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

Se cerro la alineacion del Store/Local operativo real para operar pedidos propios con backend, sin escrituras directas sobre `/orders`, sin operar pedidos ajenos y sin fingir modulos no persistentes como stock, solicitud de repartidor o finanzas.

## Archivos modificados

- `app/src/main/java/com/pedilo/app/core/firebase/FirebaseStoreOrdersAdapter.kt`
- `app/src/main/java/com/pedilo/app/ui/store/StoreApp.kt`
- `tests/store_operational_flow.test.js`

## Archivos creados

- `tests/store_operation_alignment.test.js`
- `reports/etapa-d-local-store/reporte-final.md`

## Acceso Store

- El acceso Store sigue usando login interno y resolucion de rol existente.
- `FirebaseTeamAccessAdapter` exige perfil activo (`active == true`) y rol valido desde `/users/{uid}`.
- `PublicApp` enruta Store a `StoreApp`; Admin y Driver mantienen shells separados.

## Lectura de pedidos propios

- Store observa solo `/orders` con `storeId == uid`.
- El detalle valida que el pedido exista y pertenezca al Store autenticado antes de mostrarlo.
- Firestore Rules conservan lectura por `order.storeId == request.auth.uid` y operador activo.

## Detalle de pedido

- El detalle muestra numero, estado publico/operativo, persona, items y total.
- Si hay incidencia activa, se muestra como revision operativa.
- Si no hay acciones, se muestra que backend no habilita acciones para ese pedido/version o que un pedido cerrado no tiene acciones normales.

## Acciones Store

- Acciones siguen viniendo de `nextAllowedActions`.
- Toda accion pasa por callable `operateLiveOrder`.
- Se conserva `expectedVersion`.
- Se mantienen aceptar, rechazar, marcar en preparacion, marcar listo y abrir incidencia.
- Se agrega cancelacion Store en UI/adapters solo cuando backend la entregue en `nextAllowedActions`.
- Rechazo, cancelacion e incidencia requieren motivo operativo.

## Producto no disponible / demora

- No se creo modulo nuevo.
- Se expresa mediante `open_incident` existente cuando backend lo permite, con motivo claro.
- No se finge stock real ni cambios de disponibilidad.

## Solicitud de repartidor

- No se implemento solicitud real.
- La UI aclara que el local no solicita repartidor desde esta pantalla y que la asignacion queda en el flujo operativo seguro.

## Productos / stock

- No se implemento gestion persistente.
- La UI marca Productos y stock como gestion visual no disponible en este bloque, sin guardar catalogo ni disponibilidad.

## Finanzas

- No se implementaron caja, deuda, liquidaciones ni pagos reales.
- La UI lo indica explicitamente.

## Fuera de alcance

- Repartidor completo.
- Pagos reales.
- Cierre de caja.
- Deuda.
- Liquidaciones.
- WhatsApp real.
- Chat real.
- Notificaciones reales.
- IA.
- Metricas reales completas.
- Google Play.
- Deploy.
- Seed.
- Gestion real de stock/productos.
- Solicitud real de repartidor.

## Tests agregados/modificados

- Agregado `tests/store_operation_alignment.test.js`.
- Actualizado `tests/store_operational_flow.test.js`.

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

- `node --test tests/*.test.js`: OK, 25/25 archivos de test.
- `npm --prefix functions run build`: OK.
- `bash tools/guards/check_architecture.sh`: OK.
- `bash tools/guards/check_ui_quality.sh`: OK.
- `./gradlew assembleDebug --offline`: OK con ejecucion fuera del sandbox para cache local Gradle.
- `./gradlew lintDebug --offline`: OK.
- `git diff --check`: OK.

## Proteccion contra produccion accidental

- `.firebaserc`: no modificado.
- `app/google-services.json`: no modificado.
- Scripts seed: no ejecutados.
- Deploy: no ejecutado.
- Produccion Firebase: no tocada.

## Riesgos pendientes

- No se ejecutaron pruebas instrumentadas UI en dispositivo/emulador.
- Productos/stock, solicitud de repartidor y finanzas quedan no persistentes hasta backend seguro especifico.

## Dictamen final

BLOQUE D COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Continuar con el siguiente bloque funcional planificado, manteniendo deploy, seed y cambios de configuracion sensible fuera de alcance hasta autorizacion explicita.
