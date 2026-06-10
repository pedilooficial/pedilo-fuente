# Prompts de Implementación por Etapa (Codex / Agente Programador)

**Uso:** Copiar el prompt de UNA etapa por ejecución. No combinar etapas.

**Reglas globales para todos los prompts:**
- No modificar `documentacion-generada-pedilo/` (documentación cerrada)
- No inventar reglas de producto
- No deploy Firebase/producción salvo autorización explícita
- No mezclar roles en un mismo commit
- Consultar A5 antes de cambiar estados
- Ejecutar tests y guards al finalizar
- Entregar reporte estilo Q7

---

## Prompt — Etapa Q (Transición repo)

```text
OBJETIVO: Ejecutar Etapa Q — Transición del repo según 18-plan-implementacion/03_ETAPA_Q_TRANSICION_REPO.md

FUENTES OBLIGATORIAS:
- documentacion-generada-pedilo/01-transicion-repo/Q1–Q7.md
- documentacion-generada-pedilo/00-maestros/A3, A4, A5.md
- Repo /home/oem/Desktop/pedilo

ALCANCE:
- Actualizar inventario técnico si HEAD cambió
- Verificar matriz conservar/ajustar/reemplazar
- Catalogar placeholders (Admin config, roles, claim UI, seed)
- NO implementar features de negocio

PROHIBIDO:
- Tocar lógica operateLiveOrder salvo bug crítico documentado
- Deploy producción
- Modificar documentación cerrada
- Implementar B, C, F, etc.

VALIDACIONES:
- node --test tests/*.test.js
- bash tools/guards/check_architecture.sh
- bash tools/guards/check_ui_quality.sh

ENTREGABLE:
- Reporte Q7 instancia etapa Q: archivos revisados, placeholders, dictamen, puede pasar a B
```

---

## Prompt — Etapa B (Pedido Vivo)

```text
OBJETIVO: Ejecutar Etapa B — Pedido Vivo Universal según 04_ETAPA_B_PEDIDO_VIVO.md

FUENTES: B1–B5, A5, functions/index.js, LiveOrderContract.kt

DEPENDENCIA: Q7 etapa Q aprobada

ALCANCE:
- Endurecer birth contract y 5 ejes estado
- operateLiveOrder + nextAllowedActions + eventos B3
- Idempotencia B4; timeouts B5 si aplica en esta iteración

PROHIBIDO:
- UI roles (C/F/D/E)
- WhatsApp, IA, pagos
- Cambiar reglas del Plano Maestro

TESTS: live_order_birth_contract, live_order_end_to_end_flow, operational_order_actions_backend

ENTREGABLE: Reporte Q7 etapa B
```

---

## Prompt — Etapa M (Backend/Firebase)

```text
OBJETIVO: Etapa M — Backend, Firebase, Security Rules según 05_ETAPA_M_BACKEND_FIREBASE_SEGURIDAD.md

FUENTES: M1–M5, firestore.rules, functions/index.js

DEPENDENCIA: Q7 etapa B aprobada

ALCANCE:
- Rules M4; Functions validación rol/estado/versión M3
- Colecciones M2; emulator tests M5

PROHIBIDO:
- Cliente escribe /orders
- Deploy prod sin autorización

TESTS: firestore_rules.test.js + regression live_order_*

ENTREGABLE: Reporte Q7 etapa M
```

---

## Prompt — Etapa C (Usuario público)

```text
OBJETIVO: Etapa C — Usuario público según 06_ETAPA_C_USUARIO_PUBLICO.md

FUENTES: C1–C4, ui/publicuser/

DEPENDENCIA: Q7 etapas B y M

ALCANCE:
- Formularios C3, tracking C2, cancelación C4 si B listo
- Mantener anti-placeholder

PROHIBIDO:
- Admin/Store/Driver UI
- WhatsApp real (etapa J)
- Datos demo en producción

TESTS: local_order_flow, plus_order_flow, public_tracking_flow, public_input_hardening

ENTREGABLE: Reporte Q7 etapa C
```

---

## Prompt — Etapa F (Admin)

```text
OBJETIVO: Etapa F — Admin real según 07_ETAPA_F_ADMIN.md

FUENTES: F1–F4, AdminApp.kt

DEPENDENCIA: Q7 B+M

ALCANCE:
- F2 config persistencia; F3 alta roles; endurecer F1/F4

PROHIBIDO:
- Config modifica pedidos vivos sin intervención auditada
- Mezclar con Store/Driver

TESTS: admin_operational_actions, admin_visual_shell (sin regresión operación)

ENTREGABLE: Reporte Q7 etapa F
```

---

## Prompt — Etapa D (Local)

