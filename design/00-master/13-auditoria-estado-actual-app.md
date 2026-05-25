# 13 - Auditoria total del estado actual de la app Pedilo

## 1. Resumen ejecutivo

Esta auditoria se realizo sobre el repo vivo actual:

```text
/home/oem/Desktop/pedilo
```

No se uso el backup viejo para completar huecos, no se modifico codigo de app, no se tocaron datos reales, no se toco `app/google-services.json`, no se modificaron `functions`, `firestore.rules`, `firestore.indexes.json`, `firebase.json` ni `.firebaserc`, y no se ejecuto deploy.

Hallazgo principal:

- La app actual abre una UI publica nueva y cerrada desde `MainActivity -> PublicApp()`.
- Esa UI publica usa estado local, listas internas y datos visuales/locales. No llama hoy al repositorio Firebase ni al ViewModel real.
- En el repo actual si existen piezas de nucleo/data/domain/functions/Firebase, y compilan, pero no son la entrada activa de la UI publica actual.
- Firebase esta vinculado/configurado en el repo actual: existe `app/google-services.json` ignorado, plugin google-services condicionado, dependencias Firebase, `firebase.json`, `.firebaserc`, reglas, indices y Cloud Functions.
- `reports/` esta ignorado y contiene evidencia/capturas, no codigo activo.

Dictamen: **B) App casi limpia: quedan restos/piezas reales puntuales a decidir antes de construir, pero no contaminan hoy la UI publica activa.**

## 2. Commit actual auditado

HEAD auditado:

```text
19ad73a9c82285cd68532b89180e4e950e6e3bdd
```

Log inicial revisado:

```text
19ad73a Add real Firebase core audit report
cce6dd6 Audit and harden public user flow before real integration
ebb69e0 Polish final visual details before public audit
777de5c Fix public input validation, navigation layers and splash timing
250f5ae Polish splash and launcher brand entry
9babb3f Polish final public cleanup and conventions flow
7edda43 Polish public app final UX and home destinations
dbe3fba Polish public app coherence and human UX
c73de22 Build public Local order flow in Compose
f2ccf1e Build public plus order flows in Compose
99e2a21 Build public Conventions flow
8f59c27 Polish public navigation and search copy
b99320d Add public convergence map
2e9fca6 Build public tracking and unified search flow
2a8768c Fix native back navigation and system bars
```

## 3. Estado git inicial

`git status --short` al inicio:

```text
?? design/00-master/09-auditoria-readonly-nucleo-firebase-existente.md
?? design/00-master/11-contrato-nuevo-nucleo-real.md
?? design/00-master/12-lectura-funcional-nucleo-viejo.md
```

Estos tres archivos ya estaban no trackeados antes de esta auditoria. No fueron usados como base de implementacion ni deben incluirse en el commit de este informe salvo autorizacion separada.

## 4. Estructura del repo actual

Carpetas principales observadas:

- `app/`: aplicacion Android Compose.
- `app/src/main/java/com/pedilo/app/ui/publicuser/`: UI publica actual activa.
- `app/src/main/java/com/pedilo/app/ui/`: pantalla/ViewModel operativo anterior o paralelo; no es la entrada actual.
- `app/src/main/java/com/pedilo/app/data/`: repositorio Firebase actual.
- `app/src/main/java/com/pedilo/app/domain/`: modelos y validacion de dominio actual.
- `functions/`: Cloud Functions TypeScript actuales.
- `design/00-master/`: documentacion maestra y auditorias.
- `design/01-screens/`, `design/02-flows/`, `design/public-user-approved-mockups/`, `design/Splash/`: referencias visuales/flujo.
- `tests/`: tests Node para reglas, funciones, guards y contratos.
- `tools/guards/`: guards de arquitectura y calidad UI.
- `reports/`: evidencia/capturas/logs ignorados por git.

Archivos Android principales actuales:

```text
app/src/main/java/com/pedilo/app/MainActivity.kt
app/src/main/java/com/pedilo/app/PediloApp.kt
app/src/main/java/com/pedilo/app/data/FirebasePediloRepository.kt
app/src/main/java/com/pedilo/app/data/PediloRepository.kt
app/src/main/java/com/pedilo/app/domain/*
app/src/main/java/com/pedilo/app/ui/PediloScreen.kt
app/src/main/java/com/pedilo/app/ui/PediloViewModel.kt
app/src/main/java/com/pedilo/app/ui/components/*
app/src/main/java/com/pedilo/app/ui/publicuser/*
app/src/main/java/com/pedilo/app/ui/theme/*
```

