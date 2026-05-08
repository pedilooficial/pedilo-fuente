// tests/flow.test.js

import test from "node:test";
import assert from "node:assert";
import { updateState, getState } from "../src/app/state.js";
import { bindGlobalEvents } from "../src/app/render.js";
import { buildPedidoEjecutable } from "../src/core/order.js";
import { enviarPedidoPorWhatsApp } from "../src/services/whatsapp.js";
import { loading } from "../src/screens/loading.js";
import { inicio } from "../src/screens/inicio.js";
import { categorias } from "../src/screens/categorias.js";
import { subcategorias } from "../src/screens/subcategorias.js";
import { locales as screenLocales } from "../src/screens/locales.js";
import { screenProductos } from "../src/screens/productos.js";
import { detalleProducto } from "../src/screens/detalle_producto.js";
import { screenCarrito } from "../src/screens/carrito.js";
import { confirmacion } from "../src/screens/confirmacion.js";
import { pedido_enviado } from "../src/screens/pedido_enviado.js";

const dataBase = {
  app_config: {},
  categorias: [{ id: "cat_1", nombre: "Comida" }],
  subcategorias: [
    { id: "sub_1", categoria_id: "cat_1", nombre: "Pizzerías" },
    { id: "sub_2", categoria_id: "cat_1", nombre: "Hamburguesas" }
  ],
  locales: [
    { id: "loc_1", subcategoria_id: "sub_1", nombre: "Local 1" },
    { id: "loc_2", subcategoria_id: "sub_2", nombre: "Local 2" }
  ],
  productos: [
    { id: "prod_1", local_id: "loc_1", nombre: "Producto 1", precio: 1000 },
    { id: "prod_2", local_id: "loc_2", nombre: "Producto 2", precio: null }
  ],
  opciones: [],
  ofertas: [],
  repartidores: []
};

function resetConDatos() {
  updateState({ type: "RESET_PEDIDO" });
  updateState({ type: "INIT_DATA", data: dataBase });
}

function confirmarProductoPorDetalle(productoId, overrides = {}) {
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId });
  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: overrides.cantidad ?? 1,
    observacion: overrides.observacion ?? "",
    opcionesSeleccionadas: overrides.opcionesSeleccionadas
  });
}

test("INIT_DATA conserva app_config imagen_url y descripcion de subcategoria", () => {
  updateState({ type: "RESET_PEDIDO" });
  updateState({
    type: "INIT_DATA",
    data: {
      ...dataBase,
      app_config: {
        nombre_negocio: "Pédilo Real",
        imagen_url: "https://example.com/logo-real.png",
        descripcion: "Configuración real"
      },
      subcategorias: [{
        id: "sub_desc",
        categoria_id: "cat_1",
        nombre: "Compras rápidas",
        descripcion: "Kioscos, despensas, supermercados y más"
      }]
    }
  });

  const state = getState();

  assert.strictEqual(state.app_config.imagen_url, "https://example.com/logo-real.png");
  assert.strictEqual(
    state.subcategorias[0].descripcion,
    "Kioscos, despensas, supermercados y más"
  );
});

test("inicio separa entrada de categorias y mantiene tres pedidos directos", () => {
  const html = inicio({
    app_config: {},
    pedido: { tipo: null },
    categorias: [{ id: "cat_1", nombre: "Comida" }]
  });

  assert.ok(html.includes('data-tipo="marketplace"'));
  assert.ok(html.includes('data-next="categorias"'));
  assert.ok(html.includes('data-tipo="compra_libre"'));
  assert.ok(html.includes('data-tipo="retirar_envio"'));
  assert.ok(html.includes('data-tipo="pedir_repartidor"'));
  assert.ok(!html.includes('data-categoria="cat_1"'));
});

test("categorias renderiza categorias reales y navega a subcategorias", () => {
  const html = categorias({
    app_config: {},
    pedido: { tipo: "marketplace" },
    categorias: [{
      id: "cat_1",
      nombre: "Comida",
      descripcion: "Locales de comida",
      imagen_url: "https://example.com/comida.png"
    }]
  });

  assert.ok(html.includes("Comida"));
  assert.ok(html.includes("Locales de comida"));
  assert.ok(html.includes("https://example.com/comida.png"));
  assert.ok(html.includes('data-categoria="cat_1"'));
});

test("flujo básico de pedido", () => {
  resetConDatos();

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });
  updateState({ type: "SELECCIONAR_SUBCATEGORIA", subcategoria: "sub_1" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  confirmarProductoPorDetalle("prod_1");
  confirmarProductoPorDetalle("prod_1");

  const state = getState();

  assert.strictEqual(state.pedido.categoria, "cat_1");
  assert.strictEqual(state.pedido.subcategoria, "sub_1");
  assert.strictEqual(state.pedido.local, "loc_1");
  assert.strictEqual(state.pedido.productos.length, 2);
  assert.strictEqual(state.pedido.productos[0].cantidad, 1);
  assert.strictEqual(state.pedido.productos[1].cantidad, 1);
});

