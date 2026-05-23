# PLANO MAESTRO DE IMPLEMENTACIÓN — USUARIO PÚBLICO PÉDILO

## 0. Objetivo de este documento

Este documento define cómo debe interpretarse e implementarse el plano visual aprobado de Pédilo.

Su función es evitar que Codex invente, mezcle, copie mal, use imágenes como decoración incorrecta o construya una app diferente al plano.

Este documento debe leerse antes de escribir código.

La app pública debe construirse siguiendo:

1. Las imágenes aprobadas en `design/`.
2. Este documento maestro.
3. Los rootlines definidos para cada flujo.
4. Las reglas de corte de navegación.
5. Los criterios de aceptación visual.

---

# 1. Principio absoluto

Las imágenes aprobadas son el plano visual y estructural de la app pública.

No son inspiración.
No son referencia flexible.
No son moodboard.
No son sugerencia estética.
No son “algo parecido”.

Son contrato visual obligatorio.

Codex debe construir una app real en Kotlin + Jetpack Compose que respete las imágenes en:

- composición
- distribución
- jerarquía visual
- colores
- densidad
- estilo de tarjetas
- estilo de botones
- recorrido de usuario
- puntos de convergencia
- cortes de navegación
- pantallas finales de cada flujo

El resultado no debe ser una app genérica.
El resultado no debe parecer un esqueleto Compose.
El resultado no debe parecer una tabla, formulario básico o maqueta sin diseño.

El resultado debe sentirse como una app pública real de Pédilo.

---

# 2. Principio de interpretación de imágenes

Las imágenes pueden representar dos tipos de cosas:

## 2.1 Pantalla real

Una pantalla real es una pantalla que debe existir en la app como experiencia navegable.

Ejemplos:

- Home
- Tienda
- Local
- Pantalla de Convenciones
- Pantalla de Reclamo
- Pantalla de seguimiento
- Pantalla de compra
- Pantalla de retiro
- Pantalla de confirmación
- Ticket

Cuando una imagen representa una pantalla real, Codex debe construir esa pantalla en Compose.

## 2.2 Lámina de flujo / wireflow

Una lámina de flujo muestra varias pantallas en secuencia para explicar el camino del usuario.

En este caso, Codex NO debe copiar toda la lámina como si fuera una única pantalla de la app.

Debe interpretar:

- qué pantallas reales aparecen dentro de la lámina
- cuál es el orden
- qué acción conecta una pantalla con otra
- dónde empieza el flujo
- dónde termina el flujo
- qué parte es explicación visual y no debe aparecer dentro de la app

## Regla crítica

Las imágenes no deben usarse como un asset gigante para pegar dentro de la app, salvo que se indique expresamente para una pantalla base estática.

Lo correcto es construir pantallas reales en Compose usando las imágenes como plano de composición y flujo.

---

# 3. Estado del proyecto

El trabajo actual está organizado sobre la carpeta `design/`, con referencias visuales limpias, documentos maestros, specs por pantalla y flujos separados.

No se debe restaurar UI vieja.
No se debe recrear arquitectura vieja.
No se debe traer roles anteriores.
No se debe reconstruir Admin, Store ni Driver en esta etapa.

En esta etapa se trabaja solamente:

**USUARIO PÚBLICO PÉDILO**

El núcleo/core real, backend, Firebase, WhatsApp, métricas reales, pagos reales, roles internos y tracking persistente quedan para una etapa posterior.

Estado técnico seguro registrado:

```text
d452d79 Recover clean architecture and remove mockup-runtime UI
a719d1e Build real public Home in Compose
```

Tienda principal fue construida por Codex, pero antes de avanzar debe revisarse visualmente y decidir si se acepta, ajusta o rechaza.

---

# 4. Alcance de esta etapa

Esta etapa construye una app pública visual y navegable, con datos locales/mock.

Incluye:

- Splash / inicio
- Home
- Buscador de intención desde Home
- Acceso rápido
- Ofertas
- Nuevos / locales destacados si corresponde al Home
- Local
- Pedido desde local
- Convenciones
- Reclamos
- Seguimiento
- Información del día
- Botón +
- Comprar
- Retiro / Envío
- Confirmación
- Ticket / seguimiento visual
- Tienda
- Buscador de Tienda
- Seguimiento desde Tienda
- Subcategorías de Tienda hacia locales