```text
OBJETIVO: Etapa D — Local/Store según 08_ETAPA_D_LOCAL.md

FUENTES: D1–D5, StoreApp.kt

DEPENDENCIA: Q7 F (parcial)

ALCANCE:
- Endurecer Store V1; D5 store_driver_request si B listo; multi-store D3

PROHIBIDO:
- Carrito multi-local
- Pasar a driver antes aceptar+listo

TESTS: store_operational_flow, live_order_end_to_end_flow

ENTREGABLE: Reporte Q7 etapa D
```

---

## Prompt — Etapa E (Repartidor)

```text
OBJETIVO: Etapa E — Repartidor/Driver según 09_ETAPA_E_REPARTIDOR.md

FUENTES: E1–E4, DriverApp.kt

DEPENDENCIA: Q7 D

ALCANCE:
- Capacidad E2; preparar cobro E3; integrar con G después

PROHIBIDO:
- Aprobar pago dudoso
- GPS/mapas

TESTS: driver_operational_flow

ENTREGABLE: Reporte Q7 etapa E
```

---

## Prompt — Etapa G (Pagos/finanzas)

```text
OBJETIVO: Etapa G — Pagos, tarifas, finanzas según 10_ETAPA_G_PAGOS_TARIFAS_FINANZAS.md

FUENTES: G1–G4, E3, E4, bloque Capa financiera

DEPENDENCIA: Q7 E+F

ALCANCE:
- Estados financieros; tarifas snapshot; cierre caja; modos lluvia en tarifa

PROHIBIDO:
- Pasarela online
- IA aprueba pagos sola
- Cerrar financiero en pago dudoso automático

TESTS: nuevos tests financieros + regression pedido

ENTREGABLE: Reporte Q7 etapa G
```

---

## Prompt — Etapa I (Incidencias)

```text
OBJETIVO: Etapa I según 11_ETAPA_I_INCIDENCIAS_RECLAMOS_CANCELACIONES.md

FUENTES: I1–I4

DEPENDENCIA: Q7 G parcial

ALCANCE:
- Reclamos backend I2; cancelaciones I3; producto no disponible I4

PROHIBIDO:
- Reabrir pedido cerrado

ENTREGABLE: Reporte Q7 etapa I
```

---

## Prompt — Etapa J (Comunicación)

```text
OBJETIVO: Etapa J según 12_ETAPA_J_COMUNICACION.md

FUENTES: J1–J4

DEPENDENCIA: Q7 B+M

ALCANCE:
- WhatsApp canal (no gobierna); chat interno; notificaciones

PROHIBIDO:
- WhatsApp como fuente de verdad
- Chat cambia estados

ENTREGABLE: Reporte Q7 etapa J
```

---

## Prompt — Etapa K (IA)

```text
OBJETIVO: Etapa K según 13_ETAPA_K_IA.md

FUENTES: K1–K3

DEPENDENCIA: Q7 J

ALCANCE:
- IA asiste; deriva Admin; K2 compra directa; K3 señal riesgo pagos

PROHIBIDO:
- IA cancela, asigna, cierra, aprueba pagos dudosos sola

ENTREGABLE: Reporte Q7 etapa K
```

---

## Prompt — Etapa L (Métricas/salud)

```text
OBJETIVO: Etapa L según 14_ETAPA_L_METRICAS_AUDITORIA_SALUD.md

FUENTES: L1–L3, B3

DEPENDENCIA: Q7 M

ALCANCE:
- Métricas desde eventos; salud sistema Admin

PROHIBIDO:
- Métricas inventadas en UI

ENTREGABLE: Reporte Q7 etapa L
```

---

## Prompt — Etapa O (Hardening)

```text
OBJETIVO: Etapa O según 15_ETAPA_O_PRUEBAS_HARDENING.md

FUENTES: O1–O3

DEPENDENCIA: Q7 etapas C–L

ALCANCE:
- Checklist O1; carga O2; observabilidad O3; verificar Q6

PROHIBIDO:
- Release Play

ENTREGABLE: Reporte Q7 etapa O + checklist firmado
```

---

## Prompt — Etapa P (Release/Play)

```text
OBJETIVO: Etapa P según 16_ETAPA_P_RELEASE_GOOGLE_PLAY.md

FUENTES: P1–P3

DEPENDENCIA: Q7 O + P3 decisión externa resuelta para PUBLICACIÓN

ALCANCE:
- Build release, AAB, checklist Play, Data Safety según spec P3

PROHIBIDO:
- Publicar sin P3 legal
- Inventar razón social, email, texto legal
- Release antes de O verde

ENTREGABLE: Reporte Q7 etapa P; NO publicar hasta P3 externo OK
```

---
*Un prompt por etapa — no mezclar alcances*
