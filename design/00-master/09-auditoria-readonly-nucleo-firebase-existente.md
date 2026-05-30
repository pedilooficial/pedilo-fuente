# 09 — Auditoría read-only del núcleo real y Firebase existente — Pédilo!

## 1. Propósito

Este documento define la próxima etapa delicada de Pédilo!: analizar el núcleo real anterior y la configuración Firebase existente sin modificar nada, para poder reescribir un núcleo nuevo limpio y compatible con los datos reales ya usados por el proyecto.

La intención no es volver a la versión vieja ni copiarla encima de la app actual.

La intención es:

```text
leer
entender
extraer contratos reales
detectar riesgos
documentar
reescribir limpio después
```

---

## 2. Principio central

```text
Firebase real existente no se rompe.
La versión anterior no se migra ciegamente.
La app pública nueva no se contamina con UI vieja.
El núcleo nuevo se reescribe limpio, aprendiendo del núcleo anterior.
```

La versión anterior debe tratarse como fuente de conocimiento técnico, no como código para restaurar.

---

## 3. Regla absoluta de esta etapa

Esta etapa es **read-only**.

Codex puede:

- leer archivos;
- inspeccionar estructura;
- buscar modelos;
- mapear colecciones;
- estudiar funciones;
- estudiar reglas;
- generar informe;
- proponer plan de integración futura.

Codex no puede:

- escribir datos en Firebase;
- modificar Firestore;
- modificar Realtime Database si existiera;
- modificar reglas;
- modificar Cloud Functions;
- hacer deploy;
- tocar producción;
- copiar carpetas viejas encima del proyecto actual;
- restaurar UI vieja;
- reemplazar arquitectura actual;
- modificar `google-services.json`;
- cambiar configuración del proyecto Firebase;
- borrar archivos;
- hacer migraciones;
- crear núcleo nuevo todavía.

---

## 4. Contexto actual

La etapa pública visual/mock ya fue cerrada y auditada.

Punto seguro actual:

```text
cce6dd6 Audit and harden public user flow before real integration
```

Dictamen:

```text
A) aprobado para pasar a etapa real
```

Confirmado hasta ese punto:

- usuario público visual/mock completo;
- Home;
- Tienda;
- Buscador;
- Convenciones;
- Botón +;
- Local;
- Tracking visual/mock;
- Back nativo;
- validaciones mínimas;
- placeholders correctos;
- auditoría/stress público;
- sin Firebase/backend/core real tocado;
- sin pedido real;
- sin pago real;
- sin WhatsApp real;
- sin tracking persistente real.

---

## 5. Fuente a auditar

La versión anterior se encuentra separada del proyecto actual, por ejemplo:

```text
~/Desktop/pedilo_backup_2026-05-18_20-12-19/
```

El proyecto actual está separado, por ejemplo:

```text
~/Desktop/pedilo/
```

Regla:

```text
El backup se lee.
El proyecto actual se protege.
No se copia el backup encima del proyecto actual.
```

---

## 6. Objetivo de la auditoría

Codex debe producir un informe claro que responda:

1. Qué núcleo real existe en la versión anterior.
2. Qué Firebase usa realmente.
3. Qué datos reales ya están estructurados.
4. Qué modelos de pedido existen.
5. Qué colecciones y campos no se deben romper.
6. Qué funciones o callables existen.
7. Qué reglas de seguridad existen.
8. Qué partes sirven como conocimiento reutilizable.
9. Qué partes están acopladas a UI vieja y no deben traerse.
10. Qué contrato necesita la app pública nueva para conectarse sin romper Firebase.
11. Qué riesgos existen antes de integrar.
12. Qué plan de reescritura limpia conviene seguir.

---

## 7. Información a extraer

### 7.1 Firebase / configuración

Buscar y documentar:

- proyecto Firebase usado;
- `google-services.json` existente, sin exponer claves ni contenido sensible;
- módulos Android que dependen de Firebase;
- plugins Gradle relacionados;
- dependencias Firebase;
- inicialización;
- servicios usados:
  - Firestore;
  - Realtime Database si existiera;
  - Authentication;
  - Storage;
  - Cloud Messaging;
  - Cloud Functions;
  - Analytics;
  - Crashlytics;
  - otros.

No copiar secretos en el informe.

Si se detectan credenciales o datos sensibles:

```text
mencionar que existen, pero no pegarlas.
```

---

### 7.2 Firestore / base de datos

Mapear colecciones reales o esperadas, por ejemplo:

- orders;
- customers;
- stores;
- drivers;
- users;
- incidents;
- tracking;
- notifications;
- metrics;
- settings;
- cualquier otra colección encontrada.

