# 11 — Contrato del nuevo núcleo real — Pédilo!

## 1. Propósito

Este documento define el contrato rector para construir el nuevo núcleo real de Pédilo!, usando como referencia histórica/técnica el informe de auditoría del núcleo/Firebase anterior, sin copiar el backup viejo ni migrar su arquitectura.

Este documento **no es un prompt de ejecución**.

Su función es dejar ordenado:

- qué manda el núcleo;
- qué representa la UI;
- qué contratos mínimos deben existir;
- qué riesgos no se pueden ignorar;
- qué decisiones siguen pendientes;
- qué no se debe implementar sin autorización específica;
- qué orden lógico debe respetarse cuando se avance a código.

Los prompts de trabajo para Codex se darán después, bloque por bloque, bajo decisión explícita del usuario.

---

## 2. Principio absoluto

```text
El núcleo manda.
La UI representa.
```

La UI pública no debe decidir reglas reales de pedido, estados, permisos, validación, tracking, privacidad, roles ni asignación operativa.

La UI pública debe:

- capturar intención del usuario;
- pedir datos;
- validar lo mínimo de experiencia;
- mostrar estados humanos;
- mostrar errores humanos;
- representar tickets, seguimiento y confirmaciones;
- llamar casos de uso públicos.

El núcleo real debe:

- crear pedidos;
- validar reglas;
- resolver estados;
- proteger datos;
- controlar qué se expone públicamente;
- decidir acciones permitidas;
- hablar con Firebase mediante adaptadores;
- proteger contratos reales;
- mantener consistencia entre roles.

---

## 3. Relación correcta entre UI, núcleo y Firebase

Arquitectura futura deseada:

```text
UI pública nueva
↓
Casos de uso públicos
↓
Núcleo real nuevo
↓
Adaptador Firebase compatible
↓
Firebase existente
```

La UI pública nueva ya fue construida y auditada como experiencia visual/mock. A partir de esta etapa, debe conectarse progresivamente a un núcleo real nuevo, sin absorber lógica vieja ni decidir reglas de dominio.

---

## 4. Rol del backup anterior

El backup anterior se usa como referencia, no como base de construcción directa.

Analogía aprobada:

```text
Estamos construyendo una casa nueva.
Si no sabemos cómo resolver algo, miramos la casa del vecino como referencia.
Pero no mudamos la casa del vecino ni copiamos sus paredes rotas.
```

Aplicado a Pédilo:

```text
Backup viejo = referencia técnica / archivo histórico.
Informe 10 = mapa de observaciones.
Firebase existente = infraestructura/proyecto ya vinculado.
Núcleo nuevo = construcción limpia.
UI pública nueva = representación visible.
```

Regla:

```text
El backup viejo es referencia, no código fuente para migrar.
```

---

## 5. Qué se puede reutilizar

Se puede reutilizar conceptualmente:

- proyecto Firebase existente;
- configuración ya vinculada;
- permisos útiles;
- contratos seguros;
- nombres de colecciones útiles;
- funciones existentes si son seguras;
- reglas de roles si están correctamente definidas;
- modelo histórico de estados como referencia interna;
- trackingNumber como concepto;
- stores/products como catálogo si el contrato queda claro;
- eventos/auditoría como concepto.

No significa copiar código viejo.

---

## 6. Qué no se debe traer

No debe traerse:

- UI vieja;
- navegación vieja;
- pantallas operativas viejas;
- ViewModels viejos acoplados a UI;
- rutas antiguas;
- compatibilidad artificial;
- recursos visuales legacy;
- textos antiguos;
- datos mock confundidos con reales;
- scripts de backfill sin autorización;
- parches;
- carpetas del backup copiadas encima del proyecto actual.

---

## 7. Estado confirmado desde la auditoría

Según el informe `10-informe-auditoria-nucleo-firebase-real.md`, el backup contiene un núcleo operativo más completo que el núcleo recuperado del proyecto actual.

Se detectó que el backup incluye, como referencia técnica:

- pedidos públicos creados por Cloud Functions;
- `trackingNumber`;
- reserva en `/order_tracking`;
- catálogo de locales y productos en `/stores/{storeId}/products`;
- snapshots de precio/local/productos;
- comunicación WhatsApp pendiente manual;
- estados operativos finos;
- roles `admin`, `store` y `driver`;
- reglas Firestore más amplias.

Dictamen del informe:

```text
B) Parcialmente apto: sirve como referencia, pero requiere limpiar riesgos antes de integrar.
```

Por lo tanto:

```text
No se puede integrar ciegamente.
No se puede copiar directo.
Sí se puede usar como referencia técnica controlada.
```

---

## 8. Riesgos centrales que condicionan el nuevo núcleo

### 8.1 Ambigüedad de `storeId`

El informe detectó que `storeId` puede representar:

- local comercial;
- usuario operativo vinculado;
- o ambos según versión.

Este punto debe resolverse antes de crear integración real.

No se debe implementar núcleo real sin definir claramente:

```text
commercialStoreId
storeUserId
storeId legacy
linkedStoreId
```

o los nombres finales que se aprueben.

### 8.2 Tracking público incompleto

Existe `trackingNumber`, pero no hay contrato seguro completo para consulta pública filtrada.

El nuevo núcleo debe crear o definir una forma segura de seguimiento público.

### 8.3 Datos públicos e internos mezclados en `/orders`

El documento `/orders` contiene datos del cliente junto con datos operativos internos.

La UI pública no debe leer `/orders` crudo.

Debe existir proyección, mapper o callable que filtre.

### 8.4 Pedidos terminales sin proyección segura

Pedidos entregados/archivados no deben exponer:

- dirección vieja;
- productos viejos;
- teléfono;
- datos personales;
- historial interno;
- problemas internos;
- campos operativos.

Debe mostrar una respuesta humana mínima, por ejemplo:

```text
Gracias por tu pedido.
Podés seguir pidiendo cuando quieras.
```

### 8.5 Teléfono no validado como identidad

El backup valida formato, no identidad.

Regla conceptual ya definida:

```text
El teléfono es la clave única de validación del cliente.
```

Pero todavía no está resuelto cómo se validará realmente.

### 8.6 WhatsApp no es integración automática real

El backup contiene comunicación pendiente manual, no WhatsApp automático completo.

No asumir WhatsApp real hasta que se apruebe un bloque específico.

---

## 9. Decisiones pendientes obligatorias

Estas decisiones no se deben inventar.

Deben resolverse antes o durante bloques específicos con autorización del usuario.

### 9.1 Identidad de local

```text
¿Qué representa definitivamente storeId?
¿Se separa commercialStoreId de storeUserId?
¿Qué campo se mantiene para compatibilidad?
```

### 9.2 Tracking público

```text
¿Se crea callable pública nueva?
¿Se adapta una existente?
¿Se requiere teléfono además de trackingNumber?
¿Qué se muestra en pedidos activos?
¿Qué se muestra en terminales?
```

### 9.3 Cliente/teléfono

```text
¿Existe colección customers/clients real?
¿Debe crearse?
¿Cómo se guarda historial por teléfono?
¿Cómo se manejan alertas o bloqueos?
¿Cómo se valida WhatsApp?
```

### 9.4 WhatsApp

```text
¿Será manual en etapa inicial?
¿Será automático después?
¿Qué proveedor se usará?
¿Se verifica teléfono antes de entrar al pool?
```

### 9.5 Privacidad y archivo

```text
¿Cuándo un pedido pasa a archivado?
¿Qué campos se ocultan?
¿Qué campos se purgan?
¿Qué puede consultar el cliente después de entregado?
```

### 9.6 Catálogo real

```text
¿Los stores/products reales ya alcanzan para alimentar Home/Tienda?
¿Cómo se mapean categorías públicas?
¿Cómo se marcan ofertas y nuevos locales?
```

---

## 10. Contratos mínimos del nuevo núcleo

### 10.1 Crear pedido público

El núcleo debe exponer un contrato de creación de pedido.

Entrada conceptual:

```text
origin
requestType
customerName
customerPhone
deliveryAddress
items
notes
paymentMethod
store reference opcional
products opcionales
```

Origen posible:

```text
home
shop
plus_buy
plus_pickup_shipping
local
```

Salida conceptual:

```text
orderId
trackingNumber
publicStatus
ticketSummary
```

Reglas:

- Android no debe escribir directo en `/orders`.
- Todo pedido real nace por caso de uso / función / núcleo controlado.
- La UI no decide estado inicial real.
- La UI no crea trackingNumber por su cuenta.
- No se confirma pedido real con placeholders.
- No se confirma pedido real con datos incompletos.

---

### 10.2 Tracking público seguro

Entrada conceptual:

```text
trackingNumber
phone opcional si se aprueba
```

Salida conceptual:

```text
publicOrderStatus
humanMessage
progress
eta opcional
actionsAllowed
safeSummary
```

Debe ocultar:

```text
campos internos
responsibleRole
responsibleActorId
driverId si no corresponde
reglas operativas
incidentes internos
historial completo
datos personales en terminales
productos/dirección en pedidos archivados
```

Estados públicos sugeridos:

```text
Pedido recibido
Preparando
En camino
Entregado
Estamos revisando tu pedido
Cancelado / cerrado de forma humana
```

La UI nunca debe mostrar enums técnicos crudos.

---

### 10.3 Catálogo público

Debe permitir consultar:

```text
locales visibles
productos visibles
productos disponibles
categorías públicas
ofertas
nuevos locales
datos horarios
demora estimada
estado abierto/cerrado
```

Reglas:

- no exponer datos operativos internos del local;
- no confundir usuario operador con comercio visible;
- no depender de mock una vez conectado real;
- no mezclar categorías incoherentes.

---

### 10.4 Validación de cliente

Contrato pendiente, pero el núcleo debe contemplar:

```text
phone as customer key
known customer
new customer
alerts
blocks
review required
verification required
```

