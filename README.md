# Pédilo

Base operativa para pedidos públicos y operación interna. El pedido es el centro del sistema: los pedidos públicos nacen en Cloud Functions y quedan guardados en Firestore con estado inicial.

## Estado confirmado (código actual)

### Cliente público Android

- Usuario público completo en Compose (Home, Tienda, Local, Botón +, Convenciones, seguimiento).
- Catálogo conectado a Firestore (`stores`, `products`) en lectura.
- Creación de pedidos públicos solo vía Cloud Functions (`createLocalOrder`, `createPlusOrder`).
- Seguimiento público por número vía `getPublicOrderTracking`.
- Sin login público ni escritura directa en `/orders`.

### Equipo interno

- Login con Firebase Auth.
- Resolución de rol desde `/users/{uid}` (`admin`, `store`, `driver`).
- Rol Admin entra a shell visual read-only (`AdminApp`); sin operación viva ni resolución real.

### Núcleo `core/`

- Modelos, puertos, use cases, adapters Firebase y tests de contrato.
- Guards de arquitectura en `tools/guards/check_architecture.sh`.

## Parcial / pendiente

| Área | Estado |
|------|--------|
| Pedido Vivo Universal (transiciones, asignación, eventos) | No implementado |
| Local interno operativo | No implementado |
| Driver / Repartidor operativo | No implementado |
| Admin operativo (resolución, acciones reales) | Shell visual read-only parcial |
| Subcolecciones `/orders/{id}/events` e `/incidents` | No expuestas en Functions actuales |
| Functions legacy (`createOrder`, `transitionOrder`, `assignDriver`, `adminSetStatus`) | No existen en el repo actual |

## Arquitectura de datos

- `/users`: perfiles de operadores (`admin`, `store`, `driver`).
- `/stores` y subcolección `products`: catálogo público en lectura.
- `/orders`: pedidos creados por Admin SDK desde Functions; el cliente Android no escribe directo.

## Flujo público confirmado

1. Android arma el borrador del pedido (local o Botón +).
2. Android llama la Callable correspondiente:
   - `createLocalOrder` — pedido desde un local con carrito.
   - `createPlusOrder` — compra o retiro/envío del Botón +.
3. La Function valida, crea `/orders/{orderId}` y devuelve `{ orderId, trackingNumber, publicStatus, ... }`.
4. Android muestra confirmación con número de seguimiento.
5. Seguimiento: Android llama `getPublicOrderTracking` con el número PDL-XXXXXX.

## Flujo operadores (estado actual)

1. El operador inicia sesión con Firebase Auth.
2. La app lee `/users/{uid}` y exige rol válido.
3. **Admin**: shell visual de operación/configuración/alta de roles; lectura read-only de pedidos vía `AdminOrdersUseCase`.
4. **Store / Driver**: placeholders; sin operación viva implementada.

## Cloud Functions exportadas

Región: `southamerica-east1`.

| Function | Auth | Propósito |
|----------|------|-----------|
| `createLocalOrder` | Pública | Crea pedido de local con validación de store/productos |
| `createPlusOrder` | Pública | Crea pedido Botón + (compra o retiro/envío) |
| `getPublicOrderTracking` | Pública | Consulta seguimiento por `trackingNumber` |

## Comandos de validación

```bash
node --test tests/*.test.js
./gradlew :app:compileDebugKotlin
cd functions && npm run build
bash tools/guards/check_architecture.sh
```

## Configuración local

- `app/google-services.json` no está en Git (ver `.gitignore`); debe colocarse localmente para compilar/ejecutar.
- Los reports `.md` vigentes de `reports/` pueden servir como bitácora técnica; la fuente de verdad sigue siendo código + tests + validaciones reales.
