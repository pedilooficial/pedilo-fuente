// src/services/api.js

import {
  getAppConfig,
  getCategorias,
  getSubcategorias,
  getLocales,
  getProductos,
  getOpciones,
  getOfertas,
  getRepartidores,
  isSupabaseReady
} from "./supabase.js";

/*
  API SERVICE

  - conecta con Supabase
  - transforma datos a formato app
  - fallback a mock si falla
*/

// ======================
// MOCK (fallback seguro)
// ======================

const MOCK = {
  app_config: {
    nombre: "Pédilo",
    descripcion: "Pedí lo que necesites en tu ciudad",
    splash_text: "Conectando con locales..."
  },
  categorias: [],
  subcategorias: [],
  locales: [],
  productos: [],
  opciones: [],
  ofertas: [],
  repartidores: []
};

// ======================
// TRANSFORMADORES
// ======================

function mapPrecio(precio) {
  if (precio === null || precio === undefined || precio === "") {
    return null;
  }

  return typeof precio === "number" ? precio : Number(precio);
}

function normalizarRows(rows) {
  return Array.isArray(rows) ? rows : [];
}

export function mapConfig(config) {
  if (Array.isArray(config)) {
    return (
      config.find((row) => row?.activo === true) ||
      config.find((row) => row?.activo !== false) ||
      {}
    );
  }

  if (!config || typeof config !== "object" || config.activo === false) {
    return {};
  }

  return config;
}

export function mapCategorias(rows) {
  return normalizarRows(rows).map((r) => ({
    ...r,
    id: r.id,
    nombre: r.nombre,
    descripcion: r.descripcion ?? null
  }));
}

export function mapSubcategorias(rows) {
  return normalizarRows(rows).map((r) => ({
    ...r,
    id: r.id,
    categoria_id: r.categoria_id,
    nombre: r.nombre,
    descripcion: r.descripcion ?? null
  }));
}

export function mapLocales(rows) {
  return normalizarRows(rows).map((r) => ({
    ...r,
    id: r.id,
    subcategoria_id: r.subcategoria_id,
    categoria_id: r.categoria_id,
    nombre: r.nombre,
    descripcion: r.descripcion ?? null,
    imagen_url: r.imagen_url ?? null
  }));
}

export function mapProductos(rows) {
  return normalizarRows(rows).map((r) => ({
    ...r,
    id: r.id,
    local_id: r.local_id,
    nombre: r.nombre,
    descripcion: r.descripcion ?? null,
    imagen_url: r.imagen_url ?? null,
    precio: mapPrecio(r.precio),
    origenItem: r.origenItem || r.origen_item || "producto"
  }));
}

export function mapOpciones(rows) {
  return normalizarRows(rows).map((r) => ({
    ...r,
    id: r.id,
    local_id: r.local_id,
    nombre: r.nombre,
    descripcion: r.descripcion ?? null
  }));
}

export function mapOfertas(rows) {
  return normalizarRows(rows).map((r) => ({
    ...r,
    id: r.id,
    local_id: r.local_id,
    titulo: r.titulo ?? r.nombre ?? null,
    nombre: r.nombre,
    descripcion: r.descripcion ?? null,
    precio: mapPrecio(r.precio)
  }));
}

export function mapRepartidores(rows) {
  return normalizarRows(rows).map((r) => ({
    ...r,
    id: r.id,
    nombre: r.nombre,
    descripcion: r.descripcion ?? null
  }));
}

// ======================
// CARGA REAL
// ======================

async function cargarDesdeSupabase() {
  const [
    appConfig,
    categorias,
    subcategorias,
    locales,
    productos,
    opciones,
    ofertas,
    repartidores
  ] = await Promise.all([
    getAppConfig(),
    getCategorias(),
    getSubcategorias(),
    getLocales(),
    getProductos(),
    getOpciones(),
    getOfertas(),
    getRepartidores()
  ]);

  return {
    app_config: mapConfig(appConfig),
    categorias: mapCategorias(categorias),
    subcategorias: mapSubcategorias(subcategorias),
    locales: mapLocales(locales),
    productos: mapProductos(productos),
    opciones: mapOpciones(opciones),
    ofertas: mapOfertas(ofertas),
    repartidores: mapRepartidores(repartidores)
  };
}

// ======================
// API PRINCIPAL
// ======================

export async function cargarDatos() {
  try {
    if (!(await isSupabaseReady())) {
      return MOCK;
    }

    return await cargarDesdeSupabase();
  } catch (e) {
    console.error("API fallback:", e);
    return MOCK;
  }
}
