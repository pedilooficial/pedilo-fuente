# Cierre maestro técnico-funcional — Admin → Configuración

## 1. Propósito del documento

Este documento cierra el universo **Admin → Configuración** como plano técnico-funcional de referencia.

Su objetivo es ordenar qué representa Configuración dentro de Pédilo Admin, qué bloques la componen, qué puede editar cada bloque, qué no puede tocar, dónde corta cada flujo, qué convergencias comparte y qué reglas deben respetarse para no romper lo ya existente.

Este archivo no define UI final, no define estilo visual, no implementa código y no prepara todavía una etapa de Codex. Su función es dejar auditado el comportamiento funcional esperado del universo Configuración.

## 2. Qué es Configuración

**Configuración** es el universo editable estructural de Pédilo Admin.

Configuración responde:

> “¿Cómo queda preparada la app para funcionar correctamente hacia adelante?”

Trabaja sobre estructura, reglas, parámetros, mensajes, datos configurables, entidades administrables y criterios que afectan el comportamiento futuro del sistema.

Configuración puede preparar, revisar, editar, guardar borradores, previsualizar, medir impacto, confirmar cambios y registrar resultados.

Configuración no trabaja sobre la urgencia viva del momento. No resuelve pedidos actuales ni reemplaza los flujos operativos.

## 3. Qué no es Configuración

Configuración no es:

- Operación viva.
- Alta de roles.
- Pedido #____.
- Solucionar.
- Resolución de pedidos.
- Un panel libre para editar todo.
- Backend.
- Implementación.
- Base de datos.
- Código.
- Reglas técnicas internas.
- Pantalla para ejecutar acciones sin revisión.

Configuración no debe convertirse en un lugar donde se mezclan pedidos vivos, productos, locales, roles, usuarios, mensajes, reglas y emergencias sin dueño claro.

## 4. Estado actual que debe respetar

Configuración debe respetar el estado actual de Pédilo:

- Usuario público real ya cerrado.
- Catálogo real desde Firebase.
- Pedidos reales desde Local.
- Pedidos reales desde Botón + Comprar.
- Pedidos reales desde Botón + Retiro / Envío.
- Ticket real con número real.
- Seguimiento público real por número.
- Login Equipo funcionando.
- Admin aún proyectándose por bloques.

Configuración no puede romper:

- Usuario público.
- Pedidos reales.
- Catálogo real.
- Tickets emitidos.
- TrackingNumber.
- Seguimiento público.
- Login Equipo.
- Integridad del pedido.

Todo cambio debe entenderse como controlado, explícito y ubicado dentro de su bloque dueño.

## 5. Regla de dueño único de edición

Regla madre:

> Una entidad puede verse como referencia en varios lugares, pero solo puede editarse desde un único bloque dueño.

Esto evita doble edición, contradicciones silenciosas y pantallas que parecen poder cambiar cosas que no les pertenecen.

### Matriz de dueño

| Área / entidad | Bloque dueño | Alcance |
|---|---|---|
| Experiencia pública configurable | Usuario público | Secciones visibles, textos públicos, banners internos, seguimiento visible. |
| Datos estructurales del local | Locales | Datos mínimos, información pública estructural, estado de configuración del comercio. |
| Productos, categorías, precios, imágenes y disponibilidad | Catálogo y productos | Entidades vendibles y organización del catálogo. |
| Comportamiento futuro del pedido | Pedidos | Reglas estructurales, estados visibles, seguimiento futuro, demoras y cancelaciones futuras. |
| Mensajes enviados, avisos y plantillas | Comunicación | Plantillas y comunicaciones futuras, sin enviar mensajes reales. |
| Criterios de clasificación operativa | Operación | Criterios para demora, problema, atención, revisión y lectura operativa futura. |
| Condiciones generales de integridad | Reglas y validaciones | Datos mínimos, bloqueos, reglas de publicación y validaciones generales. |
| Registros administrativos | Auditoría | Consulta y trazabilidad; no edita ni revierte. |
| Estados excepcionales controlados | Emergencias | Modo seguro, restricciones, avisos globales excepcionales, confirmación y auditoría. |
| Parámetros sin dueño específico | General | Solo ajustes globales que no pertenecen a ningún otro bloque. |
| Usuarios, roles y accesos | Alta de roles | Fuera de Configuración. |

