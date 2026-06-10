# 12 — Etapa J: Comunicación

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (B+M)  
**Dependencias:** M7  
**Documentos fuente:** J1–J4

---

## 1. Objetivo
Implementar/endurecer **Comunicación** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- Nuevos módulos comunicación
- Colas Functions
- Notificaciones in-app

## 3. Qué se implementa
- WhatsApp validación y estados J1
- Chat interno J2
- Notificaciones J3
- Mensajes públicos J4

## 4. Qué NO se implementa en esta etapa
- WhatsApp gobierna estados
- Chat cambia estado por texto

## 5. Validaciones mínimas
- Fallback si WhatsApp falla
- Texto no cambia estado

## 6. Pruebas mínimas
- Tests cola + permisos chat

## 7. Criterio de aceptación
J1 comunica no gobierna; J2 solo pedido vivo; J3 empuja no decide

## 8. Criterio de rechazo
WhatsApp como verdad; chat público V1


---
*Plan de implementación — J*
