# Plan General de Implementación — Pédilo

**Fecha:** 2026-06-10  
**Fuente:** Documentación cerrada `documentacion-generada-pedilo/`  
**Repo referencia:** `/home/oem/Desktop/pedilo` HEAD `50c51fa`  
**Estado del plan:** LISTO PARA IMPLEMENTAR (Etapa Q)

---

## 1. Objetivo

Convertir la documentación cerrada en ejecución técnica por etapas, sin redefinir producto, para que un agente programador (Codex u otro) implemente Pédilo bloque a bloque con alcance acotado, tests y certificación Q7.

## 2. Alcance

- Implementar lo definido en familias Q, B, M, C, F, D, E, G, I, J, K, L, O, P
- Endurecer lo existente en repo (núcleo V1, Store, Driver, público Firebase)
- Reemplazar placeholders documentados (Admin config/roles, reclamos UI)
- Certificar cada etapa con instancia Q7 antes de avanzar

## 3. Qué NO se implementa todavía

| Ítem | Motivo | Cuándo |
|------|--------|--------|
| Publicación Google Play | P3 decisión externa + O incompleto | Etapa P |
| GPS, mapas, rutas tipo Uber/Rappi | Prohibido en plano | Nunca |
| Pasarelas de pago online | Fuera de documentación V1 | Fuera de alcance |
| Texto legal privacidad publicable | P3 — decisión externa | Pre-Play |
| Deploy producción Firebase | Requiere autorización explícita | Post-O |

## 4. Orden de etapas

```text
Q → B → M → C → F → D → E → G → I → J → K → L → O → P
```

**Nota:** Familias H (modos operativos) y N (Android/UI) se implementan **dentro** de las etapas que corresponden (F2/G2/H* en F y G; N* transversal en cada etapa UI).

## 5. Dependencias duras

| Etapa | Depende de |
|-------|------------|
| Q | Documentación cerrada (cumplido) |
| B | Q certificado |
| M | B certificado |
| C | B + M |
| F | B + M + C (parcial: operación ya existe) |
| D | B + M + F (parcial: Store V1 existe) |
| E | B + M + D (parcial: Driver V1 existe) |
| G | B + E (cobro/cierre) + F2 (tarifas config) |
| I | B + F + J (parcial) + G (parcial) |
| J | B + M |
| K | B + J (parcial) + G (parcial) |
| L | B3 + M |
| O | Todas las anteriores implementadas |
| P | O + P3 externo para publicación |

## 6. Riesgos generales

- Implementar roles sin núcleo B endurecido
- Confundir shell Admin con backend real
- Saltar M (Rules/Functions) y escribir desde UI
- Activar WhatsApp/IA como autoridad
- Publicar con placeholders (Q6)
- Romper guards arquitectura/UI existentes

## 7. Criterio de avance

Una etapa **puede avanzar** si:

1. Criterios de aceptación de la etapa cumplidos
2. Tests mínimos en verde
3. Guards `check_architecture.sh` y `check_ui_quality.sh` pasan
4. Instancia Q7 completada para la etapa
5. No hay regresión en etapas previas certificadas

## 8. Criterio de bloqueo

Se **bloquea** avance si:

- Test crítico del núcleo falla
- Pedido vivo queda sin responsable o sin salida
- Placeholder operativo activo en producción
- Se viola A5/B2 en transiciones
- Deploy no autorizado

## 9. Definición de “etapa terminada”

```text
Código implementado según documentos fuente de la etapa
→ Tests mínimos pasan
→ Guards pasan
→ Reporte Q7 con archivos tocados, riesgos, dictamen
→ Sin placeholders operativos nuevos en el alcance de la etapa
→ Documentación cerrada NO modificada (solo reporte Q7 si aplica)
```

## 10. Repo — zonas probables (referencia)

| Zona | Ruta |
|------|------|
| Android UI | `app/src/main/java/com/pedilo/app/ui/` |
| Core | `app/src/main/java/com/pedilo/app/core/` |
| Functions | `functions/index.js` |
| Rules | `firestore.rules` |
| Tests | `tests/*.test.js` |
| Guards | `tools/guards/` |

## 11. Documentos de este plan

Ver `02_SECUENCIA_DE_ETAPAS.md`, `17_PROMPTS_IMPLEMENTACION_POR_ETAPA.md`, `18_CHECKLIST_AVANCE_IMPLEMENTACION.md`.

---
*Plan de implementación — no modifica código*
