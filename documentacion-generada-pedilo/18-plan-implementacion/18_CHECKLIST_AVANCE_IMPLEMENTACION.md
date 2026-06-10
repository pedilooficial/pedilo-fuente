# Checklist de Avance de Implementación

**Actualizar:** Tras completar cada etapa (instancia Q7)

| Etapa | Estado plan | Docs fuente | Brechas objetivo | Tests requeridos | Tests pasados | Q7 | Puede avanzar | Bloqueos | Observaciones |
|-------|-------------|-------------|------------------|------------------|---------------|-----|---------------|----------|---------------|
| Q | LISTO PARA IMPLEMENTAR | Q1–Q7, A3–A4 | BR-19 | tests/*, guards | ☐ | ☐ | Sí → B | — | **Primera etapa** |
| B | LISTO CON DEP. Q | B1–B5, A5 | BR-01–03 | live_order_* | ☐ | ☐ | Tras Q | — | Núcleo crítico |
| M | LISTO CON DEP. B | M1–M5 | Rules, Functions | firestore_rules | ☐ | ☐ | Tras B | — | |
| C | LISTO CON DEP. B,M | C1–C4 | BR-07 | public_* flows | ☐ | ☐ | Tras M | — | |
| F | LISTO CON DEP. B,M | F1–F4, H* | BR-05–06, BR-16 | admin_* | ☐ | ☐ | Tras M | — | |
| D | LISTO CON DEP. F | D1–D5 | BR-01, BR-17 | store_operational | ☐ | ☐ | Tras F parcial | — | |
| E | LISTO CON DEP. D | E1–E4 | BR-12 | driver_operational | ☐ | ☐ | Tras D | — | |
| G | LISTO CON DEP. E,F | G1–G4 | BR-04,10–11 | financieros nuevos | ☐ | ☐ | Tras E,F | — | |
| I | LISTO CON DEP. G | I1–I4 | BR-08, BR-18 | incidencias | ☐ | ☐ | Tras G parcial | — | |
| J | LISTO CON DEP. B,M | J1–J4 | BR-09, BR-13 | comunicación | ☐ | ☐ | Tras M | — | Paralelo posible con L |
| K | LISTO CON DEP. J | K1–K3 | BR-14 | IA límites | ☐ | ☐ | Tras J | — | |
| L | LISTO CON DEP. B,M | L1–L3 | BR-15 | métricas | ☐ | ☐ | Tras M | — | |
| O | LISTO CON DEP. C–L | O1–O3, N4 | BR-20–21 | carga, guards | ☐ | ☐ | Tras C–L | — | |
| P | NO IMPLEMENTAR TODAVÍA | P1–P3 | BR-22–23 | release build | ☐ | ☐ | Tras O | **P3 externo** | Publicación bloqueada |

---

## Leyenda estado plan

| Estado | Significado |
|--------|-------------|
| LISTO PARA IMPLEMENTAR | Puede iniciarse ahora |
| LISTO CON DEPENDENCIA PREVIA | Espera Q7 etapa anterior |
| BLOQUEADO POR DECISIÓN EXTERNA | Solo publicación (P3) |
| NO IMPLEMENTAR TODAVÍA | Orden o dependencias no cumplidas |

---

## Registro de certificaciones Q7 (completar al implementar)

| Etapa | Fecha | HEAD commit | Dictamen | Archivo reporte |
|-------|-------|-------------|----------|-----------------|
| Q | | | | |
| B | | | | |
| M | | | | |
| ... | | | | |

---
*Matriz de control — actualizar en implementación real*
