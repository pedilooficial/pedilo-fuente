# 10 - Informe auditoria nucleo Firebase real - Pedilo!

## 1. Resumen ejecutivo

Esta auditoria fue realizada en modo read-only sobre:

- Proyecto actual: `/home/oem/Desktop/pedilo`
- Backup auditado: `/home/oem/Desktop/pedilo_backup_2026-05-18_20-12-19`
- Documento contrato: `design/00-master/09-auditoria-readonly-nucleo-firebase-existente.md`

No se ejecuto deploy, no se escribio en Firebase, no se modificaron reglas, no se copiaron carpetas del backup y no se trajo UI vieja.

El hallazgo central es que el backup contiene un nucleo operativo mas completo que el nucleo actualmente presente en el proyecto recuperado. El backup modela pedidos publicos creados por Cloud Functions, `trackingNumber`, reserva en `order_tracking`, locales y productos en `stores/{storeId}/products`, snapshots de precio/local/productos, comunicacion WhatsApp pendiente manual, estados operativos finos, roles `admin`, `store` y `driver`, y reglas Firestore mas amplias. El proyecto actual conserva una version mas minima del contrato: `orders`, eventos, incidentes, roles basicos y funciones principales, pero sin varias piezas del contrato mas avanzado del backup.

Dictamen: **B) Parcialmente apto: sirve como referencia, pero requiere limpiar riesgos antes de integrar.**

## 2. Alcance y restricciones

Se inspeccionaron archivos de Android, dominio, repositorios, Cloud Functions, rules, indexes, tests, guards y documentacion. La auditoria evita exponer secretos: no se copia contenido de `google-services.json`, claves, API keys ni credenciales.

Restricciones cumplidas:

- No se modifico Firebase.
- No se escribieron datos.
- No se hizo deploy.
- No se modificaron `functions`, `firestore.rules`, `google-services.json` ni codigo de app.
- No se copio codigo del backup.
- Solo se genero este informe.

## 3. Archivos y carpetas auditadas

Backup:

- `README.md`
- `.firebaserc`
- `firebase.json`
- `firestore.rules`
- `firestore.indexes.json`
- `app/build.gradle.kts`
- `app/google-services.json` (solo metadatos no sensibles)
- `app/src/main/java/com/pedilo/app/data/FirebasePediloRepository.kt`
- `app/src/main/java/com/pedilo/app/data/PediloRepository.kt`
- `app/src/main/java/com/pedilo/app/domain/*`
- `app/src/main/java/com/pedilo/app/ui/PediloViewModel.kt`
- `app/src/main/java/com/pedilo/app/ui/newdelivery/*`
- `functions/src/index.ts`
- `functions/src/orderFlow.ts`
- `functions/src/catalog.ts`
- `functions/src/events.ts`
- `functions/src/operatorUsers.ts`
- `functions/src/roles.ts`
- `functions/src/validators.ts`
- `tests/*`
- `tools/guards/*`
- `tools/backfill_orders_v2.js`

Proyecto actual:

- `app/src/main/java/com/pedilo/app/data/*`
- `app/src/main/java/com/pedilo/app/domain/*`
- `functions/src/*`
- `firestore.rules`
- `firestore.indexes.json`
- `tests/*`
- `tools/guards/*`
- `README.md`

## 4. Mapa Firebase

Configuracion detectada:

- Existe `app/google-services.json` en el backup y en el proyecto actual.
- Ambos apuntan al proyecto Firebase `pediloapp-e2758`.
- El package Android detectado es `com.pedilo.app`.
- Existe bucket de Storage referenciado por configuracion, pero no se encontro `storage.rules` en el backup.
- No se detectaron service-account keys, `.pem` ni archivos de credenciales admin sueltos en el backup.

Servicios usados por codigo:

- Firestore: si, usado por Android y Functions.
- Firebase Auth: si, para operadores `admin`, `store`, `driver`.
- Cloud Functions callable: si, Node.js 20.
- Storage: aparece en configuracion del proyecto, pero no se encontro uso funcional auditado.
- Cloud Messaging: no se encontro uso directo relevante.
- Analytics/Crashlytics: no se encontro uso directo relevante.
- Realtime Database: no se encontro uso.

Dependencias Firebase en el backup Android:

- `firebase-bom`
- `firebase-auth-ktx`
- `firebase-firestore-ktx`
- `firebase-functions-ktx`
- plugin `com.google.gms.google-services`

Functions:

