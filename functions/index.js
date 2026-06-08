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
const LIVE_ACTIONS = {
  LOCAL_ACCEPT: "local_accept",
  LOCAL_REJECT: "local_reject",
  LOCAL_MARK_PREPARING: "local_mark_preparing",
  LOCAL_MARK_READY: "local_mark_ready",
  DRIVER_TAKE: "driver_take",
  DRIVER_MARK_PICKED_UP: "driver_mark_picked_up",
  DRIVER_MARK_DELIVERED: "driver_mark_delivered",
  CANCEL_ORDER: "cancel_order",
  OPEN_INCIDENT: "open_incident",
  RESOLVE_INCIDENT: "resolve_incident",
  ADMIN_INTERVENE: "admin_intervene",
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

exports.operateLiveOrder = onCall({region: REGION}, async (request) => {
  const actor = await requireOperationalActor(request);
  const clean = cleanLiveActionPayload(request.data || {}, actor.uid);
  const orderRef = db.collection(ORDERS).doc(clean.orderId);
  const eventRef = orderRef.collection("events").doc(clean.actionId);
  const incidentRef = clean.action === LIVE_ACTIONS.OPEN_INCIDENT ? orderRef.collection("incidents").doc(clean.actionId) : null;

  return db.runTransaction(async (tx) => {
    const eventSnap = await tx.get(eventRef);
    if (eventSnap.exists) {
      const savedResult = eventSnap.get("result");
      return savedResult ? {...savedResult, idempotent: true} : {
        orderId: clean.orderId,
        action: clean.action,
        idempotent: true,
      };
    }

    const orderSnap = await tx.get(orderRef);
    if (!orderSnap.exists) {
      throw new HttpsError("not-found", "No encontramos ese pedido.");
    }

    const current = liveOrderState(orderSnap.data() || {});
    validateLiveActor(actor, current, clean.action);
    validateExpectedVersion(clean, current);

    const allowed = allowedLiveActions(current);
    if (!allowed.includes(clean.action)) {
      throw new HttpsError("failed-precondition", "Esta acción no está disponible para el estado actual del pedido.");
    }

    const effect = liveActionEffect(clean, current, actor);
    const next = liveOrderState({...current, ...effect.patch, version: current.version + 1});
    const nextAllowedActions = allowedLiveActions(next);
    const now = admin.firestore.FieldValue.serverTimestamp();
    const nextCurrentResponsibleRole = patchValue(
      effect.patch,
      "currentResponsibleRole",
      patchValue(effect.patch, "responsibleRole", current.currentResponsibleRole),
    );
    const patch = {
      ...effect.patch,
      currentResponsibleRole: nextCurrentResponsibleRole,
      version: current.version + 1,
      nextAllowedActions,
      lastOperationEvent: {
        type: clean.action,
        summary: effect.eventSummary,
        actorRole: actor.role,
        actionId: clean.actionId,
        createdAt: now,
      },
      updatedAt: now,
    };
    const result = {
      orderId: clean.orderId,
      action: clean.action,
      status: patchValue(patch, "status", current.status),
      publicStatus: patchValue(patch, "publicStatus", current.publicStatus),
      operationalStatus: patchValue(patch, "operationalStatus", current.operationalStatus),
      responsibleRole: patchValue(patch, "responsibleRole", current.responsibleRole),
      currentResponsibleRole: patch.currentResponsibleRole,
      assignedActorId: patchValue(patch, "assignedActorId", current.assignedActorId),
      assignedActorRole: patchValue(patch, "assignedActorRole", current.assignedActorRole),
      activeIncident: patchValue(patch, "activeIncident", current.activeIncident),
      incidentStatus: patchValue(patch, "incidentStatus", current.incidentStatus),
      archiveStatus: patchValue(patch, "archiveStatus", current.archiveStatus),
      version: current.version + 1,
      nextAllowedActions,
      eventSummary: effect.eventSummary,
      humanMessage: effect.humanMessage,
      idempotent: false,
    };

    tx.update(orderRef, patch);
    tx.create(eventRef, {
      type: clean.action,
      summary: effect.eventSummary,
      reason: clean.reason,
      actorUid: actor.uid,
      actorRole: actor.role,
      previousStatus: current.status,
      nextStatus: result.status,
      previousOperationalStatus: current.operationalStatus,
      nextOperationalStatus: result.operationalStatus,
      previousVersion: current.version,
      nextVersion: result.version,
      actionId: clean.actionId,
      result,
      createdAt: now,
    });
    if (incidentRef) {
      tx.create(incidentRef, {
        status: "open",
        reason: clean.reason,
        actorUid: actor.uid,
        actorRole: actor.role,
        actionId: clean.actionId,
        createdAt: now,
        updatedAt: now,
      });
    }

    return result;
  });
});

async function requireOperationalActor(request) {
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Iniciá sesión para operar pedidos.");
  }

  const userSnap = await db.collection("users").doc(uid).get();
  const role = userSnap.exists ? cleanText(userSnap.get("role")).toLowerCase() : "";
  if (!["admin", "store", "driver"].includes(role) || userSnap.get("active") === false) {
    throw new HttpsError("permission-denied", "No tenés un rol operativo activo.");
  }

  return {uid, role};
}

