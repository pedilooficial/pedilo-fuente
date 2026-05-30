# Cierre maestro técnico-funcional — Admin → Alta de roles

## 1. Propósito del documento

Este documento cierra el universo **Admin → Alta de roles** como plano técnico-funcional de referencia.

Su objetivo es ordenar qué representa Alta de roles dentro de Pédilo Admin, qué bloques existen, cómo se navega, qué puede editar cada bloque, qué no puede tocar, dónde corta cada flujo, cuáles son sus convergencias comunes y qué reglas deben respetarse para no mezclar accesos con Operación, Configuración, pedidos, locales, productos o credenciales sensibles.

Este archivo no define UI final, no define estilo visual, no implementa código y no prepara todavía una etapa de Codex. Su función es dejar auditado el comportamiento funcional esperado del universo Alta de roles.

## 2. Qué es Alta de roles

**Alta de roles** es el universo dueño de usuarios, cuentas, roles, accesos, estado de ingreso y vínculos operativos de cuenta.

Alta de roles responde:

> “¿Quién puede entrar al sistema, con qué rol, en qué estado y bajo qué vínculo operativo?”

Este universo organiza y controla:

- Cuentas del equipo.
- Roles permitidos.
- Acceso habilitado o detenido.
- Cuentas pendientes.
- Cuentas inactivas.
- Vínculos entre cuenta y entidad operativa.
- Revisión de acciones sensibles sobre acceso.

Alta de roles no administra la entidad operativa en sí. Administra la cuenta que permite ingresar y su relación con esa entidad.

## 3. Qué no es Alta de roles

Alta de roles no es:

- Operación viva.
- Configuración.
- Pedido #____.
- Solucionar.
- Gestión de pedidos.
- Gestión de catálogo.
- Gestión estructural de locales.
- Gestión operativa de repartidores.
- Backend.
- Implementación.
- Panel de permisos granulares no definidos.
- Lugar para inventar roles nuevos.
- Pantalla para mostrar credenciales sensibles.

Alta de roles no debe resolver pedidos, configurar productos, operar locales, asignar entregas, editar datos comerciales ni reemplazar los flujos dueños de otros universos.

## 4. Estado actual que debe respetar

El diseño funcional de Alta de roles debe respetar que Pédilo ya cuenta con Login Equipo funcionando.

El contexto técnico real actual es:

- Firebase Auth valida email/contraseña.
- `/users/{uid}` resuelve el rol.
- `active == true` es requerido para entrar.
- `role` puede ser:
  - `admin`
  - `store`
  - `driver`

Este documento usa ese contexto como condición funcional. No implementa backend, no crea reglas, no modifica autenticación y no expone detalles técnicos en la UI.

También deben respetarse:

- Usuario público real cerrado.
- Catálogo real.
- Pedidos reales.
- Ticket real.
- Seguimiento público real.
- Admin → Operación ya proyectado.
- Admin → Configuración ya proyectado.

Alta de roles no puede romper pedidos existentes, catálogo, usuario público, tickets, tracking, operación viva ni configuración estructural.

## 5. Roles permitidos

Los únicos roles funcionales actuales son:

- `admin`
- `store`
- `driver`

No se deben inventar otros roles.

No existen en este cierre:

- supervisor
- soporte
- cajero
- operador
- cliente
- comercio
- owner
- manager
- ningún otro rol

La interfaz puede mostrar esos roles de forma humana como:

- Admin
- Local
- Repartidor

Pero la lógica funcional del universo Alta de roles solo contempla:

- `admin`
- `store`
- `driver`

Cualquier cambio de rol es sensible y debe pasar por impacto y confirmación.

## 6. Regla de dueño único de edición

Regla madre:

> Una cuenta puede verse como referencia en varios lugares, pero solo se edita desde Alta de roles.

Alta de roles es el único dueño de:

- usuarios;
- cuentas;
- roles;
- accesos;
- active / inactive;
- cuentas pendientes;
- vínculos de cuenta con rol operativo.

### Matriz de dueño

