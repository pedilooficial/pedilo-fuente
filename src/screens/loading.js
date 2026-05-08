// src/screens/loading.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock } from "../core/selectors.js";

function getAppConfig(identity = {}) {
  const config = identity.app_config;

  return config && typeof config === "object" ? config : {};
}

function renderLogo(config = {}, nombre = "") {
  const imagen = config.imagen_url || config.logo_url || "";
  const fallback = (nombre || "Pédilo").charAt(0);

  return imagen
    ? `
      <span class="loading-logo-mark">
        <img
          class="loading-logo loading-logo--splash"
          src="${imagen}"
          alt=""
          loading="eager"
          decoding="async"
          onerror="this.hidden=true;this.nextElementSibling.hidden=false"
        />
        <span class="loading-logo-fallback" hidden>${fallback}</span>
      </span>
    `
    : `<span class="loading-logo-mark"><span class="loading-logo-fallback">${fallback}</span></span>`;
}

export function loading(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "loading"
  });
  const identity = getBlock(contract, "loading_identity")?.data || {};
  const config = getAppConfig(identity);
  const nombre = config.nombre_negocio || config.nombre || "Pédilo";
  const subtitulo = config.subtitulo || "";

  return `
    <main class="pantalla pantalla--centered splash-screen">
      <section class="hero-card loading-card loading-card--splash">
        ${renderLogo(config, nombre)}
        ${subtitulo ? `<p class="loading-subtitle">${subtitulo}</p>` : ""}
      </section>
    </main>
  `;
}
