// src/core/contracts.js

import { getCoreCapabilities } from "./capabilities.js";
import { canEnviarPedido, canGoToConfirmacion, faltanOpcionesRequeridas, getConfirmacionErrors } from "./guards.js";
import {
  getCartSummary,
  getEntityMedia,
  getLocalMetadata,
  getProductoMetadata,
  getSubcategoriaActual,
  selectCategorias,
  selectLocalAbierto,
  selectLocalesForCurrentContext,
  selectOfertasForProducto,
  selectOpcionesForProducto,
  selectSubcategoriasForCategoria
} from "./selectors.js";

function action(id, payload = {}, enabled = true) {
  return { id, payload, enabled };
}

function block(type, data = {}, visible = true) {
  return { type, data, visible };
}

function getAppConfig(state = {}) {
  const rawConfig = state.app_config || {};

  if (Array.isArray(rawConfig)) {
    return rawConfig.find((config) => config?.activo !== false) || {};
  }

  if (rawConfig.activo === false) {
    return {};
  }

  return rawConfig;
}

function getIdentity(config = {}) {
  return {
    app_config: config,
    nombre: config.nombre_negocio || config.nombre || "Pédilo",
    descripcion: config.descripcion || "Pedí, comprá o enviá en tu ciudad.",
    subtitulo: config.subtitulo || config.slogan || "",
    imagen: config.imagen_url || config.logo_url || null
  };
}

function baseContract(screen, state = {}) {
  const identity = getIdentity(getAppConfig(state));

  return {
    screen,
    source: "core",
    identity,
    blocks: [],
    actions: [],
    guards: [],
    fallbackPolicy: {
      visualOnly: true,
      inventBusinessData: false
    },
    continuity: {
      pedido: state.pedido || {}
    }
  };
}

function withMedia(items = []) {
  return items.map((item) => ({
    ...item,
    media: getEntityMedia(item)
  }));
}

function buildLoadingContract(state) {
  const contract = baseContract("loading", state);
  const config = getAppConfig(state);
  const nombre = config.nombre_negocio || config.nombre || "Pédilo";
  const texto = config.splash_text || config.descripcion || "Preparando tu pedido...";

  contract.blocks.push(block("loading_identity", {
    nombre,
    texto,
    descripcion: config.descripcion || "",
    subtitulo: config.subtitulo || config.slogan || "",
    app_config: config,
    imagen: config.imagen_url || config.logo_url || null
  }));

  return contract;
}

function buildInicioContract(state) {
  const contract = baseContract("inicio", state);
  const capabilities = getCoreCapabilities(state);
  const identity = getIdentity(getAppConfig(state));

  contract.blocks.push(
    block("identity", identity),
    block("capabilities", { capabilities })
  );

  contract.actions = capabilities.map((capability) =>
    action("SELECCIONAR_TIPO", { tipo: capability.id })
  );

  return contract;
}

function buildCategoriasContract(state) {
  const contract = baseContract("categorias", state);
  const items = withMedia(selectCategorias(state));

  contract.blocks.push(block("categorias", { items }));
  contract.actions = items.map((item) =>
    action("SELECCIONAR_CATEGORIA", { categoria: item.id })
  );

  return contract;
}

function buildSubcategoriasContract(state) {
  const contract = baseContract("subcategorias", state);
  const categoriaId = state.pedido?.categoria;
  const items = withMedia(selectSubcategoriasForCategoria(state, categoriaId));

  if (!categoriaId) {
    contract.guards.push({ id: "requiere_categoria", blocking: true });
  }

  contract.blocks.push(block("subcategorias", { items }));
  contract.actions = items.map((item) =>
    action("SELECCIONAR_SUBCATEGORIA", { subcategoria: item.id })
  );

  return contract;
}

function buildLocalesContract(state) {
  const contract = baseContract("locales", state);
  const subcategoria = getSubcategoriaActual(state);
  const locales = withMedia(selectLocalesForCurrentContext(state)).map((local) => ({
    ...local,
    metadata: getLocalMetadata(local),
    ofertas: (state.ofertas || []).filter(
      (oferta) =>
        oferta.local_id === local.id &&
        oferta.activo !== false &&
        oferta.activa !== false
    )
  }));

  if (!state.pedido?.subcategoria) {
    contract.guards.push({ id: "requiere_subcategoria", blocking: true });
  }

  contract.blocks.push(
    block("locales_hero", { categoriaNombre: subcategoria?.nombre || "" }),
    block("locales", { items: locales })
  );
  contract.actions = locales.map((local) =>
    action("SELECCIONAR_LOCAL", { local: local.id })
  );

  return contract;
}

