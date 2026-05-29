# 17 — Contrato de mapeo operativo de pedidos para Admin — Pédilo!

## 1. HEAD auditado

```text
4b8b54e Connect admin operation read only orders
```

Fecha de auditoría de datos reales: 2026-05-29.

Alcance de este bloque: contrato de clasificación read-only para **Admin → Operación**. Sin cambios de UI, sin escrituras, sin deploy.

---

## 2. Objetivo del contrato

Responder con datos reales de `/orders`:

```text
¿En qué parte de Admin → Operación debe caer cada pedido?
```

El contrato define:

- señales reales observadas en Firestore;
- reglas conservadoras de mapeo a buckets de la UI operativa;
- categorías que deben permanecer vacías hasta tener señal;
- qué puede mostrar **Pedido #____** en modo lectura;
- helpers puros (`AdminOperationOrderClassification`) y tests de contrato.

No autoriza acciones operativas ni re-clasificación optimista.

---

## 3. Estado actual de Admin read-only

Confirmado en `4b8b54e`:

| Pieza | Estado |
|-------|--------|
| Lectura de lista | `FirebaseAdminOrdersAdapter.getOrdersReadOnly()` → colección `orders`, solo `.get()` |
| Detalle | `getOrderDetailReadOnly(orderId)` → documento por id, solo `.get()` |
| Caso de uso | `GetAdminOperationOrdersUseCase` delega al port |
| UI | `AdminApp` carga pedidos en `LaunchedEffect`; cuenta en tarjetas de Operación |
| Mapeo UI actual (temporal) | `todayOrders()` = todos; `activeOrders()` filtra `created/received/pending` o `publicStatus` con "recib"; `problemOrders()` busca "problema"/"reclamo" en `publicStatus` |
| Detalle en listas | `orderDetailEntriesFor` muestra como máximo **un** pedido real por subsección (`orders.firstOrNull()`) |

La UI **no** aplica aún el contrato formal de este documento. Este bloque solo documenta y fija helpers/tests.

---

## 4. Campos reales auditados

Auditoría read-only sobre **22** documentos en `/orders` (proyecto `pediloapp-e2758`). Sin volcado de JSON ni datos personales.

### 4.1 Campos relevados

| Campo | Presencia en muestra | Notas |
|-------|----------------------|-------|
| `id` (documento) | 22/22 | Solo referencia interna de auditoría |
| `trackingNumber` | 17/22 | Ausente en 5 cancelados legado |
| `publicOrderNumber` | 10/22 | Coincide con tracking en pedidos nuevos |
| `status` | 22/22 | Valores reales: `created`, `cancelled` |
| `publicStatus` | 13/22 con texto; 9/22 `null`/vacío | Texto vivo: `Pedido recibido` |
| `source` | 20/22 con valor; 5 `null` | Ver §10 |
| `requestType` | 6/22 | `buy`, `pickup_shipping`, o ausente |
| `storeId` | 4/22 | Solo pedidos `public_local` recientes |
| `storeName` | 4/22 | Solo pedidos `public_local` recientes |
| `createdAt` | 22/22 | Presente |
| `updatedAt` | 22/22 | Presente |
| `isPublicCreated` | 10/22 `true` | 12 legado sin flag |
| `subtotal` | 4/22 | Local con ítems |
| `total` | 4/22 | Local con ítems |
| `paymentMethod` | 10/22 | Públicos recientes y algunos legado |
| `items` (array) | 16/22 con ítems | Conteo usable en detalle |
| `customer` | 10/22 | Solo presencia sí/no en informe |
| `purchase` | 3/22 | `public_plus_buy` |
| `pickupShipping` | 3/22 | `public_plus_pickup_shipping` |

### 4.2 Valores únicos observados

- **status:** `created`, `cancelled`
- **publicStatus:** `Pedido recibido` (y vacío/null en legado/cancelados)
- **source:** `public_local`, `public_plus_buy`, `public_plus_pickup_shipping`, `public_app`, `null`
- **requestType:** `buy`, `pickup_shipping`, `null`

---

## 5. Combinaciones reales encontradas

Solo combinaciones existentes (sin inventar):

| status | publicStatus | source | requestType | cantidad |
|--------|--------------|--------|-------------|----------|
| cancelled | null | null | null | 5 |
| cancelled | null | public_app | null | 4 |
| created | Pedido recibido | public_local | null | 4 |
| created | null | public_app | null | 3 |
| created | Pedido recibido | public_plus_pickup_shipping | pickup_shipping | 3 |
| created | Pedido recibido | public_plus_buy | buy | 3 |

**Total:** 22 pedidos.

---

## 6. Tabla de mapeo actual (contrato formal)

Helpers: `AdminOperationOrderClassification` en `core/model`. Prioridad de evaluación: cancelación → finalización → demora → problema → activo.