## 5. UI publica activa

La entrada runtime actual es:

```kotlin
MainActivity -> setContent { PublicApp() }
```

Pantallas publicas activas:

- `PublicApp.kt`: router manual, splash, historial, reglas de back y convergencias.
- `PublicHome.kt`: Home publico, shell, header, bottom bar, accesos rapidos, ofertas y locales.
- `PublicShop.kt`: Tienda principal, buscador y carga de seguimiento.
- `PublicShopSearch.kt`: resultados/listados por busqueda o entrada desde Home/Tienda.
- `PublicShopSubcategory.kt`: resultados por subcategoria.
- `PublicShopTracking.kt`: seguimiento publico visual comun.
- `PublicConventions.kt`: Convenciones, informacion, reclamo y carga de seguimiento.
- `PublicPlus.kt`: flujo del boton `+`: elegir, comprar, retiro/envio, confirmacion y ticket.
- `PublicLocal.kt`: local publico, producto, carrito, datos, confirmacion y ticket.
- `PublicTheme.kt`: tema visual publico.

Rutas activas en `PublicApp`:

```text
Home
Plus
PlusBuy
PlusPickupShipping
PlusConfirmation
PlusTicket
Shop
Conventions
ConventionsInfo
ConventionsClaim
ConventionsTrackingEntry
PublicTracking
ShopSubcategory
ShopSearch
ShopTracking
HomeListing
Local
LocalProductDetail
LocalCart
LocalData
LocalConfirmation
LocalTicket
```

Flujos existentes:

- Home -> buscador/listados -> Local.
- Home -> ofertas/locales -> Local.
- Tienda -> buscador -> Local.
- Tienda -> subcategoria -> Local.
- Tienda -> tracking -> seguimiento visual.
- Convenciones -> informacion.
- Convenciones -> reclamo visual/local.
- Convenciones -> carga de numero -> seguimiento comun.
- Boton `+` -> comprar -> confirmacion -> ticket -> seguimiento comun.
- Boton `+` -> retiro/envio -> confirmacion -> ticket -> seguimiento comun.
- Local -> producto -> carrito -> datos -> confirmacion -> ticket -> seguimiento comun.

Datos actuales de UI:

- Home, Tienda, Search, Subcategory y Local usan listas/modelos locales dentro de los archivos Compose.
- Plus y Local usan estado local con `remember`/`mutableStateListOf`.
- Tracking usa datos visuales derivados/locales en `PublicShopTracking`.
- No se observo llamada desde `ui/publicuser` hacia `data`, `domain`, `PediloRepository`, `FirebasePediloRepository`, `PediloViewModel` ni Firebase.

Estado:

- UI publica: lista como experiencia visual/navegable actual.
- Datos: locales/mock/visuales, no persistentes.
- Tracking: visual, no tracking persistente real.
- Tickets: visuales/locales, no creacion real de pedido.
- Reclamos: visuales/locales, no backend.

Convergencias:

- Tienda tracking y Convenciones tracking convergen en `PublicShopTrackingScreen`.
- Plus ticket converge a `PublicRoute.PublicTracking`.
- Local ticket converge a `PublicRoute.PublicTracking`.
- Busqueda Home/Tienda, HomeListing y Subcategory convergen a pantallas de resultados que pueden abrir `Local`.
- Home/ofertas/locales tambien convergen a `Local`.

Limpieza de estado y Back:

- `goHome()` y `goShop()` limpian historial, salvo advertencia si hay carrito local activo.
- Al salir de Local con carrito activo se abre confirmacion; al confirmar se limpia `localCart`, `localOrderPlaced`, `pendingLocalExit` e historial.
- `LocalTicket -> onHome` limpia carrito y estado de pedido local.
- `BackHandler(enabled = route != Home)` aplica `logicalParent`.
- Si Back sale de Local con carrito activo hacia una ruta no local, pide confirmacion antes de vaciar.

## 6. Data/domain/core actual

Carpetas presentes:

- `app/src/main/java/com/pedilo/app/data/`
- `app/src/main/java/com/pedilo/app/domain/`
- No se observo carpeta `core/`.
- No se observo carpeta `usecase/`.
- No se observo carpeta `repository/` separada; el repositorio esta en `data/`.

