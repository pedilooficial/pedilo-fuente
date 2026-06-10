# A3 — Mapa del Estado Actual del Repo

**Familia:** A — Maestros  
**Código:** A3  
**Fecha cierre documental:** 2026-06-09  
**Última afinación:** 2026-06-10  
**Estado final:** APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN

---

## 1. Identidad y alcance

| Campo | Valor |
|-------|-------|
| Código | A3 |
| Familia | A — Maestros |
| Estado final | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |

### Fuentes usadas
1. Plano Maestro Conceptual Cerrado
2. Maestro Total de Documentación y Cierre de Producción
3. Repo `/home/oem/Desktop/pedilo` HEAD `50c51fa`
4. Tabla A5 (`00-maestros/A5-tabla-equivalencia-estados-plano-repo.md`)
5. Bloques en `/home/oem/Descargas/Carpeta sin título/` (si aplica)

### Objetivo
Mapa completo del repo real para orientar implementación por etapas.

### Alcance — incluye
Contenido operativo y reglas de Mapa del Estado Actual del Repo según fuentes.

### Alcance — no incluye
Modificación de código; inventar reglas fuera de fuentes.

---

## 2. Reglas confirmadas
- **Pedido Vivo Universal:** un Pedido, una verdad; lecturas por rol
- UI representa; backend valida
- IA ayuda, no decide; WhatsApp comunica, no gobierna
- Pedido cerrado no se reabre; reclamo posterior = caso vinculado, no pedido vivo nuevo

## 3. Flujo operativo
Flujo según Plano Maestro y bloque correspondiente en fuentes.

## 4. Datos necesarios
Datos mínimos según tipo de pedido — Plano §4; snapshots al confirmar.

## 5. Estados y transiciones
Ver A5 y B2.

## 6. Permisos
Según Plano Maestro — matriz por rol.

## 7. Errores y excepciones
Fallo de red, versión vieja, sin permiso, estado inválido (Maestro §5.10).

## 8. Auditoría
Eventos en `/orders/{id}/events` (B3).

## 9. Pruebas y validaciones
Tests Node aplicables + validación manual por rol.

## 10. Riesgos
Usar repo desactualizado; confundir shell Admin con módulo operativo.

---

## DICTAMEN DEL DOCUMENTO

| Campo | Valor |
|-------|-------|
| **Estado final** | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| **Documentación** | Cerrada |
| **Implementación repo** | Mapa cerrado; inventario vivo con el repo. |

### Estado actual (repo)
Núcleo V1 operativo; Admin config/roles shell; sin WhatsApp/IA/pagos/chat.

### Estado objetivo (Plano Maestro)
Plano Maestro completo operativamente.

### Brecha de implementación
Módulos no implementados (Q2); inventario se actualiza con cada release mayor del repo.

### Contradicciones resueltas
C-01 resuelta en A5. C-02 resuelta: prevalece repo HEAD `50c51fa`.

### Criterio de implementación futura
Re-inventariar tras cada bloque mayor de implementación.

---
*Documentación final afinada — 2026-06-10*
