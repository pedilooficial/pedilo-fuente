// src/app/state.js

import { render } from "./render.js";

/*
  STATE — ÚNICO ORIGEN DE VERDAD
*/

function crearPedidoBase() {
  return {
    numero: null,
    estado: "creado",
    tipo: null,
    categoria: null,
    subcategoria: null,
    local: null,
    productos: [],
    productoActual: null,
    itemEnEdicion: null,
    salidaLocalPendiente: false,
    intentos: {
      continuar: false,
      envio: false
    },
    formulario: {
      compra_libre: {
        queNecesita: "",
        donde: "",
        observaciones: ""
      },
      retirar_envio: {
        direccionRetiro: "",
        direccionEntrega: "",
        queRetira: "",
        observaciones: ""
      },
      repartidor: {
        puntoSalida: "",
        puntoDestino: "",
        detalle: "",
        observaciones: ""
      }
    },
    cliente: {
      nombre: "",
      telefono: "",
      direccion: {
        calle: "",
        numero: "",
        piso: ""
      },
      referencia: ""
    }
  };
}

function crearState() {
  return Object.freeze({
    pantalla: "loading",
    app_config: {},
    categorias: [],
    subcategorias: [],
    locales: [],
    productos: [],
    opciones: [],
    ofertas: [],
    repartidores: [],
    pedido: crearPedidoBase()
  });
}

let state = crearState();

// ======================
// HELPERS
// ======================

function generarNumeroPedido() {
  return Date.now().toString().slice(-6);
}

function generarItemId() {
  return "item_" + Date.now() + "_" + Math.floor(Math.random() * 1000);
}

function mergeCliente(currentCliente, clienteUpdate) {
  if (!clienteUpdate || typeof clienteUpdate !== "object") {
    return currentCliente;
  }

  const next = { ...currentCliente };

  if (
    clienteUpdate.direccion &&
    typeof clienteUpdate.direccion === "object"
  ) {
    next.direccion = {
      ...currentCliente.direccion,
      ...clienteUpdate.direccion
    };
  }

  for (const key of Object.keys(clienteUpdate)) {
    if (key === "direccion") continue;
    next[key] = clienteUpdate[key];
  }

  return next;
}

function mergeFormulario(currentFormulario, formularioUpdate) {
  if (!formularioUpdate || typeof formularioUpdate !== "object") {
    return currentFormulario;
  }

  const next = { ...currentFormulario };

  for (const key of Object.keys(formularioUpdate)) {
    next[key] = {
      ...currentFormulario[key],
      ...formularioUpdate[key]
    };
  }

  return next;
}

function normalizarCantidad(cantidad) {
  const parsed = Number(cantidad);

  if (!Number.isFinite(parsed) || parsed < 1) {
    return 1;
  }

  return Math.floor(parsed);
}

function calcularSubtotal(precio, cantidad) {
  return precio !== null && precio !== undefined
    ? precio * cantidad
    : null;
}

function crearProductoActual(producto, overrides = {}) {
  if (!producto) return null;

  return {
    producto,
    cantidad: normalizarCantidad(overrides.cantidad ?? 1),
    observacion: overrides.observacion ?? "",
    opcionesSeleccionadas: Array.isArray(overrides.opcionesSeleccionadas)
      ? [...overrides.opcionesSeleccionadas]
      : [],
    intentoConfirmar: Boolean(overrides.intentoConfirmar)
  };
}

function buscarProducto(current, productoId) {
  return current.productos.find((producto) => producto.id === productoId) || null;
}

function opcionesRequeridasDeProducto(current, producto) {
  const localId = producto?.local_id || current.pedido.local;

  if (!localId) {
    return [];
  }

  return current.opciones.filter(
    (opcion) =>
      opcion.local_id === localId &&
      opcion.requerido === true &&
      opcion.activo !== false &&
      opcion.activa !== false
  );
}

function valorOpcion(opcion) {
  return opcion.nombre || opcion.id;
}

function faltanOpcionesRequeridas(current, producto, opcionesSeleccionadas = []) {
  const seleccionadas = new Set(opcionesSeleccionadas);

  return opcionesRequeridasDeProducto(current, producto).some(
    (opcion) => !seleccionadas.has(valorOpcion(opcion))
  );
}

// ======================
// REDUCER
// ======================