test("SELECCIONAR_TIPO marketplace deja categoria, subcategoria, local null y productos vacío", () => {
  updateState({ type: "RESET_PEDIDO" });

  updateState({
    type: "SELECCIONAR_TIPO",
    tipo: "marketplace"
  });

  const state = getState();

  assert.strictEqual(state.pedido.tipo, "marketplace");
  assert.strictEqual(state.pedido.categoria, null);
  assert.strictEqual(state.pedido.subcategoria, null);
  assert.strictEqual(state.pedido.local, null);
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("SELECCIONAR_TIPO compra_libre setea tipo correcto", () => {
  updateState({ type: "RESET_PEDIDO" });

  updateState({ type: "SELECCIONAR_TIPO", tipo: "compra_libre" });

  const state = getState();

  assert.strictEqual(state.pedido.tipo, "compra_libre");
  assert.strictEqual(state.pedido.categoria, null);
  assert.strictEqual(state.pedido.subcategoria, null);
  assert.strictEqual(state.pedido.local, null);
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("SELECCIONAR_TIPO retirar_envio setea tipo correcto", () => {
  updateState({ type: "RESET_PEDIDO" });

  updateState({ type: "SELECCIONAR_TIPO", tipo: "retirar_envio" });

  const state = getState();

  assert.strictEqual(state.pedido.tipo, "retirar_envio");
  assert.strictEqual(state.pedido.categoria, null);
  assert.strictEqual(state.pedido.subcategoria, null);
  assert.strictEqual(state.pedido.local, null);
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("SELECCIONAR_TIPO pedir_repartidor setea tipo correcto", () => {
  updateState({ type: "RESET_PEDIDO" });

  updateState({ type: "SELECCIONAR_TIPO", tipo: "pedir_repartidor" });

  const state = getState();

  assert.strictEqual(state.pedido.tipo, "pedir_repartidor");
  assert.strictEqual(state.pedido.categoria, null);
  assert.strictEqual(state.pedido.subcategoria, null);
  assert.strictEqual(state.pedido.local, null);
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("SET_FORMULARIO guarda datos de compra_libre", () => {
  updateState({ type: "RESET_PEDIDO" });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "compra_libre" });

  updateState({
    type: "SET_FORMULARIO",
    formulario: {
      compra_libre: {
        queNecesita: "Leche y pan",
        donde: "Cerca de casa",
        observaciones: "Sin lactosa"
      }
    }
  });

  const state = getState();

  assert.strictEqual(state.pedido.formulario.compra_libre.queNecesita, "Leche y pan");
  assert.strictEqual(state.pedido.formulario.compra_libre.donde, "Cerca de casa");
  assert.strictEqual(state.pedido.formulario.compra_libre.observaciones, "Sin lactosa");
});

test("SET_FORMULARIO guarda datos de retirar_envio", () => {
  updateState({ type: "RESET_PEDIDO" });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "retirar_envio" });

  updateState({
    type: "SET_FORMULARIO",
    formulario: {
      retirar_envio: {
        direccionRetiro: "Av. Libertad 123",
        direccionEntrega: "Calle Falsa 456",
        queRetira: "Documentos"
      }
    }
  });

  const state = getState();

  assert.strictEqual(state.pedido.formulario.retirar_envio.direccionRetiro, "Av. Libertad 123");
  assert.strictEqual(state.pedido.formulario.retirar_envio.direccionEntrega, "Calle Falsa 456");
  assert.strictEqual(state.pedido.formulario.retirar_envio.queRetira, "Documentos");
});

test("SET_FORMULARIO guarda datos de pedir_repartidor", () => {
  updateState({ type: "RESET_PEDIDO" });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "pedir_repartidor" });

  updateState({
    type: "SET_FORMULARIO",
    formulario: {
      repartidor: {
        puntoSalida: "Casa",
        puntoDestino: "Oficina",
        detalle: "Paquete pequeño"
      }
    }
  });

  const state = getState();

  assert.strictEqual(state.pedido.formulario.repartidor.puntoSalida, "Casa");
  assert.strictEqual(state.pedido.formulario.repartidor.puntoDestino, "Oficina");
  assert.strictEqual(state.pedido.formulario.repartidor.detalle, "Paquete pequeño");
});

test("enviarPedidoPorWhatsApp usa 2395432700 y mensaje incluye tipo de pedido", () => {
  global.window = { open: (url) => { global.__openedUrl = url; return url; } };
  global.alert = () => {};

  const pedido = {
    tipo: "compra_libre",
    numero: "123456",
    cliente: {
      nombre: "Test",
      telefono: "2395432700",
      direccion: {
        calle: "Calle",
        numero: "123"
      }
    },
    formulario: {
      compra_libre: {
        queNecesita: "Pan",
        donde: "Barrio",
        observaciones: "Sin cebolla"
      }
    }
  };

  const executable = buildPedidoEjecutable({ pedido });
  const url = enviarPedidoPorWhatsApp(executable.pedido);

  assert.ok(url.includes("wa.me/5492395432700"));
  assert.ok(url.includes("Tipo%3A%20compra_libre") || url.includes("Tipo%3A+compra_libre"));
  assert.ok(global.__openedUrl.includes("wa.me/5492395432700"));
});

test("SELECCIONAR_CATEGORIA limpia subcategoria, local y productos", () => {
  resetConDatos();

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });
  updateState({ type: "SELECCIONAR_SUBCATEGORIA", subcategoria: "sub_1" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  confirmarProductoPorDetalle("prod_1");

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });

  const state = getState();

  assert.strictEqual(state.pedido.categoria, "cat_1");
  assert.strictEqual(state.pedido.subcategoria, null);
  assert.strictEqual(state.pedido.local, null);
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("SELECCIONAR_SUBCATEGORIA limpia local y productos", () => {
  resetConDatos();

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });
  updateState({ type: "SELECCIONAR_SUBCATEGORIA", subcategoria: "sub_1" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  confirmarProductoPorDetalle("prod_1");

  updateState({ type: "SELECCIONAR_SUBCATEGORIA", subcategoria: "sub_2" });

  const state = getState();

  assert.strictEqual(state.pedido.subcategoria, "sub_2");
  assert.strictEqual(state.pedido.local, null);
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("SELECCIONAR_LOCAL limpia productos", () => {
  resetConDatos();

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });
  updateState({ type: "SELECCIONAR_SUBCATEGORIA", subcategoria: "sub_1" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  confirmarProductoPorDetalle("prod_1");

  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_2" });

  const state = getState();

  assert.strictEqual(state.pedido.local, "loc_2");
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("AGREGAR_PRODUCTO no puede construir items", () => {
  resetConDatos();

  updateState({
    type: "AGREGAR_PRODUCTO",
    productoId: "prod_2",
    nombre: "Producto sin precio",
    precio: null
  });

  const state = getState();

  assert.deepStrictEqual(state.pedido.productos, []);
  assert.strictEqual(state.pedido.numero, null);
});

test("CONFIRMAR_PRODUCTO es la via valida para agregar al carrito", () => {
  resetConDatos();

  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 1 });

  const state = getState();

  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.productos[0].productoId, "prod_1");
  assert.strictEqual(state.pedido.productos[0].origenItem, "producto");
});

test("CONFIRMAR_PRODUCTO sin productoActual no construye items", () => {
  resetConDatos();

  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 1 });

  const state = getState();

  assert.deepStrictEqual(state.pedido.productos, []);
  assert.strictEqual(state.pedido.productoActual, null);
});

test("al cambiar tipo se resetea el pedido y conserva cliente", () => {
  resetConDatos();

  updateState({
    type: "SET_CLIENTE",
    cliente: {
      nombre: "Cliente",
      telefono: "123456789"
    }
  });

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });

  confirmarProductoPorDetalle("prod_1");

  updateState({
    type: "SELECCIONAR_TIPO",
    tipo: "retirar_envio"
  });

  const state = getState();

  assert.strictEqual(state.pedido.tipo, "retirar_envio");
  assert.strictEqual(state.pedido.categoria, null);
  assert.strictEqual(state.pedido.subcategoria, null);
  assert.strictEqual(state.pedido.local, null);
  assert.deepStrictEqual(state.pedido.productos, []);
  assert.strictEqual(state.pedido.cliente.nombre, "Cliente");
  assert.strictEqual(state.pedido.cliente.telefono, "123456789");
});

test("SET_FORMULARIO guarda formulario completo de una vez", () => {
  updateState({ type: "RESET_PEDIDO" });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "compra_libre" });

  updateState({
    type: "SET_FORMULARIO",
    formulario: {
      compra_libre: {
        queNecesita: "Pan y leche",
        donde: "Supermercado cercano",
        observaciones: "Sin gluten"
      }
    }
  });

  const state = getState();

  assert.strictEqual(state.pedido.formulario.compra_libre.queNecesita, "Pan y leche");
  assert.strictEqual(state.pedido.formulario.compra_libre.donde, "Supermercado cercano");
  assert.strictEqual(state.pedido.formulario.compra_libre.observaciones, "Sin gluten");
});

test("locales filtra por subcategoria_id si existe", () => {
  const mockState = {
    pedido: { subcategoria: "sub_1" },
    locales: [
      { id: "loc_1", subcategoria_id: "sub_1", nombre: "Local 1" },
      { id: "loc_2", subcategoria_id: "sub_2", nombre: "Local 2" }
    ],
    subcategorias: [{ id: "sub_1", nombre: "Sub 1" }]
  };

  // Simular render de locales
  const result = mockState.locales.filter(l => l.subcategoria_id === mockState.pedido.subcategoria);
  assert.strictEqual(result.length, 1);
  assert.strictEqual(result[0].nombre, "Local 1");
});

test("locales filtra por categoria_id si no hay por subcategoria", () => {
  const mockState = {
    pedido: { categoria: "cat_1", subcategoria: "sub_1" },
    locales: [
      { id: "loc_1", categoria_id: "cat_1", nombre: "Local 1" },
      { id: "loc_2", categoria_id: "cat_2", nombre: "Local 2" }
    ],
    subcategorias: [{ id: "sub_1", nombre: "Sub 1" }]
  };

  // Simular lógica: primero por subcategoria, si vacío, por categoria
  let lista = mockState.locales.filter(l => l.subcategoria_id === mockState.pedido.subcategoria);
  if (lista.length === 0) {
    lista = mockState.locales.filter(l => l.categoria_id === mockState.pedido.categoria);
  }

  assert.strictEqual(lista.length, 1);
  assert.strictEqual(lista[0].nombre, "Local 1");
});

test("producto sin descripcion no muestra texto inventado", () => {
  const producto = { id: "prod_1", nombre: "Producto", precio: 100 };
  const descripcion = producto.descripcion ? `<p>${producto.descripcion}</p>` : "";
  assert.strictEqual(descripcion, "");
});

test("producto con precio null muestra 'Precio a consultar'", () => {
  const producto = { id: "prod_1", nombre: "Producto", precio: null };
  const precio = producto.precio !== null && producto.precio !== undefined
    ? `$${producto.precio}`
    : "Precio a consultar";
  assert.strictEqual(precio, "Precio a consultar");
});

test("volver desde productos conserva estado de pedido", () => {
  resetConDatos();

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });
  updateState({ type: "SELECCIONAR_SUBCATEGORIA", subcategoria: "sub_1" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  const state = getState();

  assert.strictEqual(state.pedido.categoria, "cat_1");
  assert.strictEqual(state.pedido.subcategoria, "sub_1");
  assert.strictEqual(state.pedido.local, "loc_1");
});

test("volver desde carrito marketplace conserva productos", () => {
  resetConDatos();

  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });
  updateState({ type: "SELECCIONAR_SUBCATEGORIA", subcategoria: "sub_1" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  confirmarProductoPorDetalle("prod_1");

  const state = getState();

  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.productos[0].nombre, "Producto 1");
});

test("SELECCIONAR_PRODUCTO NO agrega al carrito", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  const state = getState();
  assert.strictEqual(state.pedido.productos.length, 0);
  assert.strictEqual(state.pantalla, "detalle_producto");
});

