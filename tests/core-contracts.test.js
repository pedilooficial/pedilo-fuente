// tests/core-contracts.test.js

import test from "node:test";
import assert from "node:assert";
import { getCoreCapabilities } from "../src/core/capabilities.js";
import { buildScreenContract } from "../src/core/contracts.js";
import {
  canEnviarPedido,
  canGoToConfirmacion,
  faltanOpcionesRequeridas
} from "../src/core/guards.js";
import {
  selectCategorias,
  selectLocalAbierto,
  selectLocalesForCurrentContext,
  selectProductosForLocal
} from "../src/core/selectors.js";

const stateBase = {
  pantalla: "productos",
  app_config: {},
  categorias: [{ id: "cat_1", nombre: "Comida" }],
  subcategorias: [{ id: "sub_1", categoria_id: "cat_1", nombre: "Pizzas" }],
  locales: [
    { id: "loc_1", categoria_id: "cat_1", subcategoria_id: "sub_1", nombre: "Local 1" },
    { id: "loc_2", categoria_id: "cat_1", subcategoria_id: "sub_1", nombre: "Oculto", activo: false }
  ],
  productos: [
    { id: "prod_1", local_id: "loc_1", nombre: "Pizza", precio: 1000 },
    { id: "prod_2", local_id: "loc_1", nombre: "Oculto", activo: false },
    { id: "prod_3", local_id: "loc_1", nombre: "No disponible", disponible: false }
  ],
  opciones: [
    { id: "op_1", local_id: "loc_1", nombre: "Muzzarella extra", requerido: true }
  ],
  ofertas: [
    { id: "of_1", local_id: "loc_1", titulo: "Promo local" },
    { id: "of_2", local_id: "loc_2", titulo: "Promo otro local" }
  ],
  repartidores: [],
  pedido: {
    tipo: "marketplace",
    categoria: "cat_1",
    subcategoria: "sub_1",
    local: "loc_1",
    productos: [],
    productoActual: null,
    cliente: {
      nombre: "",
      telefono: "",
      direccion: {}
    },
    formulario: {}
  }
};

test("Core expone capacidades desde autoridad interna", () => {
  const capabilities = getCoreCapabilities(stateBase);
  const ids = capabilities.map((capability) => capability.id);

  assert.deepStrictEqual(ids, [
    "marketplace",
    "compra_libre",
    "retirar_envio",
    "pedir_repartidor"
  ]);
});

test("Core puede limitar capacidades desde app_config si la DB lo define", () => {
  const capabilities = getCoreCapabilities({
    ...stateBase,
    app_config: {
      capacidades: ["marketplace", "compra_libre"]
    }
  });

  assert.deepStrictEqual(
    capabilities.map((capability) => capability.id),
    ["marketplace", "compra_libre"]
  );
});

test("Core puede limitar capacidades aunque app_config venga como array", () => {
  const capabilities = getCoreCapabilities({
    ...stateBase,
    app_config: [{
      capacidades: ["marketplace", "pedir_repartidor"]
    }]
  });

  assert.deepStrictEqual(
    capabilities.map((capability) => capability.id),
    ["marketplace", "pedir_repartidor"]
  );
});

test("Contrato de inicio expone identidad visual normalizada desde app_config", () => {
  const contract = buildScreenContract({
    ...stateBase,
    pantalla: "inicio",
    app_config: [{
      nombre_negocio: "Mercado Norte",
      descripcion: "Comprá cerca",
      imagen_url: "https://example.com/logo.png",
      subtitulo: "Hecho en barrio"
    }]
  });
  const identity = contract.blocks.find((item) => item.type === "identity")?.data;

  assert.strictEqual(identity.nombre, "Mercado Norte");
  assert.strictEqual(identity.descripcion, "Comprá cerca");
  assert.strictEqual(identity.imagen, "https://example.com/logo.png");
  assert.strictEqual(identity.subtitulo, "Hecho en barrio");
});

test("Contrato de inicio prioriza app_config.imagen_url como logo visible", () => {
  const contract = buildScreenContract({
    ...stateBase,
    pantalla: "inicio",
    app_config: {
      nombre_negocio: "Mercado Norte",
      imagen_url: "https://example.com/logo-db.png",
      logo_url: "https://example.com/logo-viejo.png"
    }
  });
  const identity = contract.blocks.find((item) => item.type === "identity")?.data;

  assert.strictEqual(identity.imagen, "https://example.com/logo-db.png");
});