No incluye:

- backend real
- Firebase real
- roles internos
- Admin
- Store operativo
- Driver
- pagos reales
- WhatsApp real
- métricas reales
- tracking persistente real
- reglas operativas internas
- dashboards
- paneles de gestión

---

# 5. Tecnología esperada

La app debe construirse en:

- Kotlin
- Jetpack Compose
- Android nativo
- Navigation Compose si es necesario
- datos mock/locales
- recursos locales desde `design/`

La app debe compilar y generar APK debug.

---

# 6. Mapa de carpetas de diseño

Las referencias visuales y documentos de control están organizados así:

```text
design/
  00-master/
  01-screens/
  02-flows/
  public-user-approved-mockups/
  Splash/
  README.md
  PLANO_MAESTRO_IMPLEMENTACION_PUBLICA_PEDILO.md
```

## 6.1 Documentos maestros

```text
design/00-master/
  00-estado-actual.md
  01-reglas-globales.md
  02-mapa-de-fases.md
  03-criterios-validacion.md
  04-plantilla-fase-codex.md
  05-revision-visual-pendiente.md
  06-indice-visual-aprobado.md
  07-prompt-retoma-codex.md
```

## 6.2 Specs por pantalla

```text
design/01-screens/
  home/
  tienda-principal/
  tienda-buscador/
  tienda-seguimiento/
  tienda-subcategoria/
  convenciones/
  boton-mas/
  local-publico/
  splash/
```

## 6.3 Flujos separados

```text
design/02-flows/
```

## 6.4 Splash

```text
design/Splash/
  1-splash.png
  logo-instal.png
  splash.png
```

Interpretación:

```text
1-splash.png      → pantalla vertical final del splash / inicio visual
logo-instal.png   → logo/icono instalado o marca puntual
splash.png        → lámina explicativa del flujo visual del splash
```

Reglas:

- no usar versiones viejas del logo;
- no cortar el logo;
- no deformar el logo;
- no usar el splash como screenshot runtime pegado;
- construir splash real en Compose/Android usando la referencia como plano.

## 6.5 Mockups públicos aprobados

Carpetas internas esperadas:

```text
design/public-user-approved-mockups/home/
design/public-user-approved-mockups/tienda/
design/public-user-approved-mockups/convenciones/
design/public-user-approved-mockups/boton_mas/
design/public-user-approved-mockups/local/
```

No usar la carpeta vieja `locla/` como referencia nueva.
Si existiera por historial, debe considerarse legado y no fuente principal.


7. Contrato visual global

La app debe respetar la identidad visual aprobada:

fondo oscuro, no blanco fuerte
naranja Pédilo como acento principal
tarjetas visuales oscuras
botones destacados en naranja
textos claros
jerarquía fuerte
diseño denso pero legible
aspecto de app final, no prototipo
separación clara de secciones
componentes consistentes
bottom bar visual, no tabs genéricos

No usar:

verde/lima como identidad principal
fondo blanco fuerte
Material genérico sin personalización
formularios básicos sin diseño
listas tipo Excel
pantallas vacías sin composición
assets pegados sin lógica
componentes viejos
rutas viejas
roles internos visibles
8. Navegación base pública

La app pública tiene tres destinos principales:

Home / ícono casa  →  botón +  →  Tienda

La barra inferior debe tener solo:

Ícono de Home / casa
Botón central +
Tienda

Reglas:

No debe decir “Casa”.
No debe haber más de tres botones.
No agregar Perfil.
No agregar Cuenta.
No agregar Pedidos.
No agregar Favoritos.
No agregar Carrito fijo.
No agregar accesos de Admin, Store o Driver.
9. Rootlines principales

Un rootline es la línea raíz del usuario: origen, acción, destino y corte.

Cada flujo debe construirse respetando su rootline.

9.1 Home principal
Splash → Home público
9.2 Buscador de intención desde Home
Home → tocar buscador → escribir intención → resultado relacionado → locales relacionados

Este flujo sigue `design/public-user-approved-mockups/home/02-home-buscador.png`.

9.3 Acceso rápido
Home → tocar acceso rápido → locales relacionados

