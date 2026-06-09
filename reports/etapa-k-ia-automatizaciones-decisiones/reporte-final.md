# Etapa K - IA / Automatizaciones / Decisiones asistidas

## Alcance

Se implemento una base de decisiones asistidas segura para Pedilo, sin proveedor externo activo y sin automatizaciones criticas autonomas.

La etapa introduce un motor deterministico interno que genera sugerencias persistentes y auditables para ordenes, reclamos e incidencias operativas. Las sugerencias quedan marcadas como asistencia, no como decisiones ejecutadas.

## Contrato implementado

- Motor: `deterministic_rules_v1`.
- Proveedor externo: `disabled`.
- Persistencia: subcoleccion `orders/{orderId}/ai_decisions/{aiDecisionId}`.
- Metadatos resumidos en la orden: riesgo, clasificacion, sugerencia, revision humana requerida, estado del proveedor y version de motor.
- Resolucion Admin: `accepted`, `rejected` o `not_applicable`.
- Auditoria: actor Admin, fecha de resolucion, nota opcional y evento `assisted_decision_resolved`.

## Restricciones de seguridad

- No cancela pedidos.
- No confirma pagos.
- No resuelve reclamos.
- No cierra incidencias.
- No bloquea usuarios, tiendas ni repartidores.
- No envia WhatsApp, push ni notificaciones reales.
- No ejecuta mutaciones criticas sobre estado operativo o financiero.
- No usa secretos.
- No llama a proveedores externos.

## Exposicion por rol

- Admin: ve clasificacion, riesgo, sugerencia y revision humana.
- Store: ve solo ayuda operativa propia y segura.
- Driver: ve solo ayuda operativa propia y segura.
- Usuario publico: no ve detalles internos de IA ni automatizacion; tracking solo muestra revision operativa generica cuando corresponde.

## Reglas

La subcoleccion `ai_decisions` hereda el permiso de lectura de la orden y bloquea escrituras directas desde clientes. Las escrituras quedan reservadas al backend administrativo.

## Validaciones previstas

- `git status --short`
- `node --test tests/*.test.js`
- `npm --prefix functions run build`
- `bash tools/guards/check_architecture.sh`
- `bash tools/guards/check_ui_quality.sh`
- `./gradlew assembleDebug --offline`
- `./gradlew lintDebug --offline`
- `git diff --check`

## Dictamen

BLOQUE K COMPLETO CON RIESGOS CONTROLADOS Y LISTO PARA CONTINUAR.
