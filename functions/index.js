"use strict";

const {onCall, HttpsError} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const crypto = require("node:crypto");

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
const LIVE_ORDER_VERSION = 1;
const FINANCIAL_STATUS_PENDING = "pending_review";
const COMMUNICATION_STATUS_RECEIVED = "received";
const INCIDENT_STATUS_NONE = "none";
const ARCHIVE_STATUS_LIVE = "live";
const RESPONSIBLE_ADMIN = "admin";
const ASSIGNED_ACTOR_UNASSIGNED = "";
const INITIAL_TIMEOUT_POLICY = {
  code: "initial_admin_review",
  mode: "declarative",
  duration: "",
  nextFallback: "admin_review_required",
};
const INITIAL_FALLBACK_POLICY = {
  code: "admin_review_required",
  responsibleRole: RESPONSIBLE_ADMIN,
  publicStatus: PUBLIC_STATUS,
};
const ADMIN_ACTIONS = {
  MARK_ADMIN_REVIEWED: "mark_admin_reviewed",
  CONFIRM_INTERVENTION: "confirm_intervention",
  MARK_INCIDENT: "mark_incident",
  RESOLVE_INCIDENT: "resolve_incident",
  CANCEL_BY_ADMIN: "cancel_by_admin",
  FORCE_STATUS: "force_status",
  ASSIGN_RESPONSIBLE: "assign_responsible",
  CLEAR_RESPONSIBLE: "clear_responsible",
};
const TERMINAL_STATUSES = ["cancelled", "canceled", "delivered", "closed", "archived"];
const FORCEABLE_STATUSES = {
  created: "Pedido recibido",
  preparing: "Pedido en preparación",
  on_the_way: "Pedido en camino",
  delivered: "Pedido cerrado",
  under_review: "Pedido en revisión operativa",
};

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
  const idempotencyKey = publicIdempotencyKey(LOCAL_SOURCE, clean);
  const orderRef = db.collection(ORDERS).doc(idempotencyKey);
  const trackingNumber = publicNumberFor(orderRef.id);
  const now = admin.firestore.FieldValue.serverTimestamp();
  const liveContract = liveBirthContract({
    orderType: "local_order",
    source: LOCAL_SOURCE,
    idempotencyKey,
    trackingNumber,
    snapshot: {
      orderType: "local_order",
      source: LOCAL_SOURCE,
      store: {
        id: clean.storeId,
        name: storeName,
      },
      items,
      pricing: {
        subtotal,
        total: subtotal,
        paymentMethod: clean.paymentMethod,
      },
      note: clean.note,
    },
  });

  await createOrderWithInitialEvent(orderRef, {
    ...liveContract,
    storeId: clean.storeId,
    storeName,
    customer: clean.customer,
    items,
    note: clean.note,
    paymentMethod: clean.paymentMethod,
    subtotal,
    total: subtotal,
    createdAt: now,
    updatedAt: now,
  }, now);

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
  const idempotencyKey = publicIdempotencyKey(clean.source, clean);
  const orderRef = db.collection(ORDERS).doc(idempotencyKey);
  const trackingNumber = publicNumberFor(orderRef.id);
  const now = admin.firestore.FieldValue.serverTimestamp();
  const orderData = plusOrderData(clean, trackingNumber, now, idempotencyKey);

  await createOrderWithInitialEvent(orderRef, orderData, now);

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

exports.getPublicOrderTracking = onCall({region: REGION}, async (request) => {
  const trackingNumber = normalizeTrackingNumber(request.data && request.data.trackingNumber);
  if (!trackingNumber || isPlaceholder(trackingNumber)) {
    throw new HttpsError("invalid-argument", "Ingresá un número de pedido válido.");
  }

  const byTracking = await db.collection(ORDERS).where("trackingNumber", "==", trackingNumber).limit(1).get();
  const snapshot = byTracking.empty
    ? await db.collection(ORDERS).where("publicOrderNumber", "==", trackingNumber).limit(1).get()
    : byTracking;

  if (snapshot.empty) {
    return {
      found: false,
      trackingNumber,
      publicStatus: "No encontramos ese pedido",
      status: "UNDER_REVIEW",
      humanMessage: "Revisá el número e intentá de nuevo.",
      orderType: "",
      storeName: "",
      summary: "",
      isClosed: false,
    };
  }

  return publicTrackingResponse(snapshot.docs[0].data(), trackingNumber);
});