Corte obligatorio: termina en resultados/locales relacionados.
No abrir local si la fase actual no lo autoriza.

9.4 Ofertas
Home → tocar oferta o sección Ofertas → ofertas/locales relacionados

Corte obligatorio: no abrir producto, local, carrito, checkout ni ticket en la pantalla de Ofertas.

9.5 Local
Entrada hacia local → pantalla de local → productos destacados / menú

Local es una pantalla homogénea y única.

9.6 Pedido desde local
Local → producto → carrito/pedido del local → datos → confirmación → ticket/seguimiento común

Regla central: el carrito pertenece a un solo local.

9.7 Convenciones
Home → Convenciones / más → pantalla Convenciones

Desde Convenciones salen tres caminos:

Convenciones → Información del día → leer → volver
Convenciones → Reclamo → completar reclamo → enviar aviso/registro
Convenciones → Seguimiento → ingresar número → seguimiento público común
9.8 Botón +
Home / barra inferior → + → elegir Comprar o Retiro/Envío
9.9 Comprar desde +
+ → Comprar → cargar productos de a uno → origen de compra → observación → datos comunes → confirmar
9.10 Retiro / Envío desde +
+ → Retiro/Envío → datos de retiro → pago/monto si corresponde → descripción → datos comunes → confirmar
9.11 Confirmación común
Comprar o Retiro/Envío → Confirmación → Pedido recibido / Ticket → Seguimiento común

Comprar y Retiro/Envío convergen en confirmación.

9.12 Tienda principal
Home / barra inferior → Tienda
9.13 Tienda buscador
Tienda → buscador principal → locales relacionados

Corte obligatorio: termina en lista de locales relacionados.
No abrir local.

9.14 Tienda seguimiento
Tienda → buscador de seguimiento → punto de convergencia con seguimiento

No redefinir todo el seguimiento si ya está construido.
Solo mostrar la entrada y convergencia desde Tienda.

9.15 Tienda subcategoría
Tienda → tocar subcategoría → locales relacionados

Corte obligatorio: termina en lista de locales relacionados.
No abrir local.
No mostrar productos.
No carrito.
No ticket.

10. Convergencias

Una convergencia es cuando dos caminos distintos llegan al mismo destino funcional.

Codex debe respetar las convergencias sin duplicar flujos innecesariamente.

10.1 Convergencia a locales

Distintos orígenes pueden llegar a locales relacionados:

buscador Home
acceso rápido
ofertas
tienda buscador
tienda subcategoría

Pero no todos siguen después.
Cada flujo tiene su propio corte.

Ejemplo:

Tienda → subcategoría → locales relacionados

se corta ahí.

No debe abrir local solo porque otro flujo sí lo hace.

10.2 Convergencia a seguimiento

El seguimiento puede venir desde:

Convenciones
Tienda
Ticket

Pero el seguimiento público debe mantener la misma lógica:

si el pedido está activo, muestra estado humano
si está entregado/archivado, no muestra datos viejos
10.3 Convergencia a confirmación

Los caminos:

Comprar
Retiro / Envío
Pedido desde local

pueden llegar a confirmación, pero no deben mezclarse antes.

Comprar no es retiro.
Retiro no es carrito.
Pedido desde local no es compra directa.

11. Fichas de implementación por imagen
11.1 Splash

Referencia:

design/Splash/logo-instal.png
design/Splash/splash.png

Construir:

ícono instalado con logo Pédilo
pantalla negra inicial
logo animado
fade del logo
aparición progresiva de palabra “Pedilo”
transición a Home

No construir:

splash genérico
fondo blanco
logo recortado
acceso prematuro al Home

Criterio de aceptación:

La apertura debe sentirse como identidad de marca, no como pantalla de carga básica.

11.2 Home

Referencia:

design/public-user-approved-mockups/home/01-home.png

Construir:

pantalla Home pública
buscador
accesos rápidos
ofertas
nuevos/locales destacados
banner
convenciones
bottom bar

No construir:

botones extra abajo
UI de roles
menú oculto
fondo blanco fuerte

Criterio de aceptación:

Debe verse como la imagen de Home.
Si parece una lista básica, está mal.

11.3 Buscador de intención Home

Referencia:

design/public-user-approved-mockups/home/02-home-buscador.png

Construir:

entrada desde buscador del Home
resultados de intención
recorrido hasta el punto mostrado en la lámina

No construir:

búsqueda genérica sin relación
pantallas extra
carrito
ticket
resultados fuera del flujo

Criterio de aceptación:

Debe mantener el mismo sistema visual de la lámina y cortar donde corresponde.

11.4 Acceso rápido

Referencia:

design/public-user-approved-mockups/home/03-home-acceso-rapido.png

Construir:

entrada desde acceso rápido del Home
recorrido hasta producto según referencia

No construir:

checkout
ticket
seguimiento
flujo distinto al buscador

Criterio de aceptación:

Debe verse como parte del mismo sistema del Home y del buscador.

11.5 Ofertas

Referencia:

design/public-user-approved-mockups/home/04-home-ofertas.png

Construir:

entrada desde oferta del Home
recorrido aprobado hasta producto

No construir:

otra lógica de descuento
backend de promociones
carrito si no está en la referencia

Criterio de aceptación:

Oferta debe actuar como entrada directa al recorrido aprobado.

11.6 Local

Referencia:

design/public-user-approved-mockups/local/05.0-local.png

Construir:

pantalla única de local público
imagen/logo
nombre
categoría
calificación
tiempo estimado
distancia
costo de envío
mínimo de compra
estado abierto/cerrado
categorías internas
productos destacados
promociones o aviso
menú completo si aparece

No construir:

múltiples diseños de local
UI genérica
local operativo interno
panel del negocio

Criterio de aceptación:

Debe ser un local homogéneo, único y alineado a la referencia.

11.7 Pedido desde local

Referencia:

design/public-user-approved-mockups/local/05.1-local-producto.png

Construir:

producto
selección/configuración si corresponde
carrito/pedido del local
datos
confirmación/ticket según referencia

Reglas:

carrito de un solo local
no mezclar locales
si intenta salir con carrito activo, advertencia
si confirma salida, vaciar carrito
si cancela, conservar contexto

No construir:

compra directa del botón +
retiro/envío
multi-local
11.8 Convenciones Reclamo

Referencia:

design/public-user-approved-mockups/convenciones/06.1-convenciones-reclamo.png

Construir:

entrada desde Convenciones
formulario simple de reclamo
confirmación simple de envío

Función:

El reclamo es como libro de quejas.
El cliente deja una notificación de lo ocurrido.

No construir:

soporte operativo completo
chat
resolución automática
panel interno
backend real
11.9 Convenciones Seguimiento

Referencia:

design/public-user-approved-mockups/convenciones/06.2-convenciones-seguimiento.png

Construir:

ingreso de número
consulta visual
estado humano

Reglas:

pedido activo muestra estado
entregado/archivado no muestra datos viejos
mostrar agradecimiento y opción de seguir pidiendo

No mostrar:

productos viejos
dirección vieja
historial viejo
datos internos
11.10 Convenciones Información

Referencia:

design/public-user-approved-mockups/convenciones/06.3-convenciones-informacion.png

Construir:

pantalla de lectura
información del día
volver

No construir:

formulario
pedido
acción operativa

Función:

El usuario entra, lee información pública de Pédilo y vuelve.

11.11 Botón Más / Comprar

Referencia:

design/public-user-approved-mockups/boton_mas/07.1-botonmas-comprar.png

Construir:

decisión inicial desde +
flujo Comprar
carga producto por producto
lista editable
origen de compra
observación
datos comunes

No construir:

campo gigante único
retiro/envío mezclado
pedido antes de confirmar
11.12 Botón Más / Retiro

Referencia:

design/public-user-approved-mockups/boton_mas/07.2-botonmas-retiro-envio.png

Construir:

dirección de retiro
horario opcional
titular / a nombre de quién está
si está pago
monto si no está pago
descripción
datos comunes

No construir:

carrito
productos
compra directa
11.13 Botón Más / Confirmar

Referencia:

design/public-user-approved-mockups/boton_mas/07.3-botonmas-confirmacion.png

Construir:

confirmación común
resumen
pedido recibido
ticket
tracking humano

Reglas:

Comprar y Retiro convergen acá
no crear ticket antes de confirmar
no mostrar datos técnicos
11.14 Tienda principal

Referencia:

design/public-user-approved-mockups/tienda/08.0-tienda.png

