// src/screens/locales.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock } from "../core/selectors.js";

function mediaVisual(item) {
  const imagen = item.media?.src;
  const fallback = item.media?.fallback?.value || "?";

  return imagen
    ? `<img class="card__image" src="${imagen}" alt="${item.nombre || "Imagen"}" />`
    : `<div class="card__image card__image--placeholder visual-fallback" aria-label="Sin imagen">${fallback}</div>`;
}

function renderHero(categoriaNombre) {
  return `
    <section class="hero">
      <p class="eyebrow">¿Dónde querés comprar?</p>
      <h1>Elegí el comercio que más te guste.</h1>
      ${categoriaNombre ? `<p class="hero-copy">${categoriaNombre}</p>` : ""}
    </section>
  `;
}

function renderOfertaLocal(local) {
  if (local.ofertas?.length > 0) {
    return local.ofertas
      .map((oferta) => `
        <div class="banner">
          ${oferta.titulo || oferta.nombre ? `<strong>${oferta.titulo || oferta.nombre}</strong>` : ""}
          ${oferta.descripcion ? `<p>${oferta.descripcion}</p>` : ""}
          ${oferta.descuento ? `<p>${oferta.descuento}</p>` : ""}
          ${oferta.precio !== null && oferta.precio !== undefined ? `<p>$${oferta.precio}</p>` : ""}
        </div>
      `)
      .join("");
  }

  return local.oferta
    ? `<span class="badge">Oferta</span><p class="card__meta">${local.oferta}</p>`
    : "";
}

function renderMetadataLocal(local) {
  const estado = [];
  const datos = [];

  for (const item of local.metadata || []) {
    if (item.badge) {
      estado.push(`<span class="badge">${item.value}</span>`);
    } else if (item.label === "Dirección") {
      datos.push(`<p class="card__meta">${item.value}</p>`);
    } else if (item.label === "Teléfono") {
      datos.push(`<p class="card__meta">Tel: ${item.value}</p>`);
    } else {
      datos.push(`<p class="card__meta">${item.label}: ${item.value}</p>`);
    }
  }

  return {
    estado: estado.join(""),
    metadata: datos.length > 0 ? `<div class="card__meta-group">${datos.join("")}</div>` : ""
  };
}

function renderLocales(lista) {
  return `
    <section class="grid grid--locals">
      ${
        lista.length === 0
          ? `<div class="card"><p>No hay comercios disponibles en esta categoría.</p></div>`
          : lista
              .map((local) => {
                const { estado, metadata } = renderMetadataLocal(local);

                return `
                  <article class="card card--raised">
                    ${mediaVisual(local)}
                    <div>
                      <h3>${local.nombre}</h3>
                      ${local.descripcion ? `<p class="card__text">${local.descripcion}</p>` : ""}
                      ${estado}
                      ${renderOfertaLocal(local)}
                      ${metadata}
                    </div>
                    <button class="btn btn-primary" data-local="${local.id}">
                      Ver productos
                    </button>
                  </article>
                `;
              })
              .join("")
      }
    </section>
  `;
}

export function locales(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "locales"
  });
  const hero = getBlock(contract, "locales_hero")?.data || {};
  const lista = getBlock(contract, "locales")?.data?.items || [];

  if (contract.guards.length > 0) {
    return `
      <main class="pantalla">
        <h2>¿Dónde querés comprar?</h2>
        <p class="section-note">Primero elegí una categoría.</p>
        <button class="btn btn-secondary" data-nav="subcategorias">Volver</button>
      </main>
    `;
  }

  return `
    <main class="pantalla">
      ${renderHero(hero.categoriaNombre)}
      ${renderLocales(lista)}

      <button class="btn btn-ghost" data-nav="subcategorias">Volver</button>
    </main>
  `;
}