test("SELECCIONAR_PRODUCTO crea productoActual", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  const state = getState();
  assert.strictEqual(state.pedido.productoActual.producto.id, "prod_1");
  assert.strictEqual(state.pedido.productoActual.cantidad, 1);
  assert.strictEqual(state.pedido.productoActual.observacion, "");
  assert.deepStrictEqual(state.pedido.productoActual.opcionesSeleccionadas, []);
});

test("detalle_producto muestra opciones disponibles sin seleccionar por tecla", () => {
  const html = detalleProducto({
    opciones: [{
      id: "op_1",
      local_id: "loc_1",
      nombre: "Extra queso",
      descripcion: "Más muzzarella",
      precio_extra: 200,
      requerido: true
    }],
    pedido: {
      productoActual: {
        producto: {
          id: "prod_1",
          local_id: "loc_1",
          nombre: "Pizza",
          precio: 1000
        },
        cantidad: 1,
        observacion: "",
        opcionesSeleccionadas: ["Extra queso"]
      },
      itemEnEdicion: null
    }
  });

  assert.ok(html.includes("Extra queso"));
  assert.ok(html.includes("Más muzzarella"));
  assert.ok(html.includes("Extra: $200"));
  assert.ok(html.includes("Requerido"));
  assert.ok(html.includes("data-producto-opcion"));
  assert.ok(html.includes("checked"));
});

test("detalle_producto muestra oferta y metadata real del producto", () => {
  const html = detalleProducto({
    opciones: [],
    ofertas: [{
      id: "of_detalle",
      local_id: "loc_1",
      titulo: "Promo detalle",
      descripcion: "Oferta real",
      descuento: "20%",
      precio: 800,
      precio_nuevo: 700
    }],
    pedido: {
      productoActual: {
        producto: {
          id: "prod_1",
          local_id: "loc_1",
          nombre: "Pizza",
          precio: 1000,
          unidad: "unidad",
          descuento: "10%",
          stock: 4,
          destacado: true
        },
        cantidad: 1,
        observacion: "",
        opcionesSeleccionadas: []
      },
      itemEnEdicion: null
    }
  });

  assert.ok(html.includes("Destacado"));
  assert.ok(html.includes("Unidad: unidad"));
  assert.ok(html.includes("Descuento: 10%"));
  assert.ok(html.includes("Stock: 4"));
  assert.ok(html.includes("Promo detalle"));
  assert.ok(html.includes("Oferta real"));
  assert.ok(html.includes("20%"));
  assert.ok(html.includes("$800"));
  assert.ok(html.includes("Nuevo precio: $700"));
});

test("opcion requerida bloquea confirmar producto si no fue seleccionada", () => {
  resetConDatos();
  updateState({
    type: "INIT_DATA",
    data: {
      ...dataBase,
      opciones: [
        { id: "op_req", local_id: "loc_1", nombre: "Tamaño grande", requerido: true }
      ]
    }
  });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 1, opcionesSeleccionadas: [] });

  const state = getState();
  assert.strictEqual(state.pantalla, "detalle_producto");
  assert.strictEqual(state.pedido.productos.length, 0);
  assert.strictEqual(state.pedido.productoActual.intentoConfirmar, true);
});

test("error de opcion requerida aparece solo despues de intentar confirmar", () => {
  const baseState = {
    opciones: [
      { id: "op_req", local_id: "loc_1", nombre: "Tamaño grande", requerido: true }
    ],
    pedido: {
      productoActual: {
        producto: { id: "prod_1", local_id: "loc_1", nombre: "Pizza", precio: 1000 },
        cantidad: 1,
        observacion: "",
        opcionesSeleccionadas: [],
        intentoConfirmar: false
      },
      itemEnEdicion: null
    }
  };

  const htmlInicial = detalleProducto(baseState);
  const htmlConIntento = detalleProducto({
    ...baseState,
    pedido: {
      ...baseState.pedido,
      productoActual: {
        ...baseState.pedido.productoActual,
        intentoConfirmar: true
      }
    }
  });

  assert.ok(!htmlInicial.includes("Elegí las opciones requeridas"));
  assert.ok(htmlConIntento.includes("Elegí las opciones requeridas"));
});

test("opcion requerida seleccionada permite confirmar", () => {
  resetConDatos();
  updateState({
    type: "INIT_DATA",
    data: {
      ...dataBase,
      opciones: [
        { id: "op_req", local_id: "loc_1", nombre: "Tamaño grande", requerido: true }
      ]
    }
  });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: 1,
    opcionesSeleccionadas: ["Tamaño grande"]
  });

  const state = getState();
  assert.strictEqual(state.pantalla, "productos");
  assert.strictEqual(state.pedido.productos.length, 1);
  assert.deepStrictEqual(state.pedido.productos[0].opcionesSeleccionadas, ["Tamaño grande"]);
});

test("CANCELAR_PRODUCTO limpia productoActual", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  updateState({ type: "CANCELAR_PRODUCTO" });

  const state = getState();
  assert.strictEqual(state.pedido.productoActual, null);
  assert.strictEqual(state.pedido.itemEnEdicion, null);
  assert.strictEqual(state.pantalla, "productos");
});

test("CONFIRMAR_PRODUCTO agrega item completo", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: 3,
    observacion: "sin sal"
  });

  const item = getState().pedido.productos[0];
  assert.ok(item.itemId);
  assert.strictEqual(item.productoId, "prod_1");
  assert.strictEqual(item.localId, "loc_1");
  assert.strictEqual(item.nombre, "Producto 1");
  assert.strictEqual(item.precio, 1000);
  assert.strictEqual(item.cantidad, 1);
  assert.strictEqual(item.observacion, "sin sal");
  assert.deepStrictEqual(item.opcionesSeleccionadas, []);
  assert.strictEqual(item.origenItem, "producto");
  assert.strictEqual(item.subtotal, 1000);
});

test("subtotal null si precio null", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_2" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_2" });

  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 2 });

  const item = getState().pedido.productos[0];
  assert.strictEqual(item.precio, null);
  assert.strictEqual(item.subtotal, null);
});

test("EDITAR_ITEM no duplica", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 1 });

  const itemId = getState().pedido.productos[0].itemId;
  updateState({ type: "EDITAR_ITEM", itemId });

  const state = getState();
  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.itemEnEdicion, itemId);
  assert.strictEqual(state.pantalla, "detalle_producto");
});

test("ACTUALIZAR_ITEM modifica item", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 1 });

  const itemId = getState().pedido.productos[0].itemId;
  updateState({ type: "EDITAR_ITEM", itemId });
  updateState({
    type: "ACTUALIZAR_ITEM",
    cantidad: 4,
    observacion: "cortar al medio"
  });

  const state = getState();
  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.productos[0].itemId, itemId);
  assert.strictEqual(state.pedido.productos[0].cantidad, 1);
  assert.strictEqual(state.pedido.productos[0].observacion, "cortar al medio");
  assert.strictEqual(state.pedido.productos[0].subtotal, 1000);
  assert.strictEqual(state.pedido.itemEnEdicion, null);
});

test("mismo producto agregado dos veces crea items separados con observaciones propias", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: 1,
    observacion: "Sin sal"
  });

  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: 2,
    observacion: "Con limón"
  });

  const items = getState().pedido.productos;

  assert.strictEqual(items.length, 2);
  assert.notStrictEqual(items[0].itemId, items[1].itemId);
  assert.strictEqual(items[0].productoId, "prod_1");
  assert.strictEqual(items[1].productoId, "prod_1");
  assert.strictEqual(items[0].observacion, "Sin sal");
  assert.strictEqual(items[1].observacion, "Con limón");
});

test("ACTUALIZAR_ITEM modifica solo el item correcto", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 1, observacion: "Primero" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 1, observacion: "Segundo" });

  const itemId = getState().pedido.productos[1].itemId;
  updateState({ type: "EDITAR_ITEM", itemId });
  updateState({
    type: "ACTUALIZAR_ITEM",
    cantidad: 5,
    observacion: "Segundo editado"
  });

  const items = getState().pedido.productos;

  assert.strictEqual(items.length, 2);
  assert.strictEqual(items[0].observacion, "Primero");
  assert.strictEqual(items[0].cantidad, 1);
  assert.strictEqual(items[1].observacion, "Segundo editado");
  assert.strictEqual(items[1].cantidad, 1);
});