### 6.1 Pedidos del día

| Subsección UI | Regla contrato | Pedidos que califican hoy (N) |
|---------------|----------------|-------------------------------|
| **Activos** | `status == created` **y** `publicStatus == "Pedido recibido"` | **10** (4 local + 3 buy + 3 pickup) |
| **Finalizados** | `status` ∈ `delivered`, `closed`, `archived` | **0** |
| **Cancelados** | `status` ∈ `cancelled`, `canceled` | **9** |
| **Demorados** | Sin criterio real (ver §8) | **0** |
| **Con problemas** | Sin señal real (ver §8) | **0** |
| *(sin bucket)* | `created` + `publicStatus` vacío + `public_app` (legado) | **3** — no mover a Activos sin señal |

### 6.2 Pedidos activos (sub-estados)

| Subsección UI | Regla contrato | N hoy |
|---------------|----------------|-------|
| **Esperando local** | Misma señal que Activo (`created` + `Pedido recibido`) | **10** |
| **Preparando** | Sin señal | **0** |
| **Esperando repartidor** | Sin señal | **0** |
| **En entrega** | Sin señal | **0** |

Motivo de **Esperando local:** pedido público recién creado, sin transición operativa de local/admin/repartidor en datos reales.

### 6.3 Pedidos con problemas

| Subsección UI | Regla contrato | N hoy |
|---------------|----------------|-------|
| **Local no responde** | Sin campo/estado | **0** |
| **Reclamo del cliente** | Sin campo/estado; reservado si `publicStatus` contiene "reclamo"/"problema" | **0** |

---

## 7. Categorías con datos reales

1. **Activos** — señal estable en flujo público nuevo.
2. **Cancelados** — `status=cancelled` en legado `public_app` y sin source.
3. **Esperando local** — alias operativo del activo mientras no existan estados finos.

Identificación de origen (lectura, no bucket):

| source | requestType | Etiqueta Admin |
|--------|-------------|----------------|
| `public_local` | — | Pedido de local |
| `public_plus_buy` | `buy` | Botón + Comprar |
| `public_plus_pickup_shipping` | `pickup_shipping` | Botón + Retiro / Envío |
| `public_app` | — | App pública (legado) |

---

## 8. Categorías vacías por falta de señal real

| Categoría | Motivo |
|-----------|--------|
| Finalizados | No hay `status` de entrega/cierre en la muestra |
| Demorados | No hay `sla`, `deadline`, `elapsed`, timeout ni regla configurada en `/orders` |
| Con problemas | Ningún `publicStatus` con incidencia; no se lee `/incidents` en Admin aún |
| Preparando / Esperando repartidor / En entrega | No hay `status`/`publicStatus` operativos distintos de `created` + recibido |
| Local no responde | Sin campo dedicado |
| Reclamo del cliente | Sin campo dedicado |

**Regla:** no poblar estas UI con pedidos reales hasta nueva señal o subcolección autorizada.

---

## 9. Campos faltantes o inconsistentes

| Hallazgo | Impacto |
|----------|---------|
| 18/22 sin `storeId` / `storeName` | Plus y legado no traen local; Admin no puede agrupar por local en esos pedidos |
| 12/22 sin `isPublicCreated: true` | Mezcla legado vs flujo Functions actual |
| 9 cancelados con `publicStatus` null | Clasificación solo por `status` |
| 3 activos legado (`created`, `public_app`, sin `publicStatus`) | Quedan **fuera** de Activos por contrato conservador |
| 5 sin `trackingNumber`; 12 sin `publicOrderNumber` coherente | Detalle debe preferir el número disponible |
| Respuesta Callable devuelve `status: RECEIVED` pero Firestore guarda `created` | Tracking público vs Admin leen el documento; no unificar sin decisión explícita |

---

## 10. Mapeo de source / requestType

| source | requestType | Tipo operativo | Campos distintivos |
|--------|-------------|----------------|-------------------|
| `public_local` | — | Pedido de catálogo/local | `items[]`, `storeId`, `storeName`, `subtotal`, `total` |
| `public_plus_buy` | `buy` | Botón + Comprar | `purchase`, `customer.address` |
| `public_plus_pickup_shipping` | `pickup_shipping` | Botón + Retiro/Envío | `pickupShipping` |
| `public_app` | — | Legado pre-Functions unificadas | `created` sin `publicStatus` típico |
| `null` | — | Legado cancelado | Solo `cancelled` |

`requestType` solo aparece en flujos Plus; Local usa `null`/vacío.

---

## 11. Reglas para Pedido #____ read-only

### 11.1 Permitido mostrar

