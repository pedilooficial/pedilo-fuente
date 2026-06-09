# Etapa C - Usuario Publico

## Linea base

- Rama inicial: main.
- HEAD inicial: d2b4eb16a0b6e00bcbb32e5ade29eeae5d38c7c3.
- Estado Git inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

Se alineo el flujo publico real para reforzar validaciones antes de confirmacion, evitar placeholders como datos reales, mantener tracking publico sin datos internos y corregir el reclamo/reporte publico para no fingir persistencia inexistente.

## Archivos modificados

- `app/src/main/java/com/pedilo/app/ui/publicuser/PublicInputs.kt`
- `app/src/main/java/com/pedilo/app/ui/publicuser/PublicLocal.kt`
- `app/src/main/java/com/pedilo/app/ui/publicuser/PublicPlus.kt`
- `app/src/main/java/com/pedilo/app/ui/publicuser/PublicConventions.kt`

## Archivos creados

- `tests/public_user_flow_alignment.test.js`
- `reports/etapa-c-usuario-publico/reporte-final.md`

## Home, tienda, local, Boton+ y tracking

- Home y navegacion publica se mantuvieron sobre `PublicApp` y rutas publicas existentes.
- Tienda y catalogo no se modificaron funcionalmente; siguen leyendo catalogo publico por adapters/use cases existentes.
- Local publico ahora bloquea continuar desde carrito vacio y valida nombre/direccion con helper anti-placeholder antes de avanzar.
- Boton+ Comprar ahora bloquea producto, detalle, origen, nombre y direccion placeholders antes de confirmacion.
- Boton+ Retiro / Envio ahora bloquea origen, destino, nombre, descripcion y monto placeholders antes de confirmacion.
- Tracking publico ahora valida formato `PDL-[A-Z0-9]{4,10}` en UI, alineado al backend M.

## Reclamo / reporte publico

- La pantalla de reclamo dejo de mostrar "Reclamo registrado" o "Enviar reclamo".
- El flujo queda como "Preparar aviso" / "Aviso preparado" y aclara que la app todavia no envia reclamos al sistema.
- No se creo backend nuevo de reclamos.
- No se mezclo con incidencias internas.

## Datos internos protegidos

Se mantuvo fuera de UI/tracking publico:

- `responsibleRole`
- `currentResponsibleRole`
- `assignedActorId`
- `assignedActorRole`
- `driverId`
- eventos internos
- incidencias internas
- auditoria
- payload tecnico
- escrituras directas sobre `/orders`

## Fuera de alcance

- Admin.
- Local operativo avanzado.
- Repartidor operativo avanzado.
- Pagos reales.
- Cierre de caja.
- WhatsApp real.
- Chat real.
- Notificaciones reales.
- IA.
- Metricas reales.
- Google Play.
- Deploy.
- Seed.
- Cambios de reglas de Pedido Vivo.

## Tests agregados/modificados

- Agregado `tests/public_user_flow_alignment.test.js`.

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

- `node --test tests/*.test.js`: OK, 23/23 archivos de test.
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

- No se ejecutaron pruebas instrumentadas de UI en dispositivo/emulador.
- La pantalla de reclamo queda como aviso no persistente hasta que exista un modulo real seguro en una etapa posterior.

## Dictamen final

BLOQUE C COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Continuar con el siguiente bloque funcional planificado, manteniendo deploy, seed y cambios de configuracion sensible fuera de alcance hasta autorizacion explicita.
