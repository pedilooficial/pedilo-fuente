// src/app/app.js

import { updateState } from "./state.js";
import { bindGlobalEvents } from "./render.js";
import { cargarDatos } from "../services/api.js";

/*
  APP — BOOT PRINCIPAL

  Responsabilidad:
  - iniciar eventos globales
  - mostrar loading
  - cargar datos (Supabase o mock)
  - inicializar state
  - navegar a inicio

  Reglas:
  - NO contiene datos mock
  - NO usa window.*
  - NO manipula pantallas directo
  - fallback controlado
*/

let appIniciada = false;
const MIN_LOADING_VISIBLE_MS = 4000;

function esperar(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function iniciarApp() {
  try {
    const inicioLoading = Date.now();

    // bind de eventos solo una vez
    if (!appIniciada && typeof document !== "undefined") {
      bindGlobalEvents();
      appIniciada = true;
    }

    // pantalla loading
    updateState({
      type: "NAVEGAR",
      pantalla: "loading"
    });

    // carga datos (Supabase → fallback mock)
    const data = await cargarDatos();

    // inicializa store
    updateState({
      type: "INIT_DATA",
      data
    });

    const restante = MIN_LOADING_VISIBLE_MS - (Date.now() - inicioLoading);

    if (restante > 0) {
      await esperar(restante);
    }

    // navegación inicial
    updateState({
      type: "NAVEGAR",
      pantalla: "inicio"
    });
  } catch (error) {
    console.error("Error al iniciar Pédilo:", error);

    // fallback UI mínimo (no rompe la app)
    if (typeof document !== "undefined") {
      const root = document.getElementById("contenido") || document.body;

      root.innerHTML = `
        <main class="pantalla">
          <h1>Pédilo</h1>
          <p>No pudimos iniciar la app.</p>
          <button onclick="location.reload()">Reintentar</button>
        </main>
      `;
    }
  }
}