Para cada colección, documentar:

```text
nombre de colección
propósito
campos principales
tipo de dato aproximado
quién escribe
quién lee
qué rol la usa
qué flujo depende de ella
riesgos de compatibilidad
```

No modificar ni crear datos.

---

### 7.3 Pedido real

Extraer todo lo relacionado con el pedido.

Buscar:

- modelo de orden/pedido;
- campos del pedido;
- número de pedido;
- trackingNumber;
- estado;
- origen;
- cliente;
- teléfono;
- dirección;
- local;
- repartidor;
- total;
- pago;
- timestamps;
- historial;
- auditoría;
- incidentes;
- cancelación;
- reclamos;
- asignación;
- cambios de estado;
- validación inicial;
- pool operativo;
- cualquier relación con roles.

Documentar:

```text
qué representa el pedido
cómo se crea
dónde se guarda
cómo se actualiza
cómo se lee
cómo se sigue públicamente
qué estados tiene
qué campos son obligatorios
qué campos son internos
qué campos puede ver el cliente
qué campos no debe ver el cliente
```

---

### 7.4 Tracking público

Extraer información relacionada con:

- trackingNumber;
- búsqueda por número;
- estado público visible;
- estados internos;
- estados humanos;
- tiempos estimados;
- pedido recibido;
- preparando;
- en camino;
- entregado;
- cancelado;
- problemas;
- qué ve el cliente;
- qué no ve el cliente;
- qué ocurre con pedidos entregados/archivados.

Regla futura ya definida:

```text
Pedido entregado/archivado no debe exponer detalles viejos al público.
Debe mostrar agradecimiento y opción de seguir pidiendo.
```

---

### 7.5 Validación de teléfono / cliente

Buscar:

- cómo se identifica al cliente;
- uso del teléfono como clave;
- validación por WhatsApp;
- cliente conocido;
- cliente nuevo;
- alertas;
- bloqueos;
- reportes previos;
- reglas para permitir entrada al pool operativo;
- qué pasa si no confirma.

Regla conceptual ya definida:

```text
El teléfono es la clave única de validación del cliente.
Pedidos de cualquier origen pasan por validación inicial antes de entrar al flujo operativo.
```

---

### 7.6 Roles

Extraer sin reconstruir todavía:

- usuario público;
- admin;
- local/store;
- repartidor/driver;
- otros roles si existen.

Para cada rol:

```text
qué puede leer
qué puede escribir
qué pantallas tenía
qué acciones hacía
qué dependencias tenía con Firebase
qué parte es núcleo y qué parte es UI vieja
```

Importante:

```text
No traer UI vieja.
Solo extraer reglas, permisos, contratos y flujos.
```

---

### 7.7 Cloud Functions / backend

Buscar:

- carpeta `functions`;
- callables;
- triggers;
- scheduled functions;
- endpoints;
- validaciones;
- creación de pedido;
- asignación;
- tracking;
- notificaciones;
- incidentes;
- métricas;
- limpieza;
- timeouts;
- comunicación WhatsApp;
- pagos;
- cualquier integración externa.

Para cada función:

```text
nombre
tipo
qué recibe
qué devuelve
qué colección toca
qué rol la llama
qué errores maneja
si parece productiva o experimental
riesgos de tocarla
```

No hacer deploy.

---

### 7.8 Seguridad / reglas

Buscar:

- Firestore rules;
- Storage rules;
- validaciones por rol;
- acceso público;
- restricciones;
- permisos de escritura;
- permisos de lectura;
- reglas que dependen de auth;
- reglas que dependen de custom claims;
- huecos de seguridad.

No modificar reglas.

Documentar riesgos.

---

### 7.9 Notificaciones / WhatsApp / comunicación

Extraer si existe:

- FCM;
- WhatsApp;
- mensajes al cliente;
- mensajes al repartidor;
- avisos al local;
- templates;
- estados que disparan comunicación;
- confirmación de teléfono;
- reclamos;
- cancelaciones;
- problemas.

Separar:

```text
implementado real
parcial
mock
pendiente
riesgoso
```

---

### 7.10 Pagos

Extraer si existe:

- formas de pago;
- pago en efectivo;
- pago al retirar;
- pago al local;
- pago pendiente;
- pagos online;
- integraciones;
- campos financieros;
- totales;
- comisiones;
- estados de pago.

No activar ni modificar integraciones.

---

### 7.11 Métricas

Extraer si existe:

- métricas de local;
- métricas de repartidor;
- métricas de pedido;
- tiempos;
- ganancias;
- producción;
- rankings;
- ofertas;
- nuevos locales;
- uso de categorías.

Separar:

```text
real
calculado
mock
pendiente
```

---

## 8. Separación obligatoria: útil vs no traer

El informe debe separar claramente:

### A. Reutilizable conceptualmente

Ejemplos:

- modelos de datos;
- nombres de colecciones;
- estados reales;
- reglas de validación;
- funciones productivas;
- contratos de lectura/escritura;
- reglas de seguridad.

### B. Reutilizable con adaptación

Ejemplos:

- repositorios acoplados a UI vieja;
- ViewModels parcialmente útiles;
- funciones que requieren limpieza;
- modelos que tienen campos ambiguos;
- lógica que depende de rutas antiguas.

### C. No traer

Ejemplos:

- UI vieja;
- navegación vieja;
- pantallas legacy;
- recursos visuales viejos;
- PlanScreen;
- TapZone;
- screenshots runtime;
- rutas antiguas;
- compatibilidad artificial;
- parches;
- código duplicado;
- textos viejos;
- mocks confundidos con datos reales.

---

## 9. Riesgos a detectar

Codex debe buscar y reportar:

- campos ambiguos;
- `storeId` ambiguo;
- pedidos sin trackingNumber;
- pedidos con estados inconsistentes;
- datos públicos mezclados con datos internos;
- roles mezclados;
- reglas permisivas;
- funciones que escriben sin validar;
- dependencias de UI vieja;
- datos mock dentro de código real;
- Firebase productivo mezclado con demo;
- variables o colecciones hardcodeadas;
- riesgos de romper datos existentes;
- lógica real sin tests;
- rutas antiguas que contradicen la app nueva.

---

## 10. Entregable esperado

La salida de esta fase debe ser un informe Markdown.

Nombre sugerido:

```text
design/00-master/10-informe-auditoria-nucleo-firebase-real.md
```

El informe debe tener secciones:

1. Resumen ejecutivo.
2. Archivos/carpetas auditadas.
3. Mapa Firebase.
4. Mapa Firestore.
5. Modelo real de pedido.
6. Estados del pedido.
7. Tracking público.
8. Validación de cliente/teléfono.
9. Roles.
10. Cloud Functions/backend.
11. Seguridad/rules.
12. Notificaciones/comunicación.
13. Pagos.
14. Métricas.
15. Qué se puede reutilizar conceptualmente.
16. Qué requiere adaptación.
17. Qué no debe traerse.
18. Riesgos.
19. Contrato recomendado para el nuevo núcleo.
20. Plan progresivo de integración.
21. Preguntas pendientes.
22. Dictamen final.

---

## 11. Dictamen final esperado

Codex debe terminar con uno de estos dictámenes:

```text
A) Núcleo/Firebase anterior apto como fuente de verdad técnica para reescritura limpia.
B) Parcialmente apto: sirve como referencia, pero requiere limpiar riesgos antes de integrar.
C) No apto para reutilización conceptual directa: solo sirve como archivo histórico.
```

El dictamen debe estar justificado.

---

## 12. Reglas para el nuevo núcleo futuro

Todavía no se implementa, pero el informe debe proyectar la futura arquitectura:

```text
UI pública nueva
↓
capa pública de casos de uso
↓
núcleo nuevo limpio
↓
adaptador Firebase compatible
↓
Firebase real existente
```

El objetivo futuro será:

```text
mantener Firebase actual
reescribir núcleo limpio
conectar progresivamente
sin traer UI vieja
sin romper datos reales
```

---

## 13. Restricciones de seguridad

No pegar en el informe:

- claves privadas;
- tokens;
- URLs sensibles completas si contienen secretos;
- credenciales;
- contenido completo de `google-services.json`;
- service account keys;
- datos personales reales de clientes.

Si se detectan:

```text
marcar “existe configuración sensible” sin exponerla.
```

---

## 14. Comandos sugeridos para auditoría

Codex puede usar comandos de lectura, por ejemplo:

```bash
find .
grep -R "Firebase\|Firestore\|collection\|orders\|trackingNumber\|functions\|callable\|onCall\|onRequest" -n .
grep -R "order\|pedido\|driver\|store\|customer\|phone\|status" -n .
```

Pero debe evitar comandos que modifiquen archivos o datos.

No ejecutar deploy.

No ejecutar scripts que escriban en Firebase.

---

## 15. Criterio de éxito

Esta fase está completa cuando existe un informe claro que permita decidir:

```text
qué núcleo nuevo hay que escribir
qué contrato Firebase debe respetar
qué datos no se pueden romper
qué partes viejas no se deben traer
qué orden de integración conviene seguir
```

Sin ese informe, no se debe empezar a conectar Firebase real con la app pública nueva.
