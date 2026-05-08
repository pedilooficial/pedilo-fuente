// tests/api.test.js

import test from "node:test";
import assert from "node:assert";
import {
  cargarDatos,
  mapConfig,
  mapCategorias,
  mapSubcategorias,
  mapLocales,
  mapProductos,
  mapOpciones,
  mapOfertas,
  mapRepartidores
} from "../src/services/api.js";

test("cargarDatos retorna estructura correcta", async () => {
  const data = await cargarDatos();

  assert.ok(data);
  assert.ok(Array.isArray(data.categorias));
  assert.ok(Array.isArray(data.subcategorias));
  assert.ok(Array.isArray(data.locales));
  assert.ok(Array.isArray(data.productos));
  assert.ok(Array.isArray(data.opciones));
  assert.ok(Array.isArray(data.ofertas));
  assert.ok(Array.isArray(data.repartidores));
  assert.ok(data.app_config && typeof data.app_config === "object");
});

test("DB incompleta es válida y no falla", async () => {
  const data = await cargarDatos();

  assert.ok(Array.isArray(data.categorias));
  assert.ok(Array.isArray(data.subcategorias));
  assert.ok(Array.isArray(data.locales));
  assert.ok(Array.isArray(data.productos));
  assert.ok(Array.isArray(data.opciones));
  assert.ok(Array.isArray(data.ofertas));
  assert.ok(Array.isArray(data.repartidores));
});

test("categorias contiene objetos válidos cuando existen", async () => {
  const data = await cargarDatos();

  data.categorias.forEach((cat) => {
    assert.ok(cat.id);
    assert.ok(cat.nombre);
  });
});

test("cargarDatos retorna opciones/ofertas/repartidores como arrays", async () => {
  const data = await cargarDatos();

  assert.ok(Array.isArray(data.opciones));
  assert.ok(Array.isArray(data.ofertas));
  assert.ok(Array.isArray(data.repartidores));
});

test("app_config existe aunque sea objeto vacío", async () => {
  const data = await cargarDatos();

  assert.ok(data.app_config);
  assert.strictEqual(typeof data.app_config, "object");
  assert.ok(!Array.isArray(data.app_config));
});

test("DB vacía válida", () => {
  assert.deepStrictEqual(mapCategorias([]), []);
  assert.deepStrictEqual(mapSubcategorias([]), []);
  assert.deepStrictEqual(mapLocales([]), []);
  assert.deepStrictEqual(mapProductos([]), []);
  assert.deepStrictEqual(mapOpciones([]), []);
  assert.deepStrictEqual(mapOfertas([]), []);
  assert.deepStrictEqual(mapRepartidores([]), []);
});

test("API/fallback no devuelve Local demo", async () => {
  const data = await cargarDatos();
  const nombresLocales = data.locales.map((local) => local.nombre);

  assert.ok(!nombresLocales.includes("Local demo"));
});

test("API/fallback no devuelve Producto demo", async () => {
  const data = await cargarDatos();
  const nombresProductos = data.productos.map((producto) => producto.nombre);

  assert.ok(!nombresProductos.includes("Producto demo"));
});

test("DB vacía no inventa contenido operativo", () => {
  const data = {
    categorias: mapCategorias([]),
    subcategorias: mapSubcategorias([]),
    locales: mapLocales([]),
    productos: mapProductos([]),
    opciones: mapOpciones([]),
    ofertas: mapOfertas([]),
    repartidores: mapRepartidores([])
  };

  assert.deepStrictEqual(data.locales, []);
  assert.deepStrictEqual(data.productos, []);
  assert.deepStrictEqual(data.ofertas, []);
});

test("mapConfig conserva app_config activo con imagen_url real", () => {
  const config = mapConfig([
    {
      id: "cfg_inactiva",
      nombre_negocio: "Viejo",
      imagen_url: "https://example.com/viejo.png",
      activo: false
    },
    {
      id: "cfg_activa",
      nombre_negocio: "Pédilo Real",
      subtitulo: "Pedidos de verdad",
      descripcion: "App configurada desde DB",
      imagen_url: "https://example.com/logo-real.png",
      activo: true
    }
  ]);

  assert.strictEqual(config.nombre_negocio, "Pédilo Real");
  assert.strictEqual(config.subtitulo, "Pedidos de verdad");
  assert.strictEqual(config.descripcion, "App configurada desde DB");
  assert.strictEqual(config.imagen_url, "https://example.com/logo-real.png");
});

test("mappers conservan campos extra", () => {
  const categoria = mapCategorias([{
    id: "cat_real",
    nombre: "Comida",
    descripcion: null,
    color: "rojo"
  }])[0];

  const subcategoria = mapSubcategorias([{
    id: "sub_real",
    categoria_id: "cat_real",
    nombre: "Pizzas",
    orden: 2
  }])[0];

  const local = mapLocales([{
    id: "loc_real",
    subcategoria_id: "sub_real",
    nombre: "Local",
    minimo_pedido: 3000
  }])[0];

  const producto = mapProductos([{
    id: "prod_real",
    local_id: "loc_real",
    nombre: "Producto",
    precio: "123.5",
    sku: "A-1"
  }])[0];

  const opcion = mapOpciones([{
    id: "op_1",
    local_id: "loc_real",
    nombre: "Tamaño",
    requerido: true
  }])[0];

  const oferta = mapOfertas([{
    id: "of_1",
    local_id: "loc_real",
    titulo: "Promo",
    etiqueta: "2x1"
  }])[0];

  const repartidor = mapRepartidores([{
    id: "rep_1",
    nombre: "Ana",
    zona: "Centro"
  }])[0];

  assert.strictEqual(categoria.color, "rojo");
  assert.strictEqual(subcategoria.orden, 2);
  assert.strictEqual(local.minimo_pedido, 3000);
  assert.strictEqual(producto.sku, "A-1");
  assert.strictEqual(producto.precio, 123.5);
  assert.strictEqual(opcion.requerido, true);
  assert.strictEqual(oferta.etiqueta, "2x1");
  assert.strictEqual(repartidor.zona, "Centro");
});

test("productos tienen estructura correcta cuando existen", async () => {
  const data = await cargarDatos();

  data.productos.forEach((producto) => {
    assert.ok(producto.id);
    assert.ok(producto.nombre);

    assert.ok(
      producto.precio === null ||
        producto.precio === undefined ||
        typeof producto.precio === "number",
      "precio debe ser número si existe"
    );
  });
});

test("locales solo apuntan a subcategorias existentes cuando ambas colecciones existen", async () => {
  const data = await cargarDatos();

  if (data.locales.length === 0 || data.subcategorias.length === 0) {
    assert.ok(true);
    return;
  }

  const subcategoriaIds = new Set(data.subcategorias.map((s) => s.id));

  data.locales.forEach((local) => {
    assert.ok(
      subcategoriaIds.has(local.subcategoria_id),
      `local ${local.id} apunta a subcategoria inexistente: ${local.subcategoria_id}`
    );
  });
});

test("productos solo apuntan a locales existentes cuando ambas colecciones existen", async () => {
  const data = await cargarDatos();

  if (data.productos.length === 0 || data.locales.length === 0) {
    assert.ok(true);
    return;
  }

  const localIds = new Set(data.locales.map((l) => l.id));

  data.productos.forEach((producto) => {
    assert.ok(
      localIds.has(producto.local_id),
      `producto ${producto.id} apunta a local inexistente: ${producto.local_id}`
    );
  });
});
