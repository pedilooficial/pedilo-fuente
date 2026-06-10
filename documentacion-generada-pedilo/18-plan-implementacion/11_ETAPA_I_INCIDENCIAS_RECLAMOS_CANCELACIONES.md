# 11 — Etapa I: Incidencias, Reclamos, Cancelaciones

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (G parcial)  
**Dependencias:** G10, J parcial  
**Documentos fuente:** I1–I4

---

## 1. Objetivo
Implementar/endurecer **Incidencias, Reclamos, Cancelaciones** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `orders/{id}/incidents`
- Claims collection
- Functions cancel/incident

## 3. Qué se implementa
- Incidencias activas I1
- Reclamos posteriores I2 (no reabre pedido)
- Cancelaciones por rol I3
- Producto no disponible I4

## 4. Qué NO se implementa en esta etapa
- Reabrir pedido cerrado

## 5. Validaciones mínimas
- Incidencia≠reclamo
- Auditoría obligatoria

## 6. Pruebas mínimas
- Tests incidencias + cancel por rol

## 7. Criterio de aceptación
I2 backend real; I3 matriz rol; I4 flujo local-cliente

## 8. Criterio de rechazo
Reclamo reabre pedido; cancelación sin impacto financiero


---
*Plan de implementación — I*
