// src/screens/detalle_producto.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock } from "../core/selectors.js";

function renderMedia(producto) {
  const imagen = producto.media?.src;
  const fallback = producto.media?.fallback?.value || "?";

  return imagen
    ? `<img class="card__image" src="${imagen}" alt="${producto.nombre}" />`
    : `<div class="card__image card__image--placeholder visual-fallback" aria-label="Sin imagen">${fallback}</div>`;
}

function renderMetadata(producto) {
  return (producto.metadata || [])
    .map((item) =>
      item.type === "badge"
        ? `<span class="badge">${item.value}</span>`
        : `<p class="card__meta">${item.label}: ${item.value}</p>`
    )
    .join("");
}

function renderOfertas(producto) {
  return producto.ofertas?.length
    ? `
      <section class="detail-options">
        <h2 class="section-title">Ofertas</h2>
        ${producto.ofertas
          .map((oferta) => `
            <div class="banner">
              ${oferta.titulo || oferta.nombre ? `<strong>${oferta.titulo || oferta.nombre}</strong>` : ""}
              ${oferta.descripcion ? `<p>${oferta.descripcion}</p>` : ""}
              ${oferta.descuento ? `<p>${oferta.descuento}</p>` : ""}
              ${oferta.precio !== null && oferta.precio !== undefined ? `<p>$${oferta.precio}</p>` : ""}
              ${oferta.precio_nuevo !== null && oferta.precio_nuevo !== undefined ? `<p>Nuevo precio: $${oferta.precio_nuevo}</p>` : ""}
            </div>
          `)
          .join("")}
      </section>
    `
    : "";
}

function renderOpciones(producto, productoActual, faltanRequeridas) {
  const opciones = producto.opciones || [];
  const opcionesSeleccionadas = productoActual.opcionesSeleccionadas || [];
  const mostrarErrorOpciones = Boolean(productoActual.intentoConfirmar);

  if (!opciones.length) return "";

  return `
    <section class="detail-options">
      <h2 class="section-title">Opciones</h2>
      ${opciones
        .map((opcion) => {
          const valor = opcion.nombre || opcion.id;
          const checked = opcionesSeleccionadas.includes(valor) ? "checked" : "";
          const precioExtra = opcion.precio_extra ?? opcion.precio;

          return `
            <label class="option-row">
              <input
                type="checkbox"
                value="${valor}"
                data-producto-opcion
                ${checked}
              />
              <span>
                ${opcion.nombre ? `<strong>${opcion.nombre}</strong>` : ""}
                ${opcion.descripcion ? `<small>${opcion.descripcion}</small>` : ""}
                ${precioExtra !== null && precioExtra !== undefined ? `<small>Extra: $${precioExtra}</small>` : ""}
                ${opcion.requerido ? `<small>Requerido</small>` : ""}
              </span>
            </label>
          `;
        })
        .join("")}
      ${mostrarErrorOpciones && faltanRequeridas ? `<small class="field-error">Elegí las opciones requeridas</small>` : ""}
    </section>
  `;
}

export function detalleProducto(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "detalle_producto"
  });
  const data = getBlock(contract, "producto_detalle")?.data;

  if (!data?.producto) {
    return `
      <main class="pantalla">
        <h2>Producto no seleccionado</h2>
        <button class="btn btn-secondary" data-nav="productos">Volver</button>
      </main>
    `;
  }

  const { producto, productoActual, editando, faltanRequeridas } = data;

  return `
    <main class="pantalla">
      <section class="hero">
        <p class="eyebrow">${editando ? "Editar producto" : "Detalle del producto"}</p>
        <h1>${producto.nombre}</h1>
      </section>

      <article class="card card--raised">
        ${renderMedia(producto)}
        ${producto.descripcion ? `<p class="card__text">${producto.descripcion}</p>` : ""}
        ${
          producto.precio !== null && producto.precio !== undefined
            ? `<p class="precio">$${producto.precio}</p>`
            : `<p class="card__meta">Precio a consultar</p>`
        }
        ${renderMetadata(producto)}
        ${renderOfertas(producto)}
        ${renderOpciones(producto, productoActual, faltanRequeridas)}

        <div class="form-stack">
          <label class="field-label">Observación opcional</label>
          <textarea
            class="input-mobile"
            placeholder="Aclaraciones del producto"
            data-producto-observacion
          >${productoActual.observacion || ""}</textarea>
        </div>

        <div class="card__actions">
          <button class="btn btn-primary" data-confirmar-producto>
            ${editando ? "Guardar cambios" : "Agregar al pedido"}
          </button>
          <button class="btn btn-ghost" data-cancelar-producto>
            Cancelar
          </button>
        </div>
      </article>
    </main>
  `;
}