function cleanLiveActionPayload(payload, uid) {
  const orderId = cleanText(payload.orderId);
  const action = cleanText(payload.action);
  const reason = cleanText(payload.reason);
  const expectedVersion = Number.isInteger(payload.expectedVersion) ? payload.expectedVersion : null;
  const actionId = cleanText(payload.actionId) || liveActionId({uid, orderId, action, expectedVersion, reason});

  if (!orderId || !Object.values(LIVE_ACTIONS).includes(action)) {
    throw new HttpsError("invalid-argument", "Elegí una acción operativa válida.");
  }
  if ([
    LIVE_ACTIONS.LOCAL_REJECT,
    LIVE_ACTIONS.CANCEL_ORDER,
    LIVE_ACTIONS.OPEN_INCIDENT,
    LIVE_ACTIONS.RESOLVE_INCIDENT,
    LIVE_ACTIONS.ADMIN_INTERVENE,
  ].includes(action) && reason.length < 4) {
    throw new HttpsError("invalid-argument", "Ingresá un motivo operativo claro.");
  }
  if (expectedVersion === null) {
    throw new HttpsError("invalid-argument", "Falta la versión esperada del pedido.");
  }
  if (!isSafeDocumentId(actionId)) {
    throw new HttpsError("invalid-argument", "El identificador de acción no es válido.");
  }

  return {orderId, action, reason, expectedVersion, actionId};
}

function liveActionId(payload) {
  return `act_${crypto.createHash("sha256").update(stableStringify(payload)).digest("hex").slice(0, 24)}`;
}

function patchValue(patch, field, fallback) {
  return Object.hasOwn(patch, field) ? patch[field] : fallback;
}

function isSafeDocumentId(value) {
  return /^[A-Za-z0-9_-]{8,80}$/.test(value);
}

function validateExpectedVersion(clean, current) {
  if (current.version !== clean.expectedVersion) {
    throw new HttpsError("failed-precondition", "El pedido cambió. Actualizá la vista antes de operar.");
  }
}

function validateLiveActor(actor, current, action) {
  if (actor.role === "admin") return;
  if (actor.role === "store") {
    if (![
      LIVE_ACTIONS.LOCAL_ACCEPT,
      LIVE_ACTIONS.LOCAL_REJECT,
      LIVE_ACTIONS.LOCAL_MARK_PREPARING,
      LIVE_ACTIONS.LOCAL_MARK_READY,
      LIVE_ACTIONS.CANCEL_ORDER,
      LIVE_ACTIONS.OPEN_INCIDENT,
    ].includes(action)) {
      throw new HttpsError("permission-denied", "El local no puede ejecutar esta acción.");
    }
    if (current.storeId !== actor.uid) {
      throw new HttpsError("permission-denied", "El local no corresponde a este pedido.");
    }
    return;
  }
  if (actor.role === "driver") {
    if (![
      LIVE_ACTIONS.DRIVER_TAKE,
      LIVE_ACTIONS.DRIVER_MARK_PICKED_UP,
      LIVE_ACTIONS.DRIVER_MARK_DELIVERED,
      LIVE_ACTIONS.CANCEL_ORDER,
      LIVE_ACTIONS.OPEN_INCIDENT,
    ].includes(action)) {
      throw new HttpsError("permission-denied", "El repartidor no puede ejecutar esta acción.");
    }
    if (action !== LIVE_ACTIONS.DRIVER_TAKE && current.assignedActorId !== actor.uid && current.driverId !== actor.uid) {
      throw new HttpsError("permission-denied", "El repartidor no está asignado a este pedido.");
    }
    if (action === LIVE_ACTIONS.DRIVER_TAKE && current.assignedActorId && current.assignedActorId !== actor.uid) {
      throw new HttpsError("failed-precondition", "El pedido ya fue tomado por otro repartidor.");
    }
  }
}

