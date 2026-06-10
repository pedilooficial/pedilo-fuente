# A5 — Tabla de Equivalencia Estados Plano ↔ Repo

**Familia:** A — Documentos Maestros  
**Fecha cierre documental:** 2026-06-09  
**Última afinación:** 2026-06-10  
**Estado final:** APROBADO DOCUMENTALMENTE

---

## 1. Identidad

| Campo | Valor |
|-------|-------|
| Código | A5 |
| Estado final | APROBADO DOCUMENTALMENTE |

### Fuentes
Plano Maestro §estados; `LiveOrderContract.kt`; `functions/index.js`

### Objetivo
Resolver documentalmente la nomenclatura: plano (español conceptual) vs repo (wireName inglés). Plano = objetivo; repo = actual.

### Alcance — Incluye
Tabla equivalencia 5 ejes; reglas de transición; estados solo-repo; estados solo-plano futuros

### Alcance — No incluye
Implementación de nuevos estados en código

---


## 2. Regla de resolución documental

| Capa | Autoridad |
|------|-----------|
| **Plano Maestro** | Define estados objetivo y semántica |
| **Repo** | Define estados actuales persistidos (`wireName`) |
| **UI pública** | Usa `publicStatus` (español humano) |
| **Implementación futura** | Debe converger hacia plano sin romper pedidos vivos |

**Resolución C-01:** No es contradicción de producto. Es **divergencia de nomenclatura técnica**. El plano usa español conceptual; el repo V1 usa `snake_case` inglés. Ambos son válidos en su capa. La tabla siguiente es la fuente de verdad documental.

---

## 3. Eje operativo

| Plano (objetivo) | Repo actual (`operationalStatus`) | Repo `status` (legacy) | `publicStatus` |
|------------------|-----------------------------------|------------------------|----------------|
| `pendiente_validacion` / `creado` | `waiting_admin_review` | `pending` | Pedido recibido |
| `esperando_aceptacion_local` | (usa `status=pending`) | `pending` | Pedido recibido |
| `aceptado_por_local` | `local_accepted` | `accepted` | Pedido aceptado por el local |
| `rechazado_por_local` | `rejected_by_store` | `cancelled` | Pedido cerrado |
| `en_preparacion` | `preparing` | `preparing` | Pedido en preparación |
| `listo_para_retiro` | `ready_for_pickup` | `ready_for_pickup` | Pedido listo para retirar |
| `repartidor_asignado` | `driver_assigned` | `assigned_to_driver` | Pedido asignado a repartidor |
| `retirado` | `picked_up` | `picked_up` | Pedido retirado |
| `entregado` | `delivered` | `delivered` | Pedido cerrado |
| `cancelado` | `cancelled_by_{role}` | `cancelled` | Pedido cerrado |
| `incidencia_abierta` | `incident_open` | — | Pedido en revisión operativa |
| `incidencia resuelta` | `incident_resolved` | — | Restaura `publicStatus` |
| Intervención Admin | `admin_intervention` | — | Pedido en revisión operativa |
| Admin revisado | `admin_reviewed` | — | — |

**Estados plano sin wireName dedicado aún:** `esperando_repartidor`, `esperando_retiro`, `en_entrega`, `cerrado_operativamente` — se mapean por combinación `status` + `operationalStatus` + `archiveStatus`.

---

## 4. Eje financiero

| Plano (objetivo) | Repo actual | Nota |
|------------------|-------------|------|
| `pendiente` | `pending_review` | Único valor V1 |
| `efectivo_al_entregar` | — | Objetivo G3/E3 |
| `transferencia_pendiente` | — | Objetivo G3 |
| `cobrado` | — | Objetivo E3 |
| `pendiente_cierre` | — | Objetivo E4 |
| `cerrado_financieramente` | — | Objetivo G1 |
| `disputado` | — | Objetivo G3 |

---

## 5. Eje comunicación

| Plano (objetivo) | Repo actual |
|------------------|-------------|
| `recibida` / `generado` | `received` |
| `cerrada` (post-entrega V1) | `closed` |
| `enviada`, `fallida`, `requiere_manual` | — (objetivo J1) |

---

## 6. Eje incidencia

| Plano (objetivo) | Repo actual |
|------------------|-------------|
| `sin_incidencia` | `none` |
| `incidencia_abierta` | `open` (+ `activeIncident=true`) |
| `resuelta` | `resolved` |

---

## 7. Eje archivo

| Plano (objetivo) | Repo actual |
|------------------|-------------|
| `activo` | `live` |
| `archivado` / `solo_historico` | `archived` |

---

## 8. Tipos de pedido

| Plano | Repo `orderType` |
|-------|------------------|
| `pedido_local` | `local_order` |
| `compra_directa` | `direct_purchase` |
| `retiro_envio` | `pickup_shipping` |
| `solicitud_repartidor` | `store_driver_request` (**objetivo** — no en repo) |

---

## 9. Criterio de implementación futura

1. Nuevos estados: agregar `wireName` en `LiveOrderContract.kt` + transición en `functions/index.js`
2. Mantener `publicStatus` en español para cliente
3. No renombrar valores existentes en pedidos vivos; migrar solo con bloque documentado Q7
4. Consultar esta tabla antes de cualquier cambio de vocabulario


## DICTAMEN DEL DOCUMENTO

| Campo | Valor |
|-------|-------|
| **Estado final** | APROBADO DOCUMENTALMENTE |
| **Documentación** | Cerrada |
| **Implementación repo** | Definición cerrada |

### Estado actual (repo HEAD `50c51fa`)
Repo usa inglés snake_case; plano usa español conceptual

### Estado objetivo (Plano Maestro)
Semántica unificada vía tabla A5; público siempre español

### Brecha de implementación
Convergencia progresiva en implementación por bloques

### Contradicciones resueltas
C-01 resuelta en A5. C-02 resuelta: prevalece repo HEAD `50c51fa`.

### Criterio de implementación futura
Implementar estados faltantes según B2 sin romper wireNames existentes

---
*Documentación final — Agente documental Pédilo — 2026-06-09*
