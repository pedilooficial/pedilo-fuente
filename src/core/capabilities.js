// src/core/capabilities.js

export const CAPABILITY_IDS = Object.freeze({
  MARKETPLACE: "marketplace",
  COMPRA_LIBRE: "compra_libre",
  RETIRAR_ENVIO: "retirar_envio",
  PEDIR_REPARTIDOR: "pedir_repartidor"
});

const BASE_CAPABILITIES = Object.freeze([
  Object.freeze({
    id: CAPABILITY_IDS.MARKETPLACE,
    kind: "marketplace",
    requiresProducts: true,
    requiresFormulario: false
  }),
  Object.freeze({
    id: CAPABILITY_IDS.COMPRA_LIBRE,
    kind: "pedido_directo",
    requiresProducts: false,
    requiresFormulario: true,
    formulario: "compra_libre"
  }),
  Object.freeze({
    id: CAPABILITY_IDS.RETIRAR_ENVIO,
    kind: "pedido_directo",
    requiresProducts: false,
    requiresFormulario: true,
    formulario: "retirar_envio"
  }),
  Object.freeze({
    id: CAPABILITY_IDS.PEDIR_REPARTIDOR,
    kind: "pedido_directo",
    requiresProducts: false,
    requiresFormulario: true,
    formulario: "repartidor",
    requiresLogistica: true
  })
]);

function capabilitiesFromConfig(appConfig = {}) {
  const configured = appConfig.capacidades || appConfig.capabilities;

  if (!Array.isArray(configured)) {
    return null;
  }

  const enabled = new Set(
    configured
      .map((item) => (typeof item === "string" ? item : item?.id))
      .filter(Boolean)
  );

  return BASE_CAPABILITIES.filter((capability) => enabled.has(capability.id));
}

export function getCoreCapabilities(state = {}) {
  const rawConfig = state.app_config || {};
  const appConfig = Array.isArray(rawConfig) ? rawConfig[0] || {} : rawConfig;
  const fromConfig = capabilitiesFromConfig(appConfig);

  return fromConfig || [...BASE_CAPABILITIES];
}

export function hasCapability(state = {}, capabilityId) {
  return getCoreCapabilities(state).some(
    (capability) => capability.id === capabilityId
  );
}