Contenido actual:

- `PediloRepository.kt`: contrato con crear pedido publico, login/salida operador, observar perfil, ordenes y eventos, correr acciones, asignar driver y setear estado admin.
- `FirebasePediloRepository.kt`: implementacion Firebase con Auth, Firestore y Functions.
- `Order.kt`, `OrderDraft.kt`, `OrderEvent.kt`, `OrderStatus.kt`, `UserProfile.kt`, `UserRole.kt`: modelos de dominio.
- `PublicOrderValidation.kt`: validacion/formulario publico para el flujo anterior/paralelo.
- `PediloViewModel.kt`: ViewModel que consume `PediloRepository`.
- `PediloScreen.kt`: pantalla operativa/publica anterior/paralela que consume dominio/ViewModel.

Estado de activacion:

- Existe nucleo real minimo en el repo actual: **si, como codigo trackeado y compilable**.
- Existe nucleo real activo en la UI publica actual: **no observado**.
- `MainActivity` no instancia `FirebasePediloRepository`, `PediloViewModel` ni `PediloScreen`.
- La UI publica activa no importa `data`, `domain` ni Firebase.
- `PediloScreen` y `PediloViewModel` existen, compilan y son testeables, pero no son la entrada runtime actual.

Riesgo:

- No hay evidencia de contaminacion activa de la UI publica por este nucleo.
- Si se construye un nucleo nuevo, estas piezas existentes deben revisarse/decidirse antes de reutilizarlas o eliminarlas.
- Cualquier eliminacion requiere autorizacion futura: hoy no se borro nada.

## 7. Firebase/vinculacion actual

Archivos de vinculacion/configuracion presentes:

```text
app/google-services.json
firebase.json
.firebaserc
firestore.rules
firestore.indexes.json
```

No se imprimio contenido sensible de `app/google-services.json`.

`app/google-services.json`:

- Existe en el workspace.
- Esta ignorado por git.
- Debe conservarse para no tener que vincular todo desde cero.
- No fue tocado.

Gradle/Firebase Android:

- `build.gradle.kts` raiz declara `com.google.gms.google-services` version `4.4.2` con `apply false`.
- `app/build.gradle.kts` aplica el plugin google-services solo si existe `app/google-services.json`.
- `app/build.gradle.kts` declara Firebase BOM `33.7.0`.
- Dependencias actuales: Auth KTX, Firestore KTX, Functions KTX y coroutines play-services.
- Existe task `checkFirebaseConfig` ligada a `preBuild`.

Interpretacion:

- Firebase esta vinculado/configurado en el repo actual.
- Esa vinculacion es infraestructura necesaria y no debe tocarse sin autorizacion.
- La UI publica activa no usa Firebase hoy.
- El codigo `FirebasePediloRepository` si usa Firebase si fuera instanciado.

## 8. Functions actual

Archivos actuales:

```text
functions/package.json
functions/package-lock.json
functions/tsconfig.json
functions/src/events.ts
functions/src/index.ts
functions/src/orderFlow.ts
functions/src/roles.ts
functions/src/validators.ts
```

Exports detectados en `functions/src/index.ts`:

- `createOrder` con `onCall`.
- `transitionOrder` con `onCall`.
- `assignDriver` con `onCall`.
- `adminSetStatus` con `onCall`.

Flujos detectados:

- Creacion de orden publica en `orders`.
- Eventos en subcoleccion `events`.
- Incidentes en subcoleccion `incidents`.
- Roles `store`, `driver`, `admin`.
- Transiciones: picked up, on the way, delivered, cancel, report/resolve problem, asignacion driver, set status admin.

Scripts:

- `npm run build`: `tsc`.
- `npm run serve`: build + emulators.
- `npm run deploy`: build + `firebase deploy --only functions,firestore`.

Estado:

- Functions forman parte del repo actual y estan trackeadas.
- No se ejecuto deploy, serve ni scripts de functions.
- Android no depende de `functions/` para compilar localmente, pero `FirebasePediloRepository` llama `createOrder`, `transitionOrder`, `assignDriver` y `adminSetStatus` si se usa.
- Deben considerarse infraestructura/nucleo existente en repo, no UI publica activa.

## 9. Documentacion actual

Documentos observados en `design/00-master/`:

- `00-estado-actual.md`: estado historico/operativo del usuario publico en etapas previas.
- `01-reglas-globales.md`: reglas duras vigentes para usuario publico, Firebase, deploy y anti-maqueta.
- `02-mapa-de-fases.md`: mapa historico de fases.
- `03-criterios-validacion.md`: comandos y criterios de validacion.
- `04-plantilla-fase-codex.md`: plantilla para fases futuras.
- `05-revision-visual-pendiente.md`: revision historica de Tienda principal.
- `06-indice-visual-aprobado.md`: referencias visuales aprobadas.
- `07-prompt-retoma-codex.md`: prompt historico de retoma.
- `08-mapa-convergencias-publicas.md`: contrato de convergencias publicas.
- `09-auditoria-readonly-nucleo-firebase-existente.md`: documento no trackeado al inicio; historico/read-only.
- `10-informe-auditoria-nucleo-firebase-real.md`: informe trackeado previo, historico y con mencion al backup.
- `11-contrato-nuevo-nucleo-real.md`: documento no trackeado al inicio; contrato futuro, no autorizacion de implementacion.
- `12-lectura-funcional-nucleo-viejo.md`: documento no trackeado al inicio; referencia historica, no backlog automatico.
- `RESUMEN_CORRECCIONES.md`: resumen historico de correcciones del plano visual.
- `pedilo_20260522_182410.mp4`: evidencia visual historica.

Rol documental:

- Documentos 00-08: marco visual/publico y reglas de construccion.
- Documento 10: informe historico ya existente; menciona backup y no debe mezclarse con esta auditoria actual.
- Documentos 09/11/12: existen en el workspace como no trackeados; no deben asumirse como parte del HEAD auditado ni commitearse aqui.
- Ningun documento autoriza por si solo construir nucleo nuevo en esta fase.

Referencia futura:

- Para UI publica: usar 01, 03, 06 y 08.
- Para construccion futura de nucleo: cualquier contrato debe activarse por prompt/autorizacion posterior, no por este informe.

## 10. Reports

`.gitignore` contiene:

```text
reports/
app/google-services.json
functions/lib/
functions/node_modules/
functions/.runtimeconfig.json
```

Estado:

- `reports/` aparece como ignorado (`!! reports/`) al pedir estado con ignorados.
- `reports/` no aparece en `git status --short` normal.
- Contiene capturas/logs de certificacion visual y auditorias.
- No contiene codigo activo para compilar la app.
- No debe mezclarse con `app/` ni commitearse.

## 11. Tests/guards

Guards existentes:

- `tools/guards/check_architecture.sh`: protege arquitectura contra recursos/patrones prohibidos.
- `tools/guards/check_ui_quality.sh`: protege contra `plan_`, `PlanScreen`, `PlanPhoneScreen`, `TapZone`, bottom "Casa" y otros terminos.

Tests existentes:

```text
tests/architecture_guard.test.js
tests/firestore_rules.test.js
tests/guard_negative.test.js
tests/incidents.test.js
tests/no_legacy_terms.test.js
tests/order_flow.test.js
tests/public_order.test.js
tests/public_validation.test.js
tests/role_permissions.test.js
tests/ui_quality_guard.test.js
tests/viewmodel_public_order.test.js
```

Validaciones ejecutadas:

```text
bash tools/guards/check_architecture.sh -> passed
bash tools/guards/check_ui_quality.sh -> passed
node --test tests -> 11 tests, 11 pass
./gradlew compileDebugKotlin -> BUILD SUCCESSFUL
./gradlew assembleDebug -> BUILD SUCCESSFUL
git diff --check -> sin salida, OK
```

Nota operativa:

- La primera ejecucion Gradle dentro del sandbox fallo por no poder escribir el lock de wrapper en `/home/oem/.gradle`.
- Se reejecuto con autorizacion escalada para usar la cache Gradle local.
- No hubo deploy ni acceso a datos reales.

## 12. Busquedas de riesgo

Busqueda anti-legado/mockup:

```text
grep -R "newdelivery|PlanScreen|PlanPhoneScreen|TapZone|R\.drawable\.plan_|Casa|Salir de Pedilo" ...
```

Resultado:

- Solo aparecieron los terminos prohibidos dentro de `tools/guards/check_ui_quality.sh`.
- No aparecieron como UI visible activa ni codigo runtime de app.
- No es problematico; es el guard declarando lo que debe bloquear.