| Elemento | Bloque dueño | Regla |
|---|---|---|
| Cuenta admin | Alta de roles | Se revisa y edita solo en Alta de roles. |
| Cuenta store | Alta de roles | Se revisa y edita solo en Alta de roles. |
| Cuenta driver | Alta de roles | Se revisa y edita solo en Alta de roles. |
| Datos estructurales del local | Configuración → Locales | Alta de roles puede mostrar referencia, no editar. |
| Productos del local | Configuración → Catálogo y productos | Alta de roles no edita productos. |
| Estado vivo del local | Operación → Locales activos | Alta de roles no opera locales vivos. |
| Estado vivo del repartidor | Operación → Repartidores activos | Alta de roles no opera repartidores vivos. |
| Pedido concreto | Operación → Pedido #____ | Alta de roles no abre ni resuelve pedidos. |
| Resolución guiada | Operación → Pedido #____ → Solucionar | Alta de roles no reemplaza Solucionar. |
| Usuarios, roles, active/inactive, accesos | Alta de roles | Dueño único de edición. |

## 7. Mapa general de Alta de roles

```text
Admin → Alta de roles
├── Usuarios del equipo
├── Administradores
├── Locales store
├── Repartidores driver
├── Altas pendientes
├── Usuarios inactivos
├── Vinculaciones pendientes
└── Convergencias
    ├── Cuenta concreta
    ├── Alta de cuenta
    ├── Editor de acceso
    ├── Cambio de rol
    ├── Activar / desactivar
    ├── Vincular entidad
    ├── Impacto
    ├── Confirmación sensible
    └── Resultado
```

## 8. Descripción por bloque

### Usuarios del equipo

**Qué representa**

Vista general de todas las cuentas con acceso o relación con el sistema.

Debe permitir entender quién existe, qué rol tiene y en qué estado está.

**Qué puede mostrar**

- Nombre visible.
- Email.
- Rol: `admin`, `store` o `driver`.
- Estado: activa, inactiva, pendiente o revisión.
- Vínculo operativo, si corresponde.
- Entrada hacia cuenta concreta.

**Qué puede editar o iniciar**

Desde la lista no edita directamente. Solo permite entrar a una cuenta concreta.

**Qué no puede editar**

- Rol desde la fila.
- Activación o desactivación directa.
- Pedidos.
- Locales.
- Productos.
- Repartidores operativos.
- Configuración.
- Operación.

**Dónde corta**

Corta en **Cuenta concreta**.

**Qué referencias puede mostrar**

Puede mostrar el vínculo operativo como referencia, por ejemplo local vinculado o repartidor vinculado, sin editar esa entidad.

**Riesgos que debe evitar**

- Convertirse en una tabla de acciones directas.
- Permitir activar o cambiar rol desde la lista.
- Mostrar información técnica.
- Mezclar con Configuración u Operación.

---

### Administradores

**Qué representa**

Lista de cuentas con rol `admin`.

El rol admin es sensible porque habilita acceso administrativo.

**Qué puede mostrar**

- Nombre visible.
- Email.
- Estado.
- Revisión.
- Entrada hacia cuenta concreta.

**Qué puede editar o iniciar**

No edita desde la lista. Puede abrir cuenta concreta para iniciar flujos sensibles.

**Qué no puede editar**

- Cambio de rol directo.
- Desactivación directa.
- Eliminación de admin.
- Configuración.
- Operación.
- Permisos granulares no definidos.

**Dónde corta**

Corta en **Cuenta concreta**.

**Qué referencias puede mostrar**

Puede mostrar estado de revisión o sensibilidad, sin exponer permisos técnicos.

**Riesgos que debe evitar**

- Dar poderes libres.
- Inventar niveles de admin.
- Permitir cambios sin impacto y confirmación.
- Mostrar permisos técnicos no definidos.

---

### Locales store

**Qué representa**

Lista de cuentas con rol `store`.

Administra el acceso de la cuenta local, no los datos estructurales del comercio.

**Qué puede mostrar**

- Email o nombre visible.
- Rol `store`.
- Estado.
- Local vinculado como referencia.
- Entrada hacia cuenta concreta.

**Qué puede editar o iniciar**

Puede iniciar revisión de cuenta, acceso, rol o vínculo desde cuenta concreta.

