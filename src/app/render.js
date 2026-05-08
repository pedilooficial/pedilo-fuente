// src/app/render.js

import { getState, updateState } from "./state.js";
import { resolverPantalla, navegar } from "./router.js";

import { inicio } from "../screens/inicio.js";
import { categorias } from "../screens/categorias.js";
import { subcategorias } from "../screens/subcategorias.js";
import { locales } from "../screens/locales.js";
import { screenProductos } from "../screens/productos.js";
import { detalleProducto } from "../screens/detalle_producto.js";
import { screenCarrito } from "../screens/carrito.js";
import { confirmacion } from "../screens/confirmacion.js";
import { pedido_enviado } from "../screens/pedido_enviado.js";
import { loading } from "../screens/loading.js";

import { canGoToConfirmacion } from "../core/guards.js";
import { buildPedidoEjecutable } from "../core/order.js";
import { enviarPedidoPorWhatsApp } from "../services/whatsapp.js";

/*
  ==========================================
  RENDER CENTRAL — UI PURA
  ==========================================

  REGLAS:
  - render NO decide flujo
  - render NO decide negocio
  - render NO decide validaciones globales
  - render SOLO:
    - pinta
    - captura eventos
    - despacha acciones
*/

const screens = {
  loading,
  inicio,
  categorias,
  subcategorias,
  locales,
  productos: screenProductos,
  detalle_producto: detalleProducto,
  carrito: screenCarrito,
  confirmacion,
  pedido_enviado
};

// ==========================================
// HELPERS
// ==========================================

function screenNotFound(name) {
  return `
    <div class="pantalla">
      <div class="card--raised">
        <h2>Pantalla no encontrada</h2>
        <p>${name}</p>
      </div>
    </div>
  `;
}

function getScreenHtml(screenName, state) {
  const screen = screens[screenName];

  if (!screen) {
    return screenNotFound(screenName);
  }

  return screen(state);
}

function leerClienteDesdeFormulario() {
  const cliente = {};

  document.querySelectorAll("[data-cliente]").forEach((input) => {
    const key = input.dataset.cliente;

    if (key.startsWith("direccion-")) {
      cliente.direccion = {
        ...(cliente.direccion || {}),
        [key.replace("direccion-", "")]: input.value
      };

      return;
    }

    cliente[key] = input.value;
  });

  return cliente;
}

function leerFormularioDesdePantalla() {
  const formulario = {};

  document.querySelectorAll("[data-formulario]").forEach((input) => {
    const [section, field] = input.dataset.formulario.split(".");

    if (!section || !field) return;

    if (!formulario[section]) {
      formulario[section] = {};
    }

    formulario[section][field] = input.value;
  });

  return formulario;
}

// ==========================================
// RENDER
// ==========================================

export function render() {
  try {
    const state = getState();

    const root = document.getElementById("contenido");

    if (!root) {
      return;
    }

    const pantalla = resolverPantalla(state);

    root.innerHTML = getScreenHtml(pantalla, state);
  } catch (error) {
    console.error("Render error:", error);

    const root = document.getElementById("contenido");

    if (root) {
      root.innerHTML = `
        <div class="pantalla">
          <div class="card--raised">
            <h2>Error crítico</h2>
            <p>La app no pudo renderizar.</p>
          </div>
        </div>
      `;
    }
  }
}

// ==========================================
// EVENTOS GLOBALES
// ==========================================