Busqueda Firebase/core:

```text
grep -R "Firebase|Firestore|collection|document|orders|trackingNumber|createOrder|transitionOrder" ...
```

Resultado relevante:

- `FirebasePediloRepository.kt` usa Firebase Auth, Firestore y Functions.
- `FirebasePediloRepository.kt` llama `createOrder`, `transitionOrder`, `assignDriver`, `adminSetStatus`.
- `PediloScreen.kt`/`PediloViewModel.kt` manejan `orders` y flujos operativos.
- `functions/src/*` define Firestore, roles, `orders`, eventos/incidentes y callable functions.
- `trackingNumber` no aparecio en app/functions actuales.

Interpretacion:

- Hay nucleo/Firebase real en el repo actual.
- No hay evidencia de que la UI publica activa lo consuma hoy.
- No es contaminacion activa, pero es material a decidir antes de iniciar nucleo nuevo.

Busqueda mock/placeholder/backend/convergencia:

```text
grep -R "mock|de muestra|placeholder|backend|tracking persistente|convergencia|pantalla comun" ...
```

Resultado:

- Aparecen `placeholder` como nombres de parametros/textos en inputs Compose.
- No aparecio `mock`, `de muestra`, `backend`, `tracking persistente`, `convergencia` ni `pantalla comun` como codigo problematico.
- Los placeholders son UI visible normal de campos, no problema arquitectonico.

## 13. Que esta activo

Activo como runtime visible:

- `MainActivity.kt`.
- `ui/publicuser/PublicApp.kt`.
- Pantallas `PublicHome`, `PublicShop`, `PublicShopSearch`, `PublicShopSubcategory`, `PublicShopTracking`, `PublicConventions`, `PublicPlus`, `PublicLocal`, `PublicTheme`.
- Recursos Android necesarios para compilar y mostrar la app.
- Gradle Android y plugin google-services durante build.

Activo como codigo compilable del repo:

- `data/`, `domain/`, `ui/PediloScreen.kt`, `ui/PediloViewModel.kt`.
- `functions/src/*`.
- `firestore.rules`, `firestore.indexes.json`, `firebase.json`, `.firebaserc`.

No activo en runtime publico observado:

- `PediloScreen`.
- `PediloViewModel`.
- `FirebasePediloRepository`.
- Cloud Functions, salvo que se deployen/llamen por otra entrada no observada aqui.

## 14. Que esta documentado

Documentado pero no ejecutado por esta auditoria:

- Reglas de UI publica.
- Convergencias publicas.
- Auditorias historicas de Firebase/nucleo.
- Contrato futuro/no automatico de nucleo nuevo.
- Lectura funcional historica del nucleo viejo.

Este informe no convierte esos documentos en tareas automaticas.

## 15. Que debe conservarse

Debe conservarse salvo autorizacion explicita posterior:

- `app/google-services.json`: vinculacion local necesaria, ignorada por git.
- `.firebaserc`, `firebase.json`, `firestore.rules`, `firestore.indexes.json`: vinculacion/infraestructura actual.
- `functions/`: infraestructura/nucleo existente; no tocar sin decision.
- `reports/`: evidencia ignorada; no borrar ni commitear.
- `ui/publicuser/`: UI publica cerrada/activa actual.
- `tools/guards/` y `tests/`: red de seguridad actual.

## 16. Que podria eliminarse solo si se autoriza despues

Podria evaluarse, no ejecutar automaticamente:

- `app/src/main/java/com/pedilo/app/ui/PediloScreen.kt`, si se confirma que no sera reutilizado.
- `app/src/main/java/com/pedilo/app/ui/PediloViewModel.kt`, si se decide reconstruir nucleo/ViewModel desde cero.
- `app/src/main/java/com/pedilo/app/data/FirebasePediloRepository.kt`, si se decide reemplazar el adaptador.
- `app/src/main/java/com/pedilo/app/data/PediloRepository.kt`, si se decide nuevo contrato.
- `app/src/main/java/com/pedilo/app/domain/*`, si se decide nuevo modelo de dominio.
- `functions/src/*`, solo con una decision clara sobre backend nuevo y riesgo de deploy.

Nada de esto debe borrarse en esta fase.

## 17. Que no debe tocarse

No tocar sin autorizacion explicita:

- Firebase productivo.
- `app/google-services.json`.
- `.firebaserc`.
- `firebase.json`.
- `firestore.rules`.
- `firestore.indexes.json`.
- `functions/`.
- `reports/`.
- Datos reales.
- Deploy.
- Backup viejo.

## 18. Respuestas directas

1. ¿La app actual tiene nucleo real activo?
   - Tiene nucleo real presente y compilable. No esta activo en la UI publica runtime actual.

2. ¿La UI publica depende de nucleo real o sigue visual/mock?
   - Sigue visual/local/mock. No depende de `data/domain/Firebase` hoy.

3. ¿Que carpetas actuales son UI publica cerrada?
   - `app/src/main/java/com/pedilo/app/ui/publicuser/` y soporte visual de `ui/theme/`/recursos usados.

4. ¿Que carpetas actuales son configuracion/vinculacion?
   - `app/google-services.json`, `.firebaserc`, `firebase.json`, `firestore.rules`, `firestore.indexes.json`, Gradle.

5. ¿Que carpetas actuales son data/domain/functions?
   - `app/src/main/java/com/pedilo/app/data/`, `app/src/main/java/com/pedilo/app/domain/`, `functions/`.

6. ¿Hay restos activos que contaminen?
   - No se observo contaminacion activa de UI publica. Si hay piezas reales existentes que deben decidirse antes de construir nucleo nuevo.

7. ¿Hay algo que deba borrarse antes de construir nucleo nuevo?
   - No hay obligacion de borrar en esta fase. Podria requerirse limpieza selectiva futura si se decide nucleo desde cero.

8. ¿Hay algo que no debe tocarse porque es vinculacion necesaria?
   - Si: `app/google-services.json`, `.firebaserc`, `firebase.json`, reglas, indices y configuracion Gradle.

9. ¿Firebase esta vinculado en el repo actual?
   - Si.

10. ¿Hay deploy o escritura real configurada pero no ejecutada?
    - Si, `functions/package.json` tiene script `deploy`; no fue ejecutado.

11. ¿reports/ esta correctamente ignorado?
    - Si.

12. ¿La app actual compila y pasa tests?
    - Si: guards, tests Node, `compileDebugKotlin`, `assembleDebug` y `diff --check` pasaron.

13. ¿Cual es el punto limpio real para construir?
    - La UI publica activa en `ui/publicuser/`, con `MainActivity -> PublicApp()`, conservando vinculacion Firebase pero decidiendo antes que hacer con `data/domain/functions` actuales.

14. ¿Que NO debe mezclarse con el backup viejo?
    - Todo: esta auditoria no debe importar tareas, bugs, modelos ni decisiones del backup viejo. El backup no fue usado para completar huecos.

15. ¿Que proximo paso recomienda la auditoria, sin construir todavia?
    - Definir por escrito si el nucleo nuevo reutilizara, reemplazara o aislara las piezas actuales `data/domain/functions`. Luego emitir un prompt acotado para solo esa decision, sin tocar deploy ni datos.

## 19. Dictamen

**B) App casi limpia: quedan restos puntuales a eliminar o decidir antes de construir.**

Justificacion:

- La UI publica activa esta limpia respecto de Firebase/core: `MainActivity` monta `PublicApp()` y `ui/publicuser` no importa repositorios ni Firebase.
- La app compila y pasa tests/guards.
- No hay `newdelivery`, `PlanScreen`, `PlanPhoneScreen`, `TapZone`, `R.drawable.plan_`, `Casa` ni `Salir de Pedilo` en runtime app; solo aparecen terminos prohibidos en guards.
- Firebase esta correctamente vinculado y debe conservarse.
- Pero el repo actual contiene `data/domain/PediloScreen/PediloViewModel/functions` reales y trackeados. No contaminan hoy la UI publica, pero no conviene iniciar un nucleo nuevo sin decidir formalmente si se reutilizan, se reemplazan o se aislan.

## 20. Proximo paso recomendado

Antes de construir:

```text
Emitir una decision documental corta:
1. conservar vinculacion Firebase actual;
2. mantener UI publica intacta;
3. decidir si data/domain/functions actuales se reutilizan como referencia, se reemplazan, o se aislan;
4. prohibir deploy/datos reales;
5. recien despues crear el primer prompt de nucleo nuevo.
```

Este informe no autoriza implementacion, limpieza, borrado, deploy ni cambios de backend.
