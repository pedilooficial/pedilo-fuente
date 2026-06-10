# 04 — Etapa B: Pedido Vivo Universal

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (Q certificado)  
**Dependencias:** Q7 etapa Q  
**Documentos fuente:** B1–B5, A5

---

## 1. Objetivo
Implementar/endurecer **Pedido Vivo Universal** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `functions/index.js` (birth, operateLiveOrder)
- `core/model/LiveOrderContract.kt`
- `tests/live_order_*.test.js`

## 3. Qué se implementa
- Completar 5 ejes estado según A5/B2
- nextAllowedActions coherentes
- Idempotencia todas acciones críticas (B4)
- Eventos B3 completos
- Timeouts/fallbacks ejecutables (B5)
- Preparar 4º tipo (no obligatorio cerrar aquí si bloquea D)

## 4. Qué NO se implementa en esta etapa
- UI roles
- WhatsApp/IA
- Pagos vivos

## 5. Validaciones mínimas
- Transiciones invalidas rechazadas
- Versión obligatoria en acciones
- Ningún pedido vivo sin responsable

## 6. Pruebas mínimas
- live_order_birth_contract
- live_order_end_to_end_flow
- operational_order_actions_backend
- Nuevo test timeouts si se implementan

## 7. Criterio de aceptación
5 ejes documentados implementados o con wireName en A5; B5 motor o plan de ejecución documentado en Q7; tests verdes

## 8. Criterio de rechazo
Estado imposible permitido; UI decide estados; se rompe birth contract


---
*Plan de implementación — B*