function buildProductosContract(state) {
  const contract = baseContract("productos", state);
  const localAbierto = selectLocalAbierto(state);

  if (!localAbierto) {
    contract.guards.push({ id: "requiere_local", blocking: true });
    return contract;
  }

  const productos = localAbierto.productos.map((producto) => ({
    ...producto,
    media: getEntityMedia(producto),
    metadata: getProductoMetadata(producto)
  }));
  const cart = getCartSummary(state.pedido || {});

  contract.blocks.push(
    block("local_header", {
      local: localAbierto.local,
      media: localAbierto.media,
      metadata: getLocalMetadata(localAbierto.local)
    }),
    block("salida_local", {}, Boolean(state.pedido?.salidaLocalPendiente)),
    block("local_datos", { items: getLocalMetadata(localAbierto.local) }, getLocalMetadata(localAbierto.local).length > 0),
    block("ofertas", { items: localAbierto.ofertas }, localAbierto.ofertas.length > 0),
    block("opciones", {
      items: localAbierto.opciones
    }, localAbierto.opciones.length > 0),
    block("productos", { items: productos }),
    block("pedido_cta", { cart })
  );

  contract.actions = productos.map((producto) =>
    action("SELECCIONAR_PRODUCTO", { productoId: producto.id })
  );
  contract.actions.push(action("IR_CARRITO", {}, true));

  return contract;
}

function buildDetalleProductoContract(state) {
  const contract = baseContract("detalle_producto", state);
  const productoActual = state.pedido?.productoActual;
  const producto = productoActual?.producto;

  if (!producto) {
    contract.guards.push({ id: "requiere_producto", blocking: true });
    return contract;
  }

  const opciones = selectOpcionesForProducto(state, producto);
  const opcionesSeleccionadas = productoActual.opcionesSeleccionadas || [];
  const faltanRequeridas = faltanOpcionesRequeridas(
    state,
    producto,
    opcionesSeleccionadas
  );

  contract.blocks.push(block("producto_detalle", {
    producto: {
      ...producto,
      media: getEntityMedia(producto),
      metadata: getProductoMetadata(producto),
      ofertas: selectOfertasForProducto(state, producto),
      opciones
    },
    productoActual,
    editando: Boolean(state.pedido?.itemEnEdicion),
    faltanRequeridas
  }));
  contract.actions.push(action("CONFIRMAR_PRODUCTO", { productoId: producto.id }));

  return contract;
}

function buildCarritoContract(state) {
  const contract = baseContract("carrito", state);
  const pedido = state.pedido || {};

  contract.blocks.push(block("carrito", {
    pedido,
    cart: getCartSummary(pedido)
  }));
  contract.actions.push(action("IR_CONFIRMACION", {}, canGoToConfirmacion(pedido)));

  return contract;
}

function buildConfirmacionContract(state) {
  const contract = baseContract("confirmacion", state);
  const pedido = state.pedido || {};

  contract.blocks.push(block("confirmacion", {
    pedido,
    puedeEnviar: canEnviarPedido(pedido),
    errors: getConfirmacionErrors(pedido)
  }));
  contract.actions.push(action("ENVIAR_PEDIDO", {}, canEnviarPedido(pedido)));

  return contract;
}

function buildPedidoEnviadoContract(state) {
  const contract = baseContract("pedido_enviado", state);
  const config = getAppConfig(state);

  contract.blocks.push(block("pedido_enviado", {
    pedido: state.pedido || {},
    mensaje:
      config.mensaje_pedido_enviado ||
      config.texto_cierre ||
      config.post_envio_text ||
      "Tu pedido quedó registrado y listo para seguimiento."
  }));
  contract.actions.push(action("SELECCIONAR_TIPO", { tipo: "marketplace" }));

  return contract;
}

export function buildScreenContract(state = {}) {
  switch (state.pantalla) {
    case "loading":
      return buildLoadingContract(state);

    case "inicio":
      return buildInicioContract(state);

    case "categorias":
      return buildCategoriasContract(state);

    case "subcategorias":
      return buildSubcategoriasContract(state);

    case "locales":
      return buildLocalesContract(state);

    case "productos":
      return buildProductosContract(state);

    case "detalle_producto":
      return buildDetalleProductoContract(state);

    case "carrito":
      return buildCarritoContract(state);

    case "confirmacion":
      return buildConfirmacionContract(state);

    case "pedido_enviado":
      return buildPedidoEnviadoContract(state);

    default:
      return baseContract(state.pantalla || "inicio", state);
  }
}
