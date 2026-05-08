// src/services/supabase.js

/*
  SUPABASE SERVICE
  - compatible con browser y Node (tests)
  - no rompe si no hay config
*/

let createClientFn = null;

// 👇 DETECTAR ENTORNO
async function getCreateClient() {
  if (createClientFn) return createClientFn;

  try {
    // Node (tests)
    const mod = await import("@supabase/supabase-js");
    createClientFn = mod.createClient;
    return createClientFn;
  } catch (e) {
    try {
      // Browser (fallback)
      const mod = await import("https://esm.sh/@supabase/supabase-js@2");
      createClientFn = mod.createClient;
      return createClientFn;
    } catch (err) {
      console.error("No se pudo cargar Supabase:", err);
      return null;
    }
  }
}

// CONFIG
const SUPABASE_URL = "https://vgaovfhznqtgvxlhwsxx.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZnYW92Zmh6bnF0Z3Z4bGh3c3h4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY2NTU3NDMsImV4cCI6MjA5MjIzMTc0M30.WftOfIBTzTmHSy6Etm2_GqZA-SpxWePuLuAoNf2TfiI";

let supabase = null;

function tieneConfig() {
  return (
    SUPABASE_URL &&
    SUPABASE_ANON_KEY &&
    !SUPABASE_ANON_KEY.includes("PEGAR_")
  );
}

// INIT
export async function initSupabase() {
  if (!tieneConfig()) return null;

  if (supabase) return supabase;

  const createClient = await getCreateClient();
  if (!createClient) return null;

  try {
    supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
      auth: { persistSession: false }
    });
    return supabase;
  } catch (e) {
    console.error("Supabase init error:", e);
    return null;
  }
}

export async function isSupabaseReady() {
  return !!(await initSupabase());
}

// HELPERS

async function safeSelect(table, fallback = []) {
  const db = await initSupabase();
  if (!db) {
    console.warn("Supabase no disponible. Usando fallback.", { table });
    return fallback;
  }

  try {
    const { data, error } = await db.from(table).select("*");
    if (error) throw error;
    return data || fallback;
  } catch (error) {
    console.warn("Supabase select falló. Usando fallback.", { table, error });
    return fallback;
  }
}

function elegirAppConfig(rows) {
  if (!Array.isArray(rows)) {
    return rows && typeof rows === "object" && rows.activo !== false ? rows : {};
  }

  return (
    rows.find((row) => row?.activo === true) ||
    rows.find((row) => row?.activo !== false) ||
    {}
  );
}

export async function getAppConfig() {
  const rows = await safeSelect("app_config", []);

  return elegirAppConfig(rows);
}

export async function getCategorias() {
  return safeSelect("categorias", []);
}

export async function getSubcategorias() {
  return safeSelect("subcategorias", []);
}

export async function getLocales() {
  return safeSelect("locales", []);
}

export async function getProductos() {
  return safeSelect("productos", []);
}

export async function getOpciones() {
  return safeSelect("opciones", []);
}

export async function getOfertas() {
  return safeSelect("ofertas", []);
}

export async function getRepartidores() {
  return safeSelect("repartidores", []);
}