## 6. Mapa general de Configuración

```text
Admin → Configuración
├── Usuario público
├── Locales
├── Catálogo y productos
├── Pedidos
├── Comunicación
├── Operación
├── Reglas y validaciones
├── Auditoría
├── Emergencias
└── General
```

## 7. Descripción por bloque

### Usuario público

**Qué representa**

Usuario público representa la configuración controlada de partes visibles o revisables de la experiencia pública ya existente.

No rediseña el usuario público. No reabre el flujo público cerrado. No rompe Botón +, ticket ni seguimiento.

**Qué puede editar**

- Presentación pública configurable.
- Secciones visibles.
- Banners o avisos visibles dentro de pantalla.
- Textos fijos de pantalla pública.
- Presentación del seguimiento público, sin tocar la lógica real.
- Orden o visibilidad de secciones públicas, si no cambia entidades vendibles.

**Qué no puede editar**

- Productos como entidad vendible.
- Precios.
- Imágenes de productos.
- Categorías como entidad vendible.
- Pedidos.
- Ticket real.
- TrackingNumber real.
- Roles.
- Usuarios.
- Mensajes enviados como comunicación.

**Dónde corta**

Corta antes de modificar entidades que pertenecen a Catálogo, Pedidos, Comunicación o Alta de roles.

**Qué referencias puede mostrar**

Puede mostrar referencias a productos, pedidos o seguimiento, pero solo como lectura contextual. La edición debe derivar al bloque dueño.

**Qué convergencias usa**

- Editor.
- Preview.
- Impacto.
- Confirmación.
- Resultado.

**Riesgos que debe evitar**

- Rediseñar el usuario público.
- Editar productos desde una pantalla pública.
- Cambiar el flujo de compra.
- Romper ticket o seguimiento.
- Mezclar mensajes enviados con textos visibles de pantalla.

---

### Locales

**Qué representa**

Locales representa la configuración estructural de comercios.

Edita datos del local como entidad administrativa, no su operación viva.

**Qué puede editar**

- Datos estructurales del local.
- Información pública estructural.
- Dirección, horario, descripción o datos mínimos.
- Estado de configuración.
- Publicación u ocultamiento estructural, si corresponde.
- Datos pendientes o revisión estructural.

**Qué no puede editar**

- Productos.
- Precios.
- Imágenes de productos.
- Catálogo.
- Pedidos vivos.
- Usuario asociado.
- Rol del local.
- Cuenta o acceso del local.
- Estado operativo en vivo.
- Local sin respuesta como problema operativo.

**Dónde corta**

Corta antes de operar el local, contactar, resolver incidencias o modificar productos.

**Qué referencias puede mostrar**

- Productos: solo como referencia.
- Usuario asociado: solo como referencia.
- Estado operativo: solo como referencia si ayuda a orientar.
- Pendientes estructurales.

**Qué convergencias usa**

- Local configurable.
- Editor.
- Impacto.
- Confirmación.
- Resultado.

**Riesgos que debe evitar**

- Convertirse en Operación → Locales activos.
- Editar catálogo desde Locales.
- Editar usuario o rol del local.
- Resolver “local no responde”.
- Publicar sin impacto.

---

### Catálogo y productos

**Qué representa**

Catálogo y productos representa el bloque dueño de las entidades vendibles.

Es el lugar donde se configuran productos, categorías, subcategorías, precios, imágenes, disponibilidad y visibilidad vendible.

**Qué puede editar**

- Categorías.
- Subcategorías.
- Productos.
- Nombre del producto.
- Descripción.
- Precio.
- Imagen.
- Disponibilidad.
- Visibilidad.
- Estado publicado, oculto, incompleto o borrador.

**Qué no puede editar**

- Pedidos ya creados.
- Estado de pedidos.
- Historial de pedidos.
- Usuario público como experiencia.
- Datos estructurales del local.
- Usuarios.
- Roles.
- Accesos.
- Operación viva.

**Dónde corta**

Corta antes de tocar pedidos existentes, usuarios, roles o datos estructurales del local.

**Qué referencias puede mostrar**

- Local relacionado como referencia.
- Pedidos existentes como no afectados.
- Usuario público como lugar donde puede verse el producto, pero no como edición de experiencia pública.

