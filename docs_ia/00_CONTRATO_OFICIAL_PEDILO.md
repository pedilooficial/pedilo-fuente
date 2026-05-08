# CONTRATO OFICIAL — PÉDILO FINAL

## IDENTIDAD
Pédilo = sistema de mensajería local y gestión de pedidos.
Pedido = núcleo del sistema.
Marketplace = interfaz de captura para facilitar el uso del cliente.
La app NO es solo catálogo de locales.
La app usa experiencia marketplace para que el usuario entienda rápido cómo pedir.

## PRINCIPIO CENTRAL
La DB guarda datos.
La app interpreta funcionalmente esos datos.
La UI los presenta de forma humana.
La UI no muestra DB cruda.
La UI no inventa datos comerciales.

## CAPAS
UI/screens
→ router
→ state
→ render
→ UI

services:
- DB/Supabase
- WhatsApp
- APIs externas

Los services no acceden a UI ni state directo.

## STATE
Existe un solo state.
No se permite:
- state.carrito separado
- state.cliente directo fuera de pedido
- estados paralelos por rol
- mutaciones directas

El pedido vive en:

pedido = {
  numero,
  tipo,
  categoria,
  subcategoria,
  local,
  local_libre,
  origen,
  destino,
  items,
  cliente,
  horario,
  pago,
  que_retira,
  que_lleva,
  confirmacion
}

## TIPOS DE PEDIDO
Tipos oficiales:

1. marketplace
   Usuario elige:
   categoria → subcategoria → local → productos → carrito → confirmacion

2. compra_libre
   Usuario carga lo que necesita comprar.
   No depende de productos de DB.
   Puede usar local_libre.

3. retirar_envio
   Usuario indica origen, qué retirar y destino/referencia.

4. pedir_repartidor
   Usuario pide un repartidor para llevar algo de un punto a otro.

## FLUJO BASE
Toda entrada empieza por:

loading → inicio

Desde inicio:

marketplace:
inicio → categorias/subcategorias → locales → productos → carrito → confirmacion → pedido_enviado

compra_libre:
inicio → compra_libre → confirmacion → pedido_enviado

retirar_envio:
inicio → retirar_envio → confirmacion → pedido_enviado

pedir_repartidor:
inicio → pedir_repartidor → confirmacion → pedido_enviado

## DB → UI
La app debe reflejar lo que existe en DB:

categorias
subcategorias
locales
productos
opciones
ofertas
app_config

Reglas:
- categorías pueden usar íconos/fallback visual de la app si la DB no trae imagen.
- locales solo muestran imagen/descripción/oferta si la DB lo trae.
- productos solo muestran imagen/descripción/oferta/precio si la DB lo trae.
- si un dato comercial falta, no se muestra un hueco visible.
- nunca mostrar “sin imagen”, “null”, “undefined” o placeholders técnicos al usuario.

## MARKETPLACE COMO INTERFAZ
El usuario ve una experiencia marketplace:
- categorías claras
- locales claros
- productos claros
- ofertas si existen
- carrito como resumen

Pero internamente Pédilo sigue siendo sistema de mensajería/pedidos.

## ROLES
Roles futuros oficiales:

cliente
comercio
repartidor
admin
superadmin/dueño

Regla:
Los roles NO crean arquitecturas paralelas.
Los roles usan el mismo principio:

UI del rol
→ router
→ state
→ services

Cada rol puede tener pantallas propias, pero no puede romper:
- state único
- router controlado
- services aislados
- pedido como núcleo

## CLIENTE
Puede:
- iniciar pedido
- elegir tipo
- navegar marketplace
- cargar compra libre
- pedir retiro/envío
- confirmar pedido

## COMERCIO
Puede:
- ver pedidos asignados
- gestionar productos/local
- actualizar estado
- no modifica lógica global de cliente

## REPARTIDOR
Puede:
- ver entregas asignadas
- cambiar estado operativo
- no decide reglas comerciales
- no modifica pedido fuera de acciones permitidas

## ADMIN
Puede:
- gestionar categorías
- subcategorías
- locales
- productos
- ofertas
- usuarios/roles
- configuración

No debe mezclarse con flujo cliente.

## SUPERADMIN / DUEÑO
Puede:
- controlar todo el sistema
- revisar auditoría
- gestionar permisos
- aprobar cambios críticos

## REGLAS ANTI-COLAPSO
- no mezclar capas
- no cambios parciales en bloques
- no modificar state/router sin control
- no lógica de negocio en screens
- services no acceden a UI/state
- cada rol se agrega como bloque cerrado
- cada tipo de pedido se agrega como bloque cerrado
- marketplace no puede romper mensajería
- mensajería no puede romper marketplace

## CLASIFICACIÓN DE ERRORES
ERROR CRÍTICO:
- app no inicia
- import/export roto
- router apunta a pantalla inexistente
- state queda inconsistente
- marketplace deja de funcionar
- pedido no puede confirmarse
- DB no carga ni fallback responde

WARNING:
- rol incompleto
- pantalla futura no implementada
- dato opcional faltante
- imagen ausente
- oferta ausente
- número WhatsApp pendiente
- UI mejorable

PENDIENTE CONTROLADO:
- admin avanzado
- repartidor avanzado
- comercio avanzado
- Play Store
- pagos
- notificaciones

## CONTROL
Antes de cerrar cualquier bloque:

npm run check:exports
npm run test
npm run seguro
npm run diagnostico

La IA NO aprueba cambios.
El sistema NO aprueba cambios.
La aprobación final SIEMPRE es humana.

## CRITERIO DE CIERRE
Un bloque se cierra solo si:
- no rompe flujo actual
- no rompe tests
- no rompe imports/exports
- respeta DB real
- respeta state único
- respeta roles futuros
- queda documentado
- humano aprueba