- `firebase-functions`
- `firebase-admin`
- runtime `nodejs20`

## 5. Mapa Firestore

Colecciones encontradas en el backup:

### `/orders`

Proposito: fuente principal de pedidos operativos.

Campos principales detectados:

- `trackingNumber`
- `status`
- `subStatus`
- `orderType`
- `storeMode`
- `paymentMode`
- `storeId`
- `driverId`
- `responsibleRole`
- `responsibleActorId`
- `exceptionLevel`
- `timeoutAt`
- `lastEventAt`
- `fallback`
- `nextFallbackAction`
- `maxAttempts`
- `attemptCount`
- `beforeProblemStatus`
- `problemNote`
- `source`
- `pricingSnapshot`
- `storeSnapshot`
- `productSnapshots`
- `requesterName`
- `items`
- `deliveryAddress`
- `contactPhone`
- `note`
- `distanceExtraCount`
- `distanceExtraTotal`
- `operationalAmountTotal`
- `nextAllowedActions`
- `availableActionsByRole`
- `adminAllowedStatuses`
- `createdAt`
- `updatedAt`

Escritura: Cloud Functions con Admin SDK. Android no debe escribir directo.

Lectura: operadores autenticados segun rol y rules. No hay lectura publica directa por rules.

Riesgo: mezcla datos publicos del cliente con datos internos operativos en el mismo documento. Para tracking publico futuro conviene exponer una vista derivada o funcion que filtre campos.

### `/orders/{orderId}/events`

Proposito: historial por pedido.

Campos: `actorId`, `actorRole`, `type`, `fromStatus`, `toStatus`, `note`, `source`, `createdAt`.

Escritura: Functions.

Lectura: operadores que pueden leer el pedido.

### `/orders/{orderId}/incidents`

Proposito: problemas y excepciones.

Campos: `actorId`, `actorRole`, `note`, `status`, `source`, `createdAt`.

Escritura: Functions.

Lectura: operadores autorizados.

### `/orders/{orderId}/communications`

Proposito: comunicaciones pendientes, especialmente WhatsApp manual.

Campos: `channel`, `type`, `to`, `operationalNumber`, `message`, `status`, `source`, `createdAt`.

Observacion: en `firestore.rules` del backup no aparece una regla explicita para esta subcoleccion. Eso implica que queda cerrada por defecto para cliente, pero debe documentarse antes de cualquier UI operativa.

### `/order_events`

Proposito: espejo global de eventos con `orderId` para consultas operativas.

Escritura: Functions.

Lectura: operadores autorizados si pueden leer el pedido asociado.

### `/order_tracking`

Proposito: reserva unica de `trackingNumber` hacia `orderId`.

Campos: `orderId`, `createdAt`.

Escritura: Functions al reservar identidad de pedido.

Riesgo: no existe regla Firestore especifica en backup, por lo que queda inaccesible al cliente. Para tracking publico futuro no debe leerse directo desde cliente salvo que se agregue contrato seguro.

### `/users`

Proposito: perfiles operativos.

Campos: `role`, `active`, `displayName`, `email`, `linkedStoreId`, timestamps.

Roles: `admin`, `store`, `driver`.

Escritura: admin por Functions o rules, segun flujo.

### `/stores`

Proposito: locales/comercios.

Campos: `id`, `name`, `description`, `address`, `phone`, `imageUrl`, `visible`, `operational`, `acceptsOrders`, `linkedStoreUserId`, `openingHours`, timestamps.

Lectura: publica si `visible == true`; administrable por admin o store vinculado.

### `/stores/{storeId}/products`

Proposito: productos del local.

Campos: `id`, `storeId`, `name`, `description`, `imageUrl`, `visible`, `available`, `hasPrice`, `price`, timestamps.

Lectura: publica si producto y local son visibles; administrable por admin o store vinculado.

### `/system_config/pricing`

Proposito: tarifas operativas.

Campos: `rainModeEnabled`, `normal`, `rain`, `distanceExtra`, `updatedAt`, `updatedBy`.

Lectura: publica para `pricing` en rules del backup.

### `/system_config/communication`

Proposito: configuracion de comunicacion operativa, especialmente WhatsApp.

Campos: `whatsappEnabled`, `whatsappNumber`, timestamps.

Lectura: operadores; escritura admin. La Function usa fallback si falta.

## 6. Modelo real de pedido

El modelo mas completo esta en:

- `functions/src/validators.ts`
- `functions/src/orderFlow.ts`
- `app/src/main/java/com/pedilo/app/domain/Order.kt`

Un pedido real del backup nace en `createOrderFlow`:

1. Valida `requesterName`, `itemsText`, `deliveryAddress`, `contactPhone`, `note`, `paymentMode`, `storeId` y productos.
2. Si hay `storeId`, busca `/stores/{storeId}`, exige `visible` y `acceptsOrders`.
3. Congela `storeSnapshot`.
4. Congela `productSnapshots` si se pidieron productos de local.
5. Lee pricing y genera `pricingSnapshot`.
6. Reserva identidad con `/order_tracking/{trackingNumber}`.
7. Crea `/orders/{orderId}`.
8. Crea evento inicial `order_created`.
9. Si comunicacion esta activa, crea comunicacion WhatsApp pendiente manual.
10. Devuelve `{orderId, trackingNumber, deliveryTotal, commercialAmountPending}`.

Campos obligatorios para crear pedido publico:

- `requesterName`
- `itemsText`
- `deliveryAddress`
- `contactPhone` numerico

Campos opcionales o contextuales:

- `note`
- `paymentMode`
- `storeId`
- `products`

Campos internos que no deberian exponerse al cliente:

- `responsibleRole`
- `responsibleActorId`
- `exceptionLevel`
- `fallback`
- `nextFallbackAction`
- `maxAttempts`
- `attemptCount`
- `beforeProblemStatus`
- `problemNote`
- `nextAllowedActions`
- `availableActionsByRole`
- `adminAllowedStatuses`
- `pricingSnapshot` completo
- `productSnapshots` con datos operativos si el pedido esta archivado

Campos publicos potenciales:

- `trackingNumber`
- estado humano derivado de `status`
- ETA derivada, no necesariamente `timeoutAt`
- resumen minimo del pedido activo
- direccion solo en pedido activo y con cuidado

## 7. Estados del pedido

Estados detectados en el backup:

- `created`
- `sent_to_store`
- `pending_external_store_confirmation`
- `accepted_by_store`
- `externally_confirmed_by_store`
- `rejected_by_store`
- `external_store_rejected`
- `external_store_unreachable`
- `preparing`
- `external_store_preparing`
- `ready_for_pickup`
- `externally_ready_for_pickup`
- `available_for_driver`
- `assigned_to_driver`
- `driver_on_way_to_store`
- `arrived_at_store`
- `waiting_store`
- `picked_up`
- `on_the_way`
- `arrived_customer`
- `payment_pending`
- `payment_confirmed`
- `delivered`
- `problem`
- `admin_review_required`
- `cancelled`
- `failed_delivery`
- `returned_to_store`
- `resolved_by_admin`

Estados vivos:

- desde `created` hasta `admin_review_required`, excluyendo terminales.

Estados terminales:

- `delivered`
- `cancelled`
- `failed_delivery`
- `returned_to_store`
- `resolved_by_admin`

Equivalencia publica recomendada:

- `created`, `sent_to_store`, `pending_external_store_confirmation`: Pedido recibido.
- `accepted_by_store`, `externally_confirmed_by_store`, `preparing`, `external_store_preparing`: Preparando.
- `ready_for_pickup`, `externally_ready_for_pickup`, `available_for_driver`, `assigned_to_driver`, `driver_on_way_to_store`, `arrived_at_store`, `waiting_store`, `picked_up`, `on_the_way`, `arrived_customer`: En camino, con matiz si hace falta.
- `payment_pending`, `payment_confirmed`: En camino o pago pendiente, segun UX.
- `delivered`: Entregado.
- `cancelled`, `failed_delivery`, `returned_to_store`, `resolved_by_admin`: Cierre no exitoso o resuelto; no exponer detalle interno crudo.
- `problem`, `admin_review_required`: Hay una demora o estamos revisando tu pedido.

Riesgo: los estados tecnicos son demasiados para UI publica. La app publica debe consumir una proyeccion humana, no enums crudos.

## 8. Tracking publico

El backup contempla `trackingNumber` y reserva unica en `/order_tracking/{trackingNumber}`. Sin embargo:

- No se encontro callable especifica para "consultar tracking publico por numero".
- No se encontro rule que permita lectura publica directa de `/order_tracking`.
- El cliente anterior mostraba recibo con `trackingNumber`, pero el seguimiento real parecia operativo/autenticado.