**Qué convergencias usa**

- Entidad configurable.
- Editor.
- Preview.
- Impacto.
- Confirmación.
- Resultado.

**Riesgos que debe evitar**

- Modificar pedidos ya confirmados.
- Cambiar snapshots de pedidos existentes.
- Editar datos de local.
- Convertir productos en textos públicos.
- Publicar productos sin revisión de impacto.

**Regla crítica**

Los pedidos ya creados conservan su snapshot. Cualquier cambio de producto, precio, imagen, categoría o disponibilidad afecta pedidos futuros, no pedidos ya confirmados.

---

### Pedidos

**Qué representa**

Pedidos representa la configuración de reglas estructurales futuras del pedido.

No es una pantalla para abrir pedidos reales.

**Qué puede editar**

- Reglas de creación.
- Estados visibles.
- Seguimiento como regla futura de presentación.
- Criterios de demora estructural si corresponden al pedido.
- Reglas futuras de cancelación.
- Comportamiento estructural del pedido.

**Qué no puede editar**

- Pedido #____.
- Estados vivos.
- Cancelaciones reales.
- Pedidos activos.
- Historial.
- TrackingNumber real existente.
- Tickets emitidos.
- Snapshots existentes.
- Incidencias operativas.

**Dónde corta**

Corta antes de abrir o modificar un pedido concreto.

**Qué referencias puede mostrar**

- Estados posibles.
- Seguimiento.
- Ticket.
- Tracking.
- Pedidos existentes como no afectados.

**Qué convergencias usa**

- Regla configurable.
- Editor.
- Impacto.
- Confirmación sensible.
- Resultado.

**Riesgos que debe evitar**

- Abrir Pedido #____.
- Cambiar estado vivo.
- Cancelar un pedido real.
- Reescribir historial.
- Tocar trackingNumber existente.
- Confundirse con Operación o Solucionar.

---

### Comunicación

**Qué representa**

Comunicación representa el bloque dueño de mensajes enviados, avisos y plantillas.

Debe diferenciar entre texto visible de pantalla y mensaje enviado.

**Qué puede editar**

- Plantillas de mensaje.
- Títulos o asuntos de aviso.
- Cuerpo del mensaje.
- Destinatario conceptual.
- Estado de mensaje: activo, borrador, revisión o desactivado.
- Canal previsto como configuración conceptual.
- Preview del mensaje.
- Impacto del cambio.

**Qué no puede editar**

- Textos fijos de pantalla pública.
- Pedidos vivos.
- Estados reales.
- TrackingNumber.
- Usuarios.
- Roles.
- Canales reales no construidos.
- Envíos reales.

**Dónde corta**

Corta antes de contactar usuarios o enviar mensajes reales.

**Qué referencias puede mostrar**

- Cliente, local o repartidor como destinatario conceptual.
- Momento de uso.
- Canal previsto.
- Pedido como referencia, sin abrirlo.

**Qué convergencias usa**

- Mensaje configurable.
- Editor.
- Preview.
- Impacto.
- Confirmación.
- Resultado.

**Riesgos que debe evitar**

- Asumir WhatsApp, SMS, email o push como activos.
- Enviar mensajes reales.
- Mezclar comunicación con Usuario público.
- Resolver reclamos.
- Abrir Pedido #____.

**Regla de dueño**

Texto visible dentro de una pantalla pertenece al dueño de esa pantalla. Mensaje enviado o notificado pertenece a Comunicación.

---

### Operación

**Qué representa**

Configuración → Operación representa la configuración de criterios de lectura operativa.

No es Admin → Operación viva.

**Qué puede editar**

- Criterios de demora.
- Criterios de problemas.
- Umbrales operativos.
- Clasificaciones operativas.
- Reglas de atención.
- Condiciones para marcar revisión.

**Qué no puede editar**

- Pedidos reales.
- Estados vivos.
- Listados vivos.
- Pedido #____.
- Solucionar.
- Contactos.
- Cancelaciones.
- Reasignaciones.
- Acciones manuales.

**Dónde corta**

Corta antes de mostrar casos vivos o ejecutar una acción operativa.

**Qué referencias puede mostrar**

- Nombres de clasificaciones usadas en Operación.
- Tipos de demora o problema.
- Umbrales.
- Criterios.