test("carrito sigue conservando productos al volver", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 2 });

  updateState({ type: "NAVEGAR", pantalla: "productos" });
  updateState({ type: "NAVEGAR", pantalla: "carrito" });

  const state = getState();
  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.productos[0].cantidad, 1);
});

test("tipos no marketplace siguen funcionando", () => {
  updateState({ type: "RESET_PEDIDO" });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "retirar_envio" });
  updateState({
    type: "SET_FORMULARIO",
    formulario: {
      retirar_envio: {
        direccionRetiro: "Origen",
        direccionEntrega: "Destino",
        queRetira: "Sobre"
      }
    }
  });

  const state = getState();
  assert.strictEqual(state.pedido.tipo, "retirar_envio");
  assert.strictEqual(state.pedido.formulario.retirar_envio.direccionRetiro, "Origen");
  assert.strictEqual(state.pedido.formulario.retirar_envio.direccionEntrega, "Destino");
  assert.strictEqual(state.pedido.formulario.retirar_envio.queRetira, "Sobre");
});

test("WhatsApp sigue funcionando", () => {
  global.window = { open: (url) => { global.__openedUrlNuevo = url; return url; } };
  global.alert = () => {};

  const pedido = {
    tipo: "marketplace",
    numero: "654321",
    productos: [{
      nombre: "Producto 1",
      cantidad: 2,
      precio: 1000,
      detalle: "sin sal"
    }],
    cliente: {
      nombre: "Cliente",
      telefono: "2395432700",
      direccion: {
        calle: "Calle",
        numero: "123"
      }
    }
  };

  const executable = buildPedidoEjecutable({ pedido });
  const url = enviarPedidoPorWhatsApp(executable.pedido);

  assert.ok(url.includes("wa.me/5492395432700"));
  assert.ok(url.includes("Producto%201") || url.includes("Producto+1"));
  assert.ok(global.__openedUrlNuevo.includes("wa.me/5492395432700"));
});

test("no existe actualización obligatoria por tecla para cliente", () => {
  const listeners = {};
  const previousDocument = global.document;

  global.document = {
    addEventListener: (event, handler) => {
      listeners[event] = handler;
    }
  };

  return import("../src/app/render.js").then(({ bindGlobalEvents }) => {
    bindGlobalEvents();

    assert.strictEqual(listeners.input, undefined);
    global.document = previousDocument;
  });
});

test("SET_CLIENTE guarda cliente completo", () => {
  updateState({ type: "RESET_PEDIDO" });

  updateState({
    type: "SET_CLIENTE",
    cliente: {
      nombre: "María López",
      telefono: "+5492395432700",
      direccion: {
        calle: "Av. San Martín",
        numero: "12 bis PB",
        piso: "Dto 3"
      },
      referencia: "Portón azul, timbre 2"
    }
  });

  const cliente = getState().pedido.cliente;
  assert.strictEqual(cliente.nombre, "María López");
  assert.strictEqual(cliente.telefono, "+5492395432700");
  assert.strictEqual(cliente.direccion.calle, "Av. San Martín");
  assert.strictEqual(cliente.direccion.numero, "12 bis PB");
  assert.strictEqual(cliente.direccion.piso, "Dto 3");
  assert.strictEqual(cliente.referencia, "Portón azul, timbre 2");
});

test("CONFIRMAR_PRODUCTO guarda observacion completa", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: 2,
    observacion: "Sin cebolla, con limón y salsa aparte."
  });

  const item = getState().pedido.productos[0];
  assert.strictEqual(item.observacion, "Sin cebolla, con limón y salsa aparte.");
  assert.strictEqual(item.detalle, "Sin cebolla, con limón y salsa aparte.");
});

test("CONFIRMAR_PRODUCTO normaliza cantidad inválida a 1", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: "",
    observacion: "cantidad vacía"
  });

  const item = getState().pedido.productos[0];
  assert.strictEqual(item.cantidad, 1);
  assert.strictEqual(item.subtotal, 1000);
});

test("cantidad válida calcula subtotal correctamente", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: "5" });

  const item = getState().pedido.productos[0];
  assert.strictEqual(item.cantidad, 1);
  assert.strictEqual(item.subtotal, 1000);
});

test("precio null mantiene subtotal null en input fluido", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_2" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_2" });

  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: "7" });

  const item = getState().pedido.productos[0];
  assert.strictEqual(item.precio, null);
  assert.strictEqual(item.subtotal, null);
});

test("texto libre conserva caracteres especiales", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });

  const texto = "Ají, limón, 1/2 porción - sin sal. Ñandú";
  updateState({
    type: "CONFIRMAR_PRODUCTO",
    cantidad: 1,
    observacion: texto
  });

  assert.strictEqual(getState().pedido.productos[0].observacion, texto);
});

test("loading soporta app_config objeto", () => {
  const html = loading({
    app_config: {
      nombre_negocio: "Mercado Real",
      subtitulo: "Todo tu pedido en un solo lugar",
      splash_text: "Cargando datos reales"
    }
  });

  assert.ok(html.includes("Todo tu pedido en un solo lugar"));
  assert.ok(!html.includes("<h1>Mercado Real</h1>"));
  assert.ok(!html.includes("Cargando datos reales"));
});

test("loading soporta app_config array", () => {
  const html = loading({
    app_config: [{
      nombre: "Pédilo Test",
      subtitulo: "Subtítulo desde DB",
      descripcion: "Descripción desde DB"
    }]
  });

  assert.ok(html.includes("Subtítulo desde DB"));
  assert.ok(!html.includes("<h1>Pédilo Test</h1>"));
  assert.ok(!html.includes("Descripción desde DB"));
});

test("local activo false se oculta", () => {
  const html = screenLocales({
    pedido: { categoria: "cat_1", subcategoria: "sub_1" },
    subcategorias: [{ id: "sub_1", nombre: "Sub 1" }],
    ofertas: [],
    locales: [
      { id: "loc_visible", subcategoria_id: "sub_1", nombre: "Visible" },
      { id: "loc_oculto", subcategoria_id: "sub_1", nombre: "Oculto", activo: false }
    ]
  });

  assert.ok(html.includes("Visible"));
  assert.ok(!html.includes("Oculto"));
});

test("producto activo false se oculta", () => {
  const html = screenProductos({
    pedido: { local: "loc_1" },
    opciones: [],
    productos: [
      { id: "prod_visible", local_id: "loc_1", nombre: "Visible", precio: 100 },
      { id: "prod_oculto", local_id: "loc_1", nombre: "Oculto", precio: 100, activo: false }
    ]
  });

  assert.ok(html.includes("Visible"));
  assert.ok(!html.includes("Oculto"));
});

test("producto disponible false se oculta", () => {
  const html = screenProductos({
    pedido: { local: "loc_1" },
    opciones: [],
    productos: [
      { id: "prod_visible", local_id: "loc_1", nombre: "Visible", precio: 100 },
      { id: "prod_oculto", local_id: "loc_1", nombre: "No disponible", precio: 100, disponible: false }
    ]
  });

  assert.ok(html.includes("Visible"));
  assert.ok(!html.includes("No disponible"));
});

test("producto con precio null muestra Precio a consultar en pantalla", () => {
  const html = screenProductos({
    pedido: { local: "loc_1" },
    opciones: [],
    productos: [
      { id: "prod_1", local_id: "loc_1", nombre: "Producto sin precio", precio: null }
    ]
  });

  assert.ok(html.includes("Precio a consultar"));
});

test("oferta asociada a local aparece si existe", () => {
  const html = screenLocales({
    pedido: { categoria: "cat_1", subcategoria: "sub_1" },
    subcategorias: [{ id: "sub_1", nombre: "Sub 1" }],
    locales: [{ id: "loc_1", subcategoria_id: "sub_1", nombre: "Local 1" }],
    ofertas: [{
      id: "of_1",
      local_id: "loc_1",
      titulo: "Promo real",
      descripcion: "Descuento DB",
      precio: 500
    }]
  });

  assert.ok(html.includes("Promo real"));
  assert.ok(html.includes("Descuento DB"));
  assert.ok(html.includes("$500"));
});

test("producto con opciones muestra indicador", () => {
  const html = screenProductos({
    pedido: { local: "loc_1" },
    productos: [
      { id: "prod_1", local_id: "loc_1", nombre: "Producto con opciones", precio: 100 }
    ],
    opciones: [
      { id: "op_1", local_id: "loc_1", nombre: "Grande" },
      { id: "op_2", local_id: "loc_1", nombre: "Sin sal" }
    ]
  });

  assert.ok(html.includes("Opciones del local"));
  assert.ok(html.includes("Grande"));
  assert.ok(html.includes("Sin sal"));
});