**Qué no puede editar**

- Datos estructurales del local.
- Dirección, horarios o descripción del comercio.
- Productos.
- Catálogo.
- Pedidos del local.
- Estado vivo del local.
- Local sin respuesta.
- Operación → Locales activos.
- Configuración → Locales como editor.

**Dónde corta**

Corta en **Cuenta concreta** o en **Vincular entidad** si la cuenta necesita relación operativa.

**Qué referencias puede mostrar**

Puede mostrar el local vinculado, pero los datos del local pertenecen a Configuración → Locales.

**Riesgos que debe evitar**

- Convertir cuenta store en Local configurable.
- Editar productos desde Alta de roles.
- Resolver problemas operativos del local.
- Abrir pedidos vivos.

---

### Repartidores driver

**Qué representa**

Lista de cuentas con rol `driver`.

Administra el acceso de la cuenta repartidor, no su estado operativo.

**Qué puede mostrar**

- Email o nombre visible.
- Rol `driver`.
- Estado.
- Repartidor vinculado como referencia.
- Entrada hacia cuenta concreta.

**Qué puede editar o iniciar**

Puede iniciar revisión de cuenta, acceso, rol o vínculo desde cuenta concreta.

**Qué no puede editar**

- Estado operativo del repartidor.
- Disponibilidad operativa.
- Pedidos asignados.
- Reasignaciones.
- Entregas.
- Métricas.
- Incidencias operativas.
- Operación → Repartidores activos.

**Dónde corta**

Corta en **Cuenta concreta** o **Vincular entidad**.

**Qué referencias puede mostrar**

Puede mostrar el repartidor vinculado como referencia, sin operar sobre él.

**Riesgos que debe evitar**

- Convertir cuenta driver en Repartidor operativo.
- Asignar pedidos.
- Resolver entregas.
- Mezclar con Operación.

---

### Altas pendientes

**Qué representa**

Cuentas por completar, revisar o habilitar.

Son cuentas que todavía no deben considerarse plenamente activas.

**Qué puede mostrar**

- Email o nombre visible.
- Rol previsto: `admin`, `store` o `driver`.
- Dato faltante.
- Estado: pendiente, revisión o incompleta.
- Entrada hacia alta de cuenta o cuenta concreta.

**Qué puede editar o iniciar**

Puede iniciar el flujo de **Alta de cuenta**, revisión de datos mínimos, vínculo o impacto.

**Qué no puede editar**

- No activa sin revisión.
- No crea rol nuevo.
- No envía invitación real.
- No manda email real.
- No manda WhatsApp.
- No asume automatizaciones reales.
- No muestra contraseña.
- No opera pedidos.

**Dónde corta**

Corta en **Alta de cuenta**, **Vincular entidad**, **Impacto** o **Confirmación sensible**, según corresponda.

**Qué referencias puede mostrar**

Puede mostrar rol previsto y vínculo faltante.

**Riesgos que debe evitar**

- Activar una cuenta incompleta.
- Asumir invitaciones reales.
- Crear roles nuevos.
- Mostrar datos técnicos o sensibles.

---

### Usuarios inactivos

**Qué representa**

Cuentas con acceso detenido, desactivado o bloqueado.

**Qué puede mostrar**

- Nombre o email.
- Rol.
- Estado: inactivo, bloqueado o revisión.
- Motivo visible, si corresponde.
- Entrada hacia cuenta concreta.

**Qué puede editar o iniciar**

Puede iniciar el flujo de activación o desactivación desde convergencias.

**Qué no puede editar**

- No reactiva desde la lista.
- No borra cuenta.
- No elimina historial.
- No modifica pedidos.
- No modifica entidades asociadas.
- No oculta trazabilidad.

**Dónde corta**

Corta en **Cuenta concreta** o **Activar / desactivar**.

**Qué referencias puede mostrar**

Puede mostrar motivo visible y estado de acceso.

**Riesgos que debe evitar**

- Confundir desactivar con borrar.
- Romper historial.
- Reactivar sin impacto y confirmación.
- Modificar entidades externas.

**Regla clave**

Desactivar cuenta corta ingreso, pero no borra historial ni pedidos.

---

