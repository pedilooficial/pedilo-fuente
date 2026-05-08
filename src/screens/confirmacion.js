// src/screens/confirmacion.js

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

function getLocalNombre(state = {}, pedido = {}) {
  const local = selectLocalActual({ ...state, pedido });

  return pedido.local_nombre || pedido.localNombre || local?.nombre || "";
}

function calcularTotal(items = []) {
  const conTotal = items.length > 0 && items.every(
    (item) => item.subtotal !== null && item.subtotal !== undefined
  );

  return conTotal
    ? items.reduce((total, item) => total + item.subtotal, 0)
    : null;
}

function renderTicketMarketplace(pedido = {}, state = {}) {
  const items = pedido.productos || [];
  const total = calcularTotal(items);
  const localNombre = getLocalNombre(state, pedido);

  return `
    <div class="card card--raised">
      ${localNombre ? `<p class="card__meta">Local</p><h3>${localNombre}</h3>` : ""}
      <div class="cart-list">
        ${items
          .map((item) => `
            <article class="cart-item">
              <div class="card__heading-row">
                <h3>${item.nombre}</h3>
                ${
                  item.subtotal !== null && item.subtotal !== undefined
                    ? `<span class="price-pill">$${item.subtotal}</span>`
                    : ""
                }
              </div>
              ${item.opcionesSeleccionadas?.length ? `<p class="card__meta">Opciones: ${item.opcionesSeleccionadas.join(", ")}</p>` : ""}
              ${item.observacion || item.detalle ? `<p class="card__text">Observación: ${item.observacion || item.detalle}</p>` : ""}
            </article>
          `)
          .join("")}
      </div>
      ${total !== null ? `<div class="total-card"><p class="card__meta">Total</p><strong>$${total}</strong></div>` : ""}
    </div>
  `;
}

function renderResumen(pedido = {}, state = {}) {
  const formulario = pedido.formulario || {};

  if (pedido.tipo === "marketplace") {
    return renderTicketMarketplace(pedido, state);
  }

  if (pedido.tipo === "compra_libre") {
    const compra = formulario.compra_libre || {};
    return `
      ${datoResumen("Qué comprar", compra.queNecesita)}
      ${datoResumen("Dónde comprar", compra.donde)}
      ${datoResumen("Observaciones", compra.observaciones)}
    `;
  }

  if (pedido.tipo === "retirar_envio") {
    const retiro = formulario.retirar_envio || {};
    return `
      ${datoResumen("Retiro", retiro.direccionRetiro)}
      ${datoResumen("Entrega", retiro.direccionEntrega)}
      ${datoResumen("Qué retirar", retiro.queRetira)}
      ${datoResumen("Observaciones", retiro.observaciones)}
    `;
  }

  if (pedido.tipo === "pedir_repartidor") {
    const repartidor = formulario.repartidor || {};
    return `
      ${datoResumen("Salida", repartidor.puntoSalida)}
      ${datoResumen("Destino", repartidor.puntoDestino)}
      ${datoResumen("Detalle", repartidor.detalle)}
      ${datoResumen("Observaciones", repartidor.observaciones)}
    `;
  }

  return "";
}

export function confirmacion(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "confirmacion"
  });
  const data = getBlock(contract, "confirmacion")?.data || {};
  const pedido = data.pedido || {};
  const cliente = pedido.cliente || {};
  const direccion = cliente.direccion || {};
  const errores = data.errors || {};

  return `
    <main class="pantalla">
      <section class="hero flow-hero">
        <p class="eyebrow">Confirmá tu pedido</p>
        <h1>Revisá los datos antes de enviar.</h1>
      </section>

      <section class="local-section confirmation-section">
        <h2 class="section-title">Resumen</h2>
        ${renderResumen(pedido, state)}
        ${errores.tipo && !data.puedeEnviar ? `<small class="field-error">${errores.tipo}</small>` : ""}
      </section>

      <section class="card card--raised confirmation-form">
        <h2 class="section-title">Tus datos</h2>
        <div class="form-stack">
          <label class="field-label">Nombre</label>
          <input
            class="input-mobile ${errores.nombre ? "input-error" : ""}"
            type="text"
            placeholder="Nombre"
            data-cliente="nombre"
            value="${cliente.nombre || ""}"
          />
          ${errores.nombre ? `<small class="field-error">${errores.nombre}</small>` : ""}
        </div>

        <div class="form-grid">
          <div class="form-stack">
            <label class="field-label">Teléfono</label>
            <input
              class="input-mobile ${errores.telefono ? "input-error" : ""}"
              type="tel"
              inputmode="numeric"
              autocomplete="tel"
              placeholder="Teléfono"
              data-cliente="telefono"
              value="${cliente.telefono || ""}"
            />
            ${errores.telefono ? `<small class="field-error">${errores.telefono === "Ingresá un teléfono válido" && !cliente.telefono ? "Completá este dato" : errores.telefono}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">Calle</label>
            <input
              class="input-mobile ${errores.direccion ? "input-error" : ""}"
              type="text"
              placeholder="Calle"
              data-cliente="direccion-calle"
              value="${direccion.calle || ""}"
            />
          </div>

          <div class="form-stack">
            <label class="field-label">Número / Piso</label>
            <input
              class="input-mobile"
              type="text"
              placeholder="Número, piso"
              data-cliente="direccion-numero"
              value="${direccion.numero || ""}"
            />
          </div>

          <div class="form-stack">
            <label class="field-label">Referencia</label>
            <input
              class="input-mobile"
              type="text"
              placeholder="Referencia adicional"
              data-cliente="referencia"
              value="${cliente.referencia || ""}"
            />
          </div>
        </div>

        ${errores.direccion ? `<small class="field-error">${errores.direccion}</small>` : ""}

        <div class="card__actions flow-actions">
          <button class="btn btn-ghost" data-volver>Volver</button>
          <button class="btn btn-primary" data-enviar ${errores.tipo && !data.puedeEnviar ? "disabled" : ""}>
            Enviar pedido
          </button>
        </div>
      </section>
    </main>
  `;
}
