# Matriz real / parcial / placeholder

**Fecha:** 2026-06-09  
**Criterio:** no transformar placeholders en funcionalidad, no modificar producto, no tocar producción.

| Módulo | Archivo o zona | Estado | Qué hace hoy | Qué NO hace hoy | Riesgo si se interpreta mal | Decisión |
|--------|----------------|--------|--------------|-----------------|-----------------------------|----------|
| Usuario público | `ui/publicuser/PublicApp.kt` y pantallas públicas | real | Navegación pública, local, tienda, plus, tracking y acceso interno. | No implementa cuenta pública ni módulo completo de reclamos/comunicación. | Romper el flujo público que ya crea pedidos reales. | conservar |
| Tracking público | `FirebasePublicTrackingAdapter.kt`, `functions/index.js` | real | Consulta tracking por número visible vía callable. | No ofrece chat, reclamo persistente ni mapa. | Confundir tracking con atención completa post pedido. | conservar |
| Botón + | `PublicPlus.kt`, `FirebasePublicPlusOrderAdapter.kt`, `createPlusOrder` | real/parcial | Crea pedidos de compra o retiro/envío. | No tiene IA, WhatsApp real ni cálculo financiero completo. | Venderlo como asistente inteligente o flujo financiero completo. | conservar y marcar parcial |
| Tienda/Local público | `PublicShop.kt`, `PublicLocal.kt`, `FirebasePublicCatalogAdapter.kt`, `createLocalOrder` | real/parcial | Lee catálogo y crea pedidos de local. | No administra stock/variantes/extras desde Store. | Asumir catálogo administrable por local desde app. | conservar y marcar parcial |
| Reclamos públicos | `PublicConventions.kt` | placeholder | Muestra formulario/confirmación visual local. | No crea claims reales ni auditoría backend. | Hacer creer que el reclamo quedó registrado en sistema. | marcar y aislar |
| Admin operación | `ui/admin/AdminApp.kt`, `FirebaseAdminOrdersAdapter.kt` | real/parcial | Lee y opera pedidos reales, eventos y acciones. | No es panel completo de configuración, métricas o finanzas. | Mezclar mesa operativa real con shells visuales. | conservar y marcar parcial |
| Admin configuración | `AdminApp.kt` secciones de configuración | placeholder/visual | Ordena secciones visuales futuras. | No persiste configuración ni gobierna backend. | Creer que cambios visuales modifican operación real. | marcar y aislar |
| Admin alta de roles | `AdminApp.kt`, `RoleAccessData.kt` | placeholder/visual | Presenta estructura visual de roles/accesos. | No crea usuarios ni vincula roles reales. | Operar equipo real desde pantalla no persistente. | marcar y aislar |
| Store/Local | `StoreApp.kt`, `FirebaseStoreOrdersAdapter.kt` | real/parcial | Observa pedidos propios y ejecuta acciones V1. | No administra catálogo, stock, finanzas ni solicitud específica de driver. | Esperar operación comercial completa del local. | conservar y marcar parcial |
| Driver/Repartidor | `DriverApp.kt`, `FirebaseDriverOrdersAdapter.kt` | real/parcial | Ve pedidos disponibles/asignados y opera retiro/entrega. | No tiene cierre de caja, deuda, capacidad ni pagos. | Cerrar entregas sin control financiero. | conservar y marcar parcial |
| Pagos/finanzas | `PaymentMethod.kt`, campos en `/orders` | parcial | Registra método/importes simples y `financialStatus`. | No cobra, no valida transferencias, no cierra caja. | Confundir campos declarativos con sistema financiero. | marcar parcial |
| Comunicación | UI pública/Admin, `communicationStatus` | parcial/placeholder | Guarda teléfono y estado declarativo. | No WhatsApp API, chat, FCM ni colas. | Prometer comunicación automática inexistente. | marcar y aislar |
| IA | repo completo | no implementado | No hay runtime ni proveedor detectado. | No sugiere, no modifica, no procesa compras. | Asumir asistencia inteligente inexistente. | no tocar |
| Métricas/salud | Admin visual, eventos | placeholder/parcial | Eventos por pedido y secciones visuales. | No agregación, alertas ni salud backend. | Creer que hay observabilidad productiva. | marcar y aislar |
| Pedido Vivo | `functions/index.js`, modelos core | sensible/real parcial | Estados, transiciones V1, versión, eventos, incidencias y acciones permitidas. | No timeouts ejecutables ni ejes completos. | Romper contrato central. | no tocar |
| Firestore Rules | `firestore.rules` | sensible/real | Protege lecturas/escrituras por rol y bloquea writes directos. | No reemplaza validaciones backend completas. | Abrir permisos por error. | no tocar |
| Adapters Firebase | `core/firebase/*Adapter.kt` | sensible/real | Conectan UI/core con Firestore y Functions. | No encapsulan todos los módulos futuros. | Romper wire contracts. | no tocar salvo corrección mínima |
| Scripts Firebase | `tools/seed_public_catalog.js`, `tools/verify_public_catalog.js` | sensible | Seed/verificación de catálogo. | No son runtime ni deben ejecutarse contra producción sin control. | Tocar datos reales accidentalmente. | aislar/no ejecutar |
| Config Firebase local | `.firebaserc`, `app/google-services.json`, `firebase.json` | sensible | Apunta a proyecto/config local Firebase. | No debe usarse como autorización de deploy. | Deploy o uso de producción accidental. | no tocar |