La regla futura de privacidad no esta completamente implementada: pedidos entregados/archivados no tienen una proyeccion publica separada que oculte productos, direccion y datos personales. El nuevo nucleo debe resolver esto antes de exponer tracking publico real.

Contrato recomendado:

- Callable publica `getPublicOrderTracking(trackingNumber)`.
- Debe resolver `order_tracking -> orderId`.
- Debe devolver solo campos publicos filtrados.
- Si el pedido esta terminal/archivado, devolver agradecimiento y accion de seguir pidiendo, sin direccion vieja, productos viejos ni datos personales.

## 9. Validacion de cliente / telefono

El telefono se valida como texto numerico en `createOrderFlow`:

- minimo 8
- maximo 15
- solo digitos

El backup crea comunicacion WhatsApp pendiente manual, pero no implementa verificacion fuerte de telefono antes de entrar al pool operativo. Tampoco se encontro coleccion `customers` o `clients` dedicada para historial del cliente, bloqueo, alertas o reputacion.

Brecha contra la regla conceptual nueva:

- Existe validacion de formato.
- No existe validacion de identidad por telefono.
- No existe flujo de cliente conocido/nuevo.
- No existe bloqueo o alerta previa por telefono.
- No existe confirmacion WhatsApp automatica antes del pool operativo.

## 10. Roles

Roles detectados:

### Publico

No usa Auth para crear pedidos. Llama `createOrder`. En eventos aparece como `actorId: public`, `actorRole: public`.

### Admin

Lee pedidos vivos, puede asignar repartidor, cambiar estados, crear/activar operadores, crear locales, editar productos, editar pricing/comunicacion.

### Store

Opera pedidos del local vinculado. Puede administrar su local/productos si `linkedStoreId` coincide. Puede pedir repartidor mediante `storeRequestDriver`.

### Driver

Lee pedidos asignados y pedidos disponibles para tomar. Puede reclamar pedido (`claimDriverOrder`), avanzar estados de reparto e informar adicional por distancia.

Otros actores internos tipados:

- `backend`
- `ai`
- `whatsapp`

Estos aparecen como `ActorRole` o `ResponsibleRole`, no como perfiles de usuario.

## 11. Cloud Functions / backend

Callables exportadas en backup:

- `createOrder`: publica, sin Auth. Crea pedido, evento, tracking y comunicacion pendiente.
- `transitionOrder`: protegida por Auth y rol. Ejecuta accion operacional.
- `assignDriver`: admin. Asigna repartidor.
- `claimDriverOrder`: driver. Toma pedido disponible.
- `adminSetStatus`: admin. Cambio controlado de estado.
- `upsertStore`: admin o store vinculado.
- `upsertProduct`: admin o store vinculado.
- `updatePricingConfig`: admin.
- `updateCommunicationConfig`: admin.
- `storeRequestDriver`: store vinculado.
- `addDistanceExtra`: admin o driver asignado.
- `createOperatorUser`: admin, crea Auth user y perfil `/users`.
- `setOperatorActive`: admin.

No se detectaron:

- `onRequest`
- triggers Firestore
- scheduled functions
- pagos externos
- WhatsApp real automatico
- tracking publico callable por numero

Riesgo: varias funciones escriben contratos reales de Firestore. Cambiarlas requiere deploy y puede romper datos reales. La reescritura debe crear adaptadores y pruebas antes de tocar deploy.

## 12. Seguridad / rules

El backup tiene reglas mas completas que el proyecto actual:

- `/users`: lectura propia o admin; escritura admin.
- `/stores`: lectura publica si visible; escritura admin o store vinculado.
- `/stores/{storeId}/products`: lectura publica si local y producto visibles; escritura admin o store vinculado.
- `/system_config/pricing`: lectura publica; resto operadores; escritura admin.
- `/orders`: lectura solo operadores autorizados; escritura directa denegada.
- `/orders/{orderId}/events` y `/incidents`: lectura por operadores autorizados; escritura denegada.
- `/order_events`: lectura por operadores autorizados; escritura denegada.

Riesgos:

- `isAdmin()` y `isOperator()` dependen de documentos `/users/{uid}`. Hay que mantener consistencia de perfiles.
- `canReadOrder` compara `order.storeId == request.auth.uid`, mientras otras partes usan `linkedStoreId` para gestionar stores. En el backup `createOrderFlow` guarda `storeId: linkedStoreUserId` para locales activos, lo que hace que `storeId` represente a veces usuario de local, no id del local comercial. Este es un riesgo semantico fuerte.
- No hay regla explicita para `order_tracking` ni `communications`; quedan cerradas por defecto, pero deben documentarse.
- No hay custom claims; todo depende de lectura Firestore de `/users`.

