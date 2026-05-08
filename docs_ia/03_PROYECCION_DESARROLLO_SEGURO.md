# PROYECCIÓN DE DESARROLLO SEGURO — PÉDILO

## Objetivo
Garantizar que todo avance futuro de Pédilo mantenga:
- arquitectura estable
- flujo V1 funcional
- contexto claro para IA
- trazabilidad de errores
- compatibilidad futura con Play Store

## Regla madre
Antes de desarrollar un bloque nuevo, debe mapearse:

1. Qué bloque se toca
2. Qué capa afecta
3. Qué archivos incluye
4. Qué flujo impacta
5. Qué riesgos introduce
6. Qué tests lo validan
7. Qué debe leer un chat nuevo para entenderlo

## Filtro anti-colapso obligatorio
Todo cambio debe responder:

- ¿Rompe reglas?
- ¿Qué capa toca?
- ¿Impacta state?
- ¿Impacta router?
- ¿Impacta render?
- ¿Impacta flujo?
- ¿Tiene efectos secundarios?
- ¿Es reversible?
- ¿Es bloque cerrado?

## Regla de bloques
No se permiten cambios parciales que dejen el sistema intermedio.

Un cambio debe ser:
- mínimo y aislado
o
- bloque completo, probado y documentado

## Estrés obligatorio
Antes de cerrar un bloque:

- usuario navega rápido
- datos vacíos
- error de API/Supabase
- pedido incompleto
- productos repetidos
- WhatsApp/salida externa
- pantalla sin datos
- tests automáticos

## Play Store
Todo desarrollo debe preservar:

- flujo cliente claro
- estabilidad visual
- privacidad de datos
- ausencia de secretos
- build Android futuro
- política de privacidad
- prueba en celular real

## IA y chats nuevos
La IA no decide ni aprueba.

Todo chat nuevo debe leer:
- docs_ia/00_CONTRATO_OFICIAL_PEDILO.md
- docs_ia/01_DIAGNOSTICO_ACTUAL.md
- docs_ia/02_ERRORES_Y_ADVERTENCIAS.md
- docs_ia/03_PROYECCION_DESARROLLO_SEGURO.md

## Criterio de avance
Solo se avanza si:

- npm run seguro pasa
- no hay errores críticos
- el bloque está cerrado
- el humano aprueba

## Garantía real
Este sistema no garantiza cero bugs.
Garantiza control, trazabilidad, prevención y reducción fuerte de errores humanos o de IA.

## Riesgos conocidos no bloqueantes

Estos puntos pueden ser detectados por auditorías o chats nuevos, pero NO deben interpretarse automáticamente como errores críticos del sistema actual.

### 1. window.* en app.js
Estado: warning conocido.
No bloquea desarrollo.
No implica reescritura.
Debe revisarse solo dentro de un bloque cerrado de app/render.

### 2. WhatsApp sin número real
Estado: intencional por etapa.
La app valida flujo y estructura de pedido antes de conectar número operativo definitivo.
Debe resolverse en el bloque WhatsApp / salida operativa.

### 3. Tests sin cobertura UI completa
Estado: pendiente de endurecimiento.
Los tests actuales validan datos y flujo base.
Los tests de UI son mejora futura, no bloqueo de esta etapa.

### 4. Uso de innerHTML
Estado: riesgo conocido de seguridad.
No implica colapso actual.
Debe resolverse en un bloque específico de sanitización/seguridad.

## Regla de interpretación

No todo riesgo detectado es un error actual.

Clasificación obligatoria:
- ERROR CRÍTICO: rompe contrato, flujo o seguridad inmediata.
- WARNING: riesgo conocido que requiere revisión humana.
- PENDIENTE CONTROLADO: mejora planificada por etapa.
- BLOQUE FUTURO: requiere desarrollo específico posterior.

Ningún chat nuevo debe proponer reescritura general por estos puntos.
Cualquier corrección debe tratarse como bloque cerrado, pasar por `npm run seguro` y requerir aprobación humana.
