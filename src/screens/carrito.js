// src/screens/carrito.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock, selectLocalActual } from "../core/selectors.js";

function datoResumen(label, value) {
  return value
    ? `
      <div class="card card--raised">
        <p class="card__meta">${label}</p>
        <p>${value}</p>
      </div>
    `
    : "";
}

function renderHero(eyebrow, titulo) {
  return `
    <section class="hero">
      <p class="eyebrow">${eyebrow}</p>
      <h1>${titulo}</h1>
    </section>
  `;
}

function renderAcciones(volver = "inicio") {
  return `
    <div class="card__actions">
      <button class="btn btn-secondary" data-nav="${volver}">Volver</button>
      <button class="btn btn-primary" data-ir-confirmacion>Continuar</button>
    </div>
  `;
}

function getLocalNombre(state = {}, pedido = {}) {
  const local = selectLocalActual({ ...state, pedido });

  return pedido.local_nombre || pedido.localNombre || local?.nombre || "";
}

function renderLocalPedido(pedido = {}, state = {}) {
  const datos = datoResumen("Local", getLocalNombre(state, pedido));

  return datos
    ? `<section class="cart-list">${datos}</section>`
    : "";
}

function renderMarketplace(pedido, cart, state) {
  const items = cart.items || [];

  if (!pedido.local) {
    return `
      <main class="pantalla">
        <h2>¿Está bien tu pedido?</h2>
        <p class="section-note">Aún no elegiste un comercio.</p>
        <button class="btn btn-secondary" data-nav="inicio">Volver al inicio</button>
      </main>
    `;
  }

  if (items.length === 0) {
    return `
      <main class="pantalla pantalla--centered">
        <section class="mini-screen mini-screen--flow">
          <p class="eyebrow">Pedido</p>
          <h1>Tu carrito está vacío</h1>
          <p class="section-note">Agregá productos del local para continuar.</p>
          <button class="btn btn-primary btn-full" data-nav="productos">Seguir comprando</button>
        </section>
      </main>
    `;
  }

  return `
    <main class="pantalla">
      ${renderHero("¿Está bien tu pedido?", "Revisá lo que ya agregaste.")}
      ${renderLocalPedido(pedido, state)}

      <section class="cart-list">
        ${items
          .map(
            (item) => `
              <article class="card card--raised cart-item">
                <div class="card__heading-row">
                  <h3>${item.nombre}</h3>
                  ${
                    item.precio !== null && item.precio !== undefined
                      ? `<span class="price-pill">$${item.precio}</span>`
                      : ""
                  }
                </div>

                <div class="cart-item__summary">
                  <div class="cart-controls">
                    <button class="btn btn-ghost btn-inline" data-dec="${item.itemId}">-</button>
                    <span class="cart-qty">${item.cantidad}</span>
                    <button class="btn btn-ghost btn-inline" data-producto="${item.productoId}">+</button>
                  </div>

                  <p class="card__meta">
                    Subtotal:
                    ${
                      item.subtotal !== null && item.subtotal !== undefined
                        ? `$${item.subtotal}`
                        : "Precio a consultar"
                    }
                  </p>
                </div>

                ${
                  item.observacion || item.detalle
                    ? `<p class="card__text">Observación: ${item.observacion || item.detalle}</p>`
                    : ""
                }

                ${
                  item.opcionesSeleccionadas?.length
                    ? `<p class="card__meta">Opciones: ${item.opcionesSeleccionadas.join(", ")}</p>`
                    : ""
                }

                <div class="cart-item__actions">
                  <button class="btn btn-secondary" data-editar-item="${item.itemId}">Editar</button>
                  <button class="btn btn-ghost" data-remove="${item.itemId}">Eliminar</button>
                </div>
              </article>
            `
          )
          .join("")}
      </section>

      <div class="card card--raised total-card">
        <p class="card__meta">Total</p>
        <p class="card__heading">$${cart.total || 0}</p>
      </div>

      <div class="card__actions flow-actions">
        <button class="btn btn-secondary" data-nav="productos">Seguir agregando</button>
        <button class="btn btn-primary" data-ir-confirmacion>Continuar</button>
      </div>
    </main>
  `;
}

function renderResumenFlujo({ eyebrow, titulo, resumen, volver = "inicio" }) {
  return `
    <main class="pantalla">
      ${renderHero(eyebrow, titulo)}
      ${resumen || `<p class="section-note">Completá los datos para continuar.</p>`}
      ${renderAcciones(volver)}
    </main>
  `;
}

export function screenCarrito(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "carrito"
  });
  const data = getBlock(contract, "carrito")?.data || {};
  const pedido = data.pedido || state.pedido;
  const cart = data.cart || { items: pedido.productos || [], total: 0 };

  if (pedido.tipo === "marketplace") {
    return renderMarketplace(pedido, cart, state);
  }

  if (pedido.tipo === "compra_libre") {
    const form = pedido.formulario.compra_libre;

    return renderResumenFlujo({
      eyebrow: "Resumen de compra libre",
      titulo: "Revisá tu pedido antes de confirmar.",
      resumen: [
        datoResumen("Necesidad", form.queNecesita),
        datoResumen("Dónde comprar", form.donde),
        datoResumen("Observaciones", form.observaciones)
      ].join("") || `<p class="section-note">Completá los datos de compra libre para continuar.</p>`
    });
  }

  if (pedido.tipo === "retirar_envio") {
    const form = pedido.formulario.retirar_envio;

    return renderResumenFlujo({
      eyebrow: "Resumen retiro/envío",
      titulo: "Revisá los datos del envío.",
      resumen: [
        datoResumen("Retiro", form.direccionRetiro),
        datoResumen("Entrega", form.direccionEntrega),
        datoResumen("Qué retirar", form.queRetira),
        datoResumen("Observaciones", form.observaciones)
      ].join("") || `<p class="section-note">Completá los datos del envío para continuar.</p>`
    });
  }

  if (pedido.tipo === "pedir_repartidor") {
    const form = pedido.formulario.repartidor;

    return renderResumenFlujo({
      eyebrow: "Resumen repartidor",
      titulo: "Revisá tus puntos y el detalle.",
      resumen: [
        datoResumen("Punto de salida", form.puntoSalida),
        datoResumen("Punto de destino", form.puntoDestino),
        datoResumen("Detalle", form.detalle),
        datoResumen("Observaciones", form.observaciones)
      ].join("") || `<p class="section-note">Completá los datos del repartidor para continuar.</p>`
    });
  }

  return `
    <main class="pantalla">
      <h2>Completá un tipo de pedido primero.</h2>
      <button class="btn btn-secondary" data-nav="inicio">Volver al inicio</button>
    </main>
  `;
}
