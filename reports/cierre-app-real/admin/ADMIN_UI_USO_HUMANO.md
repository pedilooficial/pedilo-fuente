# Admin UI uso humano — cierre Pédilo

**Fecha:** 2026-06-11  
**Rama inicial:** `work`  
**HEAD inicial real:** `67502a9e6b9a1c9cd9efeee96a15f20b4dc74d3e`  
**HEAD esperado por instrucción:** `3d31f4b38e06010b53fbe2599d7460028f6c2a67` no coincidía con el entorno; se trabajó sobre el HEAD real verificado.  
**Estado Git inicial:** limpio.

## Problemas humanos que tenía la UI

- Mezclaba textos de maqueta con flujos reales, especialmente en configuración pública heredada y accesos.
- Usaba palabras como “visual”, “borrador”, “confirmar visualmente”, “preparado”, “preview”, “backend”, “Firestore” o referencias de implementación en textos visibles.
- Configuración y Equipo ya tenían base real, pero algunos textos seguían sonando a prototipo o herramienta interna.
- El estado sin acciones del pedido explicaba el problema con lenguaje técnico y no orientaba al operador.
- El bottom/root de Equipo seguía rotulado como “Alta de roles”, aunque la acción real disponible es gestionar cuentas existentes y bloquear altas nuevas.
- Algunas secciones bloqueadas podían sentirse como pantallas activas si el texto no aclaraba el alcance en lenguaje simple.

## Qué se corrigió

- Se cambió `Alta de roles` por `Equipo` como raíz humana del Admin.
- Se limpiaron textos visibles de prototipo en Admin y componentes Admin.
- Configuración real ahora se presenta como controles reales de operación, sin mencionar Firestore, preview ni botones sin efecto.
- Equipo ahora explica en lenguaje simple que gestiona cuentas existentes y que las cuentas nuevas están bloqueadas por seguridad.
- Las rutas heredadas de configuración/accesos se expresan como consulta o revisión, no como acciones activas falsas.
- El detalle de pedido explica acciones disponibles y ausencia de acciones con lenguaje humano.
- Los tonos/chips de componentes cambiaron de “Preparación”/“Vista previa” a “Ajustes”/“Revisión”.
- Se actualizan tests para impedir que vuelvan textos de maqueta/prototipo en Admin.

## Archivos modificados

- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`
- `app/src/main/java/com/pedilo/app/ui/admin/components/AdminComponents.kt`
- `tests/admin_operation_alignment.test.js`
- `tests/admin_visual_shell.test.js`

## Archivos creados

- `reports/cierre-app-real/admin/ADMIN_UI_USO_HUMANO.md`

## Pantallas Admin que quedaron usables

- Home/Operación: mantiene entrada clara a pedidos, prioridades, problemas, activos y cerrados.
- Detalle de pedido: muestra estado, ubicación operativa, secciones relevantes, acciones permitidas, resultado y error humano.
- Configuración: muestra controles reales, guardado/error y alcance simple sin lenguaje técnico.
- Equipo: muestra cuentas existentes, activar/desactivar, cambio de rol y bloqueo claro de altas nuevas.
- Salud/Métricas: mantiene resumen de riesgos, alertas, módulos y auditoría en lenguaje de operador.

## Funciones bloqueadas correctamente

- Alta de cuentas nuevas: bloqueada por seguridad hasta definir invitaciones/cuentas iniciales.
- Configuración pública heredada: queda como revisión/consulta, sin prometer publicación ni cambios activos.
- Proveedores externos, cobros externos, Play, deploy y producción: no se muestran como acciones disponibles ni se tocaron.

## Textos de prototipo eliminados o reemplazados

- “visual” en textos visibles.
- “guardar borrador visual”.
- “confirmar visualmente”.
- “herramienta visual”.
- “preview” / “vista previa” como acción de producto.
- “preparado/preparada” en secciones no operativas.
- “Firestore”, “backend” y lenguaje de implementación en UI visible.
- “ruta histórica” y mensajes equivalentes de desarrollo.

## Validaciones ejecutadas

| Comando | Resultado |
|---|---|
| `git status --short` | OK al inicio; limpio tras commit. |
| `node --test tests/*.test.js` | OK: 222 tests pasan. |
| `npm --prefix functions run build` | OK: `node --check index.js`. |
| `bash tools/guards/check_architecture.sh` | OK. |
| `bash tools/guards/check_ui_quality.sh` | OK. |
| `bash tools/guards/check_no_production_release.sh` | OK. |
| `./gradlew assembleDebug --offline` | Falló por entorno antes de compilar: Gradle/Kotlin no acepta Java `25.0.2`. |
| `./gradlew lintDebug --offline` | Falló por entorno antes de compilar: Gradle/Kotlin no acepta Java `25.0.2`. |
| `git diff --check` | OK. |

## Riesgos restantes

1. Falta validar Android build/lint con JDK compatible.
2. Falta prueba manual real en celular para confirmar legibilidad y navegación con datos reales.
3. Alta de cuentas nuevas sigue bloqueada hasta definir flujo seguro de invitación/Auth.
4. Las funciones externas no definidas siguen bloqueadas y no deben activarse sin decisión explícita.

## Dictamen final

**B) UI ADMIN MEJORADA, PERO AÚN NO APROBABLE PARA USO HUMANO.**

Motivo: se corrigieron textos y experiencias de prototipo en Admin, se mantuvieron acciones reales y bloqueos claros, y pasan tests/guards Node. No corresponde A porque Android `assembleDebug` y `lintDebug` no pudieron ejecutarse por Java `25.0.2` del entorno, y todavía falta validación manual en celular con JDK compatible y datos reales.
