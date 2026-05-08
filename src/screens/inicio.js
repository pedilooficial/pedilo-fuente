// src/screens/inicio.js

import { buildScreenContract } from "../core/contracts.js";
import { getBlock } from "../core/selectors.js";

const CAPABILITY_VIEW = {
  marketplace: {
    className: "action-card action-card--primary action-card--entry",
    title: "Comprar en comercios",
    text: "Explorá categorías y locales disponibles.",
    buttonClass: "btn btn-primary",
    buttonText: "Ver categorías"
  },
  compra_libre: {
    className: "action-card",
    title: "Hacer una compra",
    text: "Decinos qué necesitás y dónde querés que lo busquemos.",
    buttonClass: "btn btn-secondary",
    buttonText: "Hacer una compra"
  },
  retirar_envio: {
    className: "action-card",
    title: "Retirar o enviar algo",
    text: "Un repartidor retira y entrega por vos.",
    buttonClass: "btn btn-secondary",
    buttonText: "Retirar o enviar"
  },
  pedir_repartidor: {
    className: "action-card",
    title: "Pedir un repartidor",
    text: "Para llevar algo de un punto a otro rápidamente.",
    buttonClass: "btn btn-secondary",
    buttonText: "Pedir repartidor"
  }
};

function getCapabilityActions(state) {
  const contract = buildScreenContract({
    ...state,
    pantalla: "inicio"
  });

  const capabilitiesBlock = getBlock(contract, "capabilities");

  const actions = [];

  for (const capability of capabilitiesBlock?.data?.capabilities || []) {
    const view = CAPABILITY_VIEW[capability.id];

    if (view) {
      actions.push({ capability, view });
    }
  }

  return actions;
}

function renderLogo(identity = {}) {
  return identity.imagen
    ? `<img class="loading-logo" src="${identity.imagen}" alt="${identity.nombre}" loading="eager" decoding="async" onerror="this.remove()" />`
    : "";
}

