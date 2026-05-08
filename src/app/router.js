// src/app/router.js

import { updateState, getState } from "./state.js";

/*
  ROUTER CENTRAL
  El router NO decide experiencia.
  El router traduce estado → vista.
*/

const pasosPermitidos = new Set([
  "loading",
  "inicio",

  // marketplace
  "categorias",
  "subcategorias",
  "locales",
  "productos",
  "detalle_producto",
  "carrito",

  // flujos compartidos
  "confirmacion",
  "pedido_enviado",

  // tipos futuros
  "compra_libre",
  "retirar_envio",
  "pedir_repartidor"
]);

function validarPaso(paso) {
  return pasosPermitidos.has(paso);
}

/*
  Navegación controlada.
  El núcleo define estado operativo.
  El router solo sincroniza la vista.
*/
export function navegar(paso) {
  if (!validarPaso(paso)) {
    console.error("Paso inválido:", paso);
    return;
  }

  const current = getState();

  updateState({
    type: "NAVEGAR",
    pantalla: paso,
    flujo: current.pedido.tipo || "marketplace"
  });
}

/*
  Router inteligente.
  Traduce estado → vista correcta.
*/
export function resolverPantalla(state) {
  return state?.pantalla || "inicio";
}
