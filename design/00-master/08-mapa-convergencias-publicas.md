# Mapa de convergencias públicas — Pédilo

## 1. Propósito

Este documento define cómo deben converger las distintas entradas del usuario público hacia pocos puntos comunes de navegación.

La idea central es evitar que cada botón, buscador o acceso cree un flujo completo distinto.

En Pédilo, muchas entradas pueden existir, pero deben alinearse a puntos comunes para reducir duplicación, ambigüedad y riesgo de loops.

---

## 2. Principio central

```text
Las entradas no son flujos completos.
Las entradas conducen a puntos de convergencia.
Desde cada punto de convergencia empieza el flujo común correspondiente.
```

Esto significa que:

- Home puede tener varias entradas;
- Tienda puede tener varias entradas;
- Convenciones puede tener entradas propias;
- Botón + tiene caminos propios;
- Local tiene su flujo propio;

pero no todos deben crear universos separados.

---

## 3. Convergencia a resultados / locales relacionados

### Entradas posibles

```text
Home → buscador
Tienda → buscador
Home → acceso rápido
Home → ofertas
Home → nuevos locales
Tienda → subcategoría
```

### Punto común

```text
Resultados / locales relacionados
```

### Comportamiento esperado

Las entradas llevan a una pantalla o experiencia de resultados donde el usuario ve locales relacionados con su búsqueda, categoría, oferta o acceso.

### Corte

La pantalla de resultados no debe avanzar automáticamente.

No debe:

- abrir Local automáticamente;
- abrir producto;
- mostrar carrito;
- generar pedido;
- mostrar ticket;
- conectar Firebase;
- usar backend real.

### Regla

```text
La entrada define el contexto.
El resultado muestra locales relacionados.
El usuario decide si entra o no a un local.
```

---

## 4. Convergencia a Local público

### Entradas posibles

```text
Resultados desde Home
Resultados desde Tienda
Ofertas
Nuevos locales
Acceso rápido
Subcategoría de Tienda
```

### Punto común

```text
Local público
```

### Flujo común desde Local

```text
Local
→ Producto
→ Carrito del local
→ Datos
→ Confirmación
→ Ticket / seguimiento común
```

### Regla crítica

El carrito pertenece a un solo local.

Si el usuario intenta salir del local con carrito activo:

```text
mostrar advertencia
confirmar salida → vaciar carrito
cancelar → conservar carrito y contexto
```

### Prohibido

- carrito multi-local;
- salida silenciosa con carrito activo;
- mezclar pedido de local con Comprar del botón +;
- mezclar pedido de local con Retiro / Envío.

---

## 5. Convergencia a Confirmación

### Entradas posibles

```text
Pedido desde local
Botón + → Comprar
Botón + → Retiro / Envío
```

### Punto común

```text
Confirmación
```

### Regla importante

Las entradas pueden llegar a una pantalla de confirmación, pero no se mezclan antes.

```text
Comprar no es Retiro / Envío.
Retiro / Envío no es carrito de productos.
Pedido desde local no es compra directa del botón +.
```

### Comportamiento esperado

Cada origen mantiene su naturaleza:

- Comprar conserva lista de productos cargados por el usuario;
- Retiro / Envío conserva datos operativos de retiro/envío;
- Pedido desde local conserva carrito de un solo local.

Luego todos pueden converger en:

```text
Confirmación → Ticket / seguimiento común
```

---

## 6. Convergencia a Seguimiento público

### Entradas posibles

```text
Tienda → seguimiento
Convenciones → seguimiento
Ticket → ver seguimiento
```

### Punto común

```text
Seguimiento público común
```

### Regla central

No debe existir un tracking distinto por cada entrada.

Tienda, Convenciones y Ticket deben converger a la misma lógica visual/pública de seguimiento.

### Convenciones

Convenciones no tiene tracking propio.

Convenciones solo aporta:

```text
pantalla de carga del número de pedido
```

Después converge al seguimiento público común.

### Pedido activo

Si el pedido está activo, puede mostrar:

- estado humano;
- tiempo estimado;
- dirección activa si corresponde;
- reportar problema;
- cancelar pedido si corresponde al estado.

### Pedido entregado / archivado

Si el pedido está entregado o archivado, no debe mostrar:

- productos viejos;
- dirección vieja;
- historial viejo;
- datos personales viejos;
- información interna.

Debe mostrar:

- agradecimiento;
- opción de seguir pidiendo.

---

## 7. Convergencia de buscadores

### Entradas

```text
Home → buscador
Tienda → buscador
```

### Punto común

```text
Buscador público común
```

### Comportamiento correcto

Al tocar el buscador desde Home o desde Tienda:

```text
abrir buscador vacío/editable
mostrar estado inicial humano
permitir escribir
recién al escribir mostrar resultados relacionados
```

### Prohibido

- abrir automáticamente “Pizzas”;
- hardcodear una búsqueda inicial;
- mostrar resultados antes de que el usuario escriba o elija una sugerencia;
- duplicar lógica innecesaria entre Home y Tienda;
- abrir Local automáticamente.

### Back nativo

Debe volver al origen correcto:

```text
Buscador desde Home → Back nativo → Home
Buscador desde Tienda → Back nativo → Tienda
```

---

## 8. Convergencia de entradas de Home

Home contiene varias entradas visuales:

```text
buscador
accesos rápidos
ofertas
nuevos locales
convenciones
botón +
tienda
```

### Regla

Home no debe resolver todos los flujos dentro de sí mismo.

Home debe derivar a puntos comunes:

```text
Buscador → buscador público común
Acceso rápido → resultados/locales relacionados
Ofertas → resultados/ofertas/locales relacionados
Nuevos locales → locales relacionados/nuevos
Convenciones → Convenciones
+ → elección Comprar / Retiro-Envío
Tienda → Tienda principal
```

---

## 9. Convergencia de Tienda

Tienda contiene varias entradas:

```text
buscador
seguimiento
categorías principales
subcategorías
```

### Regla

Tienda tampoco debe crear flujos completos separados.

Debe derivar a puntos comunes:

```text
Buscador → buscador público común
Seguimiento → seguimiento público común
Subcategoría → locales relacionados
Categoría → subcategorías / locales relacionados según fase
```

---

## 10. Back nativo y convergencias

El Back nativo del celular es el mecanismo principal de volver.

No deben agregarse botones visibles de volver en pantallas públicas, salvo autorización específica.

### Comportamiento esperado

```text
Resultados desde Home → Back nativo → Home
Resultados desde Tienda → Back nativo → Tienda
Tienda subcategoría → Back nativo → Tienda
Tienda buscador → Back nativo → Tienda
Tienda seguimiento → Back nativo → Tienda
Convenciones reclamo → Back nativo → Convenciones
Convenciones información → Back nativo → Convenciones
Convenciones seguimiento/carga → Back nativo → Convenciones
Local producto → Back nativo → Local
Local carrito → Back nativo → Local
Local datos → Back nativo → Carrito
Local confirmación → Back nativo → Datos
Botón + Comprar → Back nativo → elección del +
Botón + Retiro / Envío → Back nativo → elección del +
Home → Back nativo → confirmación de salida
```

---

## 11. Regla de construcción

Antes de construir una pantalla nueva, definir:

```text
1. origen
2. acción del usuario
3. punto de convergencia
4. corte obligatorio
5. qué no debe abrir
6. qué flujo común continúa después
```

Si no se puede responder eso, la fase no está lista para Codex.

---

## 12. Objetivo operativo

Este mapa permite trabajar por bloques seguros:

```text
entradas
→ convergencias
→ flujos comunes
→ confirmación
→ ticket / seguimiento
```

El objetivo es reducir:

- duplicación;
- consumo innecesario de Codex;
- pantallas repetidas;
- flujos inventados;
- loops;
- diferencias entre Home y Tienda;
- mantenimiento futuro.

---

## 13. Regla final

```text
Cada entrada debe saber a qué convergencia lleva.
Cada convergencia debe saber cuál es su flujo común.
Cada flujo común debe saber dónde termina.
```
