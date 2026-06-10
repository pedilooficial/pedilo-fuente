# 16 — Etapa P: Release y Google Play

**Estado del plan:** NO IMPLEMENTAR TODAVÍA  
**Dependencias:** O certificado + P3 externo  
**Documentos fuente:** P1–P3

---

## 1. Objetivo
Implementar/endurecer **Release y Google Play** según documentación cerrada, sin redefinir producto.

## 2. Zonas del repo a revisar
- `app/build.gradle.kts`
- Keystore (externo)
- Play Console

## 3. Qué se implementa
- Build release P1
- Checklist Play P2
- Data Safety según P3 spec

## 4. Qué NO se implementa en esta etapa
- Publicar sin P3 legal
- Deploy prod sin O

## 5. Validaciones mínimas
- AAB firmado
- targetSdk 35
- Q6 sin demo

## 6. Pruebas mínimas
- assembleRelease/bundleRelease
- Checklist P1 completo

## 7. Criterio de aceptación
O verde; P3 legal resuelto; Q6 limpio; checklist P1–P2

## 8. Criterio de rechazo
Play antes de O; P3 inventado; demo en release

## 9. Bloqueo P3
**BLOQUEADO POR DECISIÓN EXTERNA** hasta: razón social, email soporte, texto legal aprobado.
Implementación técnica P1/P2 puede prepararse; **publicación NO**.

---
*Plan de implementación — P*