function reducer(current, action) {
  switch (action.type) {
    case "INIT_DATA":
      return {
        ...current,
        app_config: action.data.app_config || {},
        categorias: action.data.categorias || [],
        subcategorias: action.data.subcategorias || [],
        locales: action.data.locales || [],
        productos: action.data.productos || [],
        opciones: action.data.opciones || [],
        ofertas: action.data.ofertas || [],
        repartidores: action.data.repartidores || []
      };

    case "NAVEGAR":
      return {
        ...current,
        pantalla: action.pantalla
      };

    case "SELECCIONAR_CATEGORIA":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          categoria: action.categoria,
          subcategoria: null,
          local: null,
          productos: [],
          productoActual: null,
          itemEnEdicion: null,
          salidaLocalPendiente: false,
          intentos: {
            ...current.pedido.intentos,
            continuar: false
          }
        }
      };

    case "SELECCIONAR_SUBCATEGORIA":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          subcategoria: action.subcategoria,
          local: null,
          productos: [],
          productoActual: null,
          itemEnEdicion: null,
          salidaLocalPendiente: false,
          intentos: {
            ...current.pedido.intentos,
            continuar: false
          }
        }
      };

    case "SELECCIONAR_LOCAL":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          local: action.local,
          productos: [],
          productoActual: null,
          itemEnEdicion: null,
          salidaLocalPendiente: false,
          intentos: {
            ...current.pedido.intentos,
            continuar: false
          }
        }
      };

    case "SELECCIONAR_TIPO":
      return {
        ...current,
        pedido: {
          ...crearPedidoBase(),
          tipo: action.tipo,
          cliente: current.pedido.cliente
        }
      };

    case "SELECCIONAR_PRODUCTO": {
      const producto = action.producto || buscarProducto(current, action.productoId);

      if (!producto) {
        return current;
      }

      return {
        ...current,
        pantalla: "detalle_producto",
        pedido: {
          ...current.pedido,
          productoActual: crearProductoActual(producto),
          itemEnEdicion: null
        }
      };
    }

    case "CANCELAR_PRODUCTO":
      return {
        ...current,
        pantalla: current.pedido.itemEnEdicion ? "carrito" : "productos",
        pedido: {
          ...current.pedido,
          productoActual: null,
          itemEnEdicion: null
        }
      };

    case "CONFIRMAR_PRODUCTO": {
      const productoActual = current.pedido.productoActual;

      if (!productoActual?.producto) {
        return current;
      }

      const producto = productoActual.producto;
      const cantidad = 1;
      const precio = producto.precio ?? null;
      const observacion = action.observacion ?? productoActual.observacion ?? "";
      const opcionesSeleccionadas = Array.isArray(action.opcionesSeleccionadas)
        ? action.opcionesSeleccionadas
        : productoActual.opcionesSeleccionadas;

      if (faltanOpcionesRequeridas(current, producto, opcionesSeleccionadas)) {
        return {
          ...current,
          pantalla: "detalle_producto",
          pedido: {
            ...current.pedido,
            productoActual: {
              ...productoActual,
              cantidad,
              observacion,
              opcionesSeleccionadas: [...opcionesSeleccionadas],
              intentoConfirmar: true
            }
          }
        };
      }

      const nuevoItem = {
        itemId: generarItemId(),
        productoId: producto.id,
        localId: producto.local_id || current.pedido.local,
        nombre: producto.nombre,
        precio,
        cantidad,
        observacion,
        detalle: observacion,
        opcionesSeleccionadas: [...opcionesSeleccionadas],
        origenItem: producto.origenItem || action.origenItem || "producto",
        subtotal: calcularSubtotal(precio, cantidad)
      };

      return {
        ...current,
        pantalla: "productos",
        pedido: {
          ...current.pedido,
          numero: current.pedido.numero || generarNumeroPedido(),
          productos: [...current.pedido.productos, nuevoItem],
          productoActual: null,
          itemEnEdicion: null
        }
      };
    }

    case "ABRIR_SALIDA_LOCAL":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          salidaLocalPendiente: true
        }
      };

    case "CERRAR_SALIDA_LOCAL":
      return {
        ...current,
        pantalla: "productos",
        pedido: {
          ...current.pedido,
          salidaLocalPendiente: false
        }
      };

    case "VACIAR_Y_SALIR_LOCAL":
      return {
        ...current,
        pantalla: "locales",
        pedido: {
          ...current.pedido,
          productos: [],
          productoActual: null,
          itemEnEdicion: null,
          salidaLocalPendiente: false
        }
      };

    case "EDITAR_ITEM": {
      const item = current.pedido.productos.find(
        (productoItem) => productoItem.itemId === action.itemId
      );

      if (!item) {
        return current;
      }

      const productoOriginal = buscarProducto(current, item.productoId) || {
        id: item.productoId,
        local_id: item.localId || current.pedido.local,
        nombre: item.nombre,
        precio: item.precio
      };

      return {
        ...current,
        pantalla: "detalle_producto",
        pedido: {
          ...current.pedido,
          itemEnEdicion: item.itemId,
          productoActual: crearProductoActual(productoOriginal, {
            cantidad: 1,
            observacion: item.observacion ?? item.detalle ?? "",
            opcionesSeleccionadas: item.opcionesSeleccionadas || []
          })
        }
      };
    }

    case "ACTUALIZAR_ITEM": {
      const productoActual = current.pedido.productoActual;
      const itemId = current.pedido.itemEnEdicion || action.itemId;

      if (!productoActual?.producto || !itemId) {
        return current;
      }

      const cantidad = 1;
      const observacion = action.observacion ?? productoActual.observacion ?? "";
      const opcionesSeleccionadas = Array.isArray(action.opcionesSeleccionadas)
        ? action.opcionesSeleccionadas
        : productoActual.opcionesSeleccionadas;

      if (
        faltanOpcionesRequeridas(
          current,
          productoActual.producto,
          opcionesSeleccionadas
        )
      ) {
        return {
          ...current,
          pantalla: "detalle_producto",
          pedido: {
            ...current.pedido,
            productoActual: {
              ...productoActual,
              cantidad,
              observacion,
              opcionesSeleccionadas: [...opcionesSeleccionadas],
              intentoConfirmar: true
            }
          }
        };
      }

      return {
        ...current,
        pantalla: "carrito",
        pedido: {
          ...current.pedido,
          productos: current.pedido.productos.map((item) =>
            item.itemId === itemId
              ? {
                  ...item,
                  cantidad,
                  observacion,
                  detalle: observacion,
                  opcionesSeleccionadas: [...opcionesSeleccionadas],
                  subtotal: calcularSubtotal(item.precio, cantidad)
                }
              : item
          ),
          productoActual: null,
          itemEnEdicion: null
        }
      };
    }

    case "SET_FORMULARIO":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          formulario: mergeFormulario(current.pedido.formulario, action.formulario)
        }
      };

    case "MARCAR_INTENTO_CONTINUAR":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          intentos: {
            ...current.pedido.intentos,
            continuar: true
          }
        }
      };

    case "MARCAR_INTENTO_ENVIO":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          intentos: {
            ...current.pedido.intentos,
            envio: true
          }
        }
      };

    case "MARCAR_PEDIDO_ENVIADO":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          numero: current.pedido.numero || generarNumeroPedido(),
          estado: "enviado"
        }
      };

    case "AGREGAR_PRODUCTO": {
      console.error(
        "Acción prohibida: usá SELECCIONAR_PRODUCTO y CONFIRMAR_PRODUCTO"
      );
      return current;
    }

    case "INC_ITEM":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          productos: current.pedido.productos.map((item) =>
            item.itemId === action.itemId
              ? {
                  ...item,
                  cantidad: item.cantidad + 1,
                  subtotal: calcularSubtotal(item.precio, item.cantidad + 1)
                }
              : item
          )
        }
      };

    case "DEC_ITEM":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          productos: current.pedido.productos
            .map((item) =>
              item.itemId === action.itemId
                ? {
                    ...item,
                    cantidad: item.cantidad - 1,
                    subtotal: calcularSubtotal(item.precio, item.cantidad - 1)
                  }
                : item
            )
            .filter((item) => item.cantidad > 0)
        }
      };

    case "SET_DETALLE":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          productos: current.pedido.productos.map((item) =>
            item.itemId === action.itemId
              ? { ...item, detalle: action.detalle, observacion: action.detalle }
              : item
          )
        }
      };

    case "ELIMINAR_ITEM":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          productos: current.pedido.productos.filter(
            (item) => item.itemId !== action.itemId
          )
        }
      };

    case "SET_CLIENTE":
      return {
        ...current,
        pedido: {
          ...current.pedido,
          cliente: mergeCliente(current.pedido.cliente, action.cliente)
        }
      };

    case "RESET_PEDIDO":
      return {
        ...current,
        pedido: crearPedidoBase()
      };

    default:
      return current;
  }
}

// ======================
// UPDATE (FIX TESTS)
// ======================

export function updateState(action) {
  const next = reducer(state, action);
  state = Object.freeze(next);

  // 🔥 CLAVE: evitar render en tests
  if (typeof document !== "undefined") {
    render(state);
  }
}

// ======================
// GETTER
// ======================

export function getState() {
  return state;
}