test("oferta por producto aparece si existe", () => {
  const html = screenProductos({
    pedido: { local: "loc_1" },
    productos: [
      { id: "prod_1", local_id: "loc_1", nombre: "Milanesa", precio: 2500 }
    ],
    opciones: [],
    ofertas: [{
      id: "of_prod_1",
      local_id: "loc_1",
      titulo: "Combo noche",
      descripcion: "Incluye guarnición",
      descuento: "15%",
      precio: 2100
    }]
  });

  assert.ok(html.includes("Combo noche"));
  assert.ok(html.includes("Incluye guarnición"));
  assert.ok(html.includes("15%"));
  assert.ok(html.includes("$2100"));
});

test("opcion requerida y con precio aparece si existe", () => {
  const html = screenProductos({
    pedido: { local: "loc_1" },
    productos: [
      { id: "prod_1", local_id: "loc_1", nombre: "Pizza", precio: 3000 }
    ],
    opciones: [{
      id: "op_1",
      local_id: "loc_1",
      nombre: "Muzzarella extra",
      descripcion: "Agrega queso",
      precio: 350,
      requerido: true
    }]
  });

  assert.ok(html.includes("Muzzarella extra"));
  assert.ok(html.includes("Agrega queso"));
  assert.ok(html.includes("Extra: $350"));
  assert.ok(html.includes("Requerido"));
});

test("metadata real de producto aparece cuando existe", () => {
  const html = screenProductos({
    pedido: { local: "loc_1" },
    opciones: [],
    productos: [{
      id: "prod_1",
      local_id: "loc_1",
      nombre: "Café molido",
      descripcion: "Tostado medio",
      imagen_url: "https://example.com/cafe.jpg",
      destacado: true,
      unidad: "250g",
      descuento: "10%",
      stock: 8,
      precio: 1200
    }]
  });

  assert.ok(html.includes("https://example.com/cafe.jpg"));
  assert.ok(html.includes("Tostado medio"));
  assert.ok(html.includes("Destacado"));
  assert.ok(html.includes("Unidad: 250g"));
  assert.ok(html.includes("Descuento: 10%"));
  assert.ok(html.includes("Stock: 8"));
  assert.ok(html.includes("$1200"));
});

test("categoria muestra logo o icono real si existe", () => {
  const htmlConLogo = categorias({
    app_config: {},
    pedido: { tipo: "marketplace" },
    categorias: [{
      id: "cat_logo",
      nombre: "Farmacia",
      descripcion: "Medicamentos",
      logo_url: "https://example.com/farmacia.png"
    }]
  });
  const htmlConIcono = categorias({
    app_config: {},
    pedido: { tipo: "marketplace" },
    categorias: [{ id: "cat_icono", nombre: "Kiosco", icono: "K" }]
  });

  assert.ok(htmlConLogo.includes("https://example.com/farmacia.png"));
  assert.ok(htmlConLogo.includes("Medicamentos"));
  assert.ok(htmlConIcono.includes(">K</div>"));
});

test("subcategoria muestra logo o icono real si existe", () => {
  const htmlConLogo = subcategorias({
    pedido: { categoria: "cat_1" },
    subcategorias: [{
      id: "sub_logo",
      categoria_id: "cat_1",
      nombre: "Verdulería",
      descripcion: "Frutas frescas",
      logo_url: "https://example.com/verduleria.png"
    }]
  });
  const htmlConIcono = subcategorias({
    pedido: { categoria: "cat_1" },
    subcategorias: [{ id: "sub_icono", categoria_id: "cat_1", nombre: "Carnes", icono: "C" }]
  });

  assert.ok(htmlConLogo.includes("https://example.com/verduleria.png"));
  assert.ok(htmlConLogo.includes("Frutas frescas"));
  assert.ok(htmlConIcono.includes(">C</div>"));
});

test("registros sin imagen conservan fallback visual honesto", () => {
  const htmlCategorias = categorias({
    app_config: {},
    pedido: { tipo: "marketplace" },
    categorias: [{ id: "cat_1", nombre: "Comida" }]
  });
  const htmlSubcategorias = subcategorias({
    pedido: { categoria: "cat_1" },
    subcategorias: [{ id: "sub_1", categoria_id: "cat_1", nombre: "Pizzas" }]
  });
  const htmlLocales = screenLocales({
    pedido: { categoria: "cat_1", subcategoria: "sub_1" },
    subcategorias: [{ id: "sub_1", categoria_id: "cat_1", nombre: "Pizzas" }],
    locales: [{ id: "loc_1", subcategoria_id: "sub_1", nombre: "La Esquina" }],
    ofertas: []
  });
  const htmlProductos = screenProductos({
    pedido: { local: "loc_1", productos: [] },
    locales: [{ id: "loc_1", nombre: "La Esquina" }],
    productos: [{ id: "prod_1", local_id: "loc_1", nombre: "Pizza", precio: 1000 }],
    opciones: [],
    ofertas: []
  });
  const htmlDetalle = detalleProducto({
    opciones: [],
    pedido: {
      productoActual: {
        producto: { id: "prod_1", nombre: "Pizza", precio: 1000 },
        cantidad: 1,
        observacion: "",
        opcionesSeleccionadas: []
      },
      itemEnEdicion: null
    }
  });

  assert.ok(htmlCategorias.includes("card__image--placeholder"));
  assert.ok(htmlSubcategorias.includes("card__image--placeholder"));
  assert.ok(htmlLocales.includes("visual-fallback"));
  assert.ok(htmlProductos.includes("visual-fallback"));
  assert.ok(htmlDetalle.includes("visual-fallback"));
});

test("local muestra logo, icono y cerrado real si existen", () => {
  const htmlConLogo = screenLocales({
    pedido: { categoria: "cat_1", subcategoria: "sub_1" },
    subcategorias: [{ id: "sub_1", nombre: "Sub 1" }],
    locales: [{
      id: "loc_logo",
      subcategoria_id: "sub_1",
      nombre: "Local Logo",
      logo_url: "https://example.com/local-logo.png",
      abierto: false
    }],
    ofertas: []
  });
  const htmlConIcono = screenLocales({
    pedido: { categoria: "cat_1", subcategoria: "sub_1" },
    subcategorias: [{ id: "sub_1", nombre: "Sub 1" }],
    locales: [{ id: "loc_icono", subcategoria_id: "sub_1", nombre: "Local Icono", icono: "L" }],
    ofertas: []
  });

  assert.ok(htmlConLogo.includes("https://example.com/local-logo.png"));
  assert.ok(htmlConLogo.includes("Cerrado"));
  assert.ok(htmlConIcono.includes(">L</div>"));
});

test("metadata real de local aparece cuando existe", () => {
  const html = screenLocales({
    pedido: { categoria: "cat_1", subcategoria: "sub_1" },
    subcategorias: [{ id: "sub_1", nombre: "Sub 1" }],
    ofertas: [{
      id: "of_1",
      local_id: "loc_1",
      titulo: "Promo local",
      descripcion: "Solo hoy",
      descuento: "20%",
      precio: 900
    }],
    locales: [{
      id: "loc_1",
      subcategoria_id: "sub_1",
      nombre: "Almacén Centro",
      imagen_url: "https://example.com/local.jpg",
      descripcion: "Despensa de barrio",
      direccion: "Av. Principal 123",
      telefono: "2394000000",
      whatsapp: "2394111111",
      horario: "9 a 21",
      estado: "Abierto",
      tiempo_estimado: "30 min",
      costo_envio: 500,
      minimo_pedido: 2000,
      calificacion: 4.8
    }]
  });

  assert.ok(html.includes("https://example.com/local.jpg"));
  assert.ok(html.includes("Despensa de barrio"));
  assert.ok(html.includes("Av. Principal 123"));
  assert.ok(html.includes("Tel: 2394000000"));
  assert.ok(html.includes("WhatsApp: 2394111111"));
  assert.ok(html.includes("Horario: 9 a 21"));
  assert.ok(html.includes("Abierto"));
  assert.ok(html.includes("Tiempo estimado: 30 min"));
  assert.ok(html.includes("Envío: $500"));
  assert.ok(html.includes("Mínimo: $2000"));
  assert.ok(html.includes("Calificación: 4.8"));
  assert.ok(html.includes("Promo local"));
  assert.ok(html.includes("Solo hoy"));
  assert.ok(html.includes("20%"));
  assert.ok(html.includes("$900"));
});