- Número visible: `trackingNumber` o, si falta, `publicOrderNumber`
- `status`, `publicStatus`
- `source` / etiqueta humana (`sourceLabel`)
- `requestType` cuando exista
- `storeName` si existe
- Resumen: cantidad de ítems (`itemsSummary`), `total` si existe
- Fecha: `createdAt` (y `updatedAt` solo en detalle si existe)
- Nombre de cliente **solo** si ya está en adaptador (`customerName`); sin teléfono ni dirección completa en esta etapa

### 11.2 No mostrar todavía

- Raw JSON del documento
- `id` técnico en pantalla principal (uso interno de navegación sí)
- Logs, eventos, incidencias
- Teléfono, dirección completa, datos de pago sensibles
- Roles, asignaciones, acciones (resolver, cancelar, asignar repartidor)
- Campos no leídos por el adaptador (`purchase`/`pickupShipping` completos)

### 11.3 Variantes visuales existentes

`OperationOrderVariant` en UI sigue siendo **mock por subsección** hasta conectar mapeo; no inferir problema/demora desde variante.

---

## 12. Reglas de no invención

1. No inventar `status` ni `publicStatus`.
2. No inferir demora por tiempo transcurrido sin campo explícito.
3. No usar `publicStatus contains "recib"` como única regla de activo (incluiría legado incorrecto); usar igualdad exacta `Pedido recibido`.
4. No tratar `RECEIVED` de respuesta Callable como estado de documento.
5. No escribir en `/orders` ni subcolecciones desde Admin.
6. No commitear dumps, credenciales ni PII.

---

## 13. Estados futuros que deberán generar roles

Cuando existan transiciones reales (Functions + eventos), el mapeo deberá extenderse sin romper lo conservador:

| Señal futura esperada | Bucket Admin | Rol actor |
|-----------------------|--------------|-----------|
| Local acepta / prepara | Preparando | `store` |
| Listo para retiro / espera driver | Esperando repartidor | `store` / `admin` |
| En camino | En entrega | `driver` |
| Entregado / retirado / cerrado | Finalizados | `driver` / `store` / `admin` |
| Cancelado operativo | Cancelados | `admin` / `store` |
| SLA / timeout configurado | Demorados | sistema / `admin` |
| Incidencia en `/incidents` o flag | Con problemas | `admin` |
| Local sin respuesta (flag o estado) | Local no responde | `admin` |
| Reclamo cliente | Reclamo del cliente | `admin` |

Hasta entonces, esos buckets permanecen vacíos o con placeholders visuales sin datos.

---

## 14. Riesgos

| Riesgo | Severidad | Mitigación |
|--------|-----------|------------|
| UI actual (`activeOrders`) más amplia que contrato | Media | Aplicar contrato en bloque posterior dedicado a UI |
| Pedidos legado sin `publicStatus` invisibles en Activos | Media | Documentado; eventual backfill o regla legado explícita |
| Mezcla `created` vs `RECEIVED` | Baja | Admin lee documento Firestore |
| Un solo pedido en listas (`firstOrNull`) | Alta UX | Bloque futuro: listar por bucket |
| Lectura completa `orders.get()` sin paginación | Media | Índices y filtros cuando crezca volumen |
| Cancelados sin número público | Baja | Mostrar estado; número opcional |

---

## 15. Decisiones pendientes

1. ¿Los 3 pedidos `created` + `public_app` sin `publicStatus` entran a Activos con regla legado o quedan excluidos hasta backfill?
2. ¿`Pedidos del día` filtra por `createdAt` del día calendario o muestra todos los vivos?
3. ¿Admin debe leer `/orders/{id}/incidents` para **Con problemas**?
4. ¿Unificar `status` guardado (`created`) con código de tracking (`RECEIVED`)?
5. ¿Mostrar `storeName` vacío en Plus o etiqueta por `source` solamente?

---

## 16. Dictamen final

**B) Contrato parcial — suficiente para mapeo read-only conservador; faltan señales reales para varias categorías.**

Justificación:

- Hay señal clara para **Activos** (10) y **Cancelados** (9).
- **Esperando local** puede aplicarse 1:1 con activos hoy.
- **Finalizados**, **Demorados**, **Con problemas** y sub-estados activos finos deben quedar vacíos.
- Tres pedidos legado quedan sin bucket hasta decisión §15.1.

Verificación read-only de auditoría:

- Lectura previa/posterior: **22** documentos antes y después del script de auditoría.
- No se ejecutaron escrituras desde este bloque.
- No se verificó `updatedAt` campo a campo (solo conteo global); lectura `.get()` no debería mutar documentos.

---

## Anexo — Implementación de referencia

- Kotlin: `app/src/main/java/com/pedilo/app/core/model/AdminOperationOrderClassification.kt`
- Tests: `tests/admin_order_operation_mapping.test.js`

Próximo paso recomendado (fuera de este bloque): conectar buckets de UI a `AdminOperationOrderClassification` sin acciones de escritura; listar todos los pedidos del bucket, no solo `firstOrNull()`.
