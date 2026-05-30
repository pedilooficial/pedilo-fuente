# 12 — Lectura funcional del núcleo viejo como referencia — Pédilo!

## 1. Propósito

Este documento explica, en lenguaje funcional, qué hacía el núcleo viejo de Pédilo!, para qué existía cada parte y qué aprendizajes deja como referencia histórica/técnica.

Este documento **no define el núcleo nuevo**.

Este documento **no crea tareas automáticas** para el núcleo nuevo.

Este documento **no convierte problemas del núcleo viejo en problemas del núcleo nuevo**.

El núcleo nuevo todavía no existe. Por lo tanto, no tiene bugs heredados, no tiene decisiones tomadas y no tiene obligaciones derivadas automáticamente del backup anterior.

---

## 2. Regla principal de lectura

```text
El núcleo viejo se documenta como referencia.
No se arrastra como tarea.
No se copia.
No se migra.
No se convierte en backlog automático.
```

Una cosa es documentar:

```text
cómo funcionaba el núcleo viejo
qué función cumplía
qué riesgos tenía
qué decisiones históricas tomó
```

Otra cosa distinta, y no permitida en esta etapa, sería transformar eso en:

```text
trabajo obligatorio del núcleo nuevo
decisiones cerradas del núcleo nuevo
bugs heredados del núcleo nuevo
arquitectura obligatoria del núcleo nuevo
```

La lectura correcta es:

```text
aprender del viejo
no heredar el viejo
```

---

## 3. Estado conceptual

El núcleo viejo fue auditado como fuente histórica/técnica.

El informe `10-informe-auditoria-nucleo-firebase-real.md` documentó que el backup anterior contenía un sistema operativo de pedidos más completo que el proyecto actual recuperado.

Ese sistema anterior incluía:

- creación de pedidos por backend;
- `trackingNumber`;
- reserva de identidad de pedido;
- estados operativos;
- roles;
- locales y productos;
- reglas Firestore;
- Cloud Functions;
- eventos;
- incidentes;
- configuración de pricing;
- comunicación operativa pendiente.

Pero este documento no toma esa estructura como modelo obligatorio. Solo explica qué función cumplía.

---

## 4. Idea funcional del núcleo viejo

El núcleo viejo funcionaba como un **sistema operativo de pedidos**.

No era simplemente una pantalla ni una interfaz.

Su función central era permitir que un pedido real:

```text
naciera
fuera validado
recibiera identidad
se guardara
cambiara de estado
fuera operado por roles
dejara eventos
pudiera ser seguido
pudiera registrar problemas
```

El centro funcional era el pedido.

El pedido viejo no era solo un ticket visual. Era una entidad operativa con estado, responsables, historial y datos necesarios para que distintos roles trabajaran sobre él.

---

## 5. Para qué existía el núcleo viejo

El núcleo viejo existía para que Pédilo! pudiera operar más allá de la UI.

Servía para:

1. Crear pedidos reales.
2. Asignarles un número de seguimiento.
3. Guardarlos en Firebase.
4. Proteger la creación del pedido desde backend.
5. Separar escritura real de visualización.
6. Permitir operación por roles.
7. Registrar eventos.
8. Registrar incidentes.
9. Mantener estados del pedido.
10. Controlar qué acciones podía hacer cada rol.
11. Mantener trazabilidad.
12. Relacionar pedido con local, cliente, repartidor y configuración operativa.

La intención funcional era correcta:

```text
la UI no debía mandar el pedido real;
el núcleo/backend debía controlar el pedido.
```

---

## 6. Por qué estaba armado con Firebase y Functions

El núcleo viejo usaba Firebase y Cloud Functions porque necesitaba que las reglas críticas no dependieran del teléfono del usuario ni del cliente Android.

La función de Cloud Functions era centralizar operaciones sensibles como:

- crear pedido;
- reservar trackingNumber;
- validar datos mínimos;
- cambiar estados;
- asignar repartidor;
- registrar eventos;
- operar roles;
- actualizar configuración;
- crear usuarios operativos.

La idea funcional era:

```text
Android pide.
Backend valida.
Firebase guarda.
Roles operan.
Eventos auditan.
```

Ese principio es una referencia útil.

No significa que el nuevo núcleo deba copiar el mismo código ni la misma forma exacta.

---

## 7. Función de `/orders`

En el núcleo viejo, `/orders` era la colección principal del sistema operativo.

Funcionalmente, servía para representar el pedido real.

Allí convivían datos como:

- identidad del pedido;
- estado;
- tipo de pedido;
- cliente;
- dirección;
- teléfono;
- local;
- repartidor;
- totales;
- pricing;
- eventos derivados;
- acciones posibles;
- flags operativos;
- timestamps.