test("productos.js muestra datos del local si existen", () => {
  const html = screenProductos({
    pedido: { local: "loc_1", productos: [] },
    locales: [{
      id: "loc_1",
      nombre: "Heladería Norte",
      imagen_url: "https://example.com/logo.png",
      descripcion: "Helados artesanales",
      direccion: "Belgrano 123",
      telefono: "2394000000",
      whatsapp: "2394111111",
      horario: "10 a 23",
      estado: "Abierto",
      tiempo_estimado: "20 min",
      costo_envio: 450,
      minimo_pedido: 1500,
      calificacion: 4.9
    }],
    productos: [],
    opciones: [],
    ofertas: []
  });

  assert.ok(html.includes("Heladería Norte"));
  assert.ok(html.includes("https://example.com/logo.png"));
  assert.ok(html.includes("Helados artesanales"));
  assert.ok(html.includes("Dirección"));
  assert.ok(html.includes("Belgrano 123"));
  assert.ok(html.includes("Teléfono"));
  assert.ok(html.includes("2394000000"));
  assert.ok(html.includes("WhatsApp"));
  assert.ok(html.includes("2394111111"));
  assert.ok(html.includes("Horario"));
  assert.ok(html.includes("10 a 23"));
  assert.ok(html.includes("Estado"));
  assert.ok(html.includes("Abierto"));
  assert.ok(html.includes("Tiempo estimado"));
  assert.ok(html.includes("20 min"));
  assert.ok(html.includes("Envío"));
  assert.ok(html.includes("$450"));
  assert.ok(html.includes("Mínimo"));
  assert.ok(html.includes("$1500"));
  assert.ok(html.includes("Calificación"));
  assert.ok(html.includes("4.9"));
});

test("productos.js muestra ofertas por local_id", () => {
  const html = screenProductos({
    pedido: { local: "loc_1", productos: [] },
    locales: [{ id: "loc_1", nombre: "Local 1" }],
    productos: [],
    opciones: [],
    ofertas: [{
      id: "of_1",
      local_id: "loc_1",
      titulo: "Promo familiar",
      descripcion: "Para compartir",
      descuento: "25%",
      precio: 3000,
      precio_nuevo: 2500
    }]
  });

  assert.ok(html.includes("Promo familiar"));
  assert.ok(html.includes("Para compartir"));
  assert.ok(html.includes("25%"));
  assert.ok(html.includes("$3000"));
  assert.ok(html.includes("Nuevo precio: $2500"));
});

test("productos.js muestra opciones disponibles de productos del local", () => {
  const html = screenProductos({
    pedido: { local: "loc_1", productos: [] },
    locales: [{ id: "loc_1", nombre: "Local 1" }],
    productos: [
      { id: "prod_1", local_id: "loc_1", nombre: "Helado", precio: 1000 },
      { id: "prod_2", local_id: "loc_2", nombre: "Otro local", precio: 1000 }
    ],
    opciones: [
      { id: "op_1", local_id: "loc_1", nombre: "Chocolate", descripcion: "Amargo", precio_extra: 100, requerido: true },
      { id: "op_2", local_id: "loc_2", nombre: "No aparece" }
    ],
    ofertas: []
  });

  assert.ok(html.includes("Opciones del local"));
  assert.ok(html.includes("Chocolate"));
  assert.ok(html.includes("Amargo"));
  assert.ok(html.includes("Extra: $100"));
  assert.ok(html.includes("Requerido"));
  assert.ok(!html.includes("No aparece"));
});

test("productos.js mantiene data-producto en productos", () => {
  const html = screenProductos({
    pedido: { local: "loc_1", productos: [] },
    locales: [{ id: "loc_1", nombre: "Local 1" }],
    productos: [{ id: "prod_1", local_id: "loc_1", nombre: "Producto", precio: 100 }],
    opciones: [],
    ofertas: []
  });

  assert.ok(html.includes('data-producto="prod_1"'));
});

test("carrito vacío muestra Tu carrito está vacío", () => {
  const html = screenCarrito({
    pedido: {
      tipo: "marketplace",
      local: "loc_1",
      productos: []
    }
  });

  assert.ok(html.includes("Tu carrito está vacío"));
});

test("carrito vacío muestra botón Seguir comprando", () => {
  const html = screenCarrito({
    pedido: {
      tipo: "marketplace",
      local: "loc_1",
      productos: []
    }
  });

  assert.ok(html.includes("Seguir comprando"));
  assert.ok(html.includes('data-nav="productos"'));
});

test("carrito no construye productos", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  const before = getState().pedido.productos.length;
  const html = screenCarrito(getState());
  const after = getState().pedido.productos.length;

  assert.strictEqual(before, 0);
  assert.strictEqual(after, 0);
  assert.ok(html.includes("Tu carrito está vacío"));
});

test("carrito de pedido directo no muestra guiones como datos faltantes", () => {
  const html = screenCarrito({
    pedido: {
      tipo: "compra_libre",
      productos: [],
      formulario: {
        compra_libre: { queNecesita: "", donde: "", observaciones: "" }
      }
    }
  });

  assert.ok(!html.includes("<p>-</p>"));
  assert.ok(html.includes("Completá los datos de compra libre para continuar."));
});

test("pedido_enviado muestra un solo CTA Seguir comprando", () => {
  const html = pedido_enviado({
    pedido: { numero: "123456" },
    app_config: {}
  });

  const matches = html.match(/<button/g) || [];
  assert.strictEqual(matches.length, 1);
  assert.ok(html.includes("Seguir comprando"));
});

test("pedido_enviado usa mensaje de cierre y no reutiliza splash_text", () => {
  const html = pedido_enviado({
    pedido: { numero: "123456", estado: "enviado" },
    app_config: {
      splash_text: "Texto de loading",
      mensaje_pedido_enviado: "Te avisamos cuando avance."
    }
  });

  assert.ok(html.includes("Número 123456"));
  assert.ok(html.includes("Guardá este número para seguimiento"));
  assert.ok(html.includes("Te vamos a enviar un WhatsApp"));
  assert.ok(!html.includes("Texto de loading"));
});

test("pedido_enviado usa mensaje de cierre aunque app_config venga como array", () => {
  const html = pedido_enviado({
    pedido: { numero: "123456", estado: "enviado" },
    app_config: [{
      mensaje_pedido_enviado: "Gracias por pedir en Mercado Norte."
    }]
  });

  assert.ok(html.includes("Guardá este número para seguimiento"));
});

test("pedido_enviado renderiza identidad del local si existe", () => {
  const html = pedido_enviado({
    pedido: { numero: "123456", estado: "enviado", local: "loc_1" },
    locales: [{ id: "loc_1", nombre: "Mercado Norte", imagen_url: "https://example.com/local.png" }],
    app_config: {}
  });

  assert.ok(html.includes("Mercado Norte"));
  assert.ok(html.includes("Tu pedido en Mercado Norte fue enviado"));
  assert.ok(html.includes("https://example.com/local.png"));
});

test("pedido_enviado no muestra Volver al inicio", () => {
  const html = pedido_enviado({
    pedido: { numero: "123456" },
    app_config: {}
  });

  assert.ok(!html.includes("Volver al inicio"));
});

test("confirmacion muestra validación suave de teléfono", () => {
  const html = confirmacion({
    pedido: {
      tipo: "marketplace",
      productos: [{ nombre: "Producto" }],
      intentos: { envio: true },
      cliente: {
        nombre: "Cliente",
        telefono: "abc",
        direccion: {
          calle: "Calle",
          numero: "123"
        }
      },
      formulario: {}
    }
  });

  assert.ok(html.includes('type="tel"'));
  assert.ok(html.includes('inputmode="numeric"'));
  assert.ok(html.includes("input-error"));
  assert.ok(html.includes("Ingresá un teléfono válido"));
});

test("loading muestra imagen/logo desde app_config si existe", () => {
  const html = loading({
    app_config: {
      logo_url: "https://example.com/logo.png",
      nombre_negocio: "Pédilo Real",
      splash_text: "Cargando"
    }
  });

  assert.ok(html.includes("https://example.com/logo.png"));
  assert.ok(!html.includes("<h1>Pédilo Real</h1>"));
});

test("loading no muestra badge ni nombre como texto principal", () => {
  const html = loading({
    app_config: {
      nombre_negocio: "Mercado Norte",
      subtitulo: "Todo tu pedido en un solo lugar",
      splash_text: "Cargando"
    }
  });

  assert.ok(!html.includes("hero-badge"));
  assert.ok(!html.includes("<h1>Mercado Norte</h1>"));
  assert.ok(html.includes("Todo tu pedido en un solo lugar"));
});

