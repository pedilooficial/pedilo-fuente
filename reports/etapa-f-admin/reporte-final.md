# Etapa F - Admin

## Linea base

- Rama inicial: main.
- HEAD inicial: 70de07ac5908b1151e7ada8f050c0739b3f96ec7.
- Estado Git inicial: limpio, sin archivos modificados ni no trackeados.

## Resumen de cambios

Se cerro la alineacion del Admin operativo real sin ampliar producto ni tocar produccion. La operacion Admin sigue leyendo pedidos reales y ejecutando acciones por backend. Las areas no persistentes de Configuracion y Alta de roles quedaron marcadas como visuales/preparatorias y sin prometer guardado, usuarios o permisos reales.

## Archivos modificados

- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `tests/admin_visual_shell.test.js`

## Archivos creados

- `tests/admin_operation_alignment.test.js`
- `reports/etapa-f-admin/reporte-final.md`

## Acceso Admin

- El acceso Admin sigue pasando por login interno existente.
- `FirebaseTeamAccessAdapter` exige perfil activo (`active == true`) y rol resuelto desde `/users/{uid}`.
- `PublicApp` enruta Admin, Store y Driver a sus shells separados.
- Rol no Admin no entra como Admin.

## Operacion Admin

- Admin observa `/orders` en modo lectura y detalle por read models.
- Las mutaciones operativas se mantienen por callable `operateLiveOrder`.
- Las acciones envian `expectedVersion`.
- Los errores se muestran como mensajes humanos controlados.
- No se agregaron acciones nuevas ni escrituras directas desde UI Admin.

## Pedido #

- La pantalla mantiene identidad, estado, ubicacion operativa, secciones y acciones backend.
- Las acciones visibles salen de `nextAllowedActions`.
- Si no hay acciones permitidas, se muestra "Sin acciones disponibles" y se explica que backend no habilita acciones para ese pedido/version o pedido cerrado.
- No se muestra payload crudo ni se agregan botones falsos.

## Configuracion

- La raiz de Configuracion ahora indica "Preparacion visual sin guardar cambios reales".
- Los editores y borradores se explican como revision visual sin persistencia real.
- No se implemento configuracion persistente.
- No se escribio backend, Firestore ni reglas.

## Alta de roles

- La raiz de Alta de roles ahora indica "Preparacion visual sin crear usuarios reales".
- Las altas se renombran como preparar alta Admin/Local/Repartidor.
- No se toca Firebase Auth.
- No se escribe `/users`.
- No se modifican roles reales.

## Incidencias basicas Admin

- Quedan disponibles solo las acciones ya soportadas por backend y `nextAllowedActions`: abrir incidencia, resolver incidencia, intervenir, cancelar u otras acciones operativas permitidas por contrato.
- No se implemento universo completo de reclamos.
- No se mezclaron reclamos publicos con incidencias internas.

## Fuera de alcance

- Local completo.
- Repartidor completo.
- Pagos reales.
- Cierre de caja.
- WhatsApp real.
- Chat real.
- Notificaciones reales.
- IA.
- Metricas reales completas.
- Google Play.
- Deploy.
- Seed.
- Cambios de reglas de Pedido Vivo.
- CRUD real de roles.
- Configuracion persistente real.

## Tests agregados/modificados

- Agregado `tests/admin_operation_alignment.test.js`.
- Actualizado `tests/admin_visual_shell.test.js`.

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

- `node --test tests/*.test.js`: OK, 24/24 archivos de test.
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
- Configuracion y Alta de roles siguen siendo herramientas visuales/no persistentes hasta que exista backend seguro especifico.

## Dictamen final

BLOQUE F COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.

## Proximo bloque permitido

Continuar con el siguiente bloque funcional planificado, manteniendo deploy, seed y cambios de configuracion sensible fuera de alcance hasta autorizacion explicita.