exports.adminOrderAction = onCall({region: REGION}, async (request) => {
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Iniciá sesión como Admin para operar pedidos.");
  }

  const userSnap = await db.collection("users").doc(uid).get();
  if (!userSnap.exists || userSnap.get("role") !== "admin") {
    throw new HttpsError("permission-denied", "Solo Admin puede ejecutar esta acción.");
  }

  const clean = cleanAdminActionPayload(request.data || {});
  const orderRef = db.collection(ORDERS).doc(clean.orderId);
  const eventRef = orderRef.collection("events").doc();
  const incidentRef = clean.action === ADMIN_ACTIONS.MARK_INCIDENT ? orderRef.collection("incidents").doc() : null;

  const result = await db.runTransaction(async (tx) => {
    const orderSnap = await tx.get(orderRef);
    if (!orderSnap.exists) {
      throw new HttpsError("not-found", "No encontramos ese pedido.");
    }

    const current = operationalOrderState(orderSnap.data() || {});
    if (clean.expectedVersion !== null && current.version !== clean.expectedVersion) {
      throw new HttpsError("failed-precondition", "El pedido cambió. Actualizá la vista antes de operar.");
    }
    const allowed = allowedAdminActions(current);
    if (!allowed.includes(clean.action)) {
      throw new HttpsError("failed-precondition", "Esta acción no está disponible para el estado actual del pedido.");
    }

    const now = admin.firestore.FieldValue.serverTimestamp();
    const effect = adminActionEffect(clean, current);
    const next = {...current, ...effect.patch};
    next.nextAllowedActions = allowedAdminActions(next);

    const event = {
      type: clean.action,
      summary: effect.eventSummary,
      reason: clean.reason,
      actorUid: uid,
      actorRole: "admin",
      previousStatus: current.status,
      nextStatus: next.status,
      previousOperationalStatus: current.operationalStatus,
      nextOperationalStatus: next.operationalStatus,
      createdAt: now,
    };

    tx.update(orderRef, {
      ...effect.patch,
      version: current.version + 1,
      nextAllowedActions: next.nextAllowedActions,
      lastOperationEvent: {
        type: clean.action,
        summary: effect.eventSummary,
        actorRole: "admin",
        createdAt: now,
      },
      updatedAt: now,
    });
    tx.set(eventRef, event);
    if (incidentRef) {
      tx.set(incidentRef, {
        status: "open",
        reason: clean.reason,
        actorUid: uid,
        actorRole: "admin",
        createdAt: now,
        updatedAt: now,
      });
    }

    return {
      orderId: clean.orderId,
      status: next.status,
      publicStatus: next.publicStatus,
      operationalStatus: next.operationalStatus,
      responsibleRole: next.responsibleRole,
      priority: next.priority,
      needsAttention: next.needsAttention,
      activeIncident: next.activeIncident,
      nextAllowedActions: next.nextAllowedActions,
      eventSummary: effect.eventSummary,
      humanMessage: effect.humanMessage,
    };
  });

  return result;
});

function cleanAdminActionPayload(payload) {
  const orderId = cleanText(payload.orderId);
  const action = cleanText(payload.action);
  const reason = cleanText(payload.reason);
  const forcedStatus = cleanText(payload.forcedStatus).toLowerCase();
  const responsibleRole = cleanText(payload.responsibleRole);
  const expectedVersion = Number.isInteger(payload.expectedVersion) ? payload.expectedVersion : null;

  if (!orderId || !Object.values(ADMIN_ACTIONS).includes(action)) {
    throw new HttpsError("invalid-argument", "Elegí una acción válida para este pedido.");
  }
  if ([
    ADMIN_ACTIONS.MARK_INCIDENT,
    ADMIN_ACTIONS.RESOLVE_INCIDENT,
    ADMIN_ACTIONS.CANCEL_BY_ADMIN,
    ADMIN_ACTIONS.FORCE_STATUS,
  ].includes(action) && reason.length < 4) {
    throw new HttpsError("invalid-argument", "Ingresá un motivo operativo claro.");
  }
  if (action === ADMIN_ACTIONS.FORCE_STATUS && !Object.hasOwn(FORCEABLE_STATUSES, forcedStatus)) {
    throw new HttpsError("invalid-argument", "Elegí un estado permitido por contrato.");
  }
  if (action === ADMIN_ACTIONS.ASSIGN_RESPONSIBLE && responsibleRole && responsibleRole !== "admin") {
    throw new HttpsError("invalid-argument", "V1 sólo permite asignar responsable Admin.");
  }

  return {
    orderId,
    action,
    reason,
    forcedStatus,
    responsibleRole: responsibleRole || "admin",
    expectedVersion,
  };
}