function liveOrderState(order) {
  const status = cleanText(order.status) || STATUS;
  return {
    status,
    publicStatus: cleanText(order.publicStatus) || PUBLIC_STATUS,
    operationalStatus: cleanText(order.operationalStatus) || status,
    financialStatus: cleanText(order.financialStatus) || FINANCIAL_STATUS_PENDING,
    communicationStatus: cleanText(order.communicationStatus) || COMMUNICATION_STATUS_RECEIVED,
    incidentStatus: cleanText(order.incidentStatus) || INCIDENT_STATUS_NONE,
    archiveStatus: cleanText(order.archiveStatus) || ARCHIVE_STATUS_LIVE,
    responsibleRole: cleanText(order.responsibleRole),
    currentResponsibleRole: cleanText(order.currentResponsibleRole || order.responsibleRole),
    assignedActorId: cleanText(order.assignedActorId),
    assignedActorRole: cleanText(order.assignedActorRole),
    driverId: cleanText(order.driverId),
    storeId: cleanText(order.storeId),
    source: cleanText(order.source),
    priority: cleanText(order.priority) || (order.activeIncident || order.needsAttention ? "high" : "normal"),
    needsAttention: order.needsAttention === true,
    activeIncident: order.activeIncident === true,
    adminReviewed: order.adminReviewed === true,
    version: Number.isInteger(order.version) ? order.version : LIVE_ORDER_VERSION,
  };
}

function allowedLiveActions(order) {
  const state = liveOrderState(order);
  const status = cleanText(state.status).toLowerCase();
  if (TERMINAL_STATUSES.includes(status)) return [];
  if (state.activeIncident) {
    return [LIVE_ACTIONS.RESOLVE_INCIDENT, LIVE_ACTIONS.CANCEL_ORDER, LIVE_ACTIONS.ADMIN_INTERVENE];
  }

  const actions = [LIVE_ACTIONS.OPEN_INCIDENT, LIVE_ACTIONS.CANCEL_ORDER, LIVE_ACTIONS.ADMIN_INTERVENE];
  if (status === STATUS && state.source === LOCAL_SOURCE) {
    return [LIVE_ACTIONS.LOCAL_ACCEPT, LIVE_ACTIONS.LOCAL_REJECT, ...actions];
  }
  if (status === STATUS) return actions;
  if (status === "accepted") return [LIVE_ACTIONS.LOCAL_MARK_PREPARING, ...actions];
  if (status === "preparing") return [LIVE_ACTIONS.LOCAL_MARK_READY, ...actions];
  if (status === "ready_for_pickup") return [LIVE_ACTIONS.DRIVER_TAKE, ...actions];
  if (status === "assigned_to_driver") return [LIVE_ACTIONS.DRIVER_MARK_PICKED_UP, ...actions];
  if (status === "picked_up") return [LIVE_ACTIONS.DRIVER_MARK_DELIVERED, ...actions];
  return actions;
}

