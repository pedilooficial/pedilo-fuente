# 05 — Etapa M: Backend / Firebase / Seguridad

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (B certificado)  
**Dependencias:** B7 Q7  
**Documentos fuente:** M1–M5, A5

---

## 1. Objetivo
Implementar/endurecer **Backend / Firebase / Seguridad** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `functions/index.js`
- `firestore.rules`
- `firestore.indexes.json`
- `tests/firestore_rules.test.js`
- Emulator config `firebase.json`

## 3. Qué se implementa
- Functions alineadas a B2/A5
- Rules por rol (M4)
- Transacciones e idempotencia (M3)
- Colecciones según M2
- Pruebas emulator (M5)

## 4. Qué NO se implementa en esta etapa
- Deploy producción sin autorización
- Nuevas colecciones sin rules

## 5. Validaciones mínimas
- Cliente no escribe `/orders`
- Auth+rol en cada callable
- Emulator tests pasan

## 6. Pruebas mínimas
- firestore_rules.test.js
- operational_order_actions_backend
- Tests concurrencia versión

## 7. Criterio de aceptación
Rules bloquean escrituras peligrosas; Functions validan rol/estado/versión; M5 emulator documentado

## 8. Criterio de rechazo
Escritura directa cliente a orders; Function sin validación rol


---
*Plan de implementación — M*
