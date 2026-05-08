// src/screens/subcategorias.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock } from "../core/selectors.js";

function mediaVisual(item) {
  const imagen = item.media?.src;
  const fallback = item.media?.fallback?.value || "?";

  return imagen
    ? `<img class="card__image" src="${imagen}" alt="${item.nombre}" />`
    : `<div class="card__image card__image--placeholder">${fallback}</div>`;
}

export function subcategorias(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "subcategorias"
  });
  const lista = getBlock(contract, "subcategorias")?.data?.items || [];

  if (contract.guards.length > 0) {
    return `
      <main class="pantalla">
        <h2>Primero elegí una categoría.</h2>
        <button class="btn btn-secondary" data-nav="categorias">Volver a categorías</button>
      </main>
    `;
  }

  return `
    <main class="pantalla initial-flow">
      <section class="hero initial-hero">
        <p class="eyebrow">¿Qué estás buscando?</p>
        <h1>Elegí la categoría que mejor describe tu pedido.</h1>
      </section>

      <section class="grid grid--cats initial-grid">
        ${
          lista.length === 0
            ? `<div class="card"><p>No hay opciones disponibles por ahora.</p></div>`
            : lista
                .map((s) => `
                  <article class="card card--raised">
                    ${mediaVisual(s)}
                    <h3>${s.nombre}</h3>
                    ${s.descripcion ? `<p class="card__text">${s.descripcion}</p>` : ""}
                    ${s.destacado ? `<span class="badge">Destacado</span>` : ""}
                    <button class="btn btn-primary" data-subcategoria="${s.id}">
                      Ver locales
                    </button>
                  </article>
                `)
                .join("")
        }
      </section>

      <button class="btn btn-ghost" data-nav="categorias">Volver a categorías</button>
    </main>
  `;
}
