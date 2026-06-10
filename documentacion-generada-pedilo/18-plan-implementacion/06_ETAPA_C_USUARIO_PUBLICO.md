# 06 — Etapa C: Usuario Público

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (B+M)  
**Dependencias:** M7 Q7  
**Documentos fuente:** C1–C4, N2, N3

---

## 1. Objetivo
Implementar/endurecer **Usuario Público** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `ui/publicuser/`
- `core/firebase/FirebasePublic*.kt`
- `functions` createLocalOrder, createPlusOrder, getPublicOrderTracking

## 3. Qué se implementa
- Integrar cancelación pública C4 (si B listo)
- Endurecer formularios C3
- Tracking seguro C2
- Reclamo: preparar UI→backend (backend en I)
- Anti-placeholder validators

## 4. Qué NO se implementa en esta etapa
- Validación WhatsApp (etapa J)
- Pagos online

## 5. Validaciones mínimas
- No precargar datos demo
- No confirmar incompleto
- publicStatus seguro

## 6. Pruebas mínimas
- local_order_flow, plus_order_flow, public_tracking_flow, public_input_hardening

## 7. Criterio de aceptación
Flujos públicos contra núcleo real; C3 validaciones; tracking sin datos internos

## 8. Criterio de rechazo
Placeholder en ticket; cliente ve auditoría


---
*Plan de implementación — C*