function operationalOrderState(order) {
  const status = cleanText(order.status) || STATUS;
  return {
    status,
    publicStatus: cleanText(order.publicStatus) || PUBLIC_STATUS,
    operationalStatus: cleanText(order.operationalStatus) || status,
    responsibleRole: cleanText(order.responsibleRole),
    priority: cleanText(order.priority) || (order.activeIncident || order.needsAttention ? "high" : "normal"),
    needsAttention: order.needsAttention === true,
    activeIncident: order.activeIncident === true,
    adminReviewed: order.adminReviewed === true,
    version: Number.isInteger(order.version) ? order.version : LIVE_ORDER_VERSION,
  };
}

function allowedAdminActions(order) {
  const status = cleanText(order.status).toLowerCase();
  if (TERMINAL_STATUSES.includes(status)) return [];

  const actions = [];
  if (order.adminReviewed !== true) actions.push(ADMIN_ACTIONS.MARK_ADMIN_REVIEWED);
  actions.push(ADMIN_ACTIONS.CONFIRM_INTERVENTION);
  actions.push(order.activeIncident === true ? ADMIN_ACTIONS.RESOLVE_INCIDENT : ADMIN_ACTIONS.MARK_INCIDENT);
  actions.push(ADMIN_ACTIONS.CANCEL_BY_ADMIN);
  actions.push(ADMIN_ACTIONS.FORCE_STATUS);
  actions.push(cleanText(order.responsibleRole) ? ADMIN_ACTIONS.CLEAR_RESPONSIBLE : ADMIN_ACTIONS.ASSIGN_RESPONSIBLE);
  return actions;
}

function adminActionEffect(clean, current) {
  switch (clean.action) {
    case ADMIN_ACTIONS.MARK_ADMIN_REVIEWED:
      return {
        patch: {
          adminReviewed: true,
          needsAttention: current.activeIncident,
          priority: current.activeIncident ? "high" : "normal",
          operationalStatus: "admin_reviewed",
        },
        eventSummary: "Admin marcó el pedido como revisado.",
        humanMessage: "Pedido marcado como revisado.",
      };
    case ADMIN_ACTIONS.CONFIRM_INTERVENTION:
      return {
        patch: {
          adminReviewed: true,
          needsAttention: true,
          responsibleRole: "admin",
          priority: current.activeIncident ? "high" : "medium",
          operationalStatus: "admin_intervention",
        },
        eventSummary: "Admin confirmó intervención operativa.",
        humanMessage: "Intervención operativa registrada.",
      };
    case ADMIN_ACTIONS.MARK_INCIDENT:
      return {
        patch: {
          activeIncident: true,
          needsAttention: true,
          responsibleRole: "admin",
          priority: "high",
          operationalStatus: "incident_open",
          publicStatus: "Pedido en revisión operativa",
        },
        eventSummary: `Admin abrió incidencia: ${clean.reason}`,
        humanMessage: "Incidencia operativa abierta.",
      };
    case ADMIN_ACTIONS.RESOLVE_INCIDENT:
      return {
        patch: {
          activeIncident: false,
          needsAttention: false,
          priority: "normal",
          operationalStatus: "incident_resolved",
          publicStatus: FORCEABLE_STATUSES[current.status] || current.publicStatus || PUBLIC_STATUS,
        },
        eventSummary: `Admin resolvió incidencia: ${clean.reason}`,
        humanMessage: "Incidencia resuelta.",
      };
    case ADMIN_ACTIONS.CANCEL_BY_ADMIN:
      return {
        patch: {
          status: "cancelled",
          publicStatus: "Pedido cerrado",
          operationalStatus: "cancelled_by_admin",
          activeIncident: false,
          needsAttention: false,
          priority: "closed",
          cancellationReason: clean.reason,
          cancelledByRole: "admin",
        },
        eventSummary: `Admin canceló el pedido: ${clean.reason}`,
        humanMessage: "Pedido cancelado por Admin.",
      };
    case ADMIN_ACTIONS.FORCE_STATUS:
      return {
        patch: {
          status: clean.forcedStatus,
          publicStatus: FORCEABLE_STATUSES[clean.forcedStatus],
          operationalStatus: `forced_${clean.forcedStatus}`,
          needsAttention: clean.forcedStatus === "under_review",
          priority: clean.forcedStatus === "under_review" ? "high" : "normal",
        },
        eventSummary: `Admin forzó estado a ${clean.forcedStatus}: ${clean.reason}`,
        humanMessage: "Estado actualizado por contrato operativo.",
      };
    case ADMIN_ACTIONS.ASSIGN_RESPONSIBLE:
      return {
        patch: {
          responsibleRole: clean.responsibleRole,
          operationalStatus: "responsible_assigned",
        },
        eventSummary: "Admin asignó responsable operativo.",
        humanMessage: "Responsable operativo asignado.",
      };
    case ADMIN_ACTIONS.CLEAR_RESPONSIBLE:
      return {
        patch: {
          responsibleRole: "",
          operationalStatus: "responsible_cleared",
        },
        eventSummary: "Admin limpió responsable operativo.",
        humanMessage: "Responsable operativo limpiado.",
      };
    default:
      throw new HttpsError("invalid-argument", "Acción operativa inválida.");
  }
}

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

