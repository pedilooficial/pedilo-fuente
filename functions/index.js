"use strict";

const {onCall, HttpsError} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const REGION = "southamerica-east1";
const ORDERS = "orders";
const STORES = "stores";
const PRODUCTS = "products";
const LOCAL_SOURCE = "public_local";
const PLUS_BUY_SOURCE = "public_plus_buy";
const PLUS_PICKUP_SHIPPING_SOURCE = "public_plus_pickup_shipping";
const STATUS = "created";
const PUBLIC_STATUS = "Pedido recibido";

exports.createLocalOrder = onCall({region: REGION}, async (request) => {
  const payload = request.data || {};
  const clean = cleanOrderPayload(payload);

  const storeSnap = await db.collection(STORES).doc(clean.storeId).get();
  if (!storeSnap.exists || storeSnap.get("visible") !== true) {
    throw new HttpsError("failed-precondition", "El local no está disponible.");
  }

  const storeName = clean.storeName || String(storeSnap.get("name") || "").trim();
  if (!storeName) {
    throw new HttpsError("failed-precondition", "El local no tiene nombre disponible.");
  }

  const items = [];
  for (const item of clean.items) {
    const productSnap = await db
      .collection(STORES)
      .doc(clean.storeId)
      .collection(PRODUCTS)
      .doc(item.productId)
      .get();
    if (!productSnap.exists || productSnap.get("visible") !== true || productSnap.get("available") !== true) {
      throw new HttpsError("failed-precondition", "Hay productos que ya no están disponibles.");
    }

    const productName = String(productSnap.get("name") || "").trim();
    const priceCents = numberOrNull(productSnap.get("priceCents"));
    const unitPrice = priceCents === null ? item.unitPrice : priceCents;
    const total = unitPrice === null ? null : unitPrice * item.quantity;

    items.push({
      productId: item.productId,
      name: productName || item.name,
      quantity: item.quantity,
      unitPrice,
      total,
      note: item.note,
    });
  }

  const subtotal = items.reduce((sum, item) => sum + (item.total || 0), 0);
  const orderRef = db.collection(ORDERS).doc();
  const trackingNumber = publicNumberFor(orderRef.id);
  const now = admin.firestore.FieldValue.serverTimestamp();

  await orderRef.set({
    source: LOCAL_SOURCE,
    status: STATUS,
    publicStatus: PUBLIC_STATUS,
    storeId: clean.storeId,
    storeName,
    customer: clean.customer,
    items,
    note: clean.note,
    paymentMethod: clean.paymentMethod,
    subtotal,
    total: subtotal,
    trackingNumber,
    publicOrderNumber: trackingNumber,
    isPublicCreated: true,
    createdAt: now,
    updatedAt: now,
  });

  return {
    orderId: orderRef.id,
    trackingNumber,
    publicOrderNumber: trackingNumber,
    publicStatus: PUBLIC_STATUS,
    status: "RECEIVED",
    storeName,
    itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
  };
});

exports.createPlusOrder = onCall({region: REGION}, async (request) => {
  const payload = request.data || {};
  const clean = cleanPlusOrderPayload(payload);
  const orderRef = db.collection(ORDERS).doc();
  const trackingNumber = publicNumberFor(orderRef.id);
  const now = admin.firestore.FieldValue.serverTimestamp();
  const orderData = plusOrderData(clean, trackingNumber, now);

  await orderRef.set(orderData);

  return {
    orderId: orderRef.id,
    trackingNumber,
    publicOrderNumber: trackingNumber,
    publicStatus: PUBLIC_STATUS,
    status: "RECEIVED",
    requestLabel: clean.requestType === "buy" ? "Compra" : "Retiro / Envío",
    itemCount: clean.items.length,
  };
});

function cleanOrderPayload(payload) {
  const storeId = cleanText(payload.storeId);
  const storeName = cleanText(payload.storeName);
  const customer = payload.customer || {};
  const name = cleanText(customer.name);
  const phone = cleanText(customer.phone);
  const address = cleanText(customer.address);
  const paymentMethod = cleanText(payload.paymentMethod);
  const note = cleanText(payload.note);
  const rawItems = Array.isArray(payload.items) ? payload.items : [];

  if (!storeId || !storeName || !name || !isValidPhone(phone) || !address || !paymentMethod || rawItems.length === 0) {
    throw new HttpsError("invalid-argument", "Faltan datos para confirmar el pedido.");
  }
  if ([storeId, storeName, name, phone, address, paymentMethod].some(isPlaceholder)) {
    throw new HttpsError("invalid-argument", "Revisá los datos antes de confirmar.");
  }

  const items = rawItems.map(cleanItem);
  if (items.some((item) => !item.productId || !item.name || item.quantity <= 0 || isPlaceholder(item.name))) {
    throw new HttpsError("invalid-argument", "El carrito no es válido.");
  }

  return {
    storeId,
    storeName,
    customer: {name, phone, address},
    paymentMethod,
    note,
    items,
  };
}

