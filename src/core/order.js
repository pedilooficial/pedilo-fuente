// src/core/order.js

import { canEnviarPedido } from "./guards.js";

export function buildPedidoEjecutable(state = {}) {
  const pedido = state.pedido || {};

  if (!canEnviarPedido(pedido)) {
    return {
      ok: false,
      errors: ["pedido_no_ejecutable"],
      pedido: null
    };
  }

  return {
    ok: true,
    errors: [],
    pedido: {
      ...pedido,
      __pedidoEjecutable: true
    }
  };
}
