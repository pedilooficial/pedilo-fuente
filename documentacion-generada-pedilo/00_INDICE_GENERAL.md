# ÍNDICE GENERAL — Documentación Pédilo

**Cierre documental:** 2026-06-09  
**Última afinación:** 2026-06-10  
**Paquete:** `documentacion-generada-pedilo/`

## Dictamen del paquete

**B) DOCUMENTACIÓN FINAL AFINADA CON DECISIONES EXTERNAS OBLIGATORIAS IDENTIFICADAS**

---

## 1. Resumen

| Métrica | Valor |
|---------|-------|
| Documentos de bloque (A–P, Q0) | **72** |
| Archivos de control (índice, informe, auditoría) | **3** |
| **Total archivos `.md`** | **75** |
| APROBADO DOCUMENTALMENTE | **12** |
| APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN | **59** |
| APROBADO DOCUMENTALMENTE CON DECISIÓN EXTERNA OBLIGATORIA | **1** (P3) |
| RECHAZADO Y REEMPLAZADO | **0** |
| Documentación terminada y afinada | **SÍ** |
| Lista para implementación por etapas | **SÍ** |
| App lista para producción | **NO** |
| App lista para Google Play | **NO** |

---

## 2. Convención de roles y términos

| En documentación (Plano) | En repo / código |
|--------------------------|------------------|
| **Pédilo** | Proyecto y app |
| **Pedido Vivo Universal** | Entidad `/orders/{id}` + birth contract |
| **Usuario público** | `publicuser`, funciones públicas |
| **Admin** | `admin`, `AdminApp` |
| **Local** | `store`, `StoreApp`, rol `store` |
| **Repartidor** | `driver`, `DriverApp`, rol `driver` |

**Términos unificados en dictámenes:**
- **Estado actual (repo):** qué existe en código HEAD `50c51fa`
- **Estado objetivo (Plano Maestro):** contrato de producto
- **Brecha de implementación:** qué falta en código; no invalida cierre documental
- **Decisión externa obligatoria:** solo P3 (publicación legal Play)

---

## 3. Índice completo por familia

### `00-maestros/` — Familia A
| Código | Archivo | Estado final |
|--------|---------|--------------|
| A1 | `A1-referencia-plano-maestro.md` | APROBADO DOCUMENTALMENTE |
| A2 | `A2-referencia-maestro-total.md` | APROBADO DOCUMENTALMENTE |
| A3 | `A3-mapa-estado-actual-repo.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| A4 | `A4-matriz-conservacion-reemplazo.md` | APROBADO DOCUMENTALMENTE |
| A5 | `A5-tabla-equivalencia-estados-plano-repo.md` | APROBADO DOCUMENTALMENTE |

### `01-transicion-repo/` — Familia Q
| Código | Archivo | Estado final |
|--------|---------|--------------|
| Q1 | `Q1-inventario-real-repo.md` | APROBADO DOCUMENTALMENTE |
| Q2 | `Q2-brecha-repo-vs-plano.md` | APROBADO DOCUMENTALMENTE |
| Q3 | `Q3-conservacion-ajuste-reemplazo.md` | APROBADO DOCUMENTALMENTE |
| Q4 | `Q4-migracion-progresiva-etapas.md` | APROBADO DOCUMENTALMENTE |
| Q5 | `Q5-compatibilidad-temporal.md` | APROBADO DOCUMENTALMENTE |
| Q6 | `Q6-retiro-placeholders-demo.md` | APROBADO DOCUMENTALMENTE |
| Q7 | `Q7-certificacion-transicion.md` | APROBADO DOCUMENTALMENTE |

### `02-pedido-vivo/` — Familia B
| Código | Archivo | Estado final |
|--------|---------|--------------|
| B1 | `B1-pedido-vivo-universal.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| B2 | `B2-estados-transiciones.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| B3 | `B3-eventos-auditoria.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| B4 | `B4-idempotencia-concurrencia.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| B5 | `B5-timeouts-fallbacks.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |

### `03-usuario-publico/` — Familia C
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — C1 `C1-usuario-publico.md` · C2 `C2-tracking-publico.md` · C3 `C3-formularios-publicos.md` · C4 `C4-cancelacion-publica.md`

### `04-local/` — Familia D
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — D1–D5

### `05-repartidor/` — Familia E
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — E1–E4

### `06-admin/` — Familia F
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — F1–F4

### `07-pagos-tarifas-finanzas/` — Familia G
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — G1–G4

### `08-modos-operativos/` — Familia H
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — H1–H4

### `09-incidencias-reclamos-cancelaciones/` — Familia I
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — I1–I4

### `10-comunicacion/` — Familia J
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — J1–J4

### `11-ia/` — Familia K
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — K1–K3

### `12-metricas-auditoria-salud/` — Familia L
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — L1–L3

### `13-backend-firebase-seguridad/` — Familia M
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — M1–M5

### `14-android-ui-calidad/` — Familia N
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — N1–N4

### `15-pruebas-hardening/` — Familia O
Todos: **APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN** — O1–O3

### `16-release-google-play/` — Familia P
| Código | Archivo | Estado final |
|--------|---------|--------------|
| P1 | `P1-release.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| P2 | `P2-google-play.md` | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| P3 | `P3-privacidad-datos.md` | APROBADO DOCUMENTALMENTE CON DECISIÓN EXTERNA OBLIGATORIA |

### `17-cierre-final/`
| Archivo | Tipo | Estado |
|---------|------|--------|
| `Q0-certificacion-documental-total.md` | Checklist §9 | APROBADO DOCUMENTALMENTE |
| `AUDITORIA_FINAL_CONTRADICCIONES.md` | Auditoría de contradicciones | Cerrado |

---

## 4. Documentos con brecha de implementación (59)

A3, B1–B5, C1–C4, D1–D5, E1–E4, F1–F4, G1–G4, H1–H4, I1–I4, J1–J4, K1–K3, L1–L3, M1–M5, N1–N4, O1–O3, P1–P2

## 5. Documento con decisión externa obligatoria (1)

**P3** — ver `16-release-google-play/P3-privacidad-datos.md`

## 6. Orden de implementación (Q4)

```text
Q → B → M → C → F → D → E → G → I → J → K → L → O → P
```

---

## 7. Plan de implementación por etapas

**Carpeta:** `18-plan-implementacion/` (20 archivos)

| Archivo | Función |
|---------|---------|
| `00_PLAN_GENERAL_IMPLEMENTACION.md` | Plan maestro |
| `01_MAPA_BRECHAS_A_IMPLEMENTAR.md` | 23 brechas BR-01–BR-23 |
| `02_SECUENCIA_DE_ETAPAS.md` | Orden y dependencias |
| `03`–`16_ETAPA_*.md` | Plan por etapa |
| `17_PROMPTS_IMPLEMENTACION_POR_ETAPA.md` | Prompts Codex |
| `18_CHECKLIST_AVANCE_IMPLEMENTACION.md` | Matriz de control |
| `19_INFORME_FINAL_PLAN_IMPLEMENTACION.md` | Dictamen del plan |

**Primera etapa a ejecutar:** Q — Transición repo (`03_ETAPA_Q_TRANSICION_REPO.md`)

---

*Índice afinado — 2026-06-10*
