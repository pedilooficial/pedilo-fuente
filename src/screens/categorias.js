// src/screens/categorias.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock } from "../core/selectors.js";

function mediaVisual(item) {
  const imagen = item.media?.src;
  const fallback = item.media?.fallback?.value || "?";

  return imagen
    ? `<img class="card__image" src="${imagen}" alt="${item.nombre}" />`
    : `<div class="card__image card__image--placeholder">${fallback}</div>`;
}

export function categorias(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "categorias"
  });
  const lista = getBlock(contract, "categorias")?.data?.items || [];

  return `
    <main class="pantalla initial-flow">
      <section class="hero initial-hero">
        <p class="eyebrow">Comprar en comercios</p>
        <h1>Elegí la categoría que querés explorar.</h1>
      </section>

      <section class="grid grid--cats initial-grid">
        ${
          lista.length === 0
            ? `<div class="card"><p>No hay categorías disponibles por ahora.</p></div>`
            : lista
                .map((cat) => `
                  <article class="card card--raised">
                    ${mediaVisual(cat)}
                    <h3>${cat.nombre}</h3>
                    ${cat.descripcion ? `<p class="card__text">${cat.descripcion}</p>` : ""}
                    ${cat.destacado ? `<span class="badge">Destacado</span>` : ""}
                    <button class="btn btn-primary" data-categoria="${cat.id}">
                      Ver subcategorías
                    </button>
                  </article>
                `)
                .join("")
        }
      </section>

      <button class="btn btn-ghost" data-nav="inicio">Volver al inicio</button>
    </main>
  `;
}
