# 13 — Etapa K: IA Controlada

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (J)  
**Dependencias:** J12  
**Documentos fuente:** K1–K3

---

## 1. Objetivo
Implementar/endurecer **IA Controlada** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- Servicio IA (integración externa autorizada)

## 3. Qué se implementa
- Límites K1
- Pedidos libres K2
- Pagos/comprobantes K3 señal riesgo

## 4. Qué NO se implementa en esta etapa
- IA decide estados
- IA aprueba pagos dudosos

## 5. Validaciones mínimas
- Deriva Admin en dudoso
- Confirmación usuario

## 6. Pruebas mínimas
- Tests: IA no cambia estado sin humano

## 7. Criterio de aceptación
IA asiste; no gobierna; K3 no aprueba automático dudoso

## 8. Criterio de rechazo
IA cancela/asigna/cierra sola


---
*Plan de implementación — K*