### Vinculaciones pendientes

**Qué representa**

Cuentas con rol correcto pero vínculo operativo incompleto.

Ejemplos:

- Cuenta `store` sin local vinculado.
- Cuenta `driver` sin repartidor vinculado.
- Cuenta con rol correcto pero relación incompleta.

**Qué puede mostrar**

- Cuenta.
- Rol.
- Vínculo faltante.
- Estado: sin vínculo, incompleto o revisión.
- Entrada hacia vinculación.

**Qué puede editar o iniciar**

Puede iniciar **Vincular entidad**.

**Qué no puede editar**

- No crea local completo desde acá.
- No crea repartidor operativo completo desde acá.
- No edita productos.
- No opera pedidos.
- No inventa entidad.
- No vincula sin impacto y confirmación.

**Dónde corta**

Corta en **Vincular entidad**, **Impacto** y **Confirmación sensible**.

**Qué referencias puede mostrar**

Puede mostrar entidad sugerida o bloque dueño de la entidad.

**Riesgos que debe evitar**

- Crear entidades fuera de su bloque dueño.
- Vincular a entidad inexistente sin flujo.
- Editar Locales o Repartidores operativos desde Alta de roles.

## 9. Convergencias comunes de Alta de roles

### Cuenta concreta

**Qué debe mostrar**

- Email.
- Nombre visible.
- Rol.
- Estado.
- Si puede ingresar.
- Vínculo operativo.
- Última revisión, si corresponde.
- Acciones disponibles según contexto.

**Acciones posibles**

- Editar acceso.
- Cambiar rol.
- Activar / desactivar.
- Revisar vínculo.
- Ver impacto.
- Volver.

**No debe mostrar**

- Credenciales.
- Tokens.
- UID técnico visible.
- Pedidos vivos.
- Productos.
- Operación.
- Datos internos de autenticación.
- Claves.
- Información sensible innecesaria.

Cuenta concreta es el punto desde donde se inicia cualquier acción real sobre acceso.

---

### Alta de cuenta

**Qué debe mostrar**

- Email.
- Nombre visible.
- Rol previsto.
- Estado inicial.
- Vínculo requerido.
- Datos mínimos.
- Revisión antes de activar.

**Debe prohibir**

- Activar sin rol.
- Activar sin revisión.
- Crear rol nuevo.
- Enviar invitación real.
- Mandar email real.
- Mandar WhatsApp.
- Asumir automatización real.
- Mostrar contraseña.

Si se menciona invitación, debe ser futura o pendiente, no envío real activo.

---

### Editor de acceso

**Qué debe editar**

Solo datos permitidos de cuenta, por ejemplo:

- Nombre visible.
- Observación administrativa.
- Estado de revisión.
- Dato público de cuenta si corresponde.

**Qué debe derivar**

- Rol → Cambio de rol.
- Active / inactive → Activar / desactivar.
- Vínculo → Vincular entidad.

**Qué no debe editar**

- Contraseña.
- Credenciales.
- Tokens.
- UID visible.
- Backend.
- Pedidos.
- Productos.
- Datos estructurales del local.
- Operación viva.

El editor de acceso no debe saltarse impacto ni confirmación cuando la acción sea sensible.

---

### Cambio de rol

Cambio de rol siempre es sensible.

**Debe mostrar**

- Rol actual.
- Rol nuevo.
- Qué acceso gana.
- Qué acceso pierde.
- Si requiere vínculo nuevo.
- Qué no cambia.
- Riesgo.
- Revisión antes de confirmar.

**Roles permitidos**

- `admin`
- `store`
- `driver`

**Debe aclarar**

El cambio de rol puede modificar el tipo de acceso del equipo, pero no cambia por sí mismo:

- Pedidos existentes.
- Historial.
- Tickets.
- Catálogo.
- Datos estructurales del local.

**Debe pasar por**

```text
Cambio de rol
→ Impacto
→ Confirmación sensible
→ Resultado
```

---

### Activar / desactivar

**Debe mostrar**

- Estado actual.
- Nuevo estado.
- Si podrá ingresar o no.
- Motivo visible, si corresponde.
- Qué no cambia.
- Confirmación requerida.