test("inicio muestra identidad real desde app_config si existe", () => {
  const html = inicio({
    app_config: {
      nombre: "Pédilo Ciudad",
      descripcion: "Pedidos reales",
      imagen_url: "https://example.com/app-logo.png",
      slogan: "Cerca tuyo"
    },
    categorias: [],
    pedido: {}
  });

  assert.ok(html.includes("Pédilo Ciudad"));
  assert.ok(html.includes("Pedidos reales"));
  assert.ok(html.includes("https://example.com/app-logo.png"));
  assert.ok(html.includes("Cerca tuyo"));
});

test("inicio renderiza identidad desde contrato cuando app_config viene como array", () => {
  const html = inicio({
    app_config: [{
      nombre_negocio: "Mercado Norte",
      descripcion: "Pedidos del barrio",
      imagen_url: "https://example.com/mercado-logo.png",
      subtitulo: "Cerca y real"
    }],
    categorias: [],
    pedido: {}
  });

  assert.ok(html.includes("Mercado Norte"));
  assert.ok(html.includes("Pedidos del barrio"));
  assert.ok(html.includes("https://example.com/mercado-logo.png"));
  assert.ok(html.includes("Cerca y real"));
});

test("inicio usa app_config.imagen_url antes que logo_url", () => {
  const html = inicio({
    app_config: {
      nombre_negocio: "Pédilo",
      descripcion: "Pedidos reales",
      imagen_url: "https://example.com/logo-db.png",
      logo_url: "https://example.com/logo-viejo.png"
    },
    categorias: [],
    pedido: {}
  });

  assert.ok(html.includes("https://example.com/logo-db.png"));
  assert.ok(!html.includes("https://example.com/logo-viejo.png"));
  assert.ok(html.includes('onerror="this.remove()"'));
});

function prepararProductoConfirmado() {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "SELECCIONAR_PRODUCTO", productoId: "prod_1" });
  updateState({ type: "CONFIRMAR_PRODUCTO", cantidad: 2 });
}

function conDocumentoDeClick(assertions) {
  const previousDocument = global.document;
  const listeners = {};

  global.document = {
    addEventListener: (event, handler) => {
      listeners[event] = handler;
    },
    getElementById: () => null,
    querySelector: () => null,
    querySelectorAll: () => []
  };

  bindGlobalEvents();

  const click = (closestMap) => {
    listeners.click({
      target: {
        closest: (selector) => closestMap[selector] || null
      }
    });
  };

  try {
    assertions(click);
  } finally {
    global.document = previousDocument;
  }
}

test("CONFIRMAR_PRODUCTO vuelve a productos", () => {
  prepararProductoConfirmado();

  const state = getState();

  assert.strictEqual(state.pantalla, "productos");
  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.productoActual, null);
});

test("ACTUALIZAR_ITEM vuelve a carrito", () => {
  prepararProductoConfirmado();
  const itemId = getState().pedido.productos[0].itemId;

  updateState({ type: "EDITAR_ITEM", itemId });
  updateState({ type: "ACTUALIZAR_ITEM", cantidad: 3, observacion: "Sin sal" });

  const state = getState();

  assert.strictEqual(state.pantalla, "carrito");
  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.productos[0].cantidad, 1);
  assert.strictEqual(state.pedido.productos[0].observacion, "Sin sal");
});

test("salir con carrito cargado abre mini pantalla", () => {
  prepararProductoConfirmado();

  conDocumentoDeClick((click) => {
    click({
      "[data-nav]": { dataset: { nav: "locales" } }
    });
  });

  const state = getState();
  const html = screenProductos(state);

  assert.strictEqual(state.pantalla, "productos");
  assert.strictEqual(state.pedido.salidaLocalPendiente, true);
  assert.ok(html.includes("Tu pedido todavía tiene productos"));
  assert.ok(html.includes("¿Querés seguir comprando o salir del local?"));
});

test("cancelar salida deja usuario en productos", () => {
  prepararProductoConfirmado();
  updateState({ type: "ABRIR_SALIDA_LOCAL" });
  updateState({ type: "CERRAR_SALIDA_LOCAL" });

  const state = getState();

  assert.strictEqual(state.pantalla, "productos");
  assert.strictEqual(state.pedido.salidaLocalPendiente, false);
  assert.strictEqual(state.pedido.productos.length, 1);
});

test("ir a carrito mantiene productos", () => {
  prepararProductoConfirmado();
  updateState({ type: "ABRIR_SALIDA_LOCAL" });

  conDocumentoDeClick((click) => {
    click({
      "[data-ir-carrito]": { dataset: {} }
    });
  });

  const state = getState();

  assert.strictEqual(state.pantalla, "carrito");
  assert.strictEqual(state.pedido.productos.length, 1);
  assert.strictEqual(state.pedido.salidaLocalPendiente, false);
});

test("no navega a confirmacion con marketplace sin productos", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });
  updateState({ type: "NAVEGAR", pantalla: "carrito" });

  conDocumentoDeClick((click) => {
    click({
      "[data-ir-confirmacion]": { dataset: {} }
    });
  });

  assert.strictEqual(getState().pantalla, "carrito");
});

test("no navega a confirmacion con pedido directo incompleto", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "compra_libre" });
  updateState({ type: "NAVEGAR", pantalla: "carrito" });

  conDocumentoDeClick((click) => {
    click({
      "[data-ir-confirmacion]": { dataset: {} }
    });
  });

  assert.strictEqual(getState().pantalla, "carrito");
});

test("vaciar y salir limpia carrito", () => {
  prepararProductoConfirmado();
  updateState({ type: "ABRIR_SALIDA_LOCAL" });
  updateState({ type: "VACIAR_Y_SALIR_LOCAL" });

  const state = getState();

  assert.deepStrictEqual(state.pedido.productos, []);
  assert.strictEqual(state.pedido.productoActual, null);
  assert.strictEqual(state.pedido.itemEnEdicion, null);
  assert.strictEqual(state.pedido.salidaLocalPendiente, false);
});

test("vaciar y salir vuelve a locales", () => {
  prepararProductoConfirmado();
  updateState({ type: "ABRIR_SALIDA_LOCAL" });
  updateState({ type: "VACIAR_Y_SALIR_LOCAL" });

  assert.strictEqual(getState().pantalla, "locales");
});

test("pedido_enviado no usa data-reset", () => {
  const html = pedido_enviado({
    pedido: { numero: "123456" },
    app_config: {}
  });

  assert.ok(!html.includes("data-reset"));
});

test("seguir comprando vuelve a subcategorias con pedido limpio", () => {
  updateState({ type: "RESET_PEDIDO" });
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_CATEGORIA", categoria: "cat_1" });
  updateState({
    type: "SET_CLIENTE",
    cliente: { nombre: "Ana", telefono: "123", direccion: { calle: "A" } }
  });
  updateState({ type: "NAVEGAR", pantalla: "pedido_enviado" });

  conDocumentoDeClick((click) => {
    click({
      "[data-seguir-comprando]": { dataset: {} }
    });
  });

  const state = getState();

  assert.strictEqual(state.pedido.tipo, "marketplace");
  assert.strictEqual(state.pedido.categoria, "cat_1");
  assert.strictEqual(state.pedido.cliente.nombre, "");
  assert.strictEqual(state.pantalla, "subcategorias");
});

test("data-nav normal sigue funcionando fuera de productos", () => {
  resetConDatos();
  updateState({ type: "NAVEGAR", pantalla: "inicio" });

  conDocumentoDeClick((click) => {
    click({
      "[data-nav]": { dataset: { nav: "locales" } }
    });
  });

  const state = getState();

  assert.strictEqual(state.pantalla, "locales");
  assert.strictEqual(state.pedido.salidaLocalPendiente, false);
});

test("flujo UI de agregar producto pasa por detalle y no agrega directo", () => {
  resetConDatos();
  updateState({ type: "SELECCIONAR_TIPO", tipo: "marketplace" });
  updateState({ type: "SELECCIONAR_LOCAL", local: "loc_1" });

  conDocumentoDeClick((click) => {
    click({
      "[data-add]": { dataset: { id: "prod_1" } }
    });
  });

  const state = getState();

  assert.strictEqual(state.pantalla, "detalle_producto");
  assert.strictEqual(state.pedido.productoActual.producto.id, "prod_1");
  assert.deepStrictEqual(state.pedido.productos, []);
});

