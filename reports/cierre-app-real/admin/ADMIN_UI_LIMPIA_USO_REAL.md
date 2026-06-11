# Admin UI limpia para uso real

## HEAD inicial

- Rama inicial verificada: `work`.
- HEAD inicial verificado: `add240de8fa759c9ef28d691fd62f64b7f83adff`.
- Estado inicial: limpio.
- Nota: el pedido indicaba rama esperada `main` y HEAD `25312f3869502abf68504fa6fc2eb91ab6699b7e`; este entorno estaba en `work` con el HEAD indicado arriba.

## Problemas encontrados en la UI Admin

- La navegación inferior todavía nombraba Equipo como `Alta de roles`, mezclando el mundo operativo real de usuarios con una ruta de alta no definida.
- La raíz de Configuración y Equipo usaba lenguaje de “real” y “bloqueo” que obligaba al operador a interpretar implementación o alcance técnico.
- Las rutas heredadas de Configuración pública, subsecciones de configuración y convergencia de Equipo seguían renderizando pantallas secundarias si una ruta interna quedaba activa.
- Había textos visibles con términos heredados: ruta heredada, bloque, alta bloqueada, no aplica cambios reales en esta etapa y referencias a consulta sin acción mezcladas con la experiencia principal.
- Algunos estados secundarios describían pasos no operativos como si fueran flujo normal de uso.

## Rutas heredadas eliminadas, aisladas o reemplazadas

- Las rutas internas de Configuración (`ConfigurationSection`, `ConfigurationSubsection`, `ConfigurationPublicWorld`, `ConfigurationPublicWorldPart`, `ConfigurationPublicWorldEditor`, `ConfigurationConvergence`) ya no renderizan pantallas heredadas como flujo principal: redirigen a la pantalla persistente de Configuración.
- Las rutas internas de Equipo (`RoleAccessSection`, `RoleAccessSubsection`, `RoleAccessConvergence`) ya no renderizan pantallas heredadas como flujo principal: redirigen a la pantalla persistente de Equipo.
- La navegación inferior cambió de `Alta de roles` a `Equipo`.
- Las pantallas secundarias que permanecen en código quedan aisladas de la navegación principal y con copy no operativo si se reutilizan internamente.

## Pantallas corregidas

- Home principal Admin: mantiene entrada directa a Operación, Configuración y Equipo sin rutas de maqueta.
- Configuración: queda como pantalla principal de controles persistentes con feedback de guardado/error.
- Equipo: queda como pantalla principal para cuentas existentes, activación/desactivación y cambio de rol.
- Operación: se ajustó copy de estados incompletos para evitar lenguaje de etapa interna.

## Acciones reales conectadas

- Configuración conserva `updateAdminConfig` mediante callable seguro y muestra resultado/error.
- Equipo conserva `updateTeamUser` para activar/desactivar usuarios existentes y cambiar roles.
- Operación conserva acciones de pedido basadas en `nextAllowedActions` y `expectedVersion`.

## Datos reales conectados

- Configuración sigue leyendo `admin_config` desde el adapter Firebase.
- Equipo sigue leyendo `/users` desde el adapter Firebase.
- Operación sigue leyendo pedidos reales y detalle de pedido desde el caso de uso Admin.

## Funciones bloqueadas correctamente

- Crear cuentas nuevas queda comunicado como no disponible por seguridad hasta definir invitaciones seguras.
- Las rutas heredadas de revisión/configuración pública quedan fuera del flujo principal, evitando que parezcan acciones principales.
- Las consultas secundarias no prometen guardar cambios ni publicar contenido.

## Archivos modificados

- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/components/AdminComponents.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/operation/OperationData.kt`
- `tests/admin_operation_alignment.test.js`
- `tests/admin_visual_shell.test.js`

## Archivos creados

- `reports/cierre-app-real/admin/ADMIN_UI_LIMPIA_USO_REAL.md`

## Tests modificados/agregados

- Se actualizó `tests/admin_operation_alignment.test.js` para exigir copy humano de Configuración/Equipo y bloquear términos heredados.
- Se actualizó `tests/admin_visual_shell.test.js` para validar que rutas internas de Configuración/Equipo renderizan las pantallas reales principales y que `Alta de roles` no vuelve al flujo visible.

## Validaciones ejecutadas

- `git status --short`: ejecutado al inicio del cierre, mostró modificaciones esperadas de UI/tests.
- `node --test tests/*.test.js`: pasa, 222 tests OK.
- `npm --prefix functions run build`: pasa, `node --check index.js` OK; npm emitió advertencia de configuración `http-proxy` ajena al cambio.
- `bash tools/guards/check_architecture.sh`: pasa.
- `bash tools/guards/check_ui_quality.sh`: pasa.
- `bash tools/guards/check_no_production_release.sh`: pasa.
- `./gradlew assembleDebug --offline`: falla por bloqueo técnico del entorno con Java `25.0.2` antes de compilar la app.
- `./gradlew lintDebug --offline`: falla por el mismo bloqueo técnico del entorno con Java `25.0.2`.
- `git diff --check`: pasa.

## Resultado

- La UI principal de Admin quedó reducida a tres superficies humanas: Operación, Configuración y Equipo.
- Configuración y Equipo ya no exponen rutas heredadas como flujo principal aunque una ruta interna quede activa: vuelven a las pantallas persistentes principales.
- Se eliminó el copy visible de maqueta o implementación solicitado para Admin principal: `Alta de roles`, `ruta heredada`, `bloque`, `no aplica cambios reales`, `guardar borrador visual`, `confirmar visualmente`, `herramienta visual`, `maqueta` y `prototipo`.
- Las validaciones Node, Functions y guards pasan. Gradle queda pendiente por incompatibilidad técnica del entorno con Java `25.0.2`.

## Riesgos restantes

- No se definió creación de cuentas nuevas ni invitaciones seguras; queda correctamente no disponible en UI.
- Las pantallas secundarias históricas permanecen en código para consulta/estructura, pero quedaron aisladas del flujo principal Admin.
