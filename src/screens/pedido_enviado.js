// src/screens/pedido_enviado.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock, selectLocalActual } from "../core/selectors.js";

function getLocal(state = {}, pedido = {}) {
  return selectLocalActual({ ...state, pedido });
}

function renderLocalLogo(local) {
  const imagen = local?.imagen_url || local?.logo_url || "";

  return imagen
    ? `<img class="loading-logo" src="${imagen}" alt="${local.nombre || ""}" />`
    : "";
}

export function pedido_enviado(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "pedido_enviado"
  });
  const data = getBlock(contract, "pedido_enviado")?.data || {};
  const pedido = data.pedido || {};
  const local = getLocal(state, pedido);
  const nombreLocal = pedido.local_nombre || pedido.localNombre || local?.nombre || "";

  return `
    <main class="pantalla pantalla--centered">
      <section class="hero-card sent-card">
        ${renderLocalLogo(local)}
        <p class="eyebrow">¡Listo!</p>
        <h1>${nombreLocal ? `Tu pedido en ${nombreLocal} fue enviado` : "Tu pedido fue enviado"}</h1>
        ${pedido.numero ? `<p>Número ${pedido.numero}</p>` : ""}
        <p class="card__meta">Guardá este número para seguimiento</p>
        <p class="hero-copy">Te vamos a enviar un WhatsApp para confirmar los datos antes de iniciar el proceso.</p>
        <div class="card__actions flow-actions">
          <button class="btn btn-primary" data-seguir-comprando>Seguir comprando</button>
        </div>
      </section>
    </main>
  `;
}
