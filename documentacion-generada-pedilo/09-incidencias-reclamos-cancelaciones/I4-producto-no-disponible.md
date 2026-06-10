# I4 — Producto No Disponible

**Familia:** I — Incidencias, reclamos y cancelaciones  
**Código:** I4  
**Fecha cierre documental:** 2026-06-09  
**Última afinación:** 2026-06-10  
**Estado final:** APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN

---

## 1. Identidad y alcance

| Campo | Valor |
|-------|-------|
| Código | I4 |
| Familia | I — Incidencias, reclamos y cancelaciones |
| Estado final | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |

### Fuentes usadas
1. Plano Maestro Conceptual Cerrado
2. Maestro Total de Documentación y Cierre de Producción
3. Repo `/home/oem/Desktop/pedilo` HEAD `50c51fa`
4. Tabla A5 (`00-maestros/A5-tabla-equivalencia-estados-plano-repo.md`)
5. Bloques en `/home/oem/Descargas/Carpeta sin título/` (si aplica)

### Objetivo
Reporte local, reemplazo, cliente decide.

### Alcance — incluye
Contenido operativo y reglas de Producto No Disponible según fuentes.

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
Implementar sin consultar dependencias Q4; confundir brecha de código con hueco documental.

---

## DICTAMEN DEL DOCUMENTO

| Campo | Valor |
|-------|-------|
| **Estado final** | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| **Documentación** | Cerrada |
| **Implementación repo** | Definición cerrada; código pendiente |

### Estado actual (repo)
No flujo completo.

### Estado objetivo (Plano Maestro)
Plano + bloque.

### Brecha de implementación
Flujo incidencia producto.

### Contradicciones resueltas
C-01 resuelta en A5. C-02 resuelta: prevalece repo HEAD `50c51fa`.

### Criterio de implementación futura
Implementar bloque I según Q4.

---
*Documentación final afinada — 2026-06-10*
