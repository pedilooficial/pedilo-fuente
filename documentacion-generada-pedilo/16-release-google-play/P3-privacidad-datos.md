# P3 — Privacidad y Datos

**Familia:** P — Release y Google Play  
**Código:** P3  
**Fecha cierre documental:** 2026-06-09  
**Última afinación:** 2026-06-10  
**Estado final:** APROBADO DOCUMENTALMENTE CON DECISIÓN EXTERNA OBLIGATORIA

---

## 1. Identidad y alcance

| Campo | Valor |
|-------|-------|
| Código | P3 |
| Familia | P — Release y Google Play |
| Estado final | APROBADO DOCUMENTALMENTE CON DECISIÓN EXTERNA OBLIGATORIA |

### Fuentes usadas
1. Plano Maestro Conceptual Cerrado
2. Maestro Total de Documentación y Cierre de Producción
3. Repo `/home/oem/Desktop/pedilo` HEAD `50c51fa`
4. Tabla A5 (`00-maestros/A5-tabla-equivalencia-estados-plano-repo.md`)
5. Bloques en `/home/oem/Descargas/Carpeta sin título/` (si aplica)

### Objetivo
Qué datos recolecta Pédilo, por qué, quién ve, retención, terceros.

### Alcance — incluye
Contenido operativo y reglas de Privacidad y Datos según fuentes.

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
Publicar sin política legal; confundir spec de datos con texto jurídico publicado.

---

## DICTAMEN DEL DOCUMENTO

| Campo | Valor |
|-------|-------|
| **Estado final** | APROBADO DOCUMENTALMENTE CON DECISIÓN EXTERNA OBLIGATORIA |
| **Documentación** | Cerrada |
| **Implementación repo** | Definición de datos cerrada; publicación legal pendiente owner. |

### Estado actual (repo)
Contenido de privacidad definido desde plano; texto legal publicable y email soporte no están en fuentes técnicas.

### Estado objetivo (Plano Maestro)
Política de privacidad completa según plano.

### Brecha de implementación
Publicación Play requiere decisiones externas listadas abajo.

### Decisiones externas obligatorias
1. **Razón social / titular legal** — responsable: dueño del proyecto — bloquea: publicación Play — no bloquea: implementación
2. **Email de soporte oficial** — responsable: dueño del proyecto — bloquea: ficha Play — no bloquea: implementación
3. **Aprobación jurídica del texto legal** — responsable: titular + asesoría — bloquea: publicación — no bloquea: definición de datos

### Contradicciones resueltas
C-01 resuelta en A5. C-02 resuelta: prevalece repo HEAD `50c51fa`.

### Criterio de implementación futura
Owner asigna razón social, email soporte y aprueba texto legal final.

---
*Documentación final afinada — 2026-06-10*