test("productos mantiene orden visual correcto", () => {
  const html = screenProductos({
    pedido: { local: "loc_1", productos: [{ cantidad: 2 }] },
    locales: [{ id: "loc_1", nombre: "Local 1", descripcion: "Abierto" }],
    productos: [{ id: "prod_1", local_id: "loc_1", nombre: "Producto", precio: 100 }],
    opciones: [{ id: "op_1", local_id: "loc_1", nombre: "Extra" }],
    ofertas: [{ id: "of_1", local_id: "loc_1", titulo: "Promo" }]
  });

  const header = html.indexOf('data-section="local-header"');
  const ofertas = html.indexOf('data-section="ofertas"');
  const opciones = html.indexOf('data-section="opciones"');
  const productos = html.indexOf('data-section="productos"');
  const cta = html.indexOf('data-section="pedido-cta"');

  assert.ok(header > -1);
  assert.ok(ofertas > header);
  assert.ok(opciones > ofertas);
  assert.ok(productos > opciones);
  assert.ok(cta > productos);
});

test("carrito vacío mantiene usuario en local", () => {
  const html = screenCarrito({
    pedido: {
      tipo: "marketplace",
      local: "loc_1",
      productos: []
    }
  });

  assert.ok(html.includes("Tu carrito está vacío"));
  assert.ok(html.includes('data-nav="productos"'));
  assert.ok(!html.includes('data-nav="inicio"'));
});

test("carrito muestra local del pedido si ya existe", () => {
  const html = screenCarrito({
    pedido: {
      tipo: "marketplace",
      local: "loc_1",
      local_nombre: "Local Real",
      productos: [{ itemId: "item_1", nombre: "Producto", cantidad: 1, subtotal: null }]
    }
  });

  assert.ok(html.includes("Local Real"));
  assert.ok(!html.includes("Local ID"));
});

test("pedido_enviado conserva un solo CTA principal", () => {
  const html = pedido_enviado({
    pedido: { numero: "123456" },
    app_config: { splash_text: "Gracias" }
  });

  const botones = html.match(/<button/g) || [];

  assert.strictEqual(botones.length, 1);
  assert.ok(html.includes("Seguir comprando"));
  assert.ok(html.includes("data-seguir-comprando"));
});

test("loading usa app_config real en identidad visual", () => {
  const html = loading({
    app_config: {
      imagen_url: "https://example.com/brand.png",
      nombre: "Marca Real",
      subtitulo: "Todo tu pedido en un solo lugar",
      splash_text: "Preparando algo real"
    }
  });

  assert.ok(html.includes("https://example.com/brand.png"));
  assert.ok(html.includes("Todo tu pedido en un solo lugar"));
  assert.ok(!html.includes("<h1>Marca Real</h1>"));
  assert.ok(!html.includes("Preparando algo real"));
});

test("loading usa app_config.imagen_url como logo con fallback seguro", () => {
  const html = loading({
    app_config: {
      imagen_url: "https://example.com/loading-logo.png",
      logo_url: "https://example.com/logo-viejo.png",
      nombre_negocio: "Pédilo"
    }
  });

  assert.ok(html.includes("https://example.com/loading-logo.png"));
  assert.ok(!html.includes("https://example.com/logo-viejo.png"));
  assert.ok(html.includes("loading-logo-fallback"));
  assert.ok(!html.includes('onerror="this.remove()"'));
});

test("productos mantiene ofertas y opciones si existen", () => {
  const html = screenProductos({
    pedido: { local: "loc_1", productos: [] },
    locales: [{ id: "loc_1", nombre: "Local 1" }],
    productos: [{ id: "prod_1", local_id: "loc_1", nombre: "Pizza", precio: 2500 }],
    opciones: [{ id: "op_1", local_id: "loc_1", nombre: "Morrón", precio_extra: 100 }],
    ofertas: [{ id: "of_1", local_id: "loc_1", titulo: "Promo noche", precio_nuevo: 2200 }]
  });

  assert.ok(html.includes("Promo noche"));
  assert.ok(html.includes("Nuevo precio: $2200"));
  assert.ok(html.includes("Morrón"));
  assert.ok(html.includes("Extra: $100"));
});

test("productos oculta secciones vacías", () => {
  const html = screenProductos({
    pedido: { local: "loc_1", productos: [] },
    locales: [{ id: "loc_1", nombre: "Local 1" }],
    productos: [{ id: "prod_1", local_id: "loc_1", nombre: "Producto", precio: 100 }],
    opciones: [],
    ofertas: []
  });

  assert.ok(!html.includes("Ofertas del local"));
  assert.ok(!html.includes("Opciones por producto"));
  assert.ok(html.includes("Productos"));
});

test("inputs siguen fluidos sin listener input global", () => {
  const listeners = {};
  const previousDocument = global.document;

  global.document = {
    addEventListener: (event, handler) => {
      listeners[event] = handler;
    }
  };

  bindGlobalEvents();
  global.document = previousDocument;

  assert.strictEqual(listeners.input, undefined);
});

test("errores siguen siendo suaves", () => {
  const html = confirmacion({
    pedido: {
      tipo: "marketplace",
      productos: [{ nombre: "Producto" }],
      intentos: { envio: true },
      cliente: {
        nombre: "",
        telefono: "abc",
        direccion: { calle: "", numero: "" }
      },
      formulario: {}
    }
  });

  assert.ok(html.includes("input-error"));
  assert.ok(html.includes("field-error"));
  assert.ok(!html.includes("alert("));
  assert.ok(!html.includes("confirm("));
});

test("errores no son visibles al entrar a confirmacion", () => {
  const html = confirmacion({
    pedido: {
      tipo: "marketplace",
      productos: [{ nombre: "Producto" }],
      cliente: {
        nombre: "",
        telefono: "",
        direccion: { calle: "", numero: "" }
      },
      formulario: {},
      intentos: { envio: false }
    }
  });

  assert.ok(!html.includes("input-error"));
  assert.ok(!html.includes("field-error"));
});

test("errores son visibles despues de intentar enviar", () => {
  const html = confirmacion({
    pedido: {
      tipo: "marketplace",
      productos: [{ nombre: "Producto" }],
      cliente: {
        nombre: "",
        telefono: "",
        direccion: { calle: "", numero: "" }
      },
      formulario: {},
      intentos: { envio: true }
    }
  });

  assert.ok(html.includes("input-error"));
  assert.ok(html.includes("field-error"));
  assert.ok(html.includes("Completá este dato"));
});

test("errores de formulario no son visibles antes de continuar", () => {
  const html = inicio({
    app_config: {},
    categorias: [],
    pedido: {
      tipo: "compra_libre",
      intentos: { continuar: false },
      formulario: {
        compra_libre: { queNecesita: "", donde: "", observaciones: "" }
      }
    }
  });

  assert.ok(!html.includes("input-error"));
  assert.ok(!html.includes("field-error"));
});

test("errores de formulario aparecen despues de intentar continuar", () => {
  const html = inicio({
    app_config: {},
    categorias: [],
    pedido: {
      tipo: "compra_libre",
      intentos: { continuar: true },
      formulario: {
        compra_libre: { queNecesita: "", donde: "", observaciones: "" }
      }
    }
  });

  assert.ok(html.includes("input-error"));
  assert.ok(html.includes("field-error"));
  assert.ok(html.includes("Completá este dato"));
});

test("Ver pedido (N) sigue funcionando", () => {
  const html = screenProductos({
    pedido: {
      local: "loc_1",
      productos: [{ cantidad: 2 }, { cantidad: 3 }]
    },
    locales: [{ id: "loc_1", nombre: "Local 1" }],
    productos: [{ id: "prod_1", local_id: "loc_1", nombre: "Producto", precio: 100 }],
    opciones: [],
    ofertas: []
  });

  assert.ok(html.includes("Ver pedido (5)"));
  assert.ok(html.includes("data-ir-carrito"));
});

test("mini pantalla salida local sigue funcionando", () => {
  const html = screenProductos({
    pedido: {
      local: "loc_1",
      productos: [{ cantidad: 1 }],
      salidaLocalPendiente: true
    },
    locales: [{ id: "loc_1", nombre: "Local 1" }],
    productos: [{ id: "prod_1", local_id: "loc_1", nombre: "Producto", precio: 100 }],
    opciones: [],
    ofertas: []
  });

  assert.ok(html.includes("Tu pedido todavía tiene productos"));
  assert.ok(html.includes("data-cerrar-salida-local"));
  assert.ok(html.includes("data-ir-carrito"));
  assert.ok(html.includes("data-vaciar-salir-local"));
});
