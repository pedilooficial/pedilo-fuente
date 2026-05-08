// src/core/guards.js

import { CAPABILITY_IDS } from "./capabilities.js";
import { selectOpcionesForProducto } from "./selectors.js";

function compactAddress(direccion = {}) {
  return [direccion.calle, direccion.numero, direccion.piso]
    .filter(Boolean)
    .join(" ");
}

export function getConfirmacionErrors(pedido = {}) {
  if (!pedido.intentos?.envio || canEnviarPedido(pedido)) {
    return {
      nombre: "",
      telefono: "",
      direccion: "",
      tipo: ""
    };
  }

  const cliente = pedido.cliente || {};
  const direccion = cliente.direccion || {};
  const addressText = compactAddress(direccion);
  const telefono = cliente.telefono || "";
  const digitos = telefono.replace(/\D/g, "");

  return {
    nombre: !cliente.nombre || cliente.nombre.trim().length < 2 ? "Completá este dato" : "",
    telefono:
      !telefono ||
      !/^\+?\d+$/.test(telefono) ||
      digitos.length < 10 ||
      digitos.length > 13 ||
      /^(\d)\1+$/.test(digitos)
        ? "Ingresá un teléfono válido"
        : "",
    direccion: addressText.trim().length < 3 ? "Completá este dato" : "",
    tipo: "Revisá el resumen antes de enviar."
  };
}

export function isClienteValido(cliente = {}) {
  const telefono = cliente.telefono || "";
  const digitos = telefono.replace(/\D/g, "");

  return Boolean(
    cliente.nombre &&
      cliente.nombre.trim().length >= 2 &&
      /^\+?\d+$/.test(telefono) &&
      digitos.length >= 10 &&
      digitos.length <= 13 &&
      !/^(\d)\1+$/.test(digitos) &&
      compactAddress(cliente.direccion).trim().length >= 3
  );
}

export function isFormularioValido(pedido = {}) {
  const formulario = pedido.formulario || {};

  if (pedido.tipo === CAPABILITY_IDS.COMPRA_LIBRE) {
    const compra = formulario.compra_libre || {};
    return Boolean(compra.queNecesita && compra.donde);
  }

  if (pedido.tipo === CAPABILITY_IDS.RETIRAR_ENVIO) {
    const retiro = formulario.retirar_envio || {};
    return Boolean(
      retiro.direccionRetiro &&
        retiro.direccionEntrega &&
        retiro.queRetira
    );
  }

  if (pedido.tipo === CAPABILITY_IDS.PEDIR_REPARTIDOR) {
    const repartidor = formulario.repartidor || {};
    return Boolean(
      repartidor.puntoSalida &&
        repartidor.puntoDestino &&
        repartidor.detalle
    );
  }

  return true;
}

export function canGoToConfirmacion(pedido = {}) {
  if (pedido.tipo === CAPABILITY_IDS.MARKETPLACE) {
    return Boolean(pedido.local && (pedido.productos || []).length > 0);
  }

  if (
    pedido.tipo === CAPABILITY_IDS.COMPRA_LIBRE ||
    pedido.tipo === CAPABILITY_IDS.RETIRAR_ENVIO ||
    pedido.tipo === CAPABILITY_IDS.PEDIR_REPARTIDOR
  ) {
    return isFormularioValido(pedido);
  }

  return false;
}

export function canEnviarPedido(pedido = {}) {
  if (!isClienteValido(pedido.cliente || {})) {
    return false;
  }

  if (pedido.tipo === CAPABILITY_IDS.MARKETPLACE) {
    return (pedido.productos || []).length > 0;
  }

  return isFormularioValido(pedido);
}

export function faltanOpcionesRequeridas(
  state = {},
  producto,
  opcionesSeleccionadas = []
) {
  const seleccionadas = new Set(opcionesSeleccionadas);

  return selectOpcionesForProducto(state, producto).some((opcion) => {
    const valor = opcion.nombre || opcion.id;
    return opcion.requerido === true && !seleccionadas.has(valor);
  });
}