**Qué convergencias usa**

- Criterio configurable.
- Editor.
- Impacto.
- Confirmación.
- Resultado.

**Riesgos que debe evitar**

- Abrir Pedidos del día.
- Abrir Pedidos activos.
- Mostrar pedidos con problemas reales.
- Confundirse con Operación viva.
- Resolver desde Configuración.

**Regla clave**

Configuración → Operación define criterios. Admin → Operación muestra lo vivo.

---

### Reglas y validaciones

**Qué representa**

Reglas y validaciones representa condiciones generales de integridad.

Define qué debe cumplirse para guardar, publicar, bloquear o permitir ciertas acciones futuras.

**Qué puede editar**

- Datos mínimos.
- Reglas de publicación.
- Bloqueos por incompleto.
- Validaciones generales de pedido.
- Validaciones generales de local.
- Validaciones generales de producto.
- Condiciones generales de integridad.

**Qué no puede editar**

- Productos concretos.
- Locales concretos.
- Pedidos vivos.
- Usuarios.
- Roles.
- Mensajes.
- Operación viva.
- Backend.
- Reglas reales de seguridad.
- Código.

**Dónde corta**

Corta antes de editar la entidad concreta. La regla vive acá; la entidad se edita en su bloque dueño.

**Qué referencias puede mostrar**

- Producto, local, pedido o mensaje como entidad relacionada.
- Dueño de edición.
- Bloqueo asociado.
- Alcance de la validación.

**Qué convergencias usa**

- Validación configurable.
- Editor.
- Impacto.
- Confirmación sensible.
- Resultado.

**Riesgos que debe evitar**

- Duplicar Pedidos.
- Duplicar Operación.
- Duplicar Locales.
- Duplicar Catálogo.
- Implementar backend.
- Editar entidades concretas desde la regla general.

---

### Auditoría

**Qué representa**

Auditoría representa consulta y trazabilidad de registros administrativos.

No es un editor. No es una pantalla técnica.

**Qué puede mostrar**

- Cambios de configuración.
- Publicaciones.
- Desactivaciones.
- Cambios sensibles.
- Intervenciones Admin registradas.
- Detalle de registro.
- Impacto registrado.
- Fecha o momento.
- Sección afectada.
- Responsable visible, si corresponde.

**Qué no puede editar**

- Configuración.
- Entidades.
- Usuarios.
- Roles.
- Pedidos.
- Operación viva.
- Historial propio de Pedido #____.
- Cambios ya aplicados.

**Dónde corta**

Corta en la lectura del registro. No revierte ni aplica cambios.

**Qué referencias puede mostrar**

- Bloque de origen.
- Registro relacionado.
- Impacto.
- Resultado registrado.
- Pedido o entidad como referencia, sin resolver.

**Qué convergencias usa**

- Registro detalle.
- Impacto registrado.
- Resultado de lectura.

**Riesgos que debe evitar**

- Mostrar logs técnicos crudos.
- Exponer IDs internos.
- Reemplazar historial del pedido.
- Permitir editar o revertir.
- Convertirse en pantalla de base de datos.

---

### Emergencias

**Qué representa**

Emergencias representa configuración excepcional, modo seguro y restricciones temporales controladas.

No es un panel destructivo ni una forma rápida de resolver todo.

**Qué puede editar**

- Modo seguro.
- Pausa operativa si se define.
- Avisos globales excepcionales.
- Restricciones temporales.
- Estado de emergencia.
- Alcance de una emergencia.
- Activación o desactivación excepcional controlada.

**Qué no puede editar**

- Pedidos concretos.
- Productos.
- Locales.
- Usuarios.
- Roles.
- Mensajes normales.
- Criterios normales de operación.
- Reglas generales de integridad.
- Alta de roles.

**Dónde corta**

Corta antes de resolver pedidos puntuales o ejecutar acciones destructivas.

**Qué referencias puede mostrar**

- Alcance.
- Impacto.
- Registro posterior en Auditoría.
- Restricciones activas.
- Avisos globales.

**Qué convergencias usa**

- Emergencia configurable.
- Impacto.
- Confirmación sensible.
- Resultado.

**Riesgos que debe evitar**

