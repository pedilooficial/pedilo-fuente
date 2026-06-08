# Pédilo

Base operativa de pedidos públicos y operación interna V1. La fuente de verdad del sistema es el pedido en `/orders`: nace desde Cloud Functions, se sigue públicamente por número y se opera internamente por roles.

## Estado real confirmado

### Público Android

- App pública funcional en Compose.
- Catálogo conectado a Firestore en lectura (`/stores` y `products`).
- Creación pública de pedidos vía Cloud Functions:
  - `createLocalOrder`
  - `createPlusOrder`
- Tracking público operativo vía `getPublicOrderTracking`.
- Sin login público ni escrituras directas del cliente sobre `/orders`.

### Backend / Cloud Functions

- Backend operativo V1 en `functions/index.js`.
- Nacimiento de pedidos públicos con contrato vivo inicial, `trackingNumber`, estado público y persistencia en `/orders`.
- Flujo operativo V1 con acciones reales sobre pedidos:
  - Admin
  - Store / Local
  - Driver / Repartidor
- Eventos e incidencias operativas persistidos bajo `/orders/{id}/events` y `/orders/{id}/incidents`.

### Login interno y roles

- Login interno por Firebase Auth.
- Resolución de rol desde `/users/{uid}`.
- Roles operativos válidos en código actual:
  - `admin`
  - `store`
  - `driver`

### Admin

- Operación sobre pedidos: real.
- Lectura y clasificación operativa de pedidos: real.
- Acciones operativas sobre pedidos: reales, con callable backend y auditoría de eventos.
- Configuración: visual / informativa.
- Alta de roles / accesos: visual o no persistente en el estado actual; no aplica cambios reales de configuración.

### Store / Local

- Store operativo V1 real.
- Puede tomar acciones operativas del flujo vivo del pedido.

### Driver / Repartidor

- Driver operativo V1 real.
- Ve pedidos disponibles / asignados y ejecuta acciones operativas del flujo vivo.

### Core Android

- Estructura con modelos, puertos, adapters y use cases activos.
- Guards de arquitectura en `tools/guards/check_architecture.sh`.

## Flujo confirmado

1. El usuario público arma un pedido local o Botón +.
2. Android llama una Cloud Function pública.
3. La Function valida payload y contexto, crea `/orders/{orderId}` y devuelve datos de seguimiento.
4. El pedido queda disponible para operación interna según el estado vivo y el rol responsable.
5. El usuario público consulta tracking por `trackingNumber`.
6. Admin / Store / Driver operan el pedido según acciones permitidas por estado y versión.

## Arquitectura de datos

- `/users`: perfiles internos y rol activo.
- `/stores` y `products`: catálogo público.
- `/orders`: pedido vivo principal.
- `/orders/{id}/events`: historial operativo.
- `/orders/{id}/incidents`: incidencias operativas.

## Cloud Functions exportadas

Región: `southamerica-east1`.

| Function | Auth | Propósito |
|----------|------|-----------|
| `createLocalOrder` | Pública | Crea pedido de local con validación de store/productos |
| `createPlusOrder` | Pública | Crea pedido Botón + |
| `getPublicOrderTracking` | Pública | Tracking público por `trackingNumber` |
| `adminOrderAction` | Admin | Acción operativa Admin legacy dedicada |
| `operateLiveOrder` | Admin / Store / Driver | Acción operativa unificada sobre pedido vivo |

## Validación local

```bash
node --test tests/*.test.js
npm --prefix functions test
npm --prefix functions run build
bash tools/guards/check_architecture.sh
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

## Pendiente antes de producción

- Integración/validación con Firebase real o emuladores según entorno objetivo.
- Hardening adicional de reglas, operación y observabilidad.
- Release y cierre de preparación productiva.

## Configuración local

- `app/google-services.json` debe estar presente localmente para compilar y ejecutar con Firebase.
- Los reports de `reports/` sirven como bitácora, pero la fuente de verdad sigue siendo código, tests y validaciones reales.
