# Cierre maestro técnico-funcional — Admin → Operación

## 1. Naturaleza del documento

Este documento no define identidad visual, estilo gráfico, colores, tipografía, densidad visual ni diseño final de interfaz.

La identidad visual de Pédilo ya existe y no se redefine acá.

Este cierre maestro define el comportamiento técnico-funcional del universo:

**Admin → Operación**

Su objetivo es dejar claro para la construcción posterior del rol Admin:

- qué ve Admin en Operación;
- qué significa cada raíz operativa;
- cómo se navega;
- hasta dónde llega cada camino;
- dónde corta cada raíz;
- dónde empieza una convergencia;
- qué puede aparecer en cada nivel;
- qué no debe aparecer antes de tiempo;
- qué pantallas son de clasificación;
- qué pantallas son de entidad concreta;
- cómo se relaciona todo con el Pedido #____;
- cómo debe comportarse la UI real cuando Codex construya el rol Admin.

Las imágenes y ZIP generados son referencias de navegación, jerarquía, flujo y comportamiento. No deben ser copiadas literalmente como UI final ni arrastrar defectos visuales, textos cortados, números ficticios o detalles propios del mockup.

---

## 2. Principio operativo central

Admin → Operación es el universo vivo donde Admin observa, entiende y actúa sobre la operación real cuando corresponde.

Operación no es Configuración.
Operación no es Alta de roles.
Operación no es edición de marketplace.
Operación no es carga de productos.
Operación no es gestión de permisos.

Operación responde:

**Qué está pasando ahora y dónde debe entrar Admin para entender o resolver algo.**

La navegación se construye por capas:

**raíz operativa → submundo → listado/caso → convergencia → entidad concreta → necesidad → acción guiada**

No se debe resolver desde una raíz.
No se deben mostrar acciones finales antes de llegar a una entidad concreta.
No se deben mezclar universos.

---

## 3. Universos principales de Admin

Admin se organiza en tres universos principales:

1. **Operación**
2. **Configuración**
3. **Alta de roles**

Este documento cubre solamente:

**Admin → Operación**

Configuración y Alta de roles se trabajan como universos separados.

---

## 4. Home Admin como entrada operativa

El Home Admin funciona como entrada operativa rápida.

Desde ahí se accede a cinco raíces principales de Operación:

1. **Pedidos del día**
2. **Pedidos activos**
3. **Pedidos con problemas**
4. **Repartidores activos**
5. **Locales activos**

El Home Admin permite elegir qué universo operativo abrir.

Una vez que Admin entra en una raíz, la pantalla interna no debe repetir elementos propios del Home Admin.

Las pantallas internas no arrastran:

- botón Cerrar sesión;
- identidad grande de Pédilo Admin;
- las cinco pantallitas del Home;
- contenido de otras raíces;
- Configuración dentro del contenido;
- Alta de roles dentro del contenido.

La navegación previa ya marca intención. La pantalla siguiente debe profundizar esa intención.

---

## 5. Regla general de raíces

Cada raíz operativa clasifica y orienta.

Una raíz puede mostrar referencias propias de su universo, pero no debe absorber otro universo ni ejecutar acciones finales.

Cada raíz debe definir:

- qué significa;
- qué puede mostrar;
- qué no puede mostrar;
- qué submundos contiene;
- hasta dónde llega;
- dónde corta;
- dónde converge;
- qué queda fuera.

Regla clave:

**Cada raíz clasifica hasta el borde; la pantalla común resuelve según la entidad concreta.**

---

## 6. Regla general de convergencias

Una convergencia empieza cuando distintos caminos llegan a una misma entidad real.

Ejemplos de entidades concretas:

- **Pedido #____**
- local operativo concreto;
- repartidor operativo concreto.

La convergencia no pertenece a una sola raíz.

Puede llegar desde distintos caminos, pero debe mostrar la entidad de forma clara y humana, sin breadcrumb largo ni repetición de todo el camino anterior.

---

## 7. Raíz: Pedidos del día

### 7.1 Qué es

Pedidos del día muestra el movimiento completo de pedidos del día.

Responde:

**Qué pasó hoy con los pedidos y qué parte del día debe revisar Admin.**

No es Pedidos activos global.
No es Pedidos con problemas global.
No es resolución de problemas.
No es detalle de pedido.

### 7.2 Submundos

Pedidos del día contiene:

- Activos;
- Finalizados;
- Cancelados;
- Demorados;
- Con problemas.

### 7.3 Ramas internas

#### Activos

Muestra pedidos del día que siguen en curso.

Submundos:

- Esperando local;
- Preparando;
- Esperando repartidor;
- En entrega.

