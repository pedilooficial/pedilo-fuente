# B2 — Estados y Transiciones

**Familia:** B — Núcleo del Pedido  
**Fecha cierre documental:** 2026-06-09  
**Última afinación:** 2026-06-10  
**Estado final:** APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN

---

## 1. Identidad

| Código | B2 |
| Estado final | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |

### Fuentes
Plano Maestro §estados; A5 Tabla equivalencia; `functions/index.js`; `LiveOrderContract.kt`

### Objetivo
Cerrar los 5 ejes de estado, compatibilidades, prohibiciones, transiciones y validaciones.

---

## 2. Regla de nomenclatura (C-01 resuelta)

- **Plano Maestro:** español conceptual = estado objetivo
- **Repo V1:** `snake_case` inglés en campos Firestore = estado actual
- **Cliente:** `publicStatus` español humano
- **Autoridad:** semántica del plano; persistencia del repo hasta migración documentada Q7

Ver tabla completa en **A5**.

---

## 3. Eje operativo — transiciones V1 (repo confirmado)

| Acción | Rol | `status` repo | `operationalStatus` | `publicStatus` |
|--------|-----|---------------|---------------------|----------------|
| Birth local/plus | sistema | `created` | `waiting_admin_review` | Pedido recibido |
| local_accept | store | `accepted` | `local_accepted` | Pedido aceptado por el local |
| local_reject | store | `cancelled` | `rejected_by_store` | Pedido cerrado |
| local_mark_preparing | store | `preparing` | `preparing` | Pedido en preparación |
| local_mark_ready | store | `ready_for_pickup` | `ready_for_pickup` | Pedido listo para retirar |
| driver_take | driver | `assigned_to_driver` | `driver_assigned` | Pedido asignado a repartidor |
| driver_mark_picked_up | driver | `picked_up` | `picked_up` | Pedido retirado |
| driver_mark_delivered | driver | `delivered` | `delivered` | Pedido cerrado |
| cancel_order | store/driver/admin | `cancelled` | `cancelled_by_{role}` | Pedido cerrado |
| open_incident | store/driver/admin | — | `incident_open` | Pedido en revisión operativa |
| resolve_incident | admin | — | `incident_resolved` | Restaura según status |
| admin_intervene | admin | — | `admin_intervention` | Pedido en revisión operativa |

---

## 4. Prohibiciones de combinación (Plano — cerradas)

| Prohibición |
|-------------|
| `archivado` + chat operativo activo |
| `archivado` + `en_entrega` |
| `esperando_repartidor` + `repartidor_asignado` simultáneo |
| `entregado` sin evento retiro |
| `cerrado_financieramente` + `disputado` simultáneo |
| `listo_para_retiro` sin aceptación local previa |
| `en_entrega` sin `retirado` previo |

---

## 5. Compatibilidades válidas

| Operativo | Financiero (objetivo) | Archivo | Válido |
|-----------|----------------------|---------|--------|
| `delivered` | `pendiente_cierre` | `live` | Sí |
| `delivered` | `cerrado_financieramente` | `archived` | Sí (objetivo) |
| `cancelled` | `disputado` | `live` | Sí |
| `incident_open` | `pending_review` | `live` | Sí (V1) |

---

## 6. Validaciones por transición

1. Auth + rol activo en `/users`
2. Permiso sobre pedido (storeId / driverId / admin)
3. Estado permite acción (`allowedLiveActions`)
4. `expectedVersion` coincide
5. Idempotencia en birth (`publicIdempotencyKey`)
6. Evento en `/orders/{id}/events`

---

## 7. Errores

| Código | Causa |
|--------|-------|
| `VERSION_MISMATCH` | Versión vieja |
| `INVALID_TRANSITION` | Estado no permite acción |
| `PERMISSION_DENIED` | Rol no autorizado |
| `ORDER_CLOSED` | Pedido terminal |
| `failed-precondition` | Pedido ya tomado |

## 10. Riesgos
- Estados imposibles si se agregan wireNames sin actualizar A5.
- Financiero incompleto post-entrega.

---

## DICTAMEN DEL DOCUMENTO

| Campo | Valor |
|-------|-------|
| **Estado final** | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| **Documentación** | Cerrada |
| **Implementación repo** | V1 subset; ejes financiero/comunicación/archivo incompletos |

### Estado actual (repo)
12+ operationalStatus wireNames; financialStatus solo `pending_review`; archive `live`/`archived`

### Estado objetivo (Plano Maestro)
5 ejes completos según plano §3

### Brecha de implementación
Extender wireNames según A5 sin romper pedidos vivos; migración por bloque Q7

### Contradicciones resueltas
C-01 resuelta en A5. C-02 resuelta: prevalece repo HEAD `50c51fa`.

### Criterio de implementación futura
Consultar A5 antes de agregar estados; tests `live_order_*` obligatorios

---
*Documentación final — 2026-06-09*