**Debe aclarar**

Activar significa:

> La cuenta podrá ingresar si cumple rol y condiciones.

Desactivar significa:

> La cuenta no podrá ingresar, pero no elimina datos ni referencias.

**No debe hacer**

- Eliminar cuenta.
- Borrar historial.
- Modificar pedidos.
- Borrar vínculos sin confirmación.
- Aplicar sin impacto.

---

### Vincular entidad

**Debe mostrar**

- Cuenta.
- Rol.
- Vínculo actual.
- Nueva entidad a vincular.
- Estado del vínculo.
- Si falta completar entidad.
- Qué bloque es dueño de la entidad.

**Debe aclarar**

La cuenta se administra en Alta de roles.

La entidad vinculada se edita en su bloque dueño:

- Local → Configuración → Locales.
- Repartidor operativo → Operación → Repartidores activos, si se habla de estado vivo.
- Datos de cuenta → Alta de roles.

**No debe hacer**

- Crear local completo desde acá.
- Editar local estructural.
- Editar productos.
- Operar repartidor.
- Asignar pedidos.
- Vincular sin impacto.
- Inventar entidad.

---

### Impacto

Impacto responde:

> “¿Qué cambia si aplico esto?”

**Debe mostrar**

- Cuenta afectada.
- Rol, acceso, estado o vínculo seleccionado.
- Si podrá ingresar.
- Qué afecta.
- Qué no afecta.
- Riesgos.
- Si requiere confirmación.

**Debe mostrar que no afecta**

- Pedidos existentes.
- Tickets emitidos.
- TrackingNumber.
- Catálogo.
- Productos.
- Datos estructurales del local.
- Historial operativo.
- Operación viva.

**No debe permitir**

Aplicar desde impacto sin confirmación cuando se trate de una acción sensible.

---

### Confirmación sensible

Debe aparecer antes de:

- Crear cuenta activa.
- Activar cuenta.
- Desactivar cuenta.
- Cambiar rol.
- Cambiar vínculo.
- Bloquear acceso.

**Debe mostrar**

- Acción.
- Cuenta afectada.
- Rol.
- Impacto.
- Qué no cambia.
- Revisión final.
- Botón de aplicar.
- Opción de volver.

**No debe mostrar**

- Tokens.
- UID.
- Claves.
- Credenciales.
- Lenguaje técnico visible.
- Roles inventados.

---

### Resultado

Resultado cierra todo flujo.

Puede mostrar:

- Cuenta creada.
- Cuenta guardada como borrador.
- Cuenta activada.
- Cuenta desactivada.
- Rol actualizado.
- Vínculo actualizado.
- Pendiente de revisión.
- No se pudo aplicar.
- Sin cambios.

**Debe mostrar**

- Cuenta relacionada.
- Estado final.
- Rol final.
- Si puede ingresar.
- Si queda vínculo pendiente.
- Acciones de regreso.

**No debe abrir**

- Operación.
- Configuración.
- Pedidos.
- Datos técnicos.

## 10. Flujo estándar de Alta de roles

Flujo correcto:

```text
Raíz Alta de roles
→ listado / grupo
→ cuenta concreta
→ acción disponible
→ editor o flujo sensible
→ impacto
→ confirmación si corresponde
→ resultado
→ volver
```

No debe existir:

```text
Raíz
→ acción directa sensible
→ cambio sin impacto
```

Tampoco debe existir:

```text
Alta de roles
→ operar pedido
→ resolver incidencia
```

Ni:

```text
Alta de roles
→ editar local/producto como entidad estructural
```

Las acciones sensibles deben ser explícitas, revisables y confirmadas.

## 11. Estados posibles de cuenta

Estados funcionales del universo Alta de roles:

- Activa.
- Inactiva.
- Pendiente.
- Bloqueada.
- Revisión.
- Sin vínculo.
- Vínculo incompleto.
- Alta incompleta.
- Sin cambios.

No todos estos estados tienen que existir implementados ahora. Ordenan el mapa funcional y permiten diseñar navegación, revisión e impacto sin inventar comportamientos fuera de alcance.

## 12. Auditoría de contradicciones

