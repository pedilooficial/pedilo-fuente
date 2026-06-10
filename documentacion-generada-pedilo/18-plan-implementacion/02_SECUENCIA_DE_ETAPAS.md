# Secuencia de Etapas de Implementación

**Orden:** Q → B → M → C → F → D → E → G → I → J → K → L → O → P

---

## Resumen

| # | Etapa | Archivo plan | Estado plan | Depende |
|---|-------|--------------|-------------|---------|
| 1 | Q — Transición repo | `03_ETAPA_Q_TRANSICION_REPO.md` | LISTO PARA IMPLEMENTAR | Doc cerrada |
| 2 | B — Pedido Vivo | `04_ETAPA_B_PEDIDO_VIVO.md` | LISTO CON DEPENDENCIA PREVIA (Q) | Q |
| 3 | M — Backend/Firebase | `05_ETAPA_M_BACKEND_FIREBASE_SEGURIDAD.md` | LISTO CON DEPENDENCIA PREVIA (B) | B |
| 4 | C — Usuario público | `06_ETAPA_C_USUARIO_PUBLICO.md` | LISTO CON DEPENDENCIA PREVIA (B,M) | B, M |
| 5 | F — Admin | `07_ETAPA_F_ADMIN.md` | LISTO CON DEPENDENCIA PREVIA (B,M) | B, M |
| 6 | D — Local | `08_ETAPA_D_LOCAL.md` | LISTO CON DEPENDENCIA PREVIA (F) | B, M, F |
| 7 | E — Repartidor | `09_ETAPA_E_REPARTIDOR.md` | LISTO CON DEPENDENCIA PREVIA (D) | B, M, D |
| 8 | G — Pagos/finanzas | `10_ETAPA_G_PAGOS_TARIFAS_FINANZAS.md` | LISTO CON DEPENDENCIA PREVIA (E,F) | B, E, F |
| 9 | I — Incidencias | `11_ETAPA_I_INCIDENCIAS_RECLAMOS_CANCELACIONES.md` | LISTO CON DEPENDENCIA PREVIA (G parcial) | B, F, G |
| 10 | J — Comunicación | `12_ETAPA_J_COMUNICACION.md` | LISTO CON DEPENDENCIA PREVIA (B,M) | B, M |
| 11 | K — IA | `13_ETAPA_K_IA.md` | LISTO CON DEPENDENCIA PREVIA (J) | B, J |
| 12 | L — Métricas/salud | `14_ETAPA_L_METRICAS_AUDITORIA_SALUD.md` | LISTO CON DEPENDENCIA PREVIA (B3,M) | B, M |
| 13 | O — Hardening | `15_ETAPA_O_PRUEBAS_HARDENING.md` | LISTO CON DEPENDENCIA PREVIA (todas) | C–L |
| 14 | P — Release/Play | `16_ETAPA_P_RELEASE_GOOGLE_PLAY.md` | NO IMPLEMENTAR TODAVÍA | O + P3 |

---

## Plantilla por etapa (detalle en archivos 03–16)

Cada archivo de etapa contiene:

1. Nombre y objetivo  
2. Documentos fuente (familia)  
3. Dependencias  
4. Zonas repo a revisar  
5. Qué se implementa / qué no  
6. Validaciones mínimas  
7. Pruebas mínimas  
8. Criterio aceptación / rechazo  
9. Estado del plan  

---

## Paralelización permitida (solo tras certificar dependencias)

| Paralelo | Condición |
|----------|-----------|
| C + F (parcial) | Tras M certificado |
| J + L | Tras M certificado |
| D + E endurecimiento | Tras F parcial |

**Prohibido:** G antes de E; P antes de O; saltar B.

---
*Secuencia alineada a Q4 documentación cerrada*
