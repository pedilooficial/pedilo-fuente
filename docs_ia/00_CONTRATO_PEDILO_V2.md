# CONTRATO OFICIAL — PÉDILO V2

## Objetivo
Pédilo es un sistema de pedidos de mensajería local.
La app es dinámica y está controlada por la base de datos.

## Regla madre
La app no inventa comportamiento.
La base de datos define comportamiento.

---

## Tipos de pedido

- marketplace
- compra_libre
- retirar_envio
- pedir_repartidor

---

## Reglas por tipo

### marketplace
- usa productos de DB
- puede requerir detalle/opciones/sabores según DB
- usa items

### compra_libre
- usa items manuales
- cada item: cantidad + descripción
- requiere: local, dirección, referencia, horario, pago

### retirar_envio
- NO usa items
- requiere: origen, referencia, qué se retira, horario, pago

### pedir_repartidor
- NO usa items
- requiere: origen, destino, qué lleva, horario, pago

---

## State

- state único
- pedido.tipo obligatorio
- items solo en marketplace y compra_libre
- direccion y referencia SIEMPRE separados

---

## Router

- router decide pantalla
- tipo decide validación/comportamiento

---

## DB controla

- app_config
- categorías
- subcategorías
- locales
- productos
- opciones
- sabores
- ofertas
- requiere_detalle
- tipo_detalle

---

## Prohibido

- lógica en screens
- estados paralelos
- inferencias por precio
- mezclar tipos de pedido
- implementar sin contrato

---

## Criterio de cierre

- todos los tipos funcionan
- confirmación funciona
- tests pasan
- npm run seguro pasa
