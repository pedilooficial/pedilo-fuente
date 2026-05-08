// src/services/whatsapp.js

/*
  WHATSAPP SERVICE

  - no toca UI
  - no toca state directamente
  - solo formatea y envía mensaje
*/

function formatearItems(items) {
  return items
    .map((item, i) => {
      const detalle = item.detalle
        ? `\n   Detalle: ${item.detalle}`
        : "";

      const precio =
        item.precio !== null && item.precio !== undefined
          ? `\n   Precio: $${item.precio}`
          : "";
      const opciones = item.opcionesSeleccionadas?.length
        ? `\n   Opciones: ${item.opcionesSeleccionadas.join(", ")}`
        : "";

      return `${i + 1}. ${item.nombre} x${item.cantidad}${detalle}${opciones}${precio}`;
    })
    .join("\n\n");
}

function formatearCliente(cliente = {}) {
  const direccion = cliente.direccion || {};
  const addressText = [direccion.calle, direccion.numero, direccion.piso]
    .filter(Boolean)
    .join(" ");

  return [`Nombre: ${cliente.nombre || "-"}`]
    .concat(cliente.telefono ? [`Teléfono: ${cliente.telefono}`] : [])
    .concat(addressText ? [`Dirección: ${addressText}`] : [])
    .concat(cliente.referencia ? [`Referencia: ${cliente.referencia}`] : [])
    .join("\n");
}

function formatearFormulario(pedido) {
  const tipo = pedido.tipo;
  const form = pedido.formulario || {};

  if (tipo === "compra_libre") {
    const compra = form.compra_libre || {};
    return [`Qué comprar: ${compra.queNecesita || "-"}`]
      .concat(compra.donde ? [`Dónde comprar: ${compra.donde}`] : [])
      .concat(compra.observaciones ? [`Observaciones: ${compra.observaciones}`] : [])
      .join("\n");
  }

  if (tipo === "retirar_envio") {
    const retiro = form.retirar_envio || {};
    return [`Dirección de retiro: ${retiro.direccionRetiro || "-"}`]
      .concat(`Dirección de entrega: ${retiro.direccionEntrega || "-"}`)
      .concat(`Qué retirar: ${retiro.queRetira || "-"}`)
      .concat(retiro.observaciones ? `Observaciones: ${retiro.observaciones}` : [])
      .join("\n");
  }

  if (tipo === "pedir_repartidor") {
    const rep = form.repartidor || {};
    return [`Punto de salida: ${rep.puntoSalida || "-"}`]
      .concat(`Punto de destino: ${rep.puntoDestino || "-"}`)
      .concat(`Detalle: ${rep.detalle || "-"}`)
      .concat(rep.observaciones ? `Observaciones: ${rep.observaciones}` : [])
      .join("\n");
  }

  return "";
}

export function enviarPedidoPorWhatsApp(pedido) {
  if (!pedido || pedido.__pedidoEjecutable !== true) {
    alert("Pedido no autorizado para enviar");
    return;
  }

  const cliente = pedido.cliente || {};
  const mensaje = [`Nuevo pedido Pédilo`, `Tipo: ${pedido.tipo}`, `Pedido N° ${pedido.numero || "-"}`]
    .concat(
      pedido.tipo === "marketplace"
        ? [``, "Productos:", formatearItems(pedido.productos)]
        : [``, "Detalle del pedido:", formatearFormulario(pedido)]
    )
    .concat([``, "Cliente:", formatearCliente(cliente)])
    .join("\n");

  const telefono = "5492395432700";
  const url = `https://wa.me/${telefono}?text=${encodeURIComponent(mensaje)}`;

  if (typeof window !== "undefined" && window.open) {
    window.open(url, "_blank");
  }

  return url;
}