#### Finalizados

Muestra pedidos del día cerrados correctamente.

Submundos:

- Entregados;
- Retirados;
- Enviados.

#### Cancelados

Muestra pedidos del día cerrados sin completar.

Submundos:

- Cancelados por cliente;
- Cancelados por local;
- Cancelados por operación.

#### Demorados

Muestra pedidos del día con tiempo excedido.

Submundos:

- Esperando local;
- Preparando;
- En entrega.

#### Con problemas

Muestra pedidos del día marcados con incidencia.

Submundos:

- Local no responde;
- Reclamo del cliente.

### 7.4 Punto de corte

Pedidos del día llega hasta la lista de casos/pedidos de cada submundo.

El siguiente toque natural sobre un pedido concreto ya pertenece a la convergencia:

**Pedido #____**

### 7.5 Qué no puede hacer

Pedidos del día no puede:

- resolver pedidos;
- contactar;
- cancelar;
- reasignar;
- editar pedido;
- cerrar incidencia;
- abrir Solucionar;
- abrir Pedido #____ dentro de la raíz;
- mezclar Pedidos activos global;
- mezclar Pedidos con problemas global.

### 7.6 Estado

Bloque aprobado como referencia técnico-funcional completa hasta punto de convergencia.

---

## 8. Raíz: Pedidos activos

### 8.1 Qué es

Pedidos activos muestra los pedidos vivos ahora en la operación actual.

No es lo mismo que:

**Pedidos del día → Activos**

Pedidos del día → Activos significa pedidos activos dentro del movimiento del día.

Pedidos activos significa pedidos actualmente vivos en la operación.

### 8.2 Qué responde

**Qué pedidos están vivos ahora y en qué estado operativo están.**

### 8.3 Submundos

- Esperando local;
- Preparando;
- Esperando repartidor;
- En entrega.

### 8.4 Punto de corte

Llega hasta listados de pedidos por estado activo.

Al tocar un pedido concreto, converge a:

**Pedido #____**

### 8.5 Qué no puede hacer

Pedidos activos no puede:

- resolver;
- contactar;
- cancelar;
- reasignar;
- editar pedido;
- abrir Solucionar;
- mezclar Repartidores activos como raíz;
- mezclar Locales activos como raíz;
- abrir Pedido #____ dentro del bloque.

### 8.6 Estado

Bloque aprobado como referencia técnico-funcional hasta punto de convergencia.

---

## 9. Raíz: Pedidos con problemas

### 9.1 Qué es

Pedidos con problemas es la raíz operativa especializada para revisar pedidos que tienen problemas o requieren atención.

No es lo mismo que:

**Pedidos del día → Con problemas**

Pedidos del día → Con problemas significa pedidos del día que fueron marcados con incidencia.

Pedidos con problemas significa el universo operativo especializado de problemas.

### 9.2 Qué responde

**Qué pedidos tienen problemas ahora y qué tipo de problema presentan.**

### 9.3 Submundos firmes

- Local no responde;
- Reclamo del cliente.

### 9.4 Punto de corte

Llega hasta listados de casos/pedidos por tipo de problema.

Al tocar un caso concreto, converge a:

**Pedido #____**

o, según el estado, a:

**Pedido #____ con problema**

### 9.5 Qué no puede hacer

Pedidos con problemas no puede desde la raíz:

- resolver;
- contactar;
- cancelar;
- cerrar incidencia;
- abrir Solucionar directamente;
- mostrar todas las acciones;
- mezclar Pedidos del día;
- mezclar Configuración.

### 9.6 Estado

Bloque aprobado como referencia técnico-funcional hasta punto de convergencia.

---

## 10. Raíz: Repartidores activos

### 10.1 Qué es

Repartidores activos muestra el estado operativo de repartidores dentro de Operación.

No es Alta de roles.
No es edición de perfil.
No es gestión de permisos.

### 10.2 Qué responde

**Qué repartidores están operativos ahora y en qué situación están.**

### 10.3 Submundos

- Libres;
- Ocupados;
- Pendientes de respuesta;
- Con incidencia.

### 10.4 Punto de corte

Llega hasta listados de repartidores/casos.

Al tocar un repartidor o caso concreto, puede converger a:

- Repartidor operativo concreto;
- Pedido #____ relacionado;
- incidencia contextual del pedido, si corresponde.

### 10.5 Qué no puede hacer

Repartidores activos no puede:

- editar perfil;
- cambiar permisos;
- dar de alta repartidor;
- desactivar usuario;
- resolver pedido;
- reasignar desde raíz;
- abrir Alta de roles;
- abrir Pedido #____ dentro del bloque.

