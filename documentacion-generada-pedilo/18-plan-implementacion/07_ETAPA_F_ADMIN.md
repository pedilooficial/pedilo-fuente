# 07 — Etapa F: Admin Real

**Estado del plan:** LISTO CON DEPENDENCIA PREVIA (B+M)  
**Dependencias:** M7  
**Documentos fuente:** F1–F4, F2, H1–H4 (config modos)

---

## 1. Objetivo
Implementar/endurecer **Admin Real** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `ui/admin/AdminApp.kt`
- `ui/admin/roles/`
- Functions admin + config

## 3. Qué se implementa
- Operación: endurecer F1/F4 existente
- Configuración F2: persistencia real
- Alta roles F3: CRUD `/users`
- Intervención con auditoría
- Módulos modos en config (H)

## 4. Qué NO se implementa en esta etapa
- Mezclar config con intervención en pedidos vivos sin auditoría

## 5. Validaciones mínimas
- Confirmación cambios sensibles
- Config afecta futuros; vivos conservan snapshot

## 6. Pruebas mínimas
- admin_operational_actions, admin_order_operation_mapping

## 7. Criterio de aceptación
F2/F3 dejan de ser shell; F4 acciones auditadas; operación no regresa

## 8. Criterio de rechazo
Config modifica pedidos vivos; roles sin auditoría


---
*Plan de implementación — F*
