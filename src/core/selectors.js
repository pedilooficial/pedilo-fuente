// src/core/selectors.js

export function getVisualFallback(entity = {}) {
  return {
    type: "initial",
    value: entity.icono || entity.nombre?.charAt(0) || "?",
    honest: true
  };
}

export function getEntityMedia(entity = {}) {
  const src = entity.imagen_url || entity.logo_url || null;

  return {
    src,
    alt: entity.nombre || "",
    fallback: src ? null : getVisualFallback(entity)
  };
}

export function getBlock(contract = {}, type) {
  for (const block of contract.blocks || []) {
    if (block.type === type) {
      return block;
    }
  }

  return null;
}

function byOrden(a = {}, b = {}) {
  const ordenA = a.orden ?? Number.MAX_SAFE_INTEGER;
  const ordenB = b.orden ?? Number.MAX_SAFE_INTEGER;

  if (ordenA !== ordenB) {
    return ordenA - ordenB;
  }

  return String(a.nombre || "").localeCompare(String(b.nombre || ""));
}

export function selectCategorias(state = {}) {
  return [...(state.categorias || [])].sort(byOrden);
}

export function selectSubcategoriasForCategoria(state = {}, categoriaId) {
  return (state.subcategorias || []).filter(
    (subcategoria) => subcategoria.categoria_id === categoriaId
  ).sort(byOrden);
}

export function selectLocalesForSubcategoria(state = {}, subcategoriaId) {
  return (state.locales || []).filter(
    (local) =>
      local.activo !== false &&
      local.subcategoria_id === subcategoriaId
  ).sort(byOrden);
}

export function selectLocalesForCategoria(state = {}, categoriaId) {
  return (state.locales || []).filter(
    (local) =>
      local.activo !== false &&
      local.categoria_id === categoriaId
  ).sort(byOrden);
}

export function selectLocalesForCurrentContext(state = {}) {
  const pedido = state.pedido || {};
  const bySubcategoria = selectLocalesForSubcategoria(
    state,
    pedido.subcategoria
  );

  if (bySubcategoria.length > 0 || !pedido.categoria) {
    return bySubcategoria;
  }

  return selectLocalesForCategoria(state, pedido.categoria);
}

export function selectLocalActual(state = {}) {
  const pedido = state.pedido || {};

  return (
    (state.locales || []).find((local) => local.id === pedido.local) ||
    (pedido.local ? { id: pedido.local, nombre: "" } : null)
  );
}

export function selectProductosForLocal(state = {}, localId) {
  return (state.productos || []).filter(
    (producto) =>
      producto.local_id === localId &&
      producto.activo !== false &&
      producto.disponible !== false
  ).sort(byOrden);
}

export function selectOfertasForLocal(state = {}, localId) {
  return (state.ofertas || []).filter(
    (oferta) =>
      oferta.local_id === localId &&
      oferta.activo !== false &&
      oferta.activa !== false
  ).sort(byOrden);
}

export function selectOfertasForProducto(state = {}, productoId) {
  const producto = typeof productoId === "object"
    ? productoId
    : (state.productos || []).find((item) => item.id === productoId);
  const localId = producto?.local_id;

  if (!localId) return [];

  return (state.ofertas || []).filter(
    (oferta) =>
      oferta.local_id === localId &&
      oferta.activo !== false &&
      oferta.activa !== false
  ).sort(byOrden);
}

export function selectOpcionesForProducto(state = {}, productoId) {
  const producto = typeof productoId === "object"
    ? productoId
    : (state.productos || []).find((item) => item.id === productoId);
  const localId = producto?.local_id;

  if (!localId) return [];

  return (state.opciones || []).filter(
    (opcion) =>
      opcion.local_id === localId &&
      opcion.activo !== false &&
      opcion.activa !== false
  ).sort(byOrden);
}

export function selectOpcionesForProductos(state = {}, productos = []) {
  const localIds = new Set(
    productos
      .map((producto) => producto.local_id)
      .filter(Boolean)
  );

  return (state.opciones || []).filter((opcion) =>
    localIds.has(opcion.local_id) &&
    opcion.activo !== false &&
    opcion.activa !== false
  ).sort(byOrden);
}

export function selectLocalAbierto(state = {}) {
  const local = selectLocalActual(state);

  if (!local) {
    return null;
  }

  const productos = selectProductosForLocal(state, local.id);

  return {
    local,
    media: getEntityMedia(local),
    productos,
    ofertas: selectOfertasForLocal(state, local.id),
    opciones: selectOpcionesForProductos(state, productos)
  };
}

export function getCategoriaActual(state = {}) {
  const categoriaId = state.pedido?.categoria;

  return (
    (state.categorias || []).find((categoria) => categoria.id === categoriaId) ||
    null
  );
}

export function getSubcategoriaActual(state = {}) {
  const subcategoriaId = state.pedido?.subcategoria;

  return (
    (state.subcategorias || []).find((subcategoria) => subcategoria.id === subcategoriaId) ||
    null
  );
}

export function getLocalMetadata(local = {}) {
  const estado =
    local.estado ||
    (local.abierto === true ? "Abierto" : local.abierto === false ? "Cerrado" : "");

  return [
    estado ? { label: "Estado", value: estado, badge: true } : null,
    local.horario ? { label: "Horario", value: local.horario } : null,
    local.direccion ? { label: "Dirección", value: local.direccion } : null,
    local.telefono ? { label: "Teléfono", value: local.telefono } : null,
    local.whatsapp ? { label: "WhatsApp", value: local.whatsapp } : null,
    local.tiempo_estimado ? { label: "Tiempo estimado", value: local.tiempo_estimado } : null,
    local.costo_envio !== null && local.costo_envio !== undefined
      ? { label: "Envío", value: `$${local.costo_envio}` }
      : null,
    local.minimo_pedido !== null && local.minimo_pedido !== undefined
      ? { label: "Mínimo", value: `$${local.minimo_pedido}` }
      : null,
    local.calificacion ? { label: "Calificación", value: local.calificacion } : null
  ].filter(Boolean);
}

export function getProductoMetadata(producto = {}) {
  return [
    producto.destacado ? { type: "badge", value: "Destacado" } : null,
    producto.unidad ? { label: "Unidad", value: producto.unidad } : null,
    producto.descuento ? { label: "Descuento", value: producto.descuento } : null,
    producto.stock !== null && producto.stock !== undefined
      ? { label: "Stock", value: producto.stock }
      : null,
    producto.disponible === false ? { label: "Disponibilidad", value: "No disponible" } : null
  ].filter(Boolean);
}

export function getCartSummary(pedido = {}) {
  const items = pedido.productos || [];
  const count = items.reduce((total, item) => total + (item.cantidad || 0), 0);
  const canTotal = items.length > 0 && items.every(
    (item) =>
      typeof item.subtotal === "number" ||
      (typeof item.precio === "number" && typeof item.cantidad === "number")
  );
  const total = canTotal
    ? items.reduce((sum, item) => {
        if (typeof item.subtotal === "number") return sum + item.subtotal;
        return sum + item.precio * item.cantidad;
      }, 0)
    : null;

  return { items, count, total };
}
