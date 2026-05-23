# Reglas globales — Pédilo usuario público

## Reglas duras

- No usar screenshots como UI runtime.
- No usar `plan_*.png` en runtime.
- No usar `R.drawable.plan_`.
- No usar `PlanScreen`, `PlanPhoneScreen` ni `TapZone`.
- No usar hotspots transparentes con coordenadas fijas.
- No pegar mockups como pantallas navegables.
- No avanzar varias fases juntas.
- No tocar Firebase productivo.
- No hacer deploy.
- No mezclar otros roles.
- No crear Admin, Store, Driver ni interfaces internas.
- No escribir “Casa” en bottom navigation.
- Bottom nav pública: solo Inicio, botón central `+`, Tienda.

## Construcción visual

Las imágenes aprobadas son plano visual, no recurso runtime.

Toda pantalla debe construirse con componentes reales:

```text
Column
Row
LazyColumn
Card
Text
TextField / BasicTextField
Button
IconButton
Canvas
Image solo para logo/fotos reales
```

## Regla de pedido/local

El carrito de un local pertenece a un solo local.
Si el usuario intenta salir del local con carrito activo, debe aparecer advertencia/confirmación.

## Regla de seguimiento público

Tienda y Convenciones convergen al mismo seguimiento público.
Convenciones solo tiene una pantalla de carga de número; no crea tracking separado.

## Regla de pedidos entregados/archivados

Si un número corresponde a un pedido entregado o archivado, no se deben mostrar detalles viejos al usuario público.
Debe mostrarse agradecimiento y opción de seguir pidiendo.