test("Core resuelve locales visibles sin depender de screens", () => {
  const locales = selectLocalesForCurrentContext(stateBase);

  assert.deepStrictEqual(
    locales.map((local) => local.id),
    ["loc_1"]
  );
});

test("Core ordena categorias por orden definido en DB", () => {
  const categorias = selectCategorias({
    categorias: [
      { id: "cat_3", nombre: "Tercera", orden: 3 },
      { id: "cat_1", nombre: "Primera", orden: 1 },
      { id: "cat_2", nombre: "Segunda", orden: 2 }
    ]
  });

  assert.deepStrictEqual(
    categorias.map((categoria) => categoria.id),
    ["cat_1", "cat_2", "cat_3"]
  );
});

test("Core resuelve productos visibles del local sin depender de productos.js", () => {
  const productos = selectProductosForLocal(stateBase, "loc_1");

  assert.deepStrictEqual(
    productos.map((producto) => producto.id),
    ["prod_1"]
  );
});

test("Core arma local abierto con productos ofertas y opciones relacionadas", () => {
  const localAbierto = selectLocalAbierto(stateBase);

  assert.strictEqual(localAbierto.local.id, "loc_1");
  assert.deepStrictEqual(
    localAbierto.productos.map((producto) => producto.id),
    ["prod_1"]
  );
  assert.deepStrictEqual(
    localAbierto.ofertas.map((oferta) => oferta.id),
    ["of_1"]
  );
  assert.deepStrictEqual(
    localAbierto.opciones.map((opcion) => opcion.id),
    ["op_1"]
  );
});

test("Contrato de productos expone bloques y acciones sin que UI decida negocio", () => {
  const contract = buildScreenContract(stateBase);
  const blockTypes = contract.blocks
    .filter((block) => block.visible)
    .map((block) => block.type);

  assert.strictEqual(contract.source, "core");
  assert.deepStrictEqual(blockTypes, [
    "local_header",
    "ofertas",
    "opciones",
    "productos",
    "pedido_cta"
  ]);
  assert.deepStrictEqual(
    contract.actions.map((action) => action.id),
    ["SELECCIONAR_PRODUCTO", "IR_CARRITO"]
  );
});

test("Contrato de productos no inventa bloques operativos con DB vacía", () => {
  const contract = buildScreenContract({
    ...stateBase,
    locales: [],
    productos: [],
    opciones: [],
    ofertas: []
  });

  assert.strictEqual(contract.source, "core");
  assert.strictEqual(
    contract.blocks.some((block) => block.type === "productos"),
    true
  );
  assert.strictEqual(
    contract.blocks.some((block) => block.type === "ofertas" && block.visible),
    false
  );
});

test("Core guards bloquean opciones requeridas no seleccionadas", () => {
  assert.strictEqual(
    faltanOpcionesRequeridas(stateBase, "prod_1", []),
    true
  );
  assert.strictEqual(
    faltanOpcionesRequeridas(stateBase, "prod_1", ["Muzzarella extra"]),
    false
  );
});

test("Core guards separan avance a confirmacion de envío operativo", () => {
  const pedidoMarketplace = {
    tipo: "marketplace",
    local: "loc_1",
    productos: [{ itemId: "item_1", nombre: "Pizza", cantidad: 1 }]
  };

  assert.strictEqual(canGoToConfirmacion(pedidoMarketplace), true);
  assert.strictEqual(canEnviarPedido(pedidoMarketplace), false);
});

test("Contrato de confirmacion solo habilita enviar con pedido ejecutable", () => {
  const contract = buildScreenContract({
    ...stateBase,
    pantalla: "confirmacion",
    pedido: {
      ...stateBase.pedido,
      productos: [{ itemId: "item_1", nombre: "Pizza", cantidad: 1 }],
      cliente: {
        nombre: "Ana",
        telefono: "2395432700",
        direccion: {
          calle: "Mitre",
          numero: "123"
        }
      }
    }
  });

  assert.deepStrictEqual(contract.actions, [
    { id: "ENVIAR_PEDIDO", payload: {}, enabled: true }
  ]);
});