export function inicio(state) {
  const { pedido } = state;
  const capabilityActions = getCapabilityActions(state);
  const contract = buildScreenContract({
    ...state,
    pantalla: "inicio"
  });
  const identity = getBlock(contract, "identity")?.data || {};
  const isCompraLibre = pedido?.tipo === "compra_libre";
  const isRetirarEnvio = pedido?.tipo === "retirar_envio";
  const isRepartidor = pedido?.tipo === "pedir_repartidor";
  const mostrarErrores = Boolean(pedido?.intentos?.continuar);

  const errorCampo = (valor) =>
    mostrarErrores && !valor ? "Completá este dato" : "";

  const claseError = (error) => error ? " input-error" : "";

  if (isCompraLibre) {
    const form = pedido.formulario.compra_libre;
    const errorQueNecesita = errorCampo(form.queNecesita);
    const errorDonde = errorCampo(form.donde);

    return `
      <main class="pantalla">
        <section class="hero">
          <p class="eyebrow">Compra libre</p>
          <h1>Contanos qué necesitás comprar.</h1>
          <p class="hero-copy">Te ayudamos a encontrar lo que buscás.</p>
        </section>

        <div class="card card--raised form-block">
          <div class="form-stack">
            <label class="field-label">¿Qué necesitás comprar?</label>
            <input
              class="input-mobile${claseError(errorQueNecesita)}"
              type="text"
              placeholder="Por ejemplo, leche y pan"
              data-formulario="compra_libre.queNecesita"
              value="${form.queNecesita || ""}"
            />
            ${errorQueNecesita ? `<small class="field-error">${errorQueNecesita}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">¿Dónde querés que lo compremos?</label>
            <input
              class="input-mobile${claseError(errorDonde)}"
              type="text"
              placeholder="Por ejemplo, en el mismo barrio"
              data-formulario="compra_libre.donde"
              value="${form.donde || ""}"
            />
            ${errorDonde ? `<small class="field-error">${errorDonde}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">Observaciones</label>
            <textarea
              class="input-mobile"
              placeholder="Más detalles opcionales"
              data-formulario="compra_libre.observaciones"
            >${form.observaciones || ""}</textarea>
          </div>

          <div class="card__actions">
            <button class="btn btn-secondary" data-reset>Volver</button>
            <button class="btn btn-primary" data-continuar-formulario>Continuar</button>
          </div>
        </div>
      </main>
    `;
  }

  if (isRetirarEnvio) {
    const form = pedido.formulario.retirar_envio;
    const errorRetiro = errorCampo(form.direccionRetiro);
    const errorEntrega = errorCampo(form.direccionEntrega);
    const errorQueRetira = errorCampo(form.queRetira);

    return `
      <main class="pantalla">
        <section class="hero">
          <p class="eyebrow">Retirar o enviar</p>
          <h1>Contanos de dónde y adónde vamos.</h1>
          <p class="hero-copy">Un repartidor retira y entrega por vos.</p>
        </section>

        <div class="card card--raised form-block">
          <div class="form-stack">
            <label class="field-label">Dirección de retiro</label>
            <input
              class="input-mobile${claseError(errorRetiro)}"
              type="text"
              placeholder="Dirección de retiro"
              data-formulario="retirar_envio.direccionRetiro"
              value="${form.direccionRetiro || ""}"
            />
            ${errorRetiro ? `<small class="field-error">${errorRetiro}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">Dirección de entrega</label>
            <input
              class="input-mobile${claseError(errorEntrega)}"
              type="text"
              placeholder="Dirección de entrega"
              data-formulario="retirar_envio.direccionEntrega"
              value="${form.direccionEntrega || ""}"
            />
            ${errorEntrega ? `<small class="field-error">${errorEntrega}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">¿Qué hay que retirar?</label>
            <input
              class="input-mobile${claseError(errorQueRetira)}"
              type="text"
              placeholder="Descripción de lo que se retira"
              data-formulario="retirar_envio.queRetira"
              value="${form.queRetira || ""}"
            />
            ${errorQueRetira ? `<small class="field-error">${errorQueRetira}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">Observaciones</label>
            <textarea
              class="input-mobile"
              placeholder="Detalles adicionales"
              data-formulario="retirar_envio.observaciones"
            >${form.observaciones || ""}</textarea>
          </div>

          <div class="card__actions">
            <button class="btn btn-secondary" data-reset>Volver</button>
            <button class="btn btn-primary" data-continuar-formulario>Continuar</button>
          </div>
        </div>
      </main>
    `;
  }

  if (isRepartidor) {
    const form = pedido.formulario.repartidor;
    const errorSalida = errorCampo(form.puntoSalida);
    const errorDestino = errorCampo(form.puntoDestino);
    const errorDetalle = errorCampo(form.detalle);

    return `
      <main class="pantalla">
        <section class="hero">
          <p class="eyebrow">Pedir repartidor</p>
          <h1>Contanos el punto de salida y destino.</h1>
          <p class="hero-copy">Tu pedido va de un lugar a otro con un repartidor.</p>
        </section>

        <div class="card card--raised form-block">
          <div class="form-stack">
            <label class="field-label">Punto de salida</label>
            <input
              class="input-mobile${claseError(errorSalida)}"
              type="text"
              placeholder="Dirección de salida"
              data-formulario="repartidor.puntoSalida"
              value="${form.puntoSalida || ""}"
            />
            ${errorSalida ? `<small class="field-error">${errorSalida}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">Punto de destino</label>
            <input
              class="input-mobile${claseError(errorDestino)}"
              type="text"
              placeholder="Dirección de destino"
              data-formulario="repartidor.puntoDestino"
              value="${form.puntoDestino || ""}"
            />
            ${errorDestino ? `<small class="field-error">${errorDestino}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">Detalle del pedido</label>
            <input
              class="input-mobile${claseError(errorDetalle)}"
              type="text"
              placeholder="Qué debe llevar el repartidor"
              data-formulario="repartidor.detalle"
              value="${form.detalle || ""}"
            />
            ${errorDetalle ? `<small class="field-error">${errorDetalle}</small>` : ""}
          </div>

          <div class="form-stack">
            <label class="field-label">Observaciones</label>
            <textarea
              class="input-mobile"
              placeholder="Detalles adicionales"
              data-formulario="repartidor.observaciones"
            >${form.observaciones || ""}</textarea>
          </div>

          <div class="card__actions">
            <button class="btn btn-secondary" data-reset>Volver</button>
            <button class="btn btn-primary" data-continuar-formulario>Continuar</button>
          </div>
        </div>
      </main>
    `;
  }

  return `
    <main class="pantalla initial-flow">
      <section class="hero initial-hero">
        ${renderLogo(identity)}
        <p class="eyebrow">${identity.nombre}</p>
        <h1>${identity.descripcion}</h1>
        ${identity.subtitulo ? `<p class="hero-copy">${identity.subtitulo}</p>` : ""}
      </section>

      <section class="action-grid action-grid--entry">
        ${capabilityActions
          .map(({ capability, view }) => `
            <article class="${view.className}">
              <h2>${view.title}</h2>
              <p>${view.text}</p>
              <button class="${view.buttonClass}" data-tipo="${capability.id}"${capability.id === "marketplace" ? ` data-next="categorias"` : ""}>
                ${view.buttonText}
              </button>
            </article>
          `)
          .join("")}
      </section>

      <section class="section-note">
        Estas opciones te guían según lo que necesites hoy. Si querés comprar en comercios, elegí la primera opción.
      </section>
    </main>
  `;
}
