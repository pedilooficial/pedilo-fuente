# Zonas sensibles no tocar

**Fecha:** 2026-06-09  
**Regla:** durante baseline seguro no se modifican zonas funcionales sensibles salvo corrección mínima, justificada y validada.

| Zona sensible | Motivo | Riesgo | Validación que la protege | Cambio prohibido por ahora |
|---------------|--------|--------|---------------------------|----------------------------|
| `functions/index.js` | Contiene nacimiento de pedidos, tracking, acciones Admin/live, idempotencia y transacciones. | Romper creación pública, operación o contrato del pedido. | Tests Node, Functions build, architecture guard. | Cambiar payloads, estados, transiciones, colecciones, permisos o contratos. |
| `firestore.rules` | Controla lecturas por rol y bloquea escrituras directas. | Exponer pedidos o permitir writes cliente. | `tests/firestore_rules.test.js`, architecture guard. | Abrir permisos, crear paths sin tests o permitir writes directos. |
| Firebase adapters | `app/src/main/java/com/pedilo/app/core/firebase/` | Conectan UI con Firestore/Functions y wire names. | Tests Node de arquitectura/flows, Android build/lint. | Cambiar callables, queries, campos o manejo de versiones sin etapa. |
| Modelos del pedido | `core/model/*Order*`, `LiveOrderContract.kt`, actions/status. | Son contrato compartido UI/backend. | Build Android, tests de contrato/operación. | Renombrar estados, acciones o campos operativos. |
| Acciones operativas | `LiveOrderAction`, `AdminOrderAction`, `operateLiveOrder`. | Gobiernan qué puede hacer cada rol. | `operational_order_actions_backend.test.js`, flow tests. | Agregar/quitar acciones o alterar permisos. |
| `nextAllowedActions` | Campo backend consumido por Admin/Store/Driver. | Botones incorrectos o acciones fuera de estado. | Live order tests, Store/Driver tests. | Recalcular en cliente o cambiar significado. |
| `expectedVersion` | Control de concurrencia en acciones live. | Pisadas de estado y carreras operativas. | Backend action tests, Android build. | Omitirlo, hacerlo opcional o cambiar semántica. |
| Eventos | `/orders/{id}/events`, `lastOperationEvent`. | Pérdida de auditoría mínima. | Live order end-to-end tests. | Dejar acciones sin evento o cambiar estructura sin etapa. |
| Incidencias | `/orders/{id}/incidents`, flags `activeIncident/incidentStatus`. | Excepciones sin trazabilidad. | Backend operational tests. | Convertir reclamos públicos en incidencias sin diseño de etapa. |
| UI pública funcional | `ui/publicuser/PublicLocal.kt`, `PublicPlus.kt`, tracking/catalog. | Romper alta de pedidos reales. | Public flow tests, UI quality guard, Android build/lint. | Cambios visibles funcionales fuera de etapa. |
| Admin operativo | `ui/admin/AdminApp.kt`, `FirebaseAdminOrdersAdapter.kt`. | Confundir operación real con shells visuales o romper mesa. | Admin tests, Android build/lint. | Refactor grande, mezclar configuración visual con writes reales. |
| Store/Local operativo | `ui/store/StoreApp.kt`, `FirebaseStoreOrdersAdapter.kt`. | Local deja de operar pedidos propios. | Store flow tests, Android build/lint. | Cambiar ownership `storeId == uid` o acciones. |
| Driver/Repartidor operativo | `ui/driver/DriverApp.kt`, `FirebaseDriverOrdersAdapter.kt`. | Driver deja de ver/tomar/entregar pedidos. | Driver flow tests, Android build/lint. | Cambiar queries de disponibles/asignados o acciones. |
| Guards | `tools/guards/` | Son barrera de arquitectura/UI. | Tests de guards y ejecución directa. | Bajar exigencia, excluir zonas críticas o silenciar fallos. |
| Scripts Firebase | `tools/seed_public_catalog.js`, `tools/verify_public_catalog.js`. | Pueden tocar datos si se ejecutan contra proyecto real. | Catalog tests, revisión manual. | Ejecutar seed, cambiar destino o relajar confirmaciones. |
| `.firebaserc` | Proyecto default Firebase. | Deploy/uso accidental de producción. | Revisión manual y no deploy. | Modificar proyecto default. |
| `app/google-services.json` | Config Android Firebase local. | Exposición o cambio de backend objetivo. | Build Android y revisión manual. | Modificar, reemplazar o publicar contenido. |

## Decisión

Estas zonas quedan explícitamente fuera de cambios funcionales durante el baseline seguro. El próximo bloque técnico puede leerlas y diseñar etapas, pero no debe alterarlas sin objetivo, tests y criterio de aceptación propio.