function cleanPlusOrderPayload(payload) {
  const requestType = cleanText(payload.requestType);
  const source = cleanText(payload.source);
  const contact = payload.contact || {};
  const name = cleanText(contact.name);
  const phone = cleanText(contact.phone);
  const sourceReference = cleanText(payload.sourceReference);
  const destination = cleanText(payload.destination);
  const note = cleanText(payload.note);
  const paymentMethod = cleanText(payload.paymentMethod);
  const amount = cleanText(payload.amount);
  const schedule = cleanText(payload.schedule);
  const rawItems = Array.isArray(payload.items) ? payload.items : [];

  if (!["buy", "pickup_shipping"].includes(requestType) || !name || !isValidPhone(phone) || !paymentMethod) {
    throw new HttpsError("invalid-argument", "Faltan datos para confirmar el pedido.");
  }
  if (hasPlaceholder([name, phone, sourceReference, destination, paymentMethod])) {
    throw new HttpsError("invalid-argument", "Revisá los datos antes de confirmar.");
  }

  const items = rawItems.map(cleanPlusItem);
  if (requestType === "buy") {
    if (source !== PLUS_BUY_SOURCE || !sourceReference || !destination || items.length === 0) {
      throw new HttpsError("invalid-argument", "Faltan datos para confirmar la compra.");
    }
    if (items.some((item) => !item.name || !item.detail || hasPlaceholder([item.name, item.detail]))) {
      throw new HttpsError("invalid-argument", "Revisá los productos antes de confirmar.");
    }
  } else {
    if (source !== PLUS_PICKUP_SHIPPING_SOURCE || !sourceReference || !destination || items.length !== 1 || !items[0].name) {
      throw new HttpsError("invalid-argument", "Faltan datos para confirmar el retiro.");
    }
    if (hasPlaceholder([sourceReference, destination, items[0].name])) {
      throw new HttpsError("invalid-argument", "Revisá los datos antes de confirmar.");
    }
  }

  return {
    source,
    requestType,
    customer: {name, phone},
    items,
    sourceReference,
    destination,
    note,
    paymentMethod,
    amount,
    schedule,
  };
}

function cleanPlusItem(item) {
  return {
    name: cleanText(item.name),
    detail: cleanText(item.detail),
  };
}

function plusOrderData(clean, trackingNumber, now) {
  const base = {
    source: clean.source,
    status: STATUS,
    publicStatus: PUBLIC_STATUS,
    requestType: clean.requestType,
    customer: clean.customer,
    note: clean.note,
    paymentMethod: clean.paymentMethod,
    amount: clean.amount,
    schedule: clean.schedule,
    trackingNumber,
    publicOrderNumber: trackingNumber,
    isPublicCreated: true,
    createdAt: now,
    updatedAt: now,
  };

  if (clean.requestType === "buy") {
    return {
      ...base,
      purchase: {
        itemsText: clean.items.map((item) => `${item.name} - ${item.detail}`).join("\n"),
        items: clean.items,
        storeReference: clean.sourceReference,
        whereToBuy: clean.sourceReference,
      },
      customer: {
        ...clean.customer,
        address: clean.destination,
      },
    };
  }

  return {
    ...base,
    pickupShipping: {
      pickupAddress: clean.sourceReference,
      deliveryAddress: clean.destination,
      packageDescription: clean.items[0].name,
      receiverName: clean.items[0].detail,
      referenceName: clean.items[0].detail,
    },
  };
}

function cleanItem(item) {
  return {
    productId: cleanText(item.productId),
    name: cleanText(item.name),
    quantity: Number.isInteger(item.quantity) ? item.quantity : Number(item.quantity) || 0,
    unitPrice: numberOrNull(item.unitPrice),
    note: cleanText(item.note),
  };
}

function publicNumberFor(orderId) {
  const suffix = orderId.replace(/[^a-z0-9]/gi, "").slice(0, 6).toUpperCase();
  return `PDL-${suffix}`;
}

function cleanText(value) {
  return typeof value === "string" ? value.trim() : "";
}

function numberOrNull(value) {
  return Number.isFinite(value) ? value : null;
}

function isValidPhone(value) {
  return cleanText(value).replace(/\D/g, "").length >= 6;
}

function isPlaceholder(value) {
  return ["nombre", "tu nombre", "telefono", "teléfono", "direccion", "dirección", "pedido", "producto", "paquete"]
    .includes(cleanText(value).toLowerCase());
}

function hasPlaceholder(values) {
  return values.some(isPlaceholder);
}
