# Pédilo

Base operativa para pedidos públicos y operación interna. El pedido es el centro del sistema: nace en Cloud Functions, queda guardado en Firestore con estado inicial y siempre recibe un evento inicial.

## Arquitectura

- Cliente público Android: crea pedidos sin login y sin escribir directo en Firestore.
- Operadores: `store`, `driver` y `admin` usan Firebase Auth y perfil en `/users/{uid}`.
- `/users`: solo operadores.
- `/orders`: pedidos creados por Admin SDK desde Functions.
- `/orders/{orderId}/events`: historial obligatorio.
- `/orders/{orderId}/incidents`: problemas y excepciones.

## Flujo público

1. Android arma un `OrderDraft` con detalle, dirección, teléfono y nota opcional.
2. Android llama la Callable Function `createOrder`.
3. `createOrder` valida campos mínimos, crea `/orders/{orderId}` y crea el evento `order_created`.
4. La Function devuelve `{ orderId }`.
5. Android muestra confirmación con seguimiento.

## Flujo operadores

1. El operador inicia sesión con Firebase Auth.
2. La app lee `/users/{uid}` y exige role válido.
3. La app escucha pedidos vivos según el rol:
   - `admin`: todos los pedidos vivos.
   - `driver`: pedidos asignados por `driverId`.
   - `store`: pedidos asignados por `storeId`.
4. Toda acción operativa pasa por Functions protegidas.

## Functions

- `createOrder`: pública, sin auth, crea pedido y evento inicial.
- `transitionOrder`: protegida, valida role y transición.
- `assignDriver`: protegida, solo admin.
- `adminSetStatus`: protegida, solo admin y correcciones controladas.

## Comandos

```bash
./gradlew :app:compileDebugKotlin
cd functions && npm run build
bash tools/guards/check_architecture.sh
```