Regla conceptual aprobada:

```text
Todo pedido, sin importar origen, pasa por validación inicial antes de entrar al flujo operativo.
```

Pendiente definir implementación exacta.

---

### 10.5 Roles operativos

El núcleo debe contemplar roles:

```text
admin
store
driver
public
backend/ai/whatsapp como actores internos si corresponde
```

Regla:

```text
Los roles operativos no pertenecen a la UI pública.
```

La UI pública no debe cargar permisos de admin/store/driver.

---

## 11. Contrato de privacidad pública

La respuesta pública del núcleo debe ser siempre filtrada.

La UI pública puede mostrar:

- número de pedido;
- estado humano;
- mensaje claro;
- ETA si aplica;
- local visible si aplica;
- resumen mínimo activo;
- acciones públicas permitidas.

La UI pública no debe mostrar:

- datos internos;
- roles responsables;
- reglas de asignación;
- historial completo;
- incidentes internos;
- teléfono guardado;
- dirección vieja en pedidos terminales;
- productos viejos en pedidos archivados;
- pricingSnapshot completo;
- productSnapshots internos;
- campos de auditoría;
- eventos internos.

---

## 12. Contrato de eventos/auditoría

El núcleo debe registrar eventos de pedido.

La UI pública no debe escribir eventos directamente.

Eventos conceptuales:

```text
order_created
status_changed
incident_created
communication_created
driver_assigned
store_confirmed
payment_updated
delivery_completed
```

Los eventos son para auditoría interna/operativa, no para exposición pública directa.

---

## 13. Contrato de estados

El núcleo puede manejar estados técnicos finos.

La UI pública solo debe recibir estados humanos.

Ejemplo:

```text
created / sent_to_store / pending_external_store_confirmation
→ Pedido recibido

preparing / external_store_preparing
→ Preparando

assigned_to_driver / picked_up / on_the_way
→ En camino

delivered
→ Entregado

problem / admin_review_required
→ Estamos revisando tu pedido
```

Este mapper debe vivir en núcleo/casos de uso, no en pantallas sueltas.

---

## 14. Reglas de implementación futura

Cuando se empiece código, cada bloque debe cumplir:

- objetivo exacto;
- alcance limitado;
- validaciones obligatorias;
- no deploy salvo autorización;
- no Firebase productivo sin permiso explícito;
- no tocar UI vieja;
- no copiar backup;
- commit único;
- reporte final;
- detenerse al terminar.

---

## 15. Orden recomendado de implementación

### Fase 1 — Contratos y tipos base

Crear tipos limpios del núcleo nuevo:

- pedido público;
- request de creación;
- tracking público;
- cliente;
- local público;
- producto público;
- estado público;
- errores humanos.

Sin conectar Firebase todavía.

### Fase 2 — Adaptador catálogo read-only

Conectar lectura segura de:

```text
stores
stores/{storeId}/products
```

Objetivo:

```text
Home/Tienda/Local dejan de depender de mock para catálogo.
```

Sin crear pedidos.

### Fase 3 — Tracking público seguro

Definir o crear contrato seguro:

```text
trackingNumber → public tracking response
```

No exponer `/orders` crudo.

Puede requerir callable nueva, pero no hacer deploy sin autorización.

### Fase 4 — Crear pedido real controlado

Conectar flujos públicos a creación real de pedido:

- Botón + Comprar;
- Botón + Retiro/Envío;
- Local.

Solo cuando estén resueltos:

- `storeId`;
- validación mínima;
- privacidad;
- respuesta pública.

### Fase 5 — Validación de teléfono/cliente

Implementar cliente conocido/nuevo, alertas, verificación.

### Fase 6 — Roles operativos

Recién después reconstruir Admin/Local/Repartidor sobre el núcleo nuevo.

---

## 16. Qué no autoriza este documento

Este documento no autoriza:

- implementar núcleo real automáticamente;
- hacer deploy;
- modificar Firebase;
- cambiar reglas;
- crear funciones;
- tocar datos reales;
- copiar código del backup;
- borrar arquitectura sin prompt;
- activar WhatsApp;
- crear pagos;
- reconstruir roles.

Cualquier implementación debe venir de un prompt específico dado por el usuario.

---

## 17. Criterio de éxito del nuevo núcleo

El nuevo núcleo será correcto si:

```text
la UI solo representa;
el núcleo decide;
Firebase existente se conserva;
los contratos son claros;
los datos públicos están filtrados;
los estados técnicos no se exponen crudos;
los pedidos reales nacen controlados;
los roles operativos quedan separados;
el backup no se copia;
los riesgos detectados se resuelven antes de producción.
```

---

## 18. Dictamen de este contrato

Este documento deja preparado el marco para construir el núcleo nuevo, pero no cierra decisiones pendientes.

Dictamen:

```text
Apto como contrato rector inicial, condicionado a resolver storeId, tracking público, privacidad de pedidos terminales y validación de teléfono antes de integración real.
```

No se debe avanzar a código real sin convertir cada fase en un prompt específico y acotado.