### 10.6 Estado

Bloque aprobado como referencia técnico-funcional hasta punto de convergencia.

---

## 11. Raíz: Locales activos

### 11.1 Qué es

Locales activos muestra el estado operativo de locales dentro de Operación.

No es Configuración.
No es edición de local.
No es carga de productos.
No es marketplace.

### 11.2 Qué responde

**Qué locales están operando ahora y en qué situación están.**

### 11.3 Submundos

- Vendiendo ahora;
- Sin respuesta;
- Pausados;
- Con configuración pendiente;
- Sin productos vendibles.

### 11.4 Punto de corte

Llega hasta listados de locales/casos.

Al tocar un local o caso concreto, puede converger a:

- Local operativo concreto;
- Pedido #____ relacionado;
- incidencia contextual del pedido, si corresponde;
- Configuración de local solo si más adelante se define una derivación explícita.

### 11.5 Qué no puede hacer

Locales activos no puede:

- editar local;
- editar productos;
- cargar catálogo;
- cambiar visibilidad;
- configurar marketplace;
- pausar/activar desde raíz;
- resolver pedidos;
- abrir Configuración dentro del bloque;
- abrir Pedido #____ dentro del bloque.

### 11.6 Estado

Bloque aprobado como referencia técnico-funcional hasta punto de convergencia.

---

## 12. Convergencia: Pedido #____

### 12.1 Qué es

Pedido #____ es la pantalla común de pedido concreto.

Puede recibir entrada desde distintas raíces:

- Pedidos del día;
- Pedidos activos;
- Pedidos con problemas;
- Repartidores activos;
- Locales activos;
- Local operativo concreto;
- Repartidor operativo concreto.

La pantalla se identifica como:

**Pedido #____**

No usar como título principal:

- Pedido vivo;
- Detalle del pedido;
- Resolución del pedido.

### 12.2 Qué muestra primero

Al entrar, lo primero que se ve es el estado general del pedido.

La pantalla responde:

**Qué está pasando con este pedido ahora.**

### 12.3 Comportamientos posibles

#### Estado normal

Si el pedido avanza correctamente, muestra estado, contexto y datos necesarios.

No muestra acciones ni ruido.

#### Necesita atención

Si el pedido necesita algo, muestra:

- qué necesita;
- quién debe actuar;
- desde cuándo;
- impacto;
- entrada a revisión si corresponde.

#### Con problema

Si el pedido tiene problema, muestra:

- estado actual;
- problema;
- qué se esperaba;
- qué no ocurrió;
- responsable actual;
- tiempo detenido;
- impacto;
- entrada a Solucionar si corresponde.

#### Acción no disponible

Si se intenta una operación no disponible, informa de forma humana:

- estado actual;
- motivo;
- qué se puede hacer ahora;
- volver al pedido.

### 12.4 Qué no puede hacer

Pedido #____ no debe mostrar todo de golpe.
No debe mostrar acciones si no hay necesidad.
No debe inventar acciones.
No debe mostrar acciones sensibles sin contexto.

### 12.5 Estado

Bloque aprobado como referencia técnico-funcional de convergencia.

---

## 13. Convergencia: Pedido #____ → Solucionar

### 13.1 Qué es

Solucionar es el flujo de resolución guiada dentro de Pedido #____.

Aparece solo cuando el pedido lo necesita.

No aparece desde raíces.
No aparece sin contexto.
No es una lista libre de botones.

### 13.2 Flujo

El bloque se organiza en:

1. Inicio de solución;
2. Opciones de resolución;
3. Acción sensible;
4. Resultado.

### 13.3 Comportamiento

Primero muestra el problema puntual.
Después muestra opciones precisas.
Separa:

- acción recomendada;
- acciones secundarias;
- excepción Admin;
- acción sensible con confirmación;
- resultado visual.

### 13.4 Regla de Admin

Admin puede operar bajo malas condiciones o cuando el flujo normal falla.

Pero esas excepciones deben estar:

- justificadas;
- separadas visualmente;
- con impacto;
- con confirmación;
- con resultado.

### 13.5 Estado

Bloque aprobado como referencia técnico-funcional de resolución guiada.

---

## 14. Convergencia: Local operativo concreto

### 14.1 Qué es

Local operativo concreto muestra el estado operativo de un local específico dentro de Operación.

No es Configuración.
No edita local.
No edita productos.
No carga catálogo.

### 14.2 Desde dónde puede venir

Puede venir desde:

- Locales activos → Vendiendo ahora;
- Locales activos → Sin respuesta;
- Locales activos → Pausados;
- Locales activos → Con configuración pendiente;
- Locales activos → Sin productos vendibles;
- Pedidos con problemas → Local no responde;
- Pedido #____ → local relacionado.

