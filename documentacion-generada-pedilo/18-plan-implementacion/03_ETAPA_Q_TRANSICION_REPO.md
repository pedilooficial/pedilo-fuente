# 03 — Etapa Q: Transición del Repo

**Estado del plan:** LISTO PARA IMPLEMENTAR  
**Dependencias:** Documentación cerrada  
**Documentos fuente:** Q1–Q7, A3, A4, Q6

---

## 1. Objetivo
Implementar/endurecer **Transición del Repo** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- Repo completo `/home/oem/Desktop/pedilo`
- `tools/guards/`
- `tools/seed_public_catalog.js`
- Placeholders: `AdminApp.kt`, `PublicConventions.kt`

## 3. Qué se implementa
- Inventario técnico actualizado (Q1)
- Matriz conservar/ajustar/reemplazar aplicada (A4/Q3)
- Placeholders catalogados y aislados (Q6)
- Baseline tests + guards en verde
- Instancia Q7 etapa Q

## 4. Qué NO se implementa en esta etapa
- Implementación de features de negocio
- Deploy producción

## 5. Validaciones mínimas
- `node --test tests/*.test.js` pasa
- `bash tools/guards/check_architecture.sh` pasa
- `bash tools/guards/check_ui_quality.sh` pasa
- Placeholders listados en reporte Q7

## 6. Pruebas mínimas
- Todos los tests Node existentes
- architecture_guard + ui_quality_guard

## 7. Criterio de aceptación
Inventario Q1 actualizado; matriz Q3/A4 documentada en reporte Q7; guards verdes; placeholders identificados

## 8. Criterio de rechazo
Tests fallan; placeholder operativo no catalogado; se modifica documentación cerrada

## 9. Certificación transición
Completar plantilla Q7: bloque Q, archivos revisados, placeholders, dictamen, puede pasar a B.

---
*Plan de implementación — Q*