## 13. Notificaciones / comunicacion

Implementado parcialmente:

- Configuracion `/system_config/communication`.
- Fallback con WhatsApp habilitado y numero operativo.
- Subcoleccion `/orders/{orderId}/communications`.
- Mensajes `ticket_tracking` y `distance_extra_updated`.
- Estado `pending_manual`.

No implementado:

- Envio WhatsApp automatico.
- FCM.
- Templates formales.
- Confirmacion automatica de telefono.
- Notificacion real a repartidor/local.

Riesgo: puede confundirse "comunicacion pendiente manual" con integracion real. La reescritura debe nombrarlo explicitamente.

## 14. Pagos

El backup soporta:

- `paymentMode`: `cash` o `transfer`.
- Validacion de transferencia antes de cerrar como `delivered`.
- `payment_pending` y `payment_confirmed`.
- `commercialAmountPending` si productos no tienen precio.
- `pricingSnapshot` con reparto de montos: total envio, monto Pedilo, monto driver, adicional por distancia.
- `distanceExtraCount`, `distanceExtraTotal`, `operationalAmountTotal`.

No detectado:

- Integracion de pago online.
- Proveedor externo.
- Comprobante real.
- Liquidacion automatica.

## 15. Metricas

Detectado:

- Datos suficientes para calcular tiempos por timestamps y estados.
- Pricing y montos operativos para metricas internas.
- `order_events` global para auditoria.
- `stores.visible/name` y `products.visible/name` indexados.

No detectado:

- Coleccion dedicada de metricas.
- Agregados de ranking.
- Ganancias consolidadas.
- Rankings/ofertas/nuevos locales reales.

## 16. Reutilizable conceptualmente

- Principio de arquitectura: Android no escribe directo en `/orders`; pedidos nacen en Functions.
- `createOrder` publica sin Auth, con validacion server-side.
- `trackingNumber` y reserva en `/order_tracking`.
- Eventos obligatorios y espejo `/order_events`.
- Separacion de roles `admin`, `store`, `driver`.
- Estados operativos ricos.
- Snapshots de precio/local/producto.
- Rules que bloquean escritura directa en pedidos.
- Guards que impiden escrituras directas Android.
- Pricing y comunicacion como `system_config`.
- Modelo de stores/products visibles.

## 17. Reutilizable con adaptacion

- `FirebasePediloRepository`: util como referencia de contratos, pero no debe conectarse directo a la UI publica nueva sin capa de casos de uso.
- `PediloViewModel`: acoplado a UI vieja; rescatar intenciones, no estructura.
- `Order.kt`: buen mapa de campos, pero mezcla campos publicos e internos.
- `OrderStatus.kt`: completo para backend/operacion, no para UI publica directa.
- `orderFlow.ts`: fuente de verdad conceptual, pero requiere normalizar `storeId`, tracking publico, validacion de telefono y privacidad.
- `firestore.rules`: base util, pero requiere resolver semantica `storeId` vs usuario de local.
- `tools/backfill_orders_v2.js`: indica migracion previa, pero no debe ejecutarse sin plan y backup.

## 18. No debe traerse

- UI vieja `app/src/main/java/com/pedilo/app/ui/newdelivery/*`.
- Navegacion vieja.
- Pantallas operativas viejas.
- Recursos visuales legacy.
- Textos operativos viejos para publico.
- Cualquier mock o demo de `DemoDeliveryData`.
- El acoplamiento directo de ViewModel viejo con la app publica nueva.
- Scripts de backfill sin revision manual.
- Compatibilidad artificial que oculte ambiguedades de datos.

## 19. Riesgos detectados

