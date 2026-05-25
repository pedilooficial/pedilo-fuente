# Auditoría final de usuario público real

Fecha: 2026-05-25

## Dictamen

B) Parcial, requiere una verificación manual puntual antes de pasar a roles.

El usuario público real quedó operativo para catálogo, Local, Botón + Comprar y seguimiento público real. Durante la auditoría se detectó y corrigió un bug de navegación en subcategorías: la card visible de Pizzería Roma solo abría desde una zona pequeña inferior. Ahora toda la card abre el Local.

Queda pendiente repetir manualmente el flujo Botón + Retiro / Envío completo con ingreso humano de datos, porque la automatización por ADB no permitió cargar con precisión todos los campos en esta corrida. No se detectó bug estructural del flujo en código/tests.

## Pruebas realizadas

- Home abrió con catálogo real y sin crash.
- Tienda cargó con entrada de seguimiento y categorías.
- Tienda -> Pizzas mostró Pizzería Roma desde Firebase real.
- Pizzería Roma abrió Local y mostró productos reales.
- Local permitió agregar Empanadas reales al carrito.
- Local no escribió pedido antes de Confirmar.
- Local creó un pedido real solo al tocar Confirmar pedido.
- Doble tap en Confirmar Local creó un solo documento.
- Ticket Local mostró número real.
- Seguimiento desde ticket Local abrió el seguimiento común y mostró estado real.
- Botón + Comprar permitió confirmar un pedido real.
- Botón + Comprar no escribió antes de Confirmar.
- Doble tap en Confirmar Compra creó un solo documento.
- Ticket Compra mostró número real.
- Seguimiento consultado no modificó el pedido.
- Subcategoría Pizzas fue reprobada tras corrección: tocar título/área superior de la card abre Local.
- Rotación permanece bloqueada en portrait por manifest.

## Pedidos reales creados en auditoría

- `ZzD21mqE...` / `PDL-ZZD21M`: `source=public_local`, `status=created`, `publicStatus=Pedido recibido`.
- `I7X2v1gV...` / `PDL-I7X2V1`: `source=public_plus_buy`, `status=created`, `publicStatus=Pedido recibido`.

No se reportan datos personales completos en este documento.

## Verificaciones Firebase

- Antes de confirmar Local, el último pedido seguía siendo `PDL-JISHTW`.
- Al entrar a confirmación Local, seguía sin crearse pedido nuevo.
- Después de confirmar Local, `PDL-ZZD21M` tuvo `tracking_count=1`.
- Antes de confirmar Compra, el último pedido seguía siendo `PDL-ZZD21M`.
- Después de confirmar Compra, `PDL-I7X2V1` tuvo `tracking_count=1`.
- Luego de consultar seguimiento, ambos pedidos auditados seguían en `status=created`.

## Seguridad revisada

- UI pública no contiene escritura directa a `/orders`.
- UI pública no muestra textos técnicos prohibidos por grep.
- `/orders` sigue cerrado a cliente directo según tests de rules.
- Catálogo real sigue verificando `pizzeria-roma` y 5 productos esperados.
- No se tocó `users`.
- No se tocó `roles`.
- No se tocó `payments`.
- No se tocó WhatsApp.
- No hubo deploy.

## Logcat

Se guardó logcat de auditoría en `reports/final-public-real-audit/logcat-final-public-real-audit.txt` y no se commitea.

Búsquedas sin hallazgos atribuibles a Pédilo:

- `FATAL EXCEPTION`
- `AndroidRuntime`
- `ANR`
- `NullPointerException`
- `IndexOutOfBoundsException`
- `IllegalStateException` de `com.pedilo.app`

## Validaciones ejecutadas

- `GOOGLE_APPLICATION_CREDENTIALS="$HOME/.config/pedilo/firebase-admin.json" GOOGLE_CLOUD_PROJECT=pediloapp-e2758 node tools/verify_public_catalog.js`
- `bash tools/guards/check_architecture.sh`
- `bash tools/guards/check_ui_quality.sh`
- `node --test tests`
- `npm --prefix functions run build`
- `./gradlew compileDebugKotlin`
- `./gradlew assembleDebug`
- `git diff --check`

## Bugs encontrados

1. En Tienda -> Pizzas, la card visual de Pizzería Roma no abría desde toda la superficie. Solo una zona inferior respondía.

## Bugs corregidos

1. `RelatedStoreCard` en `PublicShopSubcategory.kt` ahora aplica `clickable(role = Role.Button, onClick = onView)` a toda la fila visible.
2. Se agregó test para asegurar que la card de subcategoría abre desde toda la card visible.

## Pendientes

- Repetir con ingreso manual humano el flujo Botón + Retiro / Envío hasta ticket real para cerrar la certificación al 100%.
- No avanzar a roles hasta completar esa repetición puntual.
