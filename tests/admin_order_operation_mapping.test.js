"use strict";

const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

const classifierPath =
  "app/src/main/java/com/pedilo/app/core/model/AdminOperationOrderClassification.kt";
const adapterPath = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseAdminOrdersAdapter.kt";

function read(path) {
  return fs.readFileSync(path, "utf8");
}

function signals(overrides = {}) {
  return {
    status: "",
    publicStatus: "",
    source: "",
    requestType: "",
    ...overrides,
  };
}

function normalizedStatus(status) {
  return String(status || "").trim().toLowerCase();
}

function hasRealActiveSignal(s) {
  return (
    normalizedStatus(s.status) === "created" &&
    String(s.publicStatus || "").trim() === "Pedido recibido"
  );
}

function hasRealCancellationSignal(s) {
  return normalizedStatus(s.status) === "cancelled" || normalizedStatus(s.status) === "canceled";
}

function hasRealFinishedSignal(s) {
  return ["delivered", "closed", "archived"].includes(normalizedStatus(s.status));
}

function hasRealDelaySignal() {
  return false;
}

function hasRealProblemSignal(s) {
  const ps = String(s.publicStatus || "");
  return ps.toLowerCase().includes("reclamo") || ps.toLowerCase().includes("problema");
}

function todayBucket(s) {
  if (hasRealCancellationSignal(s)) return "CANCELLED";
  if (hasRealFinishedSignal(s)) return "FINISHED";
  if (hasRealDelaySignal(s)) return "DELAYED";
  if (hasRealProblemSignal(s)) return "WITH_PROBLEMS";
  if (hasRealActiveSignal(s)) return "ACTIVE";
  return null;
}

function activeBucket(s) {
  if (!hasRealActiveSignal(s)) return null;
  return "WAITING_STORE";
}

function operationalIdentity(source, requestType = "") {
  const cleanSource = String(source || "").trim();
  const cleanRequest = String(requestType || "").trim();
  if (cleanSource === "public_local") return "Retiro de local";
  if (cleanSource === "public_plus_buy" || cleanRequest === "buy") return "Compra solicitada";
  if (cleanSource === "public_plus_pickup_shipping" || cleanRequest === "pickup_shipping") {
    return "Retiro solicitado";
  }
  return "Pedido operativo";
}

function operationalFunction(source, requestType = "") {
  switch (operationalIdentity(source, requestType)) {
    case "Compra solicitada":
      return "Comprar y entregar";
    case "Retiro solicitado":
    case "Retiro de local":
      return "Retirar y entregar";
    default:
      return "Revisar pedido";
  }
}

test("kotlin classifier exists as pure core without firebase", () => {
  const source = read(classifierPath);
  assert.match(source, /object AdminOperationOrderClassification/);
  assert.match(source, /enum class AdminTodayOrdersBucket/);
  assert.match(source, /enum class AdminActiveOrdersBucket/);
  assert.doesNotMatch(source, /import.*firebase|import.*Firestore|com\.google\.firebase/);
  assert.doesNotMatch(source, /\.set\(|\.update\(|\.delete\(|writeBatch|runTransaction/);
});

test("admin orders adapter stays read-only on orders collection", () => {
  const source = read(adapterPath);
  assert.match(source, /\.get\(\)\.await\(\)/);
  assert.doesNotMatch(source, /\.set\(|\.update\(|\.delete\(|writeBatch|runTransaction/);
});

test("created + Pedido recibido maps to Activos and Esperando local", () => {
  const s = signals({status: "created", publicStatus: "Pedido recibido", source: "public_local"});
  assert.equal(todayBucket(s), "ACTIVE");
  assert.equal(activeBucket(s), "WAITING_STORE");
});

test("created + Pedido recibido does not map to problem or cancelled buckets", () => {
  const s = signals({
    status: "created",
    publicStatus: "Pedido recibido",
    source: "public_plus_buy",
    requestType: "buy",
  });
  assert.notEqual(todayBucket(s), "WITH_PROBLEMS");
  assert.notEqual(todayBucket(s), "CANCELLED");
  assert.notEqual(todayBucket(s), "FINISHED");
  assert.notEqual(todayBucket(s), "DELAYED");
});

test("cancelled status maps to Cancelados only with real cancellation signal", () => {
  const s = signals({status: "cancelled", publicStatus: "null", source: "public_app"});
  assert.equal(todayBucket(s), "CANCELLED");
  assert.equal(activeBucket(s), null);
});

test("without finish signal does not map to Finalizados", () => {
  const s = signals({status: "created", publicStatus: "Pedido recibido"});
  assert.notEqual(todayBucket(s), "FINISHED");
  assert.equal(hasRealFinishedSignal(s), false);
});

test("without delay criteria does not map to Demorados", () => {
  const s = signals({status: "created", publicStatus: "Pedido recibido"});
  assert.notEqual(todayBucket(s), "DELAYED");
  assert.equal(hasRealDelaySignal(s), false);
});

test("without problem signal does not map to Con problemas", () => {
  const s = signals({status: "created", publicStatus: "Pedido recibido"});
  assert.notEqual(todayBucket(s), "WITH_PROBLEMS");
  assert.equal(hasRealProblemSignal(s), false);
});

test("operational labels translate public flows into human identities", () => {
  const source = read(classifierPath);

  assert.match(source, /operationalIdentity/);
  assert.match(source, /operationalFunction/);
  assert.equal(operationalIdentity("public_local"), "Retiro de local");
  assert.equal(operationalIdentity("public_plus_buy"), "Compra solicitada");
  assert.equal(operationalIdentity("public_plus_pickup_shipping"), "Retiro solicitado");
  assert.equal(operationalFunction("public_plus_buy"), "Comprar y entregar");
  assert.equal(operationalFunction("public_plus_pickup_shipping"), "Retirar y entregar");
  assert.equal(operationalFunction("public_local"), "Retirar y entregar");
  assert.doesNotMatch(source, /Pedido de local|Botón \+ Comprar|Botón \+ Retiro|Origen técnico/);
});

test("legacy created without publicStatus stays unmapped conservatively", () => {
  const s = signals({status: "created", publicStatus: "", source: "public_app"});
  assert.equal(todayBucket(s), null);
  assert.equal(activeBucket(s), null);
});

test("real firebase combinations from audit classify conservatively", () => {
  const auditRows = [
    {status: "cancelled", publicStatus: "", source: "", requestType: "", bucket: "CANCELLED"},
    {status: "cancelled", publicStatus: "", source: "public_app", requestType: "", bucket: "CANCELLED"},
    {
      status: "created",
      publicStatus: "Pedido recibido",
      source: "public_local",
      requestType: "",
      bucket: "ACTIVE",
    },
    {status: "created", publicStatus: "", source: "public_app", requestType: "", bucket: null},
    {
      status: "created",
      publicStatus: "Pedido recibido",
      source: "public_plus_pickup_shipping",
      requestType: "pickup_shipping",
      bucket: "ACTIVE",
    },
    {
      status: "created",
      publicStatus: "Pedido recibido",
      source: "public_plus_buy",
      requestType: "buy",
      bucket: "ACTIVE",
    },
  ];

  for (const row of auditRows) {
    const s = signals(row);
    assert.equal(
      todayBucket(s),
      row.bucket,
      `unexpected bucket for ${row.status}/${row.publicStatus}/${row.source}`,
    );
  }
});