function plusOrderData(clean, trackingNumber, now, idempotencyKey) {
  const orderType = clean.requestType === "buy" ? "direct_purchase" : "pickup_shipping";
  const base = liveBirthContract({
    orderType,
    source: clean.source,
    idempotencyKey,
    trackingNumber,
    snapshot: {
      orderType,
      source: clean.source,
      requestType: clean.requestType,
      items: clean.items,
      sourceReference: clean.sourceReference,
      destination: clean.destination,
      paymentMethod: clean.paymentMethod,
      amount: clean.amount,
      schedule: clean.schedule,
      note: clean.note,
    },
  });

  const plusBase = {
    ...base,
    source: clean.source,
    requestType: clean.requestType,
    customer: clean.customer,
    note: clean.note,
    paymentMethod: clean.paymentMethod,
    amount: clean.amount,
    schedule: clean.schedule,
    createdAt: now,
    updatedAt: now,
  };

  if (clean.requestType === "buy") {
    return {
      ...plusBase,
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
    ...plusBase,
    pickupShipping: {
      pickupAddress: clean.sourceReference,
      deliveryAddress: clean.destination,
      packageDescription: clean.items[0].name,
      receiverName: clean.items[0].detail,
      referenceName: clean.items[0].detail,
    },
  };
}

function liveBirthContract({orderType, source, idempotencyKey, trackingNumber, snapshot}) {
  const responsibleRole = RESPONSIBLE_ADMIN;
  const nextAllowedActions = allowedAdminActions({
    status: STATUS,
    adminReviewed: false,
    activeIncident: false,
    responsibleRole,
  });

  return {
    orderType,
    source,
    status: STATUS,
    publicStatus: PUBLIC_STATUS,
    operationalStatus: "waiting_admin_review",
    financialStatus: FINANCIAL_STATUS_PENDING,
    communicationStatus: COMMUNICATION_STATUS_RECEIVED,
    incidentStatus: INCIDENT_STATUS_NONE,
    archiveStatus: ARCHIVE_STATUS_LIVE,
    currentResponsibleRole: responsibleRole,
    responsibleRole,
    assignedActorId: ASSIGNED_ACTOR_UNASSIGNED,
    assignedActorRole: ASSIGNED_ACTOR_UNASSIGNED,
    priority: "normal",
    needsAttention: true,
    activeIncident: false,
    adminReviewed: false,
    nextAllowedActions,
    liveSnapshot: snapshot,
    initialSnapshot: snapshot,
    timeoutPolicy: INITIAL_TIMEOUT_POLICY,
    fallbackPolicy: INITIAL_FALLBACK_POLICY,
    version: LIVE_ORDER_VERSION,
    idempotencyKey,
    trackingNumber,
    publicOrderNumber: trackingNumber,
    isPublicCreated: true,
  };
}

async function createOrderWithInitialEvent(orderRef, orderData, now) {
  const eventRef = orderRef.collection("events").doc("initial");
  await db.runTransaction(async (tx) => {
    const existing = await tx.get(orderRef);
    if (existing.exists) return;

    tx.create(orderRef, orderData);
    tx.create(eventRef, {
      type: "order_created",
      summary: "Pedido Vivo Universal creado desde canal público.",
      actorRole: "public_user",
      actorUid: "",
      source: orderData.source,
      orderType: orderData.orderType,
      previousStatus: "",
      nextStatus: orderData.status,
      previousOperationalStatus: "",
      nextOperationalStatus: orderData.operationalStatus,
      version: orderData.version,
      idempotencyKey: orderData.idempotencyKey,
      createdAt: now,
    });
  });
}

function publicIdempotencyKey(source, payload) {
  const hash = crypto
    .createHash("sha256")
    .update(stableStringify({source, payload: publicIdempotencyPayload(payload)}))
    .digest("hex")
    .slice(0, 24);
  return `ord_${hash}`;
}

function publicIdempotencyPayload(payload) {
  return JSON.parse(stableStringify(payload));
}

function stableStringify(value) {
  if (Array.isArray(value)) {
    return `[${value.map(stableStringify).join(",")}]`;
  }
  if (value && typeof value === "object") {
    return `{${Object.keys(value).sort().map((key) => `${JSON.stringify(key)}:${stableStringify(value[key])}`).join(",")}}`;
  }
  return JSON.stringify(value);
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
  const clean = orderId.replace(/^ord_/i, "").replace(/[^a-z0-9]/gi, "");
  const suffix = clean.slice(-6).toUpperCase();
  return `PDL-${suffix}`;
}

function normalizeTrackingNumber(value) {
  return cleanText(value).replace(/\s+/g, "").toUpperCase();
}

function publicTrackingResponse(order, requestedTrackingNumber) {
  const status = publicStatusCode(order.status);
  const terminal = isTerminalStatus(order.status);
  const trackingNumber = cleanText(order.trackingNumber || order.publicOrderNumber || requestedTrackingNumber);
  const publicStatus = terminal ? "Pedido cerrado" : cleanText(order.publicStatus) || PUBLIC_STATUS;

  if (terminal) {
    return {
      found: true,
      trackingNumber,
      publicStatus,
      status,
      humanMessage: "Gracias por tu pedido. Este pedido ya fue cerrado.",
      orderType: publicOrderType(order),
      storeName: "",
      summary: "",
      isClosed: true,
    };
  }

  return {
    found: true,
    trackingNumber,
    publicStatus,
    status,
    humanMessage: "Estamos siguiendo tu pedido con el estado actual disponible.",
    orderType: publicOrderType(order),
    storeName: cleanText(order.storeName),
    summary: publicOrderSummary(order),
    isClosed: false,
  };
}

function publicStatusCode(status) {
  const clean = cleanText(status).toLowerCase();
  if (["preparing", "in_preparation"].includes(clean)) return "PREPARING";
  if (["on_the_way", "shipping", "delivering"].includes(clean)) return "ON_THE_WAY";
  if (["delivered", "closed", "archived"].includes(clean)) return "DELIVERED";
  if (["cancelled", "canceled"].includes(clean)) return "CANCELLED";
  if (["under_review", "review"].includes(clean)) return "UNDER_REVIEW";
  return "RECEIVED";
}

function isTerminalStatus(status) {
  return ["delivered", "closed", "archived", "cancelled", "canceled"].includes(cleanText(status).toLowerCase());
}

function publicOrderType(order) {
  if (order.source === LOCAL_SOURCE) return "Local";
  if (order.source === PLUS_BUY_SOURCE) return "Compra";
  if (order.source === PLUS_PICKUP_SHIPPING_SOURCE) return "Retiro / Envío";
  return "Pedido";
}

function publicOrderSummary(order) {
  if (order.source === LOCAL_SOURCE && Array.isArray(order.items)) {
    return order.items.map((item) => cleanText(item.name)).filter(Boolean).slice(0, 3).join(", ");
  }
  if (order.source === PLUS_BUY_SOURCE && order.purchase) {
    return cleanText(order.purchase.itemsText).split("\n").filter(Boolean).slice(0, 3).join(", ");
  }
  if (order.source === PLUS_PICKUP_SHIPPING_SOURCE && order.pickupShipping) {
    return cleanText(order.pickupShipping.packageDescription);
  }
  return "";
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