### 14.3 Estados representados

- Local en estado normal;
- Local necesita atención;
- Local sin respuesta;
- acción no disponible desde Operación.

### 14.4 Qué no puede hacer

No puede:

- editar local;
- editar productos;
- modificar visibilidad;
- configurar marketplace;
- abrir configuración;
- resolver pedidos directamente;
- mostrar acciones finales;
- abrir Pedido #____ dentro del bloque.

Si algo pertenece a pedido, debe ir a Pedido #____.
Si algo pertenece a estructura, debe ir a Configuración en un universo separado.

### 14.5 Estado

Bloque aprobado como referencia técnico-funcional, con posibles ajustes de copy/espaciado en etapa final.

---

## 15. Convergencia: Repartidor operativo concreto

### 15.1 Qué es

Repartidor operativo concreto muestra el estado operativo de un repartidor específico dentro de Operación.

No es Alta de roles.
No edita perfil.
No modifica permisos.

### 15.2 Desde dónde puede venir

Puede venir desde:

- Repartidores activos → Libres;
- Repartidores activos → Ocupados;
- Repartidores activos → Pendientes de respuesta;
- Repartidores activos → Con incidencia;
- Pedidos activos → Esperando repartidor;
- Pedidos activos → En entrega;
- Pedido #____ → repartidor relacionado.

### 15.3 Estados representados

- Disponible;
- Ocupado;
- necesita atención;
- acción no disponible desde Operación.

### 15.4 Qué no puede hacer

No puede:

- editar perfil;
- modificar permisos;
- dar de alta;
- desactivar usuario;
- abrir Alta de roles;
- resolver pedido directamente;
- abrir Pedido #____ dentro del bloque.

Si algo pertenece a pedido, debe ir a Pedido #____.
Si algo pertenece a usuario/acceso/rol, debe ir a Alta de roles.

### 15.5 Estado

Bloque aprobado como referencia técnico-funcional, con posibles ajustes de copy/espaciado en etapa final.

---

## 16. Incidencia

Por ahora, incidencia no se trabaja como bloque propio.

La opción segura definida es:

**Incidencia vive como contexto dentro de Pedido #____**, salvo que más adelante se decida convertirla en entidad propia.

Esto evita crear un universo paralelo prematuro.

---

## 17. Ley operativa de acciones

Que un pedido necesite una acción no significa que cualquier usuario pueda ejecutarla.

Para roles no-admin, la acción solo corresponde al rol responsable, en el contexto y momento correcto.

En Admin puede haber excepciones operativas, porque Admin puede trabajar bajo malas condiciones o cuando el flujo normal falla.

Pero esas excepciones no son botones sueltos.

Deben aparecer dentro de resolución guiada, con:

- contexto;
- explicación;
- impacto;
- confirmación;
- resultado;
- auditoría futura si corresponde.

---

## 18. Relación con el núcleo

El núcleo no se trabaja en esta etapa.

La UI no define backend, Firestore, Cloud Functions, transiciones, reglas ni auditoría técnica.

Pero la UI debe estar preparada para representar lo que el núcleo informe:

- estado del pedido;
- necesidad actual;
- responsable;
- acción posible;
- acción no disponible;
- resultado de acción.

La UI representa.
El núcleo decide.

---

## 19. Qué debe respetar Codex después

Cuando se construya el rol Admin real, Codex debe tomar estos archivos como plano técnico-funcional.

Debe respetar:

- separación de universos;
- raíces operativas;
- puntos de corte;
- convergencias;
- identidad de Pedido #____;
- Solucionar dentro del contexto correcto;
- Local operativo sin convertirlo en Configuración;
- Repartidor operativo sin convertirlo en Alta de roles;
- ausencia de acciones finales en raíces;
- lectura de estado antes de acción;
- acciones guiadas y no sueltas.

Codex no debe copiar literalmente:

- defectos visuales de mockups;
- textos cortados;
- números ficticios;
- copy de maqueta;
- espaciados imperfectos;
- íconos inconsistentes.

---

## 20. Estado final de Admin → Operación

Admin → Operación queda cerrado como plano técnico-funcional de referencia.

Incluye:

- cinco raíces operativas completas;
- pantallas hoja hasta punto de convergencia;
- convergencia Pedido #____;
- resolución guiada Solucionar;
- convergencia Local operativo concreto;
- convergencia Repartidor operativo concreto.

Este cierre permite pasar al siguiente universo:

**Admin → Configuración**

sin volver a mezclarlo con Operación.