1. **Ambiguedad de `storeId`**: a veces representa local comercial, a veces usuario operativo vinculado (`linkedStoreUserId`). Este punto debe limpiarse antes de cualquier integracion real.
2. **Tracking publico incompleto**: existe `trackingNumber`, pero no una consulta publica segura y filtrada.
3. **Privacidad de pedidos terminales**: no hay proyeccion especifica para ocultar direccion/productos/datos personales en pedidos entregados o archivados.
4. **Telefono no validado como identidad**: solo hay validacion de formato.
5. **Comunicacion WhatsApp no automatica**: se escribe pendiente manual; no asumir integracion real.
6. **Proyecto actual mas minimo que backup**: integrar contra el actual sin revisar el backup perderia contratos importantes.
7. **Rules sin `order_tracking` explicito**: correcto por defecto cerrado, pero no sirve para tracking publico directo.
8. **`communications` sin rule explicita**: cerrada por defecto, pero no documentada para operadores.
9. **Estados tecnicos excesivos para publico**: necesitan mapper humano.
10. **Scripts de backfill**: potencialmente destructivos si se ejecutan sin plan.
11. **Datos publicos e internos en un mismo documento**: requiere capa de filtrado.
12. **No hay tests de tracking publico real**: antes de integrar deben agregarse.

## 20. Contrato recomendado para el nuevo nucleo

Arquitectura futura recomendada:

```text
UI publica nueva
-> casos de uso publicos
-> nucleo nuevo limpio
-> adaptador Firebase compatible
-> Firebase real existente
```

Contratos minimos:

### Crear pedido publico

Entrada:

- origen: `home`, `shop`, `plus_buy`, `plus_pickup_shipping`, `local`
- nombre
- telefono
- direccion
- items
- observaciones
- forma de pago
- `storeId` comercial opcional
- productos opcionales con `productId` y `quantity`

Salida:

- `orderId`
- `trackingNumber`
- total envio
- importe comercial pendiente si aplica

Regla: crear siempre via Function, nunca desde Android directo.

### Tracking publico

Entrada:

- `trackingNumber`

Salida:

- estado humano
- progreso publico
- ETA si aplica
- acciones publicas permitidas
- datos filtrados segun estado vivo/terminal

Regla: no exponer campos internos ni datos personales de pedidos archivados.

### Catalogo publico

Lectura:

- locales visibles
- productos visibles y disponibles
- categorias derivadas

Regla: usar `stores` y `products`, pero resolver antes semantica entre local comercial y usuario operativo.

### Operacion

Mantener:

- roles `admin`, `store`, `driver`
- funciones operativas
- rules que bloquean escrituras directas
- eventos e incidentes

## 21. Plan progresivo de integracion

1. Congelar informe y confirmar con revision humana.
2. Definir diccionario final de colecciones y campos: especialmente `storeId`, `storeUserId`, `commercialStoreId`.
3. Crear tests de contrato para Firebase real sin tocar produccion.
4. Escribir capa de dominio nueva en Android sin UI vieja.
5. Escribir adaptador Firebase read-only para catalogo publico (`stores/products`).
6. Integrar tracking publico filtrado mediante nueva callable o adaptador seguro.
7. Integrar creacion de pedido real con `createOrder`, despues de validar telefono y privacidad.
8. Agregar guards anti-escritura directa y anti-exposicion de campos internos.
9. Auditar rules antes de deploy.
10. Solo despues, planificar deploy controlado si una Function nueva es necesaria.

## 22. Preguntas pendientes

1. En datos reales, `storeId` de `/orders` representa id de local, uid del operador store, o ambos segun version?
2. Existen documentos reales en `/order_tracking` para todos los pedidos existentes?
3. Hay pedidos antiguos sin `trackingNumber`?
4. La app real necesita leer tracking publico sin Auth o con verificacion por telefono?
5. Que politica exacta aplica a pedidos entregados: cuando se archivan y que datos se purgan/ocultan?
6. WhatsApp sera manual, automatizado o fuera de alcance inicial?
7. `customers/clients` debe crearse nuevo o ya existe fuera del codigo auditado?
8. Hay datos reales de `stores/products` que puedan alimentar Home/Tienda?
9. Que flujo de validacion de telefono se aprueba antes del pool operativo?
10. Se mantiene `order_events` global o se reemplaza por consultas/subcolecciones?

## 23. Dictamen final

**B) Parcialmente apto: sirve como referencia, pero requiere limpiar riesgos antes de integrar.**

Justificacion:

El backup es una fuente tecnica valiosa: contiene modelo de pedido, reglas, Functions, roles, eventos, trackingNumber, catalogo y pricing. Sin embargo, no debe reutilizarse de forma directa porque mezcla datos publicos e internos, no implementa tracking publico filtrado, no valida telefono como identidad, tiene ambiguedad fuerte en `storeId`, y contiene UI vieja que no debe volver. La app publica nueva debe conectarse mediante un nucleo limpio y un adaptador Firebase compatible, con tests y guards antes de cualquier deploy.