export function bindGlobalEvents() {
  document.addEventListener("click", (e) => {
    const target = e.target;

    const state = getState();

    // ======================================
    // NAV
    // ======================================

    const nav = target.closest("[data-nav]");

    if (nav) {
      if (
        nav.dataset.nav === "locales" &&
        state.pantalla === "productos" &&
        (state.pedido.productos || []).length > 0
      ) {
        updateState({ type: "ABRIR_SALIDA_LOCAL" });
        return;
      }

      navegar(nav.dataset.nav);
      return;
    }

    // ======================================
    // TIPO
    // ======================================

    const tipo = target.closest("[data-tipo]");

    if (tipo) {
      if (
        state.pantalla === "pedido_enviado" &&
        tipo.dataset.tipo === "marketplace"
      ) {
        updateState({ type: "RESET_PEDIDO" });
      }

      updateState({
        type: "SELECCIONAR_TIPO",
        tipo: tipo.dataset.tipo
      });

      navegar(tipo.dataset.next || "inicio");
      return;
    }

    // ======================================
    // CATEGORIA
    // ======================================

    const categoria = target.closest("[data-categoria]");

    if (categoria) {
      updateState({
        type: "SELECCIONAR_CATEGORIA",
        categoria: categoria.dataset.categoria
      });

      navegar("subcategorias");
      return;
    }

    // ======================================
    // SUBCATEGORIA
    // ======================================

    const subcategoria = target.closest("[data-subcategoria]");

    if (subcategoria) {
      updateState({
        type: "SELECCIONAR_SUBCATEGORIA",
        subcategoria: subcategoria.dataset.subcategoria
      });

      navegar("locales");
      return;
    }

    // ======================================
    // LOCAL
    // ======================================

    const local = target.closest("[data-local]");

    if (local) {
      updateState({
        type: "SELECCIONAR_LOCAL",
        local: local.dataset.local
      });

      navegar("productos");
      return;
    }

    // ======================================
    // PRODUCTO
    // ======================================

    const producto = target.closest("[data-producto]");
    const add = target.closest("[data-add]");

    if (producto || add) {
      updateState({
        type: "SELECCIONAR_PRODUCTO",
        productoId:
          producto?.dataset.producto ||
          add?.dataset.producto ||
          add?.dataset.id
      });

      render();
      return;
    }

    // ======================================
    // CONFIRMAR PRODUCTO
    // ======================================

    const confirmarProducto = target.closest(
      "[data-confirmar-producto]"
    );

    if (confirmarProducto) {
      const observacion =
        document.querySelector("[data-producto-observacion]")?.value || "";

      const opcionesSeleccionadas = Array.from(
        document.querySelectorAll("[data-producto-opcion]:checked")
      ).map((o) => o.value);

      updateState({
        type: state.pedido.itemEnEdicion
          ? "ACTUALIZAR_ITEM"
          : "CONFIRMAR_PRODUCTO",

        observacion,
        opcionesSeleccionadas
      });

      render();
      return;
    }

    // ======================================
    // CANCELAR PRODUCTO
    // ======================================

    const cancelarProducto = target.closest(
      "[data-cancelar-producto]"
    );

    if (cancelarProducto) {
      updateState({
        type: "CANCELAR_PRODUCTO"
      });

      render();
      return;
    }

    // ======================================
    // ITEMS
    // ======================================

    const inc = target.closest("[data-inc]");

    if (inc) {
      updateState({
        type: "EDITAR_ITEM",
        itemId: inc.dataset.inc
      });

      render();
      return;
    }

    const dec = target.closest("[data-dec]");

    if (dec) {
      updateState({
        type: "DEC_ITEM",
        itemId: dec.dataset.dec
      });

      render();
      return;
    }

    const remove = target.closest("[data-remove]");

    if (remove) {
      updateState({
        type: "ELIMINAR_ITEM",
        itemId: remove.dataset.remove
      });

      render();
      return;
    }

    const editar = target.closest("[data-editar-item]");

    if (editar) {
      updateState({
        type: "EDITAR_ITEM",
        itemId: editar.dataset.editarItem
      });

      render();
      return;
    }

    const cerrarSalidaLocal = target.closest("[data-cerrar-salida-local]");

    if (cerrarSalidaLocal) {
      updateState({ type: "CERRAR_SALIDA_LOCAL" });
      return;
    }

    const vaciarSalirLocal = target.closest("[data-vaciar-salir-local]");

    if (vaciarSalirLocal) {
      updateState({ type: "VACIAR_Y_SALIR_LOCAL" });
      return;
    }

    const irCarrito = target.closest("[data-ir-carrito]");

    if (irCarrito) {
      if (getState().pedido.salidaLocalPendiente) {
        updateState({ type: "CERRAR_SALIDA_LOCAL" });
      }

      navegar("carrito");
      return;
    }

    const seguirComprando = target.closest("[data-seguir-comprando]");

    if (seguirComprando) {
      const categoriaActual = state.pedido.categoria;

      updateState({ type: "RESET_PEDIDO" });
      updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });

      if (categoriaActual) {
        updateState({
          type: "SELECCIONAR_CATEGORIA",
          categoria: categoriaActual
        });
      }

      navegar("subcategorias");
      return;
    }

    // ======================================
    // FORMULARIO
    // ======================================

    const continuarFormulario = target.closest(
      "[data-continuar-formulario]"
    );

    if (continuarFormulario) {
      const formulario = leerFormularioDesdePantalla();

      updateState({
        type: "SET_FORMULARIO",
        formulario
      });

      updateState({ type: "MARCAR_INTENTO_CONTINUAR" });

      if (!canGoToConfirmacion(getState().pedido)) {
        return;
      }

      navegar("carrito");
      return;
    }

    // ======================================
    // CONFIRMACION
    // ======================================

    const irConfirmacion = target.closest(
      "[data-ir-confirmacion]"
    );

    if (irConfirmacion) {
      if (!canGoToConfirmacion(getState().pedido)) {
        return;
      }

      navegar("confirmacion");
      return;
    }

    const volver = target.closest("[data-volver]");

    if (volver) {
      navegar("carrito");
      return;
    }

    // ======================================
    // ENVIO
    // ======================================

    const enviar = target.closest("[data-enviar]");

    if (enviar) {
      updateState({
        type: "SET_CLIENTE",
        cliente: leerClienteDesdeFormulario()
      });

      updateState({ type: "MARCAR_INTENTO_ENVIO" });

      const executable = buildPedidoEjecutable(getState());

      if (!executable.ok) {
        return;
      }

      updateState({ type: "MARCAR_PEDIDO_ENVIADO" });
      enviarPedidoPorWhatsApp(executable.pedido);
      navegar("pedido_enviado");


      return;
    }

    // ======================================
    // RESET
    // ======================================

    const reset = target.closest("[data-reset]");

    if (reset) {
      updateState({
        type: "RESET_PEDIDO"
      });

      navegar("inicio");

      return;
    }
  });
}
