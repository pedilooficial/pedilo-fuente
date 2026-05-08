// src/screens/productos.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock } from "../core/selectors.js";

function mediaVisual(item) {
  const imagen = item.media?.src;
  const fallback = item.media?.fallback?.value || "?";

  return imagen
    ? `<img class="card__image" src="${imagen}" alt="${item.nombre || "Imagen"}" />`
    : `<div class="card__image card__image--placeholder visual-fallback" aria-label="Sin imagen">${fallback}</div>`;
}

function renderLocalHeader(data) {
  const local = data.local || {};
  let estado = "";

  for (const item of data.metadata || []) {
    if (item.badge) {
      estado = item.value;
      break;
    }
  }

  return `
    <section class="local-open" data-section="local-header">
      ${mediaVisual({ ...local, media: data.media })}
      <div class="local-open__content">
        ${estado ? `<span class="badge">${estado}</span>` : ""}
        <h1>${local.nombre || "Local seleccionado"}</h1>
        ${local.descripcion ? `<p class="hero-copy">${local.descripcion}</p>` : ""}
      </div>
    </section>
  `;
}

function renderSalidaLocal(visible) {
  return visible
    ? `
      <section class="exit-overlay" aria-modal="true" role="dialog">
        <div class="mini-screen exit-dialog">
          <p class="eyebrow">Salir del local</p>
          <h2>Tu pedido todavía tiene productos</h2>
          <p class="section-note">¿Querés seguir comprando o salir del local?</p>
          <div class="exit-actions">
            <button class="btn btn-secondary" type="button" data-cerrar-salida-local>Cancelar</button>
            <button class="btn btn-primary" type="button" data-ir-carrito>Ir a carrito</button>
            <button class="btn btn-ghost" type="button" data-vaciar-salir-local>Vaciar y salir</button>
          </div>
        </div>
      </section>
    `
    : "";
}

function renderLocalDatos(items) {
  const datos = [];

  for (const item of items || []) {
    datos.push(`
      <div class="fact-item">
        <span>${item.label}</span>
        <strong>${item.value}</strong>
      </div>
    `);
  }

  return datos.length > 0
    ? `
      <section class="local-section local-facts" data-section="local-datos">
        <div class="local-facts__grid">
          ${datos.join("")}
        </div>
      </section>
    `
    : "";
}

function renderOfertas(items) {
  if (!items?.length) return "";

  return `
    <section class="local-section" data-section="ofertas">
      <h2 class="section-title">Ofertas del local</h2>
      <div class="offer-list">
        ${items
          .map((oferta) => `
            <article class="banner banner--info offer-card">
              ${oferta.titulo || oferta.nombre ? `<strong>${oferta.titulo || oferta.nombre}</strong>` : ""}
              ${oferta.descripcion ? `<p>${oferta.descripcion}</p>` : ""}
              <div class="meta-row">
                ${oferta.descuento ? `<span>${oferta.descuento}</span>` : ""}
                ${oferta.precio !== null && oferta.precio !== undefined ? `<span>$${oferta.precio}</span>` : ""}
                ${oferta.precio_nuevo !== null && oferta.precio_nuevo !== undefined ? `<span>Nuevo precio: $${oferta.precio_nuevo}</span>` : ""}
              </div>
            </article>
          `)
          .join("")}
      </div>
    </section>
  `;
}

function renderOpciones(data) {
  const items = data.items || [];

  if (!items.length) return "";

  return `
    <section class="local-section" data-section="opciones">
      <h2 class="section-title">Opciones del local</h2>
      <div class="option-list">
        ${items
          .map((opcion) => {
            const precioExtra = opcion.precio_extra ?? opcion.precio;

            return `
              <article class="option-pill">
                ${opcion.nombre ? `<strong>${opcion.nombre}</strong>` : ""}
                ${opcion.descripcion ? `<span>${opcion.descripcion}</span>` : ""}
                ${precioExtra !== null && precioExtra !== undefined ? `<span>Extra: $${precioExtra}</span>` : ""}
                ${opcion.requerido !== null && opcion.requerido !== undefined ? `<span>${opcion.requerido ? "Requerido" : "Opcional"}</span>` : ""}
              </article>
            `;
          })
          .join("")}
      </div>
    </section>
  `;
}

function renderProductoMetadata(producto) {
  const parts = [];

  for (const item of producto.metadata || []) {
    if (item.type === "badge") {
      parts.push(`<span class="badge">${item.value}</span>`);
    } else {
      parts.push(`<p class="card__meta">${item.label}: ${item.value}</p>`);
    }
  }

  return parts.join("");
}

function renderProductos(items) {
  return `
    <section class="local-section" data-section="productos">
      <h2 class="section-title">Productos</h2>
      <div class="grid grid--products">
        ${
          items.length === 0
            ? `<div class="card"><p>No hay productos disponibles en este local.</p></div>`
            : items
                .map((producto) => `
                  <article class="card card--raised card--clickable product-card" data-producto="${producto.id}">
                    ${mediaVisual(producto)}
                    <div class="product-card__body">
                      <h3>${producto.nombre}</h3>
                      ${producto.descripcion ? `<p class="card__text">${producto.descripcion}</p>` : ""}
                      <div class="product-card__badges">
                        ${renderProductoMetadata(producto)}
                        ${producto.oferta && !producto.ofertas?.length ? `<span class="badge">Promos disponibles</span>` : ""}
                      </div>
                      <div class="product-card__meta">
                        ${
                          producto.precio !== null && producto.precio !== undefined
                            ? `<p class="precio">$${producto.precio}</p>`
                            : `<p class="card__meta">Precio a consultar</p>`
                        }
                      </div>
                      ${producto.requiere_detalle ? `<p class="hint">Podés aclarar gustos o detalles después.</p>` : ""}
                    </div>
                    <button class="btn btn-primary btn-full" type="button">
                      Agregar
                    </button>
                  </article>
                `)
                .join("")
        }
      </div>
    </section>
  `;
}

export function screenProductos(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "productos"
  });

  if (contract.guards.length > 0) {
    return `
      <main class="pantalla">
        <h2>¿Qué querés pedir?</h2>
        <p class="section-note">Primero elegí un comercio.</p>
        <button class="btn btn-secondary" data-nav="locales">Volver</button>
      </main>
    `;
  }

  const header = getBlock(contract, "local_header")?.data || {};
  const salida = getBlock(contract, "salida_local");
  const datos = getBlock(contract, "local_datos")?.data?.items || [];
  const ofertas = getBlock(contract, "ofertas")?.data?.items || [];
  const opciones = getBlock(contract, "opciones")?.data || {};
  const productos = getBlock(contract, "productos")?.data?.items || [];
  const cart = getBlock(contract, "pedido_cta")?.data?.cart || {};

  return `
    <main class="pantalla">
      ${renderLocalHeader(header)}
      ${renderSalidaLocal(Boolean(salida?.visible))}
      ${renderLocalDatos(datos)}
      ${renderOfertas(ofertas)}
      ${renderOpciones(opciones)}
      ${renderProductos(productos)}

      <div class="pedido-cta card__actions" data-section="pedido-cta">
        <button class="btn btn-secondary" data-nav="locales">Volver</button>
        <button class="btn btn-primary btn-cart" data-ir-carrito>
          ${cart.count > 0 ? `Ver pedido (${cart.count})` : "Ver pedido"}
          ${cart.total !== null && cart.total !== undefined ? `<span>Total estimado: $${cart.total}</span>` : ""}
        </button>
      </div>
    </main>
  `;
}
