# 10 — Etapa G: Pagos, Tarifas y Finanzas

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (E+F)  
**Dependencias:** E9, F7  
**Documentos fuente:** G1–G4, E3, E4, H1–H2

---

## 1. Objetivo
Implementar/endurecer **Pagos, Tarifas y Finanzas** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- Functions financieras nuevas
- Estados financialStatus
- Config tarifas F2

## 3. Qué se implementa
- Separación operativo/financiero
- Tarifas snapshot G2
- Cobro E3
- Cierre caja E4
- Modos lluvia en tarifa

## 4. Qué NO se implementa en esta etapa
- Pasarela online
- IA aprueba pagos sola

## 5. Validaciones mínimas
- Entregar ≠ cerrar financieramente
- Pago dudoso no cierra solo

## 6. Pruebas mínimas
- Tests financieros nuevos + regression live_order

## 7. Criterio de aceptación
Estados financieros plano; cierre caja; snapshot tarifas

## 8. Criterio de rechazo
Cierre financiero automático en dudoso; precio retroactivo


---
*Plan de implementación — G*