Construir:

pantalla Tienda
buscador principal
buscador de seguimiento
categorías principales
subcategorías horizontales
bottom bar

Categorías:

Rápido y al paso
Para comer tranquilo
Algo dulce
Lo que necesito
11.15 Tienda Buscador

Referencia:

design/public-user-approved-mockups/tienda/08.1-tienda-buscador.png

Rootline:

Tienda → buscador → locales relacionados

Corte:

Termina en locales.

No construir:

apertura de local
productos
carrito
ticket
11.16 Tienda Seguimiento

Referencia:

design/public-user-approved-mockups/tienda/08.2-tienda-seguimiento.png

Rootline:

Tienda → buscador de seguimiento → convergencia seguimiento

No reconstruir todo seguimiento si ya existe.
Solo mostrar entrada y convergencia desde Tienda.

11.17 Tienda Subcategoría

Referencia:

design/public-user-approved-mockups/tienda/08.3-tienda-subcategoria.png

Rootline:

Tienda → subcategoría → locales relacionados

Corte:

Termina en locales.

No construir:

apertura local
productos
carrito
ticket
12. Reglas de corte obligatorias

Un error común es que Codex siga más allá del punto definido.

Esto está prohibido.

Cortes fijos
Tienda buscador → locales relacionados

No abrir local.

Tienda subcategoría → locales relacionados

No abrir local.

Información del día → leer → volver

No formulario.

Reclamo → enviar reclamo

No resolución operativa.

Comprar / Retiro → Confirmar

No mezclar flujos previos.

Pedido entregado → agradecimiento

No mostrar datos viejos.

13. Componentes reales a construir

La app debe tener componentes reutilizables, no pantallas improvisadas.

Componentes mínimos:

PublicBottomBar
PublicSearchBar
PublicPrimaryButton
PublicSecondaryButton
PublicCard
PublicBanner
PublicSectionHeader
PublicLocalCard
PublicProductCard
PublicFlowStep
PublicFormField
PublicTicketCard
PublicStatusCard
PublicEmptyState
PublicWarningDialog

Los componentes deben respetar el estilo visual global.

14. Datos mock permitidos

Como no se conecta core real todavía, usar datos locales/mock.

Permitido simular:

locales
productos
categorías
ofertas
nuevos
estados de seguimiento
ticket
reclamo enviado
información del día

No permitido:

escribir backend
crear pedido real
simular WhatsApp real
simular pago real
simular métricas como producción real

No mostrar “Demo ·” como texto visible feo.
Si hay que aclarar muestra, hacerlo de forma humana.

15. Reglas de seguimiento

Estados humanos permitidos:

Pedido recibido
Preparando
En camino
Entregado

Si el pedido está activo:

mostrar estado humano
puede mostrar reportar problema
puede mostrar cancelar si corresponde al mock

Si el pedido está entregado/archivado:

no mostrar productos viejos
no mostrar dirección vieja
no mostrar historial viejo
no mostrar datos personales
mostrar agradecimiento
ofrecer seguir pidiendo
16. Reglas de carrito

El carrito pertenece a un solo local.

No existe carrito multi-local.

Si el usuario está dentro de un local con carrito cargado y quiere salir:

mostrar advertencia
si confirma, vaciar carrito completo
si cancela, conservar carrito y contexto

No mostrar advertencia al moverse dentro del mismo local.

17. Textos prohibidos

Nunca mostrar al usuario público:

nombres de enums
rutas internas
nombres de funciones
variables
logs
stack traces
responsibleRole
responsibleActorId
nextAllowedActions
claim_driver_order
pending_external_store_confirmation
errores crudos

Todo debe traducirse a lenguaje humano.

18. Validación visual

La validación visual debe hacerse contra las imágenes.

Para cada pantalla:

Abrir referencia.
Abrir app real.
Comparar.
Clasificar:
APROBADA
PARCIAL
RECHAZADA

No aprobar si:

parece esqueleto Compose
parece Excel
parece formulario básico
no respeta densidad
no respeta composición
no respeta colores
no respeta corte del flujo
19. Criterios de rechazo automático

Se rechaza si:

no se parece a las referencias
usa diseño genérico
inventa pantallas
cambia flujos
agrega roles
usa blanco fuerte como base
usa verde/lima como identidad
bottom bar tiene más de tres botones
aparece texto “Casa”
Tienda buscador abre local
Tienda subcategoría abre local
seguimiento entregado muestra datos viejos
compra es texto libre gigante
retiro se trata como compra
local permite salida con carrito sin advertencia
aparecen textos técnicos
no respeta los mockups
usa las láminas como imágenes pegadas sin construir UI real, salvo decisión explícita
20. Estrategia correcta de implementación

La forma correcta de construir es:

20.1 Primero sistema visual

Crear:

tema
colores
tipografía
componentes base
bottom bar
cards
botones
inputs
banners
dialogs
20.2 Después pantallas base

Crear:

Splash
Home
Tienda
Local
20.3 Después flujos

Crear:

buscador Home
acceso rápido
ofertas
pedido desde local
convenciones
botón +
tienda buscador
tienda seguimiento
tienda subcategoría
20.4 Después QA

Comparar contra cada imagen.

No integrar core real todavía.

21. Entregable esperado de Codex

Codex debe entregar:

Estructura de archivos creada.
Pantallas implementadas.
Componentes creados.
Flujos navegables.
Datos mock utilizados.
Qué no se implementó por pertenecer al core futuro.
Validaciones ejecutadas.
Comparación contra plano.
Capturas si hay dispositivo/emulador.
Dictamen final.
22. Dictamen esperado

El resultado esperado es:

App pública Pédilo construida desde cero, visualmente fiel al plano aprobado, navegable, con datos mock, sin backend, sin roles internos, sin lógica productiva real y preparada para integrar core en una etapa posterior.

No se acepta menos que el plano.
---

# 23. Índice visual aprobado vigente

Este bloque es la fuente rápida de rutas visuales aprobadas.
Debe coincidir con `design/00-master/06-indice-visual-aprobado.md`.

## 23.1 Splash

```text
design/Splash/
  1-splash.png
  logo-instal.png
  splash.png
```

## 23.2 Home

```text
design/public-user-approved-mockups/home/
  01-home.png
  02-home-buscador.png
  03-home-acceso-rapido.png
  04-home-ofertas.png
  09-home-nuevos-locales.png
```

## 23.3 Tienda

```text
design/public-user-approved-mockups/tienda/
  08.0-tienda.png
  08.1-tienda-buscador.png
  08.2-tienda-seguimiento.png
  08.3-tienda-subcategoria.png
```

## 23.4 Convenciones

```text
design/public-user-approved-mockups/convenciones/
  06.0-convenciones.png
  06.1-convenciones-reclamo.png
  06.2-convenciones-seguimiento.png
  06.3-convenciones-informacion.png
```

Nota: `06.2-convenciones-seguimiento.png` es pantalla de carga del número.
Luego converge al seguimiento público común.

## 23.5 Botón +

```text
design/public-user-approved-mockups/boton_mas/
  07.0-botonmas-eleccion.png
  07.1-botonmas-comprar.png
  07.2-botonmas-retiro-envio.png
  07.3-botonmas-confirmacion.png
  07.4-botonmas-ticket.png
```

## 23.6 Local público

```text
design/public-user-approved-mockups/local/
  05.0-local.png
  05.1-local-producto.png
  05.2-local-carrito.png
  05.3-local-datos.png
  05.4-local-confirmacion.png
  05.5-local-navegacion-interna.png
```

Nota: `05.5-local-navegacion-interna.png` es lámina explicativa, no pantalla final.
Aclara que las pestañas internas solo filtran productos dentro del mismo local.

---

# 24. Correcciones críticas aplicadas a este plano

Este documento corrige las referencias viejas del plano anterior:

- `locla/` fue reemplazado por `local/`.
- Las referencias antiguas de Convenciones dentro de `home/` fueron reemplazadas por `convenciones/`.
- Las referencias antiguas del botón más fueron reemplazadas por la serie nueva `07.0` a `07.4`.
- Splash usa `1-splash.png`, `logo-instal.png` y `splash.png`.
- Home usa la serie limpia `01`, `02`, `03`, `04`, `09`.
- Tienda, Convenciones, Botón + y Local quedan alineados con el índice visual aprobado.
- Las imágenes siguen siendo plano visual, no runtime pegado.
