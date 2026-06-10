# 08 — Etapa D: Local / Store

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (F parcial)  
**Dependencias:** F7  
**Documentos fuente:** D1–D5, N2

---

## 1. Objetivo
Implementar/endurecer **Local / Store** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `ui/store/StoreApp.kt`
- `FirebaseStoreOrdersAdapter.kt`
- Functions store actions

## 3. Qué se implementa
- Endurecer D1 V1
- Multi-store D3
- Productos D4
- Solicitud repartidor D5 (4º tipo)
- Catálogo pasivo D2

## 4. Qué NO se implementa en esta etapa
- Chat J; finanzas G completas

## 5. Validaciones mínimas
- Carrito mono-local
- No a repartidor antes aceptar+listo

## 6. Pruebas mínimas
- store_operational_flow

## 7. Criterio de aceptación
Store opera según B2; D5 si 4º tipo en B; permisos storeId

## 8. Criterio de rechazo
Local opera pedido ajeno; pasa a driver antes de listo


---
*Plan de implementación — D*
