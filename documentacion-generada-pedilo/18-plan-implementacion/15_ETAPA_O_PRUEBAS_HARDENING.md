# 15 — Etapa O: Pruebas y Hardening

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (C–L)  
**Dependencias:** Todas etapas funcionales  
**Documentos fuente:** O1–O3, N4

---

## 1. Objetivo
Implementar/endurecer **Pruebas y Hardening** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `tests/`
- `tools/guards/`
- Plan carga O2

## 3. Qué se implementa
- Checklist O1
- Carga O2
- Observabilidad O3
- JUnit N4 opcional

## 4. Qué NO se implementa en esta etapa
- Release Play
- Deploy prod

## 5. Validaciones mínimas
- 1000 pedidos plan
- Concurrencia 100 drivers
- Fallos WhatsApp/IA simulados

## 6. Pruebas mínimas
- Todos tests + guards + checklist O1

## 7. Criterio de aceptación
O1 checklist completo; O2 ejecutado o documentado; sin placeholders

## 8. Criterio de rechazo
Placeholder en build release; logs sensibles


---
*Plan de implementación — O*
