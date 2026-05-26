# Cierre final usuario publico real

## Resumen ejecutivo

Dictamen: A) Usuario publico real limpio y cerrado para iniciar roles.

El usuario publico queda certificado para la etapa previa a roles: carga catalogo real, navega Home/Tienda/Plus/Local/Seguimiento, crea pedidos unicamente desde pantallas de confirmacion, consulta tracking publico filtrado y no expone operaciones internas. No se construyeron roles, Admin, pagos ni WhatsApp.

## HEAD auditado

- HEAD auditado: `c7046ab Harden confirm submit locking`
- Base previa relevante: `87d3d5a Prepare home banner for future managed image`

## Estado del entorno

- `rg` disponible en: `/home/oem/.vscode/extensions/openai.chatgpt-26.519.32039-linux-x64/bin/linux-x86_64/rg`
- No fue necesario instalar `ripgrep`.
- La verificacion de catalogo real fallo dentro del sandbox por DNS, pero paso fuera del sandbox con red permitida.

## Validaciones ejecutadas

- `bash tools/guards/check_architecture.sh`: OK
- `bash tools/guards/check_ui_quality.sh`: OK
- `node --test tests`: OK, 11/11
- `npm --prefix functions run build`: OK
- `./gradlew compileDebugKotlin`: OK
- `./gradlew assembleDebug`: OK
- `git diff --check`: OK
- `GOOGLE_APPLICATION_CREDENTIALS="$HOME/.config/pedilo/firebase-admin.json" GOOGLE_CLOUD_PROJECT=pediloapp-e2758 node tools/verify_public_catalog.js`: OK fuera del sandbox

Resultado catalogo real:

```text
store:pizzeria-roma:exists=true
products:5:empanadas,gaseosa,muzzarella,napolitana,promo-dia
expected-products-present=true
```

## Flujos revisados

- Home abre con marca publica, buscador, accesos, ofertas, locales disponibles, banner y bottom bar.
- Tienda abre y muestra buscador, consulta de seguimiento y categorias.
- Subcategoria Pizzas muestra `Pizzeria Roma` como local real.
- Local abre desde Pizzas y muestra productos reales del catalogo.
- Boton + abre con opciones `Quiero comprar` y `Hacer un retiro / envio`.
- Seguimiento real probado previamente con `PDL-9MOV5J`, mostrando `Pedido recibido`.

## Anti-demo / anti-mock

- Busqueda en UI publica activa: sin coincidencias para `demo`, `mock`, `de muestra`, `fallback`, `Casa`, `Salir de Pedilo`, `backend`, `Firebase`, `callable`, `exception`, `stacktrace` ni `tracking persistente`.
- Coincidencias restantes estan en tests, guardas, scripts de verificacion/seed o dependencias vendorizadas bajo `tools/node_modules`; no son UI visible.
- No hay fallback local de catalogo activo en UI publica.

## Anti-escritura directa

- Busqueda directa en `app/src/main/java/com/pedilo/app/ui/publicuser` para `collection("orders")` / `collection('orders')`: vacia.
- Local, Boton + Comprar y Boton + Retiro/Envio delegan creacion a use cases/callables solo desde Confirmar.
- Seguimiento usa callable filtrada y no escribe pedidos.

## Doble tap / duplicados

- Bug puntual corregido en `c7046ab`: el bloqueo `isSubmitting` ahora se activa inmediatamente al tocar Confirmar en Local y Boton +, antes de lanzar la coroutine.
- Tests de flujo de Local y Plus pasaron.
- No se detecto escritura previa a Confirmar ni camino directo a `/orders` desde UI publica.

## Prueba manual

APK reinstalado con:

```text
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.pedilo.app/.MainActivity
```

Prueba minima realizada:

- Home abre sin crash.
- Tienda abre sin crash.
- Pizzas muestra Pizzeria Roma.
- Local abre sin crash.
- Boton + abre sin crash.
- Seguimiento esta disponible desde Tienda.
- No aparece `Casa`.
- No aparece `Salir de Pedilo`.

## Logcat / stress

- En la auditoria interrumpida se ejecuto `adb shell monkey -p com.pedilo.app -v 300`.
- Monkey finalizo sin abortar la app.
- Logcat filtrado no mostro `FATAL EXCEPTION`, `AndroidRuntime`, `ANR`, `NullPointerException`, `IllegalStateException` ni `IndexOutOfBoundsException` atribuibles a `com.pedilo.app`.
- El dispositivo reporto tombstones nativos del sistema durante monkey; no se identificaron como crash Java de Pédilo.

## Bugs encontrados

- Ventana minima de doble tap porque `isSubmitting` se marcaba dentro de la coroutine.

## Bugs corregidos

- `isSubmittingPlusOrder` y `isSubmittingLocalOrder` se activan en el evento de Confirmar antes de lanzar la coroutine.

## Bugs pendientes

- Ninguno detectado en el alcance del usuario publico.

## No tocado

- No se construyeron roles.
- No se construyo Admin.
- No se tocaron pagos.
- No se toco WhatsApp.
- No se modificaron Firebase Rules.
- No se modificaron Functions.
- No hubo deploy.