### Alta de roles editando locales

**Riesgo**

Una cuenta `store` podría confundirse con un local configurable.

**Solución**

Alta de roles edita la cuenta y su vínculo. Los datos estructurales del local se editan en Configuración → Locales.

---

### Alta de roles editando productos

**Riesgo**

Una cuenta store puede estar vinculada a un local con productos y parecer que puede editarlos.

**Solución**

Productos, precios, imágenes, categorías y disponibilidad pertenecen a Configuración → Catálogo y productos.

---

### Alta de roles editando repartidor operativo

**Riesgo**

Una cuenta `driver` puede confundirse con el repartidor activo en operación.

**Solución**

Alta de roles administra la cuenta driver. El estado operativo del repartidor pertenece a Operación → Repartidores activos.

---

### Alta de roles mezclada con Configuración

**Riesgo**

La cuenta puede mostrar referencias a local, productos o configuración y parecer que puede editarlos.

**Solución**

Solo se edita acceso, rol, estado y vínculo de cuenta. Las entidades relacionadas se editan en su bloque dueño.

---

### Alta de roles mezclada con Operación

**Riesgo**

Desde una cuenta store o driver se podrían mostrar pedidos o acciones vivas.

**Solución**

Alta de roles no opera pedidos, no resuelve incidencias y no abre Pedido #____ ni Solucionar.

---

### Cambio de rol sin impacto

**Riesgo**

Cambiar rol puede modificar acceso de forma sensible.

**Solución**

Cambio de rol siempre pasa por Impacto y Confirmación sensible.

---

### Desactivar cuenta rompiendo pedidos

**Riesgo**

Desactivar una cuenta podría interpretarse como borrar historial o afectar pedidos.

**Solución**

Desactivar corta ingreso. No borra historial, pedidos, tickets ni referencias.

---

### Crear roles inventados

**Riesgo**

Aparecen roles no existentes como supervisor, cajero, operador o soporte.

**Solución**

Solo se permiten `admin`, `store` y `driver`.

---

### Asumir invitaciones reales

**Riesgo**

Alta de cuenta podría parecer que envía emails, WhatsApp o notificaciones.

**Solución**

No se asumen invitaciones reales. Si se menciona invitación, debe ser futura o pendiente.

---

### Mostrar credenciales o datos técnicos sensibles

**Riesgo**

La UI podría mostrar UID, tokens, claves o datos internos.

**Solución**

La UI habla de forma humana: cuenta, rol, estado, acceso, vínculo. No muestra credenciales ni datos técnicos sensibles.

---

### Vincular entidad inexistente sin flujo

**Riesgo**

Se vincula una cuenta a un local o repartidor inexistente.

**Solución**

Vincular entidad debe pasar por impacto y confirmación. La entidad vinculada debe existir o derivar a su bloque dueño.

## 13. Reglas finales obligatorias

1. Alta de roles es dueño único de usuarios, cuentas, roles, accesos, active/inactive y vínculos.
2. No opera pedidos.
3. No configura locales.
4. No edita productos.
5. No resuelve incidencias.
6. No reemplaza Configuración.
7. No reemplaza Operación.
8. No inventa roles nuevos.
9. Los únicos roles actuales son `admin`, `store` y `driver`.
10. Cambio de rol siempre pasa por impacto y confirmación.
11. Activar/desactivar siempre pasa por impacto y confirmación.
12. Vincular entidad siempre pasa por impacto y confirmación.
13. No se muestran credenciales ni datos técnicos sensibles.
14. Desactivar cuenta corta ingreso, no borra historial ni pedidos.
15. Las referencias visuales no son UI final literal.

## 14. Dictamen final

**Admin → Alta de roles queda cerrado como plano técnico-funcional de referencia.**

El universo queda ordenado como dueño único de cuentas, usuarios, roles, accesos, estados de ingreso y vínculos operativos.

No implementa todavía. No define backend. No define UI final. No inventa roles nuevos.

Con este cierre quedan cubiertos los tres universos principales del Admin:

- Operación.
- Configuración.
- Alta de roles.

El próximo paso posterior debería ser una **auditoría maestra total de Admin completo**, no más pantallas internas.