La función de `/orders` era actuar como fuente operativa principal.

Lectura funcional:

```text
/orders era el lugar donde vivía el pedido operativo.
```

Lectura de aprendizaje:

```text
un pedido real necesita una fuente central y trazable.
```

Límite de esta lectura:

```text
esto no obliga al núcleo nuevo a copiar la estructura exacta de /orders.
```

---

## 8. Función de `trackingNumber`

En el núcleo viejo, `trackingNumber` servía como identidad pública del pedido.

Funcionalmente, resolvía este problema:

```text
el cliente no debe manejar IDs internos;
necesita un número simple para consultar su pedido.
```

La reserva en `/order_tracking` servía para evitar duplicados y vincular número público con pedido interno.

Lectura de aprendizaje:

```text
un pedido público necesita una identidad consultable por el cliente.
```

Límite de esta lectura:

```text
no implica copiar la estructura vieja ni el mecanismo exacto.
solo confirma que la función de identidad pública existía.
```

---

## 9. Función de los estados operativos

El núcleo viejo tenía muchos estados técnicos.

Funcionalmente, esos estados servían para que la operación pudiera saber con precisión qué estaba pasando:

- pedido creado;
- enviado al local;
- aceptado;
- preparando;
- listo;
- disponible para repartidor;
- asignado;
- retirado;
- en camino;
- entregado;
- con problema;
- cancelado;
- resuelto.

La función de esos estados era operativa, no pública.

Lectura de aprendizaje:

```text
el núcleo puede necesitar estados internos finos.
```

Pero para el cliente público, esos estados no deben mostrarse crudos.

Lectura de aprendizaje adicional:

```text
debe existir diferencia entre estado interno y estado humano visible.
```

Límite de esta lectura:

```text
no se define todavía el mapa final de estados del núcleo nuevo.
```

---

## 10. Función de eventos e incidentes

El núcleo viejo registraba eventos e incidentes.

Funcionalmente, eso servía para:

- auditar cambios;
- saber quién hizo qué;
- reconstruir la historia del pedido;
- registrar problemas;
- separar estado actual de historial;
- permitir revisión operativa.

Lectura de aprendizaje:

```text
un sistema real de pedidos necesita trazabilidad.
```

Límite:

```text
no se define todavía cómo será el sistema final de eventos del núcleo nuevo.
```

---

## 11. Función de roles

El núcleo viejo distinguía roles:

- público;
- admin;
- local/store;
- repartidor/driver;
- backend;
- actores internos.

Funcionalmente, los roles existían para separar responsabilidades.

Cada rol necesitaba ver o hacer cosas diferentes.

Ejemplos funcionales:

```text
el público crea o consulta;
el local confirma/prepara;
el repartidor toma o avanza entrega;
el admin resuelve y corrige;
el backend valida y registra.
```

Lectura de aprendizaje:

```text
Pédilo! no debe operar todo desde un solo tipo de usuario.
```

Límite:

```text
no se reconstruyen roles en este documento.
no se define todavía el diseño final de roles nuevos.
```

---

## 12. Función de stores/products

El núcleo viejo tenía locales y productos.

Funcionalmente, esto servía para:

- mostrar locales visibles;
- cargar catálogo;
- permitir productos por local;
- congelar snapshots al crear pedido;
- relacionar pedido con comercio;
- permitir operación del local.

Lectura de aprendizaje:

```text
un pedido de local necesita referencia estable al comercio y a los productos.
```

Límite:

```text
no se decide todavía cómo será la identidad final del local en el núcleo nuevo.
```

---

## 13. Función de pricing/configuración

El núcleo viejo tenía configuración de precios y comunicación.

Funcionalmente, servía para que reglas operativas no estuvieran hardcodeadas en la UI.

Ejemplos:

- tarifa normal;
- tarifa lluvia;
- extras por distancia;
- número operativo de WhatsApp;
- configuración de comunicación.

Lectura de aprendizaje:

```text
algunas reglas del negocio deben vivir en configuración controlada.
```

Límite:

```text
no se define todavía qué configuración real tendrá el núcleo nuevo.
```

---

## 14. Función de comunicaciones

El núcleo viejo generaba comunicaciones pendientes, especialmente relacionadas con WhatsApp manual.

Funcionalmente, servía para dejar registro de que había que comunicar algo al cliente.

Lectura de aprendizaje:

```text
el sistema necesita una capa de comunicación o registro comunicacional.
```

Límite importante:

```text
eso no significa que ya existiera WhatsApp automático real.
no se debe asumir integración automática.
```

---

## 15. Qué estaba bien como concepto

