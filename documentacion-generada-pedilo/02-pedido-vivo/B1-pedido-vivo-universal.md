# B1 — Pedido Vivo Universal

**Familia:** B — Núcleo del Pedido  
**Fecha cierre documental:** 2026-06-09  
**Última afinación:** 2026-06-10  
**Estado final:** APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN

---

## 1. Identidad

| Código | B1 |
| Estado final | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |

### Fuentes
1. Plano Maestro Conceptual Cerrado (`Pedilo_Plano_Maestro_Conceptual_Cerrado_Definitivo.md`)
2. Maestro Total de Documentación
3. Repo `/home/oem/Desktop/pedilo` HEAD `50c51fa`
4. Tabla equivalencia A5
5. Bloques `/home/oem/Descargas/Carpeta sin título/`

### Objetivo
Cerrar identidad, tipos, responsables, snapshots, tracking, cierre, archivo, reclamo posterior.

### Incluye
Cerrar identidad, tipos, responsables, snapshots, tracking, cierre, archivo, reclamo posterior.

### No incluye
Modificación de código; inventar reglas fuera de fuentes.

---

## 2. Reglas confirmadas
- Un Pedido, una verdad (Pedido Vivo Universal)
- UI representa; backend valida
- IA ayuda, no decide; WhatsApp comunica, no gobierna
- Pedido cerrado no se reabre; reclamo = caso posterior
- Ver A5 para equivalencia estados

## 3. Flujo operativo
Inicio→validación→acción→estado→evento→cierre. Ver plano §3.

## 4. Datos necesarios
Campos mínimos por tipo plano §4.

## 5. Estados y transiciones
Ver A5 y B2.

## 6. Permisos
Por rol plano §roles.

## 7. Errores y excepciones
Versión vieja, doble acción, sin permiso — rechazar.

## 8. Auditoría
Subcolección events.

## 9. Pruebas y validaciones
live_order_*, operational_order_actions_backend.

## 10. Riesgos
- Pedidos sin salida si timeouts no se implementan (B5).
- 4º tipo ausente limita locales.

---

## DICTAMEN DEL DOCUMENTO

| Campo | Valor |
|-------|-------|
| **Estado final** | APROBADO DOCUMENTALMENTE CON BRECHA DE IMPLEMENTACIÓN |
| **Documentación** | Cerrada |
| **Implementación repo** | Definición núcleo cerrada |

### Estado actual (repo)
4 tipos: local_order, direct_purchase, pickup_shipping (repo); store_driver_request (objetivo). Birth contract obligatorio. Snapshots al confirmar.

### Estado objetivo (Plano Maestro)
Repo: 3 tipos + núcleo V1. Objetivo: 4 tipos + 5 ejes completos + timeouts ejecutables.

### Brecha de implementación
Implementar 4º tipo; completar ejes financiero/comunicación/archivo; motor timeouts.

### Contradicciones resueltas
C-01 resuelta en A5. C-02 resuelta: prevalece repo HEAD `50c51fa`.

### Criterio de implementación futura
Bloque B antes de roles; tests obligatorios.

---
*Documentación final — 2026-06-09*
