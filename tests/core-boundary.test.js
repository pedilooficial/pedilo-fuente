// tests/core-boundary.test.js

import test from "node:test";
import assert from "node:assert";
import fs from "node:fs";
import path from "node:path";

const screenDir = path.join(process.cwd(), "src", "screens");
const screenFiles = fs
  .readdirSync(screenDir)
  .filter((file) => file.endsWith(".js"))
  .map((file) => path.join(screenDir, file));

function read(file) {
  return fs.readFileSync(file, "utf8");
}

test("screens no importan DB/API/WhatsApp ni salida operativa", () => {
  for (const file of screenFiles) {
    const content = read(file);

    assert.ok(!content.includes("../services/api.js"), file);
    assert.ok(!content.includes("../services/supabase.js"), file);
    assert.ok(!content.includes("../services/whatsapp.js"), file);
    assert.ok(!content.includes("enviarPedidoPorWhatsApp"), file);
  }
});

test("screens no filtran ni cruzan relaciones DB crudas", () => {
  for (const file of screenFiles) {
    const content = read(file);

    assert.ok(!content.includes(".filter("), file);
    assert.ok(!content.includes(".find("), file);
    assert.ok(!content.includes("local_id"), file);
    assert.ok(!content.includes("producto_id"), file);
    assert.ok(!content.includes("categoria_id"), file);
    assert.ok(!content.includes("subcategoria_id"), file);
  }
});

test("render no define validadores de negocio propios", () => {
  const content = read(path.join(process.cwd(), "src", "app", "render.js"));

  assert.ok(!content.includes("function clienteValido"));
  assert.ok(!content.includes("function formularioValido"));
  assert.ok(!content.includes("pedidoListo"));
});

test("render solo envia WhatsApp con PedidoEjecutable autorizado por Core", () => {
  const content = read(path.join(process.cwd(), "src", "app", "render.js"));

  assert.ok(content.includes("buildPedidoEjecutable"));
  assert.ok(content.includes("enviarPedidoPorWhatsApp(executable.pedido)"));
  assert.ok(!content.includes("enviarPedidoPorWhatsApp(getState().pedido)"));
});

test("WhatsApp exige PedidoEjecutable", () => {
  const content = read(path.join(process.cwd(), "src", "services", "whatsapp.js"));

  assert.ok(content.includes("__pedidoEjecutable"));
  assert.ok(content.includes("Pedido no autorizado para enviar"));
});