Del núcleo viejo, como referencia conceptual, estaban bien estas ideas:

1. El pedido era central.
2. La UI no escribía pedidos directamente.
3. El backend validaba creación.
4. Existía identidad pública de pedido.
5. Existían estados internos.
6. Existían eventos.
7. Existían roles.
8. Existía catálogo de locales/productos.
9. Existía configuración operativa.
10. Existía separación parcial entre acciones públicas y operativas.
11. Existían reglas Firestore para proteger escrituras.
12. Existía intención de trazabilidad.

Estas ideas pueden servir como aprendizaje.

No son instrucciones de copiar implementación.

---

## 16. Qué estaba mal o era riesgoso como estructura vieja

El núcleo viejo tenía riesgos o puntos débiles históricos:

1. Algunos campos mezclaban significado operativo y comercial.
2. Datos públicos e internos convivían demasiado cerca.
3. El tracking público no estaba completo como contrato seguro.
4. Los pedidos terminales no tenían una proyección pública segura documentada.
5. El teléfono validaba formato, no identidad.
6. La comunicación WhatsApp era pendiente/manual, no integración automática completa.
7. Algunas piezas estaban acopladas a UI vieja.
8. Había riesgo de copiar lógica vieja contaminada.
9. Algunos estados técnicos no eran aptos para mostrarse al cliente.
10. Ciertas reglas dependían de convenciones internas que requerían cuidado.

Lectura correcta:

```text
estos fueron riesgos del núcleo viejo.
no son bugs del núcleo nuevo.
```

El núcleo nuevo todavía no existe.

---

## 17. Qué aprendizaje deja para el núcleo nuevo

El aprendizaje útil es funcional:

```text
Pédilo! necesita un núcleo que controle pedidos reales.
Pédilo! necesita separar UI de reglas.
Pédilo! necesita identidad pública de pedido.
Pédilo! necesita estados internos y estados públicos separados.
Pédilo! necesita privacidad pública.
Pédilo! necesita trazabilidad.
Pédilo! necesita roles.
Pédilo! necesita catálogo real.
Pédilo! necesita validación de cliente.
```

Esto no define todavía cómo se implementará.

Solo deja claro qué funciones existen en el tipo de sistema que se está construyendo.

---

## 18. Qué no debe heredarse

No debe heredarse:

- estructura exacta vieja;
- nombres ambiguos;
- UI vieja;
- navegación vieja;
- ViewModels viejos;
- código acoplado a pantallas legacy;
- scripts antiguos;
- mocks viejos;
- pantallas operativas viejas;
- exposición de campos internos;
- tracking incompleto;
- WhatsApp asumido como real si no lo es;
- roles mezclados en UI pública.

---

## 19. Diferencia entre referencia y obligación

Este documento no dice:

```text
el núcleo nuevo debe arreglar lo viejo
```

Dice:

```text
el núcleo viejo muestra funciones que un sistema real de Pédilo! necesitaba resolver.
```

La diferencia es importante.

El núcleo nuevo se diseñará desde cero, bajo decisión del usuario.

El backup solo ayuda a entender:

- qué existía;
- por qué existía;
- qué función cumplía;
- qué cosas conviene no repetir.

---

## 20. Relación con el documento 11

El documento `11-contrato-nuevo-nucleo-real.md` define un marco rector inicial para el núcleo nuevo.

Este documento 12 no reemplaza al 11.

Este documento 12 agrega una lectura funcional del viejo para no mirar el backup como código, sino como sistema histórico.

Relación:

```text
10 = auditoría técnica del viejo
11 = contrato rector inicial del nuevo
12 = lectura funcional del viejo como referencia
```

Ninguno de estos documentos autoriza implementación automática.

---

## 21. Conclusión

El núcleo viejo cumplía la función de sistema operativo de pedidos reales.

Su valor principal no está en copiar su código, sino en entender:

```text
qué problema intentaba resolver
qué funciones eran necesarias
qué dependencias existían
qué riesgos aparecieron
qué debe evitarse al construir de nuevo
```

El núcleo nuevo debe nacer limpio.

El núcleo viejo queda como referencia histórica/técnica.

El núcleo nuevo no hereda sus problemas.

---

## 22. Dictamen

```text
El núcleo viejo queda documentado como referencia funcional.
No queda convertido en backlog.
No define tareas obligatorias del núcleo nuevo.
No autoriza implementación.
No autoriza migración.
No autoriza copia de código.
```

Siguiente paso, cuando el usuario lo decida:

```text
definir el primer bloque real de construcción del núcleo nuevo,
con prompt específico, alcance acotado y sin arrastrar arquitectura vieja.
```