function liveActionEffect(clean, current, actor) {
  switch (clean.action) {
    case LIVE_ACTIONS.LOCAL_ACCEPT:
      return livePatch("Pedido aceptado por el local.", "Pedido aceptado.", {
        status: "accepted",
        publicStatus: "Pedido aceptado por el local",
        operationalStatus: "local_accepted",
        responsibleRole: "store",
        currentResponsibleRole: "store",
        assignedActorId: actor.uid,
        assignedActorRole: "store",
        needsAttention: false,
        priority: "normal",
      });
    case LIVE_ACTIONS.LOCAL_REJECT:
      return livePatch(`Local rechazó el pedido: ${clean.reason}`, "Pedido rechazado por el local.", {
        status: "cancelled",
        publicStatus: "Pedido cerrado",
        operationalStatus: "rejected_by_store",
        archiveStatus: "archived",
        activeIncident: false,
        incidentStatus: INCIDENT_STATUS_NONE,
        needsAttention: false,
        priority: "closed",
        cancellationReason: clean.reason,
        cancelledByRole: "store",
        responsibleRole: "",
        currentResponsibleRole: "",
        assignedActorId: "",
        assignedActorRole: "",
      });
    case LIVE_ACTIONS.LOCAL_MARK_PREPARING:
      return livePatch("Local marcó el pedido en preparación.", "Pedido en preparación.", {
        status: "preparing",
        publicStatus: "Pedido en preparación",
        operationalStatus: "preparing",
        responsibleRole: "store",
        currentResponsibleRole: "store",
        assignedActorId: actor.uid,
        assignedActorRole: "store",
      });
    case LIVE_ACTIONS.LOCAL_MARK_READY:
      return livePatch("Local marcó el pedido listo.", "Pedido listo para retirar.", {
        status: "ready_for_pickup",
        publicStatus: "Pedido listo para retirar",
        operationalStatus: "ready_for_pickup",
        responsibleRole: "driver",
        currentResponsibleRole: "driver",
        assignedActorId: "",
        assignedActorRole: "",
        driverId: "",
      });
    case LIVE_ACTIONS.DRIVER_TAKE:
      return livePatch("Repartidor tomó el pedido.", "Pedido asignado a repartidor.", {
        status: "assigned_to_driver",
        publicStatus: "Pedido asignado a repartidor",
        operationalStatus: "driver_assigned",
        responsibleRole: "driver",
        currentResponsibleRole: "driver",
        assignedActorId: actor.uid,
        assignedActorRole: "driver",
        driverId: actor.uid,
      });
    case LIVE_ACTIONS.DRIVER_MARK_PICKED_UP:
      return livePatch("Repartidor marcó pedido retirado.", "Pedido retirado.", {
        status: "picked_up",
        publicStatus: "Pedido retirado",
        operationalStatus: "picked_up",
        responsibleRole: "driver",
        currentResponsibleRole: "driver",
      });
    case LIVE_ACTIONS.DRIVER_MARK_DELIVERED:
      return livePatch("Repartidor marcó pedido entregado.", "Pedido entregado.", {
        status: "delivered",
        publicStatus: "Pedido cerrado",
        operationalStatus: "delivered",
        communicationStatus: "closed",
        archiveStatus: "archived",
        responsibleRole: "",
        currentResponsibleRole: "",
        assignedActorId: "",
        assignedActorRole: "",
        activeIncident: false,
        incidentStatus: INCIDENT_STATUS_NONE,
        needsAttention: false,
        priority: "closed",
      });
    case LIVE_ACTIONS.CANCEL_ORDER:
      return livePatch(`${actor.role} canceló el pedido: ${clean.reason}`, "Pedido cancelado.", {
        status: "cancelled",
        publicStatus: "Pedido cerrado",
        operationalStatus: `cancelled_by_${actor.role}`,
        archiveStatus: "archived",
        activeIncident: false,
        incidentStatus: INCIDENT_STATUS_NONE,
        needsAttention: false,
        priority: "closed",
        cancellationReason: clean.reason,
        cancelledByRole: actor.role,
        responsibleRole: "",
        currentResponsibleRole: "",
        assignedActorId: "",
        assignedActorRole: "",
      });
    case LIVE_ACTIONS.OPEN_INCIDENT:
      return livePatch(`${actor.role} abrió incidencia: ${clean.reason}`, "Incidencia abierta.", {
        publicStatus: "Pedido en revisión operativa",
        operationalStatus: "incident_open",
        activeIncident: true,
        incidentStatus: "open",
        needsAttention: true,
        priority: "high",
        responsibleRole: "admin",
        currentResponsibleRole: "admin",
      });
    case LIVE_ACTIONS.RESOLVE_INCIDENT:
      return livePatch(`Admin resolvió incidencia: ${clean.reason}`, "Incidencia resuelta.", {
        publicStatus: publicStatusForLiveStatus(current.status),
        operationalStatus: "incident_resolved",
        activeIncident: false,
        incidentStatus: "resolved",
        needsAttention: false,
        priority: "normal",
      });
    case LIVE_ACTIONS.ADMIN_INTERVENE:
      return livePatch(`Admin intervino el pedido: ${clean.reason}`, "Intervención Admin registrada.", {
        publicStatus: "Pedido en revisión operativa",
        operationalStatus: "admin_intervention",
        needsAttention: true,
        priority: current.activeIncident ? "high" : "medium",
        responsibleRole: "admin",
        currentResponsibleRole: "admin",
      });
    default:
      throw new HttpsError("invalid-argument", "Acción operativa inválida.");
  }
}

function livePatch(eventSummary, humanMessage, patch) {
  return {eventSummary, humanMessage, patch};
}

function publicStatusForLiveStatus(status) {
  const clean = cleanText(status).toLowerCase();
  if (clean === "accepted") return "Pedido aceptado por el local";
  if (clean === "preparing") return "Pedido en preparación";
  if (clean === "ready_for_pickup") return "Pedido listo para retirar";
  if (clean === "assigned_to_driver") return "Pedido asignado a repartidor";
  if (clean === "picked_up") return "Pedido retirado";
  if (clean === "delivered") return "Pedido cerrado";
  return PUBLIC_STATUS;
}

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
  const nextAllowedActions = allowedLiveActions({
    source,
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
  if (["accepted", "ready_for_pickup", "assigned_to_driver"].includes(clean)) return "RECEIVED";
  if (["preparing", "in_preparation"].includes(clean)) return "PREPARING";
  if (["picked_up", "on_the_way", "shipping", "delivering"].includes(clean)) return "ON_THE_WAY";
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
