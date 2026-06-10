# Mapa de Brechas a Implementar

**Fuente:** Documentación cerrada + repo `50c51fa`  
**Estado:** LISTO PARA IMPLEMENTAR

---

## Leyenda

| Prioridad | Significado |
|-----------|-------------|
| P0 | Bloquea operación segura |
| P1 | Necesario para etapa objetivo |
| P2 | Completa definición documental |

| Etapa | Código en plan |

---

## Tabla de brechas

| ID | Documento | Familia | Descripción | Estado actual | Estado objetivo | Prioridad | Depende | Etapa | Riesgo si no se implementa |
|----|-----------|---------|-------------|---------------|-----------------|-----------|---------|-------|---------------------------|
| BR-01 | B1, D5 | B/D | 4º tipo `store_driver_request` | No existe | Tipo operativo local solicita repartidor | P1 | B, M | D (D5) | Locales sin flujo documentado |
| BR-02 | B2, A5 | B | Ejes financiero/comunicación/archivo completos | Parcial (`pending_review`, `received`, `live`) | 5 ejes plano | P0 | B | B | Estados imposibles; cierre incorrecto |
| BR-03 | B5 | B | Motor timeouts/fallbacks autónomo | Declarativo en birth | Ejecución sistema + escalada | P0 | B, M | B | Pedidos trabados sin salida |
| BR-04 | B1 | B | Snapshots modos/tarifas en birth | Parcial | `liveSnapshot` congelado completo | P1 | F2, G2 | G | Precios disputables |
| BR-05 | F2 | F | Admin Configuración persistencia | Shell UI | Config versionada Firestore | P1 | M | F | Config no gobierna futuro |
| BR-06 | F3 | F | Alta de roles CRUD | Shell UI | `/users` + Functions Admin | P1 | M | F | No operar equipo real |
| BR-07 | C4 | C | Cancelación pública por estado | No existe | Function + reglas B2 | P1 | B, M | C | Cliente sin salida regulada |
| BR-08 | I2, C1 | I/C | Reclamos posteriores backend | UI `sent=true` | Colección claims | P1 | B, M | I | Reclamos no auditados |
| BR-09 | C3, J1 | C/J | Validación teléfono WhatsApp | No existe | Cola + fallback | P1 | J | J | Cliente nuevo sin validar |
| BR-10 | G1–G3, E3 | G/E | Pagos y cobro en entrega | No existe | Estados financieros vivos | P0 | B, E | G | Entrega sin cierre financiero |
| BR-11 | E4, G1 | G/E | Cierre de caja repartidor | No existe | Colección + revisión Admin | P0 | G | G | Deuda sin control |
| BR-12 | E2 | E | Capacidad repartidor configurable | No existe | `capacidad_base` en config | P2 | F2 | E | Doble toma / sobrecarga |
| BR-13 | J1–J3 | J | WhatsApp, chat, notificaciones | No existe | Canales según plano | P1 | B, M | J | Comunicación manual only |
| BR-14 | K1–K3 | K | IA controlada | No existe | Asistencia sin autoridad | P2 | J | K | Sin estructuración compra directa |
| BR-15 | L1–L3 | L | Métricas y salud sistema | No existe | Agregación eventos | P2 | B3, M | L | Admin sin visibilidad |
| BR-16 | H1–H4 | H | Modos lluvia/saturación/mantenimiento | No existe | Config F2 + snapshot | P1 | F2, G2 | F/G | Tarifas sin modo |
| BR-17 | D4 | D | Variantes/extras/stock | Productos simples | Modelo extendido opcional | P2 | F2 | D | Catálogo limitado |
| BR-18 | I1–I4 | I | Incidencias flujo completo | Parcial V1 | Tipos + tiempos plano | P1 | B, F | I | Excepciones sin salida |
| BR-19 | Q6 | Q | Placeholders en producción | Admin shell, claim UI, seed | Aislados/eliminados en release | P0 | — | Q/O | Demo en producción |
| BR-20 | O2 | O | Pruebas carga 1000 pedidos | No ejecutadas | Plan O2 ejecutado | P1 | O | O | Colapso en producción |
| BR-21 | N4 | N | Tests JUnit Android | No existen | Suite mínima | P2 | N | O | Regresión UI no detectada |
| BR-22 | P1–P2 | P | Release certificado | Debug OK | AAB firmado + checklist | P1 | O | P | Publicación insegura |
| BR-23 | P3 | P | Privacidad legal publicable | Spec datos cerrada | Texto legal + Play | — | Externo | P | **BLOQUEADO publicación** |

---

## Brechas ya cubiertas parcialmente (endurecer, no rehacer)

| Área | Repo V1 | Etapa |
|------|---------|-------|
| Birth contract Pedido | `functions/index.js` | B |
| operateLiveOrder | Functions + Store/Driver UI | B, M |
| Público local/plus/tracking | Adapters + UI | C |
| Admin operación lectura/acciones | AdminApp | F |
| Store/Driver ops básicas | StoreApp, DriverApp | D, E |
| Idempotencia birth + versión | Functions | B |
| Rules deny client writes | firestore.rules | M |
| Guards arquitectura/UI | tools/guards | Q, O |

---
*Mapa derivado de Q2, informe final e índice*