- Cancelar masivamente.
- Cambiar estados reales.
- Reemplazar Pedido #____.
- Reemplazar Solucionar.
- Activar canales no construidos.
- Aplicar cambios sin impacto y confirmación.

**Regla obligatoria**

Toda emergencia requiere alcance, impacto, confirmación, resultado y registro posterior en Auditoría.

---

### General

**Qué representa**

General representa parámetros globales sin dueño específico.

Debe ser el último bloque de Configuración y no debe absorber lo que ya pertenece a otro bloque.

**Qué puede editar**

- Parámetros generales sin dueño específico.
- Preferencias administrativas generales.
- Información general no crítica.
- Configuración pendiente global si no pertenece a otro bloque.
- Estado general de configuración.

**Qué no puede editar**

- Productos.
- Locales.
- Usuario público.
- Pedidos.
- Mensajes.
- Criterios operativos.
- Validaciones.
- Emergencias.
- Auditoría.
- Usuarios.
- Roles.
- Accesos.

**Dónde corta**

Corta cuando detecta que algo tiene dueño específico. En ese caso debe derivar al bloque dueño.

**Qué referencias puede mostrar**

- Estado general de bloques.
- Pendientes.
- Bloque dueño sugerido.
- Parámetros globales no críticos.

**Qué convergencias usa**

- Parámetro configurable.
- Editor.
- Impacto.
- Confirmación.
- Resultado.

**Riesgos que debe evitar**

- Ser un depósito de opciones.
- Editar cualquier cosa.
- Invadir bloques dueños.
- Crear un panel de todo.
- Mezclar Alta de roles.

## 8. Convergencias comunes de Configuración

Las convergencias son pantallas funcionales compartidas por varios bloques. No representan un bloque nuevo, sino puntos comunes de comportamiento.

### Entidad configurable

Aparece cuando el Admin toca un elemento concreto configurable.

Sirve para ver:

- Estado actual.
- Tipo de entidad o regla.
- Alcance.
- Qué controla.
- Qué está bloqueado.
- Referencias relacionadas.
- Acceso a editar, previsualizar o ver impacto, según corresponda.

No debe ejecutar acciones directas críticas.

### Editor

Aparece cuando el elemento sí puede editarse desde ese bloque dueño.

Debe mostrar:

- Valor actual.
- Nuevo valor.
- Campos editables.
- Campos bloqueados.
- Campos requeridos.
- Guardar borrador.
- Continuar a impacto.
- Previsualizar, si corresponde.

No debe publicar ni aplicar cambios sensibles sin impacto.

### Preview / revisión

Aparece cuando el cambio puede verse antes de aplicarse.

Sirve para mostrar una vista conceptual o revisión del cambio.

Debe dejar claro que no está publicado ni aplicado todavía.

No todos los bloques necesitan preview.

### Impacto

Aparece cuando un cambio puede afectar comportamiento, visibilidad, publicación, reglas o configuración futura.

Debe mostrar:

- Qué cambia.
- Qué afecta.
- Qué no afecta.
- Qué requiere.
- Si corresponde confirmación.

Todo cambio con impacto debe pasar por esta pantalla o equivalente.

### Confirmación sensible

Aparece cuando el cambio es crítico, sensible, excepcional o puede afectar comportamiento futuro importante.

No todo cambio necesita confirmación sensible, pero todo cambio sensible sí.

Debe mostrar:

- Qué se aplicará.
- Alcance.
- Impacto.
- Qué no cambia.
- Advertencia humana.
- Botón de aplicar.
- Opción de volver.

### Resultado

Todo flujo debe terminar en un resultado.

Puede mostrar:

- Cambio aplicado.
- Guardado como borrador.
- Pendiente de revisión.
- No se pudo aplicar.
- Sin cambios.
- Derivado a otro bloque.

El resultado debe orientar cómo volver al bloque, a la entidad o a Configuración.

## 9. Flujo estándar de edición

Flujo correcto:

```text
Raíz de Configuración
→ grupo / listado
→ entidad configurable
→ estado actual
→ editar
→ preview o revisión
→ impacto
→ confirmación si corresponde
→ resultado
→ volver
```

Este flujo protege contra cambios impulsivos y mantiene claridad funcional.

No debe existir este flujo:

```text
Raíz
→ todos los botones juntos
→ acción directa sin impacto
```

Tampoco debe existir:

```text
Configuración
→ operación viva
→ resolver pedido
```

Ni:

```text
Configuración
→ editar entidad que pertenece a otro bloque
```

## 10. Estados posibles

Estados generales permitidos en Configuración:

- Activo / Publicado.
- Borrador.
- Incompleto.
- Oculto / Desactivado.
- Pendiente de revisión.
- Bloqueado / no editable.
- Derivado a otro bloque.
- Sin cambios.

Estos estados deben ser humanos, claros y no técnicos.

No deben confundirse con estados vivos de pedidos.

## 11. Auditoría de contradicciones

### Usuario público vs Catálogo

**Riesgo**

Usuario público muestra productos y podría parecer que los edita.

**Solución**

Usuario público edita experiencia pública configurable. Catálogo y productos edita la entidad producto.

### Locales vs Catálogo

**Riesgo**

Locales puede mostrar que un comercio no tiene productos vendibles y parecer que permite cargarlos.

**Solución**

Locales edita datos estructurales del comercio. Catálogo y productos edita productos, precios, imágenes y disponibilidad.

### Pedidos vs Reglas y validaciones

**Riesgo**

Ambos pueden hablar de reglas.

**Solución**

Pedidos define comportamiento estructural futuro del pedido. Reglas y validaciones define condiciones generales de integridad, mínimos y bloqueos.

### Comunicación vs Usuario público

**Riesgo**

Ambos pueden tener textos.

**Solución**

Texto visible en pantalla pública pertenece a Usuario público. Mensaje enviado o aviso comunicacional pertenece a Comunicación.

### Operación vs Configuración → Operación

**Riesgo**

Configuración → Operación puede parecer Operación viva.

**Solución**

Configuración → Operación define criterios. Admin → Operación muestra casos vivos.

### Emergencias vs Operación

**Riesgo**

Emergencias podría usarse como botón destructivo para resolver todo.

**Solución**

Emergencias configura modo excepcional con alcance, impacto, confirmación, resultado y auditoría. No resuelve pedidos puntuales.

### General como basurero

**Riesgo**

General puede absorber cualquier cosa no pensada.

**Solución**

General solo contiene parámetros sin dueño específico. Si algo tiene dueño, deriva al bloque dueño.

### Configuración mezclando Alta de roles

**Riesgo**

Locales, Comunicación, Auditoría o General podrían mostrar usuarios y permitir editarlos.

**Solución**

Usuarios, roles, accesos, active/inactive y permisos pertenecen a Alta de roles, fuera de Configuración.

### Configuración modificando pedidos ya creados

**Riesgo**

Catálogo, Pedidos u Operación podrían afectar pedidos existentes.

**Solución**

Pedidos existentes conservan identidad, historial, ticket, tracking y snapshot. Configuración afecta reglas futuras salvo definición explícita con impacto, confirmación y auditoría.

### Configuración asumiendo WhatsApp o notificaciones reales

**Riesgo**

Comunicación o Emergencias podrían mostrar canales como activos.

**Solución**

Los canales no construidos se muestran como futuros, pendientes o no activos. No se envían mensajes reales.

## 12. Reglas finales obligatorias

1. Configuración no resuelve operación viva.
2. Una entidad tiene un solo dueño de edición.
3. Alta de roles queda fuera.
4. Usuario público se respeta como flujo cerrado.
5. Catálogo no modifica pedidos ya creados.
6. Pedidos no abre Pedido #____.
7. Comunicación no envía mensajes reales.
8. Operación configura criterios, no casos vivos.
9. Auditoría consulta, no edita.
10. Emergencias siempre requiere impacto y confirmación.
11. General no absorbe lo que tiene dueño.
12. Las referencias visuales no son UI final literal.

## 13. Dictamen final

**Admin → Configuración queda cerrado como plano técnico-funcional de referencia.**

El universo queda ordenado por bloques dueños, con límites claros, convergencias compartidas y reglas de protección para no romper usuario público, catálogo real, pedidos existentes, tickets, tracking, seguimiento ni Login Equipo.

Este cierre no implementa todavía. No define backend. No define UI final. No prepara Codex todavía.

El próximo universo separado será:

**Admin → Alta de roles**
