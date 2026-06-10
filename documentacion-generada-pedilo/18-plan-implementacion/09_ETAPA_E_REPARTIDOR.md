# 09 — Etapa E: Repartidor / Driver

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (D)  
**Dependencias:** D8  
**Documentos fuente:** E1–E4, N2

---

## 1. Objetivo
Implementar/endurecer **Repartidor / Driver** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `ui/driver/DriverApp.kt`
- `FirebaseDriverOrdersAdapter.kt`

## 3. Qué se implementa
- Endurecer E1 V1
- Capacidad E2
- Cobro E3 (parcial si G no listo)
- Cierre caja E4 con G

## 4. Qué NO se implementa en esta etapa
- Aprobar pago dudoso
- GPS/mapas

## 5. Validaciones mínimas
- No tomar sobre capacidad
- No entregar sin retiro

## 6. Pruebas mínimas
- driver_operational_flow, live_order_end_to_end

## 7. Criterio de aceptación
Driver según B2/E1; capacidad enforced; cobro preparado para G

## 8. Criterio de rechazo
Doble toma; entrega sin retiro


---
*Plan de implementación — E*
