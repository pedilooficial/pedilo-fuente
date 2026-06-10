"use strict";

const {onCall, HttpsError} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const crypto = require("node:crypto");

admin.initializeApp();

const db = admin.firestore();
const REGION = "southamerica-east1";
const ORDERS = "orders";
const PUBLIC_CLAIMS = "public_claims";
const STORES = "stores";
const PRODUCTS = "products";
const LOCAL_SOURCE = "public_local";
const PLUS_BUY_SOURCE = "public_plus_buy";
const PLUS_PICKUP_SHIPPING_SOURCE = "public_plus_pickup_shipping";
const STATUS = "created";
const PUBLIC_STATUS = "Pedido recibido";
const LIVE_ORDER_VERSION = 1;
const FINANCIAL_STATUS_PENDING = "pending_review";
const FINANCIAL_STATUS_COLLECT_ON_DELIVERY = "collect_on_delivery";
const FINANCIAL_STATUS_TRANSFER_DECLARED = "transfer_declared_pending";
const FINANCIAL_STATUS_PAID_DECLARED = "paid_declared";
const FINANCIAL_STATUS_CONFIRMED_INTERNAL = "confirmed_internal";
const FINANCIAL_STATUS_REJECTED = "rejected";
const FINANCIAL_STATUS_DISPUTED = "disputed";
const FINANCIAL_STATUS_SETTLEMENT_PENDING = "settlement_pending";
const FINANCIAL_STATUS_SETTLED = "settled";
const PAYMENT_METHOD_CASH = "cash";
const PAYMENT_METHOD_TRANSFER = "transfer";
const PAYMENT_METHOD_CARD = "card";
const PAYMENT_METHOD_ALREADY_PAID = "already_paid";
const COMMUNICATION_STATUS_RECEIVED = "received";
const COMMUNICATION_STATUS_PREPARED = "prepared";
const COMMUNICATION_STATUS_DISABLED = "disabled";
const COMMUNICATION_STATUS_CLOSED = "closed";
const INCIDENT_STATUS_NONE = "none";
const CLAIM_STATUS_RECEIVED = "received";
const ARCHIVE_STATUS_LIVE = "live";
const RESPONSIBLE_ADMIN = "admin";
const ASSIGNED_ACTOR_UNASSIGNED = "";
const INITIAL_TIMEOUT_POLICY = {
  code: "initial_admin_review",
  mode: "declarative",
  executable: false,
  duration: "",
  nextFallback: "admin_review_required",
  note: "Declarative only: no scheduler is attached in Bloque B.",
};
const INITIAL_FALLBACK_POLICY = {
  code: "admin_review_required",
  mode: "declarative",
  executable: false,
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
const LIVE_ORDER_STATES = {
  initial: [STATUS],
  operational: ["accepted", "preparing", "ready_for_pickup", "assigned_to_driver", "picked_up", "under_review"],
  terminal: TERMINAL_STATUSES,
  financial: [
    FINANCIAL_STATUS_PENDING,
    "pending_payment",
    FINANCIAL_STATUS_COLLECT_ON_DELIVERY,
    FINANCIAL_STATUS_TRANSFER_DECLARED,
    FINANCIAL_STATUS_PAID_DECLARED,
    FINANCIAL_STATUS_CONFIRMED_INTERNAL,
    FINANCIAL_STATUS_REJECTED,
    FINANCIAL_STATUS_DISPUTED,
    FINANCIAL_STATUS_SETTLEMENT_PENDING,
    FINANCIAL_STATUS_SETTLED,
  ],
  communication: [
    COMMUNICATION_STATUS_RECEIVED,
    "pending",
    COMMUNICATION_STATUS_PREPARED,
    "sent",
    "failed",
    COMMUNICATION_STATUS_CLOSED,
    COMMUNICATION_STATUS_DISABLED,
  ],
  incident: [INCIDENT_STATUS_NONE, "open", "in_review", "resolved", "cancelled", "dismissed"],
  archive: [ARCHIVE_STATUS_LIVE, "archived"],
};
const INCIDENT_TYPES = [
  "delay",
  "product_unavailable",
  "customer_unreachable",
  "address_problem",
  "driver_problem",
  "store_problem",
  "payment_problem",
  "public_claim",
  "admin_intervention",
  "other",
];
const FORCEABLE_STATUSES = {
  created: "Pedido recibido",
  preparing: "Pedido en preparación",
  on_the_way: "Pedido en camino",
  delivered: "Pedido cerrado",
  under_review: "Pedido en revisión operativa",
};
const OPERATIONAL_ROLES = ["admin", "store", "driver"];
const PUBLIC_TRACKING_PATTERN = /^PDL-[A-Z0-9]{4,10}$/;
const COMMUNICATION_CHANNELS = {
  INTERNAL: "internal",
  WHATSAPP: "whatsapp",
  PUSH: "push",
  PUBLIC_TRACKING: "public_tracking",
};
const COMMUNICATION_STATUSES = {
  PENDING: "pending",
  PREPARED: COMMUNICATION_STATUS_PREPARED,
  SENT: "sent",
  FAILED: "failed",
  SKIPPED: "skipped",
  DISABLED: COMMUNICATION_STATUS_DISABLED,
};
const COMMUNICATION_TEMPLATES = {
  order_created: "Tu pedido fue recibido y queda en revisión operativa.",
  local_accept: "Tu pedido fue aceptado por el local.",
  local_mark_preparing: "Tu pedido está en preparación.",
  local_mark_ready: "Tu pedido está listo para retirar.",
  driver_take: "Tu pedido fue asignado para reparto.",
  driver_mark_picked_up: "Tu pedido está en camino.",
  driver_mark_delivered: "Tu pedido fue cerrado.",
  cancel_order: "Tu pedido fue cancelado. El seguimiento queda cerrado.",
  local_reject: "Tu pedido fue cerrado por operación.",
  open_incident: "Tu pedido está en revisión operativa.",
  resolve_incident: "La revisión operativa fue registrada.",
  admin_intervene: "Tu pedido está en revisión operativa.",
  public_claim_received: "Reclamo recibido. Queda registrado para revisión.",
  cancel_by_admin: "Tu pedido fue cancelado. El seguimiento queda cerrado.",
  mark_incident: "Tu pedido está en revisión operativa.",
  communication_failed: "La comunicación no pudo completarse y quedó registrada.",
  phone_validation_prepared: "Teléfono recibido; validación real no ejecutada.",
};
const AI_PROVIDER_STATUS_DISABLED = "disabled";
const ASSISTED_ENGINE_VERSION = "deterministic_rules_v1";
const AI_DECISION_STATUSES = {
  PENDING: "pending",
  SUGGESTED: "suggested",
  ACCEPTED: "accepted",
  REJECTED: "rejected",
  EXPIRED: "expired",
  NOT_APPLICABLE: "not_applicable",
};
const AI_RISK_LEVELS = ["low", "medium", "high", "critical"];
const ASSISTED_DECISION_RESOLUTION_STATUSES = [
  AI_DECISION_STATUSES.ACCEPTED,
  AI_DECISION_STATUSES.REJECTED,
  AI_DECISION_STATUSES.NOT_APPLICABLE,
];
const HEALTH_STATUSES = {
  OK: "ok",
  WARNING: "warning",
  CRITICAL: "critical",
  DISABLED: "disabled",
  PREPARED: "prepared",
  UNKNOWN: "unknown",
};
const MODULE_HEALTH = [
  {key: "whatsapp", label: "WhatsApp", moduleStatus: HEALTH_STATUSES.DISABLED, source: "communication_provider", warningCode: "WHATSAPP_PROVIDER_DISABLED"},
  {key: "push_fcm", label: "Push/FCM", moduleStatus: HEALTH_STATUSES.DISABLED, source: "communication_provider", warningCode: "PUSH_PROVIDER_DISABLED"},
  {key: "external_ai", label: "IA externa", moduleStatus: HEALTH_STATUSES.DISABLED, source: "assisted_decisions", warningCode: "EXTERNAL_AI_DISABLED"},
  {key: "advanced_cashbox", label: "Caja avanzada", moduleStatus: "not_implemented", source: "finance", warningCode: "ADVANCED_CASHBOX_NOT_IMPLEMENTED"},
  {key: "bank_gateway", label: "Banco / pasarela", moduleStatus: "not_implemented", source: "finance", warningCode: "BANK_GATEWAY_NOT_IMPLEMENTED"},
  {key: "google_play", label: "Google Play", moduleStatus: "not_ready", source: "release", warningCode: "GOOGLE_PLAY_NOT_READY"},
  {key: "production", label: "Producción", moduleStatus: "not_ready", source: "release", warningCode: "PRODUCTION_NOT_READY"},
  {key: "load_hardening", label: "Hardening de carga", moduleStatus: "pending_o", source: "architecture", warningCode: "LOAD_HARDENING_PENDING"},
];

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
  const finance = buildFinancialContract({
    paymentMethod: clean.paymentMethod,
    subtotal,
    source: LOCAL_SOURCE,
    orderType: "local_order",
  });
  const idempotencyKey = publicIdempotencyKey(LOCAL_SOURCE, clean);
  const orderRef = db.collection(ORDERS).doc(idempotencyKey);
  const trackingNumber = publicNumberFor(orderRef.id);
  const now = admin.firestore.FieldValue.serverTimestamp();
  const delivery = {
    addressLine: clean.customer.address,
    locality: "",
  };
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
      delivery,
      pricing: {
        ...finance.financialSnapshot,
      },
      note: clean.note,
    },
  });

  await createOrderWithInitialEvent(orderRef, {
    ...liveContract,
    storeId: clean.storeId,
    storeName,
    customer: clean.customer,
    delivery,
    items,
    note: clean.note,
    ...finance,
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
  if (!isValidTrackingNumber(trackingNumber)) {
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

exports.submitPublicClaim = onCall({region: REGION}, async (request) => {
  const clean = cleanPublicClaimPayload(request.data || {});
  const claimRef = db.collection(PUBLIC_CLAIMS).doc();
  const eventRef = claimRef.collection("events").doc("received");
  let linkedOrderId = "";
  let orderClaimRef = null;

  if (clean.trackingNumber) {
    const byTracking = await db.collection(ORDERS).where("trackingNumber", "==", clean.trackingNumber).limit(1).get();
    const snapshot = byTracking.empty
      ? await db.collection(ORDERS).where("publicOrderNumber", "==", clean.trackingNumber).limit(1).get()
      : byTracking;
    if (!snapshot.empty) {
      linkedOrderId = snapshot.docs[0].id;
      orderClaimRef = db.collection(ORDERS).doc(linkedOrderId).collection("claims").doc(claimRef.id);
    }
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const claim = {
    claimId: claimRef.id,
    orderId: linkedOrderId,
    trackingNumber: clean.trackingNumber,
    status: CLAIM_STATUS_RECEIVED,
    type: clean.type,
    reason: clean.reason,
    description: clean.description,
    customerName: clean.customerName,
    contact: clean.contact,
    sourceRole: "public_user",
    sourceActorId: "",
    publicImpact: "claim_received",
    operationalImpact: "no_live_order_mutation",
    createdAt: now,
    updatedAt: now,
  };
  const event = {
    type: "public_claim_received",
    source: "public_claim",
    actorRole: "public_user",
    actorUid: "",
    orderId: linkedOrderId,
    trackingNumber: clean.trackingNumber,
    reason: clean.reason,
    previousStatus: "",
    nextStatus: CLAIM_STATUS_RECEIVED,
    previousIncidentStatus: "",
    nextIncidentStatus: "",
    createdAt: now,
  };
  const claimCommunicationRecords = [{
    communicationId: safeCommunicationId({sourceEventId: eventRef.id, channel: COMMUNICATION_CHANNELS.PUBLIC_TRACKING, claimId: claimRef.id}),
    orderId: linkedOrderId,
    claimId: claimRef.id,
    incidentId: "",
    eventType: "public_claim_received",
    channel: COMMUNICATION_CHANNELS.PUBLIC_TRACKING,
    targetRole: "public_user",
    targetUserId: "",
    targetPhone: clean.contact,
    status: COMMUNICATION_STATUSES.PREPARED,
    messageType: "public_claim_received",
    templateKey: "public_claim_received",
    messageBody: COMMUNICATION_TEMPLATES.public_claim_received,
    createdAt: now,
    sentAt: null,
    failedAt: null,
    failureReason: "",
    triggeredByRole: "public_user",
    triggeredByActorId: "",
    sourceEventId: eventRef.id,
    publicSafe: true,
  }, {
    communicationId: safeCommunicationId({sourceEventId: eventRef.id, channel: COMMUNICATION_CHANNELS.WHATSAPP, claimId: claimRef.id}),
    orderId: linkedOrderId,
    claimId: claimRef.id,
    incidentId: "",
    eventType: "public_claim_received",
    channel: COMMUNICATION_CHANNELS.WHATSAPP,
    targetRole: "public_user",
    targetUserId: "",
    targetPhone: clean.contact,
    status: COMMUNICATION_STATUSES.DISABLED,
    messageType: "public_claim_received",
    templateKey: "public_claim_received",
    messageBody: COMMUNICATION_TEMPLATES.public_claim_received,
    createdAt: now,
    sentAt: null,
    failedAt: null,
    failureReason: communicationFailureReason(COMMUNICATION_CHANNELS.WHATSAPP),
    triggeredByRole: "public_user",
    triggeredByActorId: "",
    sourceEventId: eventRef.id,
    publicSafe: true,
  }];
  let claimAssistedDecision = null;
  if (linkedOrderId) {
    claimAssistedDecision = assistedDecisionForOrder({
      orderId: linkedOrderId,
      order: {
        status: STATUS,
        publicStatus: "Reclamo recibido",
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
        financialStatus: FINANCIAL_STATUS_PENDING,
        trackingNumber: clean.trackingNumber,
        needsAttention: true,
      },
      sourceEventId: eventRef.id,
      scope: "public_claim",
      claimId: claimRef.id,
      now,
    });
  }

  await db.runTransaction(async (tx) => {
    tx.create(claimRef, claim);
    tx.create(eventRef, event);
    writeClaimCommunications(tx, claimRef, claimCommunicationRecords);
    if (orderClaimRef) {
      tx.create(orderClaimRef, {
        claimId: claimRef.id,
        status: CLAIM_STATUS_RECEIVED,
        type: clean.type,
        reason: clean.reason,
        sourceRole: "public_user",
        publicImpact: "claim_received",
        operationalImpact: "no_live_order_mutation",
        createdAt: now,
        updatedAt: now,
      });
      writeOrderCommunications(tx, db.collection(ORDERS).doc(linkedOrderId), claimCommunicationRecords);
      if (claimAssistedDecision) {
        writeAssistedDecision(tx, db.collection(ORDERS).doc(linkedOrderId), claimAssistedDecision);
      }
    }
  });

  return {
    claimId: claimRef.id,
    status: CLAIM_STATUS_RECEIVED,
    publicMessage: "Reclamo recibido. Queda registrado para revisión operativa.",
  };
});

exports.adminOrderAction = onCall({region: REGION}, async (request) => {
  const actor = await requireAdminActor(request);

  const clean = cleanAdminActionPayload(request.data || {});
  const orderRef = db.collection(ORDERS).doc(clean.orderId);
  const eventRef = orderRef.collection("events").doc();
  const incidentRef = clean.action === ADMIN_ACTIONS.MARK_INCIDENT ? orderRef.collection("incidents").doc() : null;
  let resolveIncidentRef = null;

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
    const effect = adminActionEffect(clean, current, actor);
    if (clean.action === ADMIN_ACTIONS.RESOLVE_INCIDENT && current.activeIncidentId) {
      resolveIncidentRef = orderRef.collection("incidents").doc(current.activeIncidentId);
    }
    const next = {...current, ...effect.patch};
    next.nextAllowedActions = allowedAdminActions(next);

    const event = {
      type: clean.action,
      summary: effect.eventSummary,
      reason: clean.reason,
      actorUid: actor.uid,
      actorRole: "admin",
      previousStatus: current.status,
      nextStatus: next.status,
      previousOperationalStatus: current.operationalStatus,
      nextOperationalStatus: next.operationalStatus,
      previousIncidentStatus: current.incidentStatus,
      nextIncidentStatus: next.incidentStatus || current.incidentStatus,
      previousCommunicationStatus: current.communicationStatus,
      nextCommunicationStatus: next.communicationStatus || current.communicationStatus,
      createdAt: now,
    };
    const communicationRecords = communicationRecordsForOrder({
      orderId: clean.orderId,
      order: {...(orderSnap.data() || {}), ...effect.patch},
      eventType: clean.action,
      triggeredByRole: "admin",
      triggeredByActorId: actor.uid,
      sourceEventId: eventRef.id,
      incidentId: clean.action === ADMIN_ACTIONS.MARK_INCIDENT ? incidentRef.id : current.activeIncidentId,
      now,
    });
    const assistedDecision = assistedDecisionForOrder({
      orderId: clean.orderId,
      order: {...(orderSnap.data() || {}), ...effect.patch},
      sourceEventId: eventRef.id,
      scope: clean.action,
      incidentId: clean.action === ADMIN_ACTIONS.MARK_INCIDENT ? incidentRef.id : current.activeIncidentId,
      now,
    });

    tx.update(orderRef, {
      ...effect.patch,
      ...assistedDecisionOrderPatch(assistedDecision),
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
    writeOrderCommunications(tx, orderRef, communicationRecords);
    writeAssistedDecision(tx, orderRef, assistedDecision);
    if (incidentRef) {
      tx.set(incidentRef, incidentDocument({
        incidentId: incidentRef.id,
        orderId: clean.orderId,
        clean,
        current,
        actor,
        now,
        priority: next.priority,
        type: "admin_intervention",
      }));
      tx.update(orderRef, {activeIncidentId: incidentRef.id});
    }
    if (resolveIncidentRef) {
      tx.set(resolveIncidentRef, incidentResolutionPatch({
        status: "resolved",
        clean,
        actor,
        now,
      }), {merge: true});
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

exports.resolveAssistedDecision = onCall({region: REGION}, async (request) => {
  const actor = await requireAdminActor(request);
  const clean = cleanAssistedDecisionResolutionPayload(request.data || {});
  const orderRef = db.collection(ORDERS).doc(clean.orderId);
  const decisionRef = orderRef.collection("ai_decisions").doc(clean.aiDecisionId);
  const eventRef = orderRef.collection("events").doc();

  return db.runTransaction(async (tx) => {
    const orderSnap = await tx.get(orderRef);
    if (!orderSnap.exists) {
      throw new HttpsError("not-found", "No encontramos ese pedido.");
    }
    const decisionSnap = await tx.get(decisionRef);
    if (!decisionSnap.exists) {
      throw new HttpsError("not-found", "No encontramos esa sugerencia.");
    }

    const now = admin.firestore.FieldValue.serverTimestamp();
    const previousStatus = cleanText(decisionSnap.get("status"));
    const patch = {
      status: clean.status,
      resolvedAt: now,
      resolvedByRole: "admin",
      resolvedByActorId: actor.uid,
      resolutionNote: clean.resolutionNote,
      audit: {
        ...(decisionSnap.get("audit") || {}),
        resolution: {
          previousStatus,
          nextStatus: clean.status,
          actorRole: "admin",
          actorUid: actor.uid,
          note: clean.resolutionNote,
          createdAt: now,
          noCriticalActionExecuted: true,
        },
      },
      updatedAt: now,
    };

    tx.set(decisionRef, patch, {merge: true});
    tx.set(eventRef, {
      type: "assisted_decision_resolved",
      summary: `Admin ${clean.status} sugerencia asistida.`,
      reason: clean.resolutionNote,
      actorUid: actor.uid,
      actorRole: "admin",
      previousStatus,
      nextStatus: clean.status,
      previousOperationalStatus: cleanText(orderSnap.get("operationalStatus")),
      nextOperationalStatus: cleanText(orderSnap.get("operationalStatus")),
      sourceEventId: clean.aiDecisionId,
      createdAt: now,
    });

    return {
      orderId: clean.orderId,
      aiDecisionId: clean.aiDecisionId,
      status: clean.status,
      humanMessage: "Sugerencia asistida auditada. No se ejecutó ninguna acción crítica.",
    };
  });
});

exports.getOperationalHealth = onCall({region: REGION}, async (request) => {
  await requireAdminActor(request);

  const [ordersSnap, claimsSnap] = await Promise.all([
    db.collection(ORDERS).limit(250).get(),
    db.collection(PUBLIC_CLAIMS).limit(250).get(),
  ]);
  const orders = ordersSnap.docs.map((doc) => ({id: doc.id, ...doc.data()}));
  const publicClaims = claimsSnap.docs.map((doc) => ({id: doc.id, ...doc.data()}));
  const orderRelatedPairs = await Promise.all(orders.map(async (order) => {
    const orderRef = db.collection(ORDERS).doc(order.id);
    const [events, incidents, claims, communications, aiDecisions] = await Promise.all([
      orderRef.collection("events").orderBy("createdAt", "desc").limit(5).get(),
      orderRef.collection("incidents").limit(25).get(),
      orderRef.collection("claims").limit(25).get(),
      orderRef.collection("communications").limit(25).get(),
      orderRef.collection("ai_decisions").limit(25).get(),
    ]);
    return [order.id, {
      events: events.docs.map((doc) => ({id: doc.id, ...doc.data()})),
      incidents: incidents.docs.map((doc) => ({id: doc.id, ...doc.data()})),
      claims: claims.docs.map((doc) => ({id: doc.id, ...doc.data()})),
      communications: communications.docs.map((doc) => ({id: doc.id, ...doc.data()})),
      aiDecisions: aiDecisions.docs.map((doc) => ({id: doc.id, ...doc.data()})),
    }];
  }));

  return buildOperationalHealthReport({
    orders,
    publicClaims,
    orderRelated: Object.fromEntries(orderRelatedPairs),
    generatedAt: new Date().toISOString(),
  });
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
    validateLiveTransition(current, clean.action);

    const allowed = allowedLiveActions(current);
    if (!allowed.includes(clean.action)) {
      throw new HttpsError("failed-precondition", "Esta acción no está disponible para el estado actual del pedido.");
    }

    const effect = liveActionEffect(clean, current, actor);
    const resolveIncidentRef = clean.action === LIVE_ACTIONS.RESOLVE_INCIDENT && current.activeIncidentId
      ? orderRef.collection("incidents").doc(current.activeIncidentId)
      : null;
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
      communicationStatus: patchValue(patch, "communicationStatus", current.communicationStatus),
      archiveStatus: patchValue(patch, "archiveStatus", current.archiveStatus),
      version: current.version + 1,
      nextAllowedActions,
      eventSummary: effect.eventSummary,
      humanMessage: effect.humanMessage,
      idempotent: false,
    };
    const event = liveOrderEvent({
      clean,
      actor,
      current,
      result,
      summary: effect.eventSummary,
      now,
    });
    const communicationRecords = communicationRecordsForOrder({
      orderId: clean.orderId,
      order: {...(orderSnap.data() || {}), ...patch},
      eventType: clean.action,
      triggeredByRole: actor.role,
      triggeredByActorId: actor.uid,
      sourceEventId: clean.actionId,
      incidentId: clean.action === LIVE_ACTIONS.OPEN_INCIDENT ? clean.actionId : current.activeIncidentId,
      now,
    });
    const assistedDecision = assistedDecisionForOrder({
      orderId: clean.orderId,
      order: {...(orderSnap.data() || {}), ...patch},
      sourceEventId: clean.actionId,
      scope: clean.action,
      incidentId: clean.action === LIVE_ACTIONS.OPEN_INCIDENT ? clean.actionId : current.activeIncidentId,
      now,
    });

    tx.update(orderRef, {
      ...patch,
      ...assistedDecisionOrderPatch(assistedDecision),
    });
    tx.create(eventRef, event);
    writeOrderCommunications(tx, orderRef, communicationRecords);
    writeAssistedDecision(tx, orderRef, assistedDecision);
    if (incidentRef) {
      tx.create(incidentRef, incidentDocument({
        incidentId: clean.actionId,
        orderId: clean.orderId,
        clean,
        current,
        actor,
        now,
        priority: result.activeIncident ? "high" : "normal",
        type: incidentTypeFor(clean, actor),
      }));
    }
    if (resolveIncidentRef) {
      tx.set(resolveIncidentRef, incidentResolutionPatch({
        status: "resolved",
        clean,
        actor,
        now,
      }), {merge: true});
    }

    return result;
  });
});

async function requireAdminActor(request) {
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Iniciá sesión como Admin para operar pedidos.");
  }

  const userSnap = await db.collection("users").doc(uid).get();
  const role = userSnap.exists ? cleanText(userSnap.get("role")).toLowerCase() : "";
  if (!userSnap.exists || role !== "admin" || userSnap.get("active") !== true) {
    throw new HttpsError("permission-denied", "Solo Admin activo puede ejecutar esta acción.");
  }

  return {uid, role};
}

async function requireOperationalActor(request) {
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Iniciá sesión para operar pedidos.");
  }

  const userSnap = await db.collection("users").doc(uid).get();
  const role = userSnap.exists ? cleanText(userSnap.get("role")).toLowerCase() : "";
  if (!userSnap.exists || !OPERATIONAL_ROLES.includes(role) || userSnap.get("active") !== true) {
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

  if (!isSafeDocumentId(orderId) || !Object.values(LIVE_ACTIONS).includes(action)) {
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

function liveOrderEvent({clean, actor, current, result, summary, now}) {
  return {
    type: clean.action,
    summary,
    reason: clean.reason,
    actorUid: actor.uid,
    actorRole: actor.role,
    previousStatus: current.status,
    nextStatus: result.status,
    previousOperationalStatus: current.operationalStatus,
    nextOperationalStatus: result.operationalStatus,
    previousIncidentStatus: current.incidentStatus,
    nextIncidentStatus: result.incidentStatus,
    previousCommunicationStatus: current.communicationStatus,
    nextCommunicationStatus: result.communicationStatus,
    previousVersion: current.version,
    nextVersion: result.version,
    actionId: clean.actionId,
    result,
    audit: {
      orderId: clean.orderId,
      action: clean.action,
      actorRole: actor.role,
      previousResponsibleRole: current.currentResponsibleRole,
      nextResponsibleRole: result.currentResponsibleRole,
      previousArchiveStatus: current.archiveStatus,
      nextArchiveStatus: result.archiveStatus,
      cancellationReason: clean.action === LIVE_ACTIONS.CANCEL_ORDER ? clean.reason : "",
    },
    createdAt: now,
  };
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

function validateLiveTransition(current, action) {
  if (isTerminalStatus(current.status)) {
    throw new HttpsError("failed-precondition", "El pedido ya está cerrado.");
  }
  if (action === LIVE_ACTIONS.LOCAL_MARK_PREPARING && current.status !== "accepted") {
    throw new HttpsError("failed-precondition", "El pedido debe estar aceptado antes de prepararse.");
  }
  if (action === LIVE_ACTIONS.LOCAL_MARK_READY && current.status !== "preparing") {
    throw new HttpsError("failed-precondition", "El pedido debe estar en preparación antes de quedar listo.");
  }
  if (action === LIVE_ACTIONS.DRIVER_TAKE && current.status !== "ready_for_pickup") {
    throw new HttpsError("failed-precondition", "El pedido debe estar listo para retiro antes de ser tomado.");
  }
  if (action === LIVE_ACTIONS.DRIVER_MARK_PICKED_UP && current.status !== "assigned_to_driver") {
    throw new HttpsError("failed-precondition", "El pedido debe estar asignado antes de retirarse.");
  }
  if (action === LIVE_ACTIONS.DRIVER_MARK_DELIVERED && current.status !== "picked_up") {
    throw new HttpsError("failed-precondition", "El pedido debe estar retirado antes de entregarse.");
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
    if (["driver", "admin"].includes(current.currentResponsibleRole) && ![
      LIVE_ACTIONS.LOCAL_ACCEPT,
      LIVE_ACTIONS.LOCAL_REJECT,
      LIVE_ACTIONS.CANCEL_ORDER,
      LIVE_ACTIONS.OPEN_INCIDENT,
    ].includes(action)) {
      throw new HttpsError("permission-denied", "El local no es responsable de esta acción.");
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
    if (current.currentResponsibleRole !== "driver") {
      throw new HttpsError("permission-denied", "El repartidor no es responsable de esta acción.");
    }
  }
}

function liveOrderState(order) {
  const status = cleanText(order.status) || STATUS;
  return {
    id: cleanText(order.id),
    orderType: cleanText(order.orderType),
    status,
    publicStatus: cleanText(order.publicStatus) || PUBLIC_STATUS,
    operationalStatus: cleanText(order.operationalStatus) || status,
    financialStatus: cleanText(order.financialStatus) || FINANCIAL_STATUS_PENDING,
    amountToCollect: Number(order.amountToCollect) || 0,
    collectionRequired: order.collectionRequired === true,
    communicationStatus: cleanText(order.communicationStatus) || COMMUNICATION_STATUS_RECEIVED,
    aiRiskLevel: cleanText(order.aiRiskLevel) || "low",
    aiClassification: cleanText(order.aiClassification) || "normal_order",
    aiSuggestedAction: cleanText(order.aiSuggestedAction),
    aiSuggestedActionType: cleanText(order.aiSuggestedActionType),
    aiRequiresHumanReview: order.aiRequiresHumanReview === true,
    aiProviderStatus: cleanText(order.aiProviderStatus) || AI_PROVIDER_STATUS_DISABLED,
    incidentStatus: cleanText(order.incidentStatus) || INCIDENT_STATUS_NONE,
    archiveStatus: cleanText(order.archiveStatus) || ARCHIVE_STATUS_LIVE,
    responsibleRole: cleanText(order.responsibleRole),
    currentResponsibleRole: cleanText(order.currentResponsibleRole || order.responsibleRole),
    assignedActorId: cleanText(order.assignedActorId),
    assignedActorRole: cleanText(order.assignedActorRole),
    driverId: cleanText(order.driverId),
    storeId: cleanText(order.storeId),
    trackingNumber: cleanText(order.trackingNumber),
    publicOrderNumber: cleanText(order.publicOrderNumber || order.trackingNumber),
    source: cleanText(order.source),
    priority: cleanText(order.priority) || (order.activeIncident || order.needsAttention ? "high" : "normal"),
    needsAttention: order.needsAttention === true,
    activeIncident: order.activeIncident === true,
    activeIncidentId: cleanText(order.activeIncidentId),
    adminReviewed: order.adminReviewed === true,
    nextAllowedActions: Array.isArray(order.nextAllowedActions) ? order.nextAllowedActions.map(cleanText).filter(Boolean) : [],
    createdAt: order.createdAt || null,
    updatedAt: order.updatedAt || null,
    lastOperationEvent: order.lastOperationEvent || null,
    initialSnapshot: order.initialSnapshot || null,
    liveSnapshot: order.liveSnapshot || null,
    timeoutPolicy: order.timeoutPolicy || INITIAL_TIMEOUT_POLICY,
    fallbackPolicy: order.fallbackPolicy || INITIAL_FALLBACK_POLICY,
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
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
        responsibleRole: "store",
        currentResponsibleRole: "store",
        assignedActorId: actor.uid,
        assignedActorRole: "store",
        needsAttention: false,
        priority: "normal",
      });
    case LIVE_ACTIONS.LOCAL_REJECT:
      return livePatch(`Local rechazó el pedido: ${clean.reason}`, "Pedido rechazado por el local.", {
        ...cancellationAuditPatch({clean, current, actor}),
        status: "cancelled",
        publicStatus: "Pedido cerrado",
        operationalStatus: "rejected_by_store",
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
        archiveStatus: "archived",
        activeIncident: false,
        incidentStatus: INCIDENT_STATUS_NONE,
        needsAttention: false,
        priority: "closed",
        activeIncidentId: "",
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
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
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
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
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
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
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
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
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
        ...cancellationAuditPatch({clean, current, actor}),
        status: "cancelled",
        publicStatus: "Pedido cerrado",
        operationalStatus: `cancelled_by_${actor.role}`,
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
        archiveStatus: "archived",
        activeIncident: false,
        incidentStatus: INCIDENT_STATUS_NONE,
        needsAttention: false,
        priority: "closed",
        activeIncidentId: "",
        responsibleRole: "",
        currentResponsibleRole: "",
        assignedActorId: "",
        assignedActorRole: "",
      });
    case LIVE_ACTIONS.OPEN_INCIDENT:
      return livePatch(`${actor.role} abrió incidencia: ${clean.reason}`, "Incidencia abierta.", {
        publicStatus: "Pedido en revisión operativa",
        operationalStatus: "incident_open",
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
        activeIncident: true,
        incidentStatus: "open",
        activeIncidentId: clean.actionId,
        needsAttention: true,
        priority: "high",
        responsibleRole: "admin",
        currentResponsibleRole: "admin",
      });
    case LIVE_ACTIONS.RESOLVE_INCIDENT:
      return livePatch(`Admin resolvió incidencia: ${clean.reason}`, "Incidencia resuelta.", {
        publicStatus: publicStatusForLiveStatus(current.status),
        operationalStatus: "incident_resolved",
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
        activeIncident: false,
        incidentStatus: "resolved",
        activeIncidentId: "",
        needsAttention: false,
        priority: "normal",
      });
    case LIVE_ACTIONS.ADMIN_INTERVENE:
      return livePatch(`Admin intervino el pedido: ${clean.reason}`, "Intervención Admin registrada.", {
        publicStatus: "Pedido en revisión operativa",
        operationalStatus: "admin_intervention",
        communicationStatus: COMMUNICATION_STATUS_PREPARED,
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

function communicationTemplate(eventType) {
  const key = Object.hasOwn(COMMUNICATION_TEMPLATES, eventType) ? eventType : "communication_failed";
  return {
    templateKey: key,
    messageBody: COMMUNICATION_TEMPLATES[key],
  };
}

function communicationStatusForChannel(channel) {
  if ([COMMUNICATION_CHANNELS.WHATSAPP, COMMUNICATION_CHANNELS.PUSH].includes(channel)) {
    return COMMUNICATION_STATUSES.DISABLED;
  }
  return COMMUNICATION_STATUSES.PREPARED;
}

function communicationFailureReason(channel) {
  if (channel === COMMUNICATION_CHANNELS.WHATSAPP) return "No secure WhatsApp provider is configured for this environment.";
  if (channel === COMMUNICATION_CHANNELS.PUSH) return "No secure push channel is configured for this environment.";
  return "";
}

function communicationTargetSpecs(order) {
  const specs = [
    {channel: COMMUNICATION_CHANNELS.INTERNAL, targetRole: "admin", targetUserId: "", publicSafe: false},
    {channel: COMMUNICATION_CHANNELS.PUBLIC_TRACKING, targetRole: "public_user", targetUserId: "", publicSafe: true},
  ];
  if (cleanText(order.storeId)) {
    specs.push({channel: COMMUNICATION_CHANNELS.INTERNAL, targetRole: "store", targetUserId: cleanText(order.storeId), publicSafe: false});
  }
  const driverTarget = cleanText(order.driverId || order.assignedActorId);
  if (driverTarget) {
    specs.push({channel: COMMUNICATION_CHANNELS.INTERNAL, targetRole: "driver", targetUserId: driverTarget, publicSafe: false});
  }
  specs.push({channel: COMMUNICATION_CHANNELS.WHATSAPP, targetRole: "public_user", targetUserId: "", publicSafe: true});
  specs.push({channel: COMMUNICATION_CHANNELS.PUSH, targetRole: cleanText(order.currentResponsibleRole || order.responsibleRole || "admin"), targetUserId: "", publicSafe: false});
  return specs;
}

function communicationRecordsForOrder({orderId, order, eventType, triggeredByRole, triggeredByActorId, sourceEventId, incidentId = "", claimId = "", now}) {
  const template = communicationTemplate(eventType);
  return communicationTargetSpecs(order).map((target) => {
    const status = communicationStatusForChannel(target.channel);
    const communicationId = safeCommunicationId({sourceEventId, channel: target.channel, targetRole: target.targetRole, claimId, incidentId});
    return {
      communicationId,
      orderId,
      claimId,
      incidentId,
      eventType,
      channel: target.channel,
      targetRole: target.targetRole,
      targetUserId: target.targetUserId,
      targetPhone: target.channel === COMMUNICATION_CHANNELS.WHATSAPP ? normalizedPublicPhone(order) : "",
      status,
      messageType: eventType,
      templateKey: template.templateKey,
      messageBody: template.messageBody,
      createdAt: now,
      sentAt: null,
      failedAt: null,
      failureReason: communicationFailureReason(target.channel),
      triggeredByRole,
      triggeredByActorId,
      sourceEventId,
      publicSafe: target.publicSafe,
    };
  });
}

function safeCommunicationId(parts) {
  const raw = stableStringify(parts);
  return `comm_${crypto.createHash("sha256").update(raw).digest("hex").slice(0, 24)}`;
}

function normalizedPublicPhone(order) {
  const customer = order && typeof order.customer === "object" ? order.customer : {};
  return cleanText(customer.phone || order.contactPhone || order.phone);
}

function writeOrderCommunications(tx, orderRef, records) {
  for (const record of records) {
    tx.set(orderRef.collection("communications").doc(record.communicationId), record, {merge: true});
  }
}

function writeClaimCommunications(tx, claimRef, records) {
  for (const record of records) {
    tx.set(claimRef.collection("communications").doc(record.communicationId), record, {merge: true});
  }
}

function assistedDecisionForOrder({orderId, order, sourceEventId, scope, claimId = "", incidentId = "", communicationId = "", now}) {
  const analysis = deterministicAssistedAnalysis(order, {claimId, incidentId, communicationId});
  const aiDecisionId = safeAssistedDecisionId({orderId, sourceEventId, scope, claimId, incidentId, communicationId});
  return {
    aiDecisionId,
    orderId,
    claimId,
    incidentId,
    communicationId,
    sourceEventId,
    scope,
    inputSummary: analysis.inputSummary,
    classification: analysis.classification,
    riskLevel: analysis.riskLevel,
    suggestedAction: analysis.suggestedAction,
    suggestedActionType: analysis.suggestedActionType,
    confidence: analysis.confidence,
    requiresHumanReview: analysis.requiresHumanReview,
    status: analysis.status,
    createdAt: now,
    resolvedAt: null,
    resolvedByRole: "",
    resolvedByActorId: "",
    resolutionNote: "",
    ruleVersion: ASSISTED_ENGINE_VERSION,
    engineVersion: ASSISTED_ENGINE_VERSION,
    providerStatus: AI_PROVIDER_STATUS_DISABLED,
    audit: {
      engine: "deterministic_rules",
      providerStatus: AI_PROVIDER_STATUS_DISABLED,
      noExternalProviderUsed: true,
      noCriticalActionExecuted: true,
      sourceEventId,
      scope,
      signals: analysis.signals,
    },
  };
}

function deterministicAssistedAnalysis(order, links = {}) {
  const signals = assistedDecisionSignals(order, links);
  let classification = "normal_order";
  let riskLevel = "low";
  let suggestedAction = "continuar seguimiento";
  let suggestedActionType = "review_order";
  let confidence = 0.52;

  if (signals.incoherentState) {
    classification = "incoherent_state";
    riskLevel = "critical";
    suggestedAction = "revisar estado operativo";
    suggestedActionType = "review_order";
    confidence = 0.88;
  } else if (signals.openIncident) {
    classification = "incident_risk";
    riskLevel = "high";
    suggestedAction = "revisar incidencia";
    suggestedActionType = "review_incident";
    confidence = 0.84;
  } else if (signals.linkedClaim) {
    classification = "claim_risk";
    riskLevel = "high";
    suggestedAction = "revisar reclamo";
    suggestedActionType = "review_claim";
    confidence = 0.82;
  } else if (signals.communicationFailed) {
    classification = "communication_risk";
    riskLevel = "medium";
    suggestedAction = "revisar comunicación";
    suggestedActionType = "review_communication";
    confidence = 0.78;
  } else if (signals.cancelledWithFinancialImpact) {
    classification = "cancellation_financial_review";
    riskLevel = "high";
    suggestedAction = "revisar cancelación financiera";
    suggestedActionType = "review_finance";
    confidence = 0.8;
  } else if (signals.financialReview) {
    classification = "financial_review";
    riskLevel = "medium";
    suggestedAction = "revisar pago declarado";
    suggestedActionType = "review_finance";
    confidence = 0.76;
  } else if (signals.incompleteData) {
    classification = "incomplete_data";
    riskLevel = "medium";
    suggestedAction = "revisar datos del pedido";
    suggestedActionType = "review_order";
    confidence = 0.72;
  } else if (signals.needsAttention) {
    classification = "requires_review";
    riskLevel = "medium";
    suggestedAction = "intervención Admin sugerida";
    suggestedActionType = "admin_intervention";
    confidence = 0.7;
  }

  const requiresHumanReview = riskLevel !== "low" || signals.needsAttention;
  return {
    inputSummary: assistedInputSummary(order, signals),
    classification,
    riskLevel,
    suggestedAction,
    suggestedActionType,
    confidence,
    requiresHumanReview,
    status: AI_DECISION_STATUSES.SUGGESTED,
    signals,
  };
}

function assistedDecisionSignals(order, links = {}) {
  const status = cleanText(order.status).toLowerCase();
  const operationalStatus = cleanText(order.operationalStatus).toLowerCase();
  const financialStatus = cleanText(order.financialStatus);
  const communicationStatus = cleanText(order.communicationStatus);
  const openIncident = order.activeIncident === true || cleanText(order.incidentStatus) === "open" || !!links.incidentId;
  const linkedClaim = !!links.claimId || cleanText(order.publicStatus).toLowerCase().includes("reclamo");
  const communicationFailed = communicationStatus === "failed" || !!links.communicationId;
  const financialReview = [
    FINANCIAL_STATUS_PENDING,
    FINANCIAL_STATUS_TRANSFER_DECLARED,
    FINANCIAL_STATUS_PAID_DECLARED,
    FINANCIAL_STATUS_DISPUTED,
    FINANCIAL_STATUS_SETTLEMENT_PENDING,
  ].includes(financialStatus);
  const incompleteData = !cleanText(order.trackingNumber || order.publicOrderNumber) ||
    (!cleanText(order.storeId) && cleanText(order.source) === LOCAL_SOURCE);
  const remainingActions = Array.isArray(order.nextAllowedActions) ? order.nextAllowedActions.length : 0;
  const incoherentState = status === "delivered" && cleanText(order.archiveStatus) !== "archived" ||
    TERMINAL_STATUSES.includes(status) && remainingActions > 0;
  const cancelledWithFinancialImpact = ["cancelled", "canceled"].includes(status) && order.financialReviewRequired === true;
  return {
    openIncident,
    linkedClaim,
    communicationFailed,
    financialReview,
    incompleteData,
    incoherentState,
    cancelledWithFinancialImpact,
    needsAttention: order.needsAttention === true,
    activeIncident: order.activeIncident === true,
    status,
    operationalStatus,
    financialStatus,
    communicationStatus,
  };
}

function assistedInputSummary(order, signals) {
  return [
    `estado=${signals.status || cleanText(order.status) || "sin_estado"}`,
    `operativo=${signals.operationalStatus || cleanText(order.operationalStatus) || "sin_operativo"}`,
    `finanzas=${signals.financialStatus || "sin_finanzas"}`,
    `comunicacion=${signals.communicationStatus || "sin_comunicacion"}`,
    signals.openIncident ? "incidencia_abierta" : "",
    signals.linkedClaim ? "reclamo_vinculado" : "",
  ].filter(Boolean).join("; ");
}

function safeAssistedDecisionId(parts) {
  return `aid_${crypto.createHash("sha256").update(stableStringify(parts)).digest("hex").slice(0, 24)}`;
}

function writeAssistedDecision(tx, orderRef, decision) {
  tx.set(orderRef.collection("ai_decisions").doc(decision.aiDecisionId), decision, {merge: true});
}

function assistedDecisionOrderPatch(decision) {
  return {
    aiDecisionStatus: decision.status,
    aiRiskLevel: decision.riskLevel,
    aiClassification: decision.classification,
    aiSuggestedAction: decision.suggestedAction,
    aiSuggestedActionType: decision.suggestedActionType,
    aiRequiresHumanReview: decision.requiresHumanReview,
    aiProviderStatus: decision.providerStatus,
    aiEngineVersion: decision.engineVersion,
  };
}

function cancellationAuditPatch({clean, current, actor}) {
  const financialReviewRequired = cancellationNeedsFinancialReview(current);
  return {
    cancellationReason: clean.reason,
    cancelledByRole: actor.role,
    cancelledByActorId: actor.uid,
    previousStatus: current.status,
    previousOperationalStatus: current.operationalStatus,
    financialStatusAtCancellation: current.financialStatus,
    publicStatusAtCancellation: current.publicStatus,
    archiveStatusAtCancellation: current.archiveStatus,
    financialReviewRequired,
    financialReviewNote: financialReviewRequired
      ? "Cancelación con pago/cobro declarado: requiere revisión financiera mínima sin conciliación automática."
      : "Cancelación sin revisión financiera adicional requerida por el contrato mínimo.",
  };
}

function cancellationNeedsFinancialReview(order) {
  return [
    FINANCIAL_STATUS_COLLECT_ON_DELIVERY,
    FINANCIAL_STATUS_TRANSFER_DECLARED,
    FINANCIAL_STATUS_PAID_DECLARED,
    FINANCIAL_STATUS_DISPUTED,
    FINANCIAL_STATUS_SETTLEMENT_PENDING,
  ].includes(cleanText(order.financialStatus)) ||
    order.collectionRequired === true ||
    Number(order.amountToCollect) > 0;
}

function incidentTypeFor(clean, actor) {
  const reason = cleanText(clean.reason).toLowerCase();
  if (reason.includes("demora") || reason.includes("tarde")) return "delay";
  if (reason.includes("stock") || reason.includes("producto")) return "product_unavailable";
  if (reason.includes("cliente") || reason.includes("responde")) return "customer_unreachable";
  if (reason.includes("dirección") || reason.includes("direccion") || reason.includes("address")) return "address_problem";
  if (reason.includes("pago") || reason.includes("cobro")) return "payment_problem";
  if (actor.role === "driver") return "driver_problem";
  if (actor.role === "store") return "store_problem";
  if (actor.role === "admin") return "admin_intervention";
  return "other";
}

function incidentDocument({incidentId, orderId, clean, current, actor, now, priority, type}) {
  return {
    incidentId,
    orderId,
    status: "open",
    type: INCIDENT_TYPES.includes(type) ? type : "other",
    reason: clean.reason,
    description: clean.reason,
    sourceRole: actor.role,
    sourceActorId: actor.uid,
    actorUid: actor.uid,
    actorRole: actor.role,
    createdAt: now,
    updatedAt: now,
    publicImpact: "Pedido en revisión operativa",
    operationalImpact: "requires_admin_review",
    priority,
    linkedAction: clean.action,
    previousStatus: current.status,
    previousOperationalStatus: current.operationalStatus,
  };
}

function incidentResolutionPatch({status, clean, actor, now}) {
  return {
    status,
    updatedAt: now,
    resolvedAt: now,
    resolvedByRole: actor.role,
    resolvedByActorId: actor.uid,
    resolutionNote: clean.reason,
  };
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

  if (!isSafeDocumentId(orderId) || !Object.values(ADMIN_ACTIONS).includes(action)) {
    throw new HttpsError("invalid-argument", "Elegí una acción válida para este pedido.");
  }
  if (expectedVersion === null) {
    throw new HttpsError("invalid-argument", "Falta la versión esperada del pedido.");
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

function cleanPublicClaimPayload(payload) {
  const trackingNumber = normalizeTrackingNumber(payload.trackingNumber);
  const customerName = cleanText(payload.customerName || payload.name);
  const contact = cleanText(payload.contact || payload.phone);
  const reason = cleanText(payload.reason);
  const description = cleanText(payload.description);
  const type = cleanText(payload.type).toLowerCase() || "other";

  if (trackingNumber && !isValidTrackingNumber(trackingNumber)) {
    throw new HttpsError("invalid-argument", "Ingresá un número de pedido válido.");
  }
  if (!customerName || isPlaceholder(customerName) || !isValidPhone(contact)) {
    throw new HttpsError("invalid-argument", "Ingresá nombre y teléfono válidos.");
  }
  if (reason.length < 4 || description.length < 8 || hasPlaceholder([reason, description])) {
    throw new HttpsError("invalid-argument", "Contanos el motivo del reclamo con datos reales.");
  }

  return {
    trackingNumber,
    customerName,
    contact,
    reason,
    description,
    type: INCIDENT_TYPES.includes(type) ? type : "other",
  };
}

function cleanAssistedDecisionResolutionPayload(payload) {
  const orderId = cleanText(payload.orderId);
  const aiDecisionId = cleanText(payload.aiDecisionId);
  const status = cleanText(payload.status);
  const resolutionNote = cleanText(payload.resolutionNote);

  if (!isSafeDocumentId(orderId) || !isSafeDocumentId(aiDecisionId) || !ASSISTED_DECISION_RESOLUTION_STATUSES.includes(status)) {
    throw new HttpsError("invalid-argument", "Elegí una sugerencia y resolución válidas.");
  }
  if (resolutionNote.length < 4) {
    throw new HttpsError("invalid-argument", "Ingresá una nota de resolución.");
  }

  return {orderId, aiDecisionId, status, resolutionNote};
}

function operationalOrderState(order) {
  const status = cleanText(order.status) || STATUS;
  return {
    status,
    publicStatus: cleanText(order.publicStatus) || PUBLIC_STATUS,
    operationalStatus: cleanText(order.operationalStatus) || status,
    financialStatus: cleanText(order.financialStatus) || FINANCIAL_STATUS_PENDING,
    amountToCollect: Number(order.amountToCollect) || 0,
    collectionRequired: order.collectionRequired === true,
    incidentStatus: cleanText(order.incidentStatus) || INCIDENT_STATUS_NONE,
    communicationStatus: cleanText(order.communicationStatus) || COMMUNICATION_STATUS_RECEIVED,
    aiRiskLevel: cleanText(order.aiRiskLevel) || "low",
    aiClassification: cleanText(order.aiClassification) || "normal_order",
    aiSuggestedAction: cleanText(order.aiSuggestedAction),
    aiSuggestedActionType: cleanText(order.aiSuggestedActionType),
    aiRequiresHumanReview: order.aiRequiresHumanReview === true,
    aiProviderStatus: cleanText(order.aiProviderStatus) || AI_PROVIDER_STATUS_DISABLED,
    archiveStatus: cleanText(order.archiveStatus) || ARCHIVE_STATUS_LIVE,
    responsibleRole: cleanText(order.responsibleRole),
    priority: cleanText(order.priority) || (order.activeIncident || order.needsAttention ? "high" : "normal"),
    needsAttention: order.needsAttention === true,
    activeIncident: order.activeIncident === true,
    activeIncidentId: cleanText(order.activeIncidentId),
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

function adminActionEffect(clean, current, actor) {
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
          incidentStatus: "open",
          communicationStatus: COMMUNICATION_STATUS_PREPARED,
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
          incidentStatus: "resolved",
          activeIncidentId: "",
          communicationStatus: COMMUNICATION_STATUS_PREPARED,
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
          ...cancellationAuditPatch({clean, current, actor}),
          status: "cancelled",
          publicStatus: "Pedido cerrado",
          operationalStatus: "cancelled_by_admin",
          communicationStatus: COMMUNICATION_STATUS_PREPARED,
          archiveStatus: "archived",
          activeIncident: false,
          incidentStatus: INCIDENT_STATUS_NONE,
          activeIncidentId: "",
          needsAttention: false,
          priority: "closed",
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
  if (items.some((item) => !item.productId || isPlaceholder(item.productId) || !item.name || item.quantity <= 0 || isPlaceholder(item.name))) {
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

function buildFinancialContract({paymentMethod, subtotal, source, orderType}) {
  const normalizedMethod = normalizePaymentMethod(paymentMethod);
  const cleanSubtotal = assertValidMoneyAmount(subtotal, "subtotal");
  const deliveryFee = 0;
  const extraFees = [];
  const discounts = [];
  const total = cleanSubtotal + deliveryFee + extraFees.reduce((sum, fee) => sum + assertValidMoneyAmount(fee.amount || 0, "extraFee"), 0) -
    discounts.reduce((sum, discount) => sum + assertValidMoneyAmount(discount.amount || 0, "discount"), 0);

  if (total < 0) {
    throw new HttpsError("invalid-argument", "El total financiero no puede ser negativo.");
  }

  const collectionRequired = normalizedMethod === PAYMENT_METHOD_CASH;
  const amountToCollect = collectionRequired ? total : 0;
  const collectedAmount = 0;
  const financialStatus = financialStatusForPaymentMethod(normalizedMethod);
  const cashResponsibleRole = collectionRequired ? "driver" : "";
  const cashResponsibleActorId = "";
  const financialNotes = financialNotesForPaymentMethod(normalizedMethod);
  const financialSnapshot = {
    schemaVersion: 1,
    source,
    orderType,
    financialStatus,
    paymentMethod: normalizedMethod,
    subtotal: cleanSubtotal,
    deliveryFee,
    extraFees,
    discounts,
    total,
    amountToCollect,
    collectedAmount,
    collectionRequired,
    cashResponsibleRole,
    cashResponsibleActorId,
    financialNotes,
  };

  return {
    financialStatus,
    paymentMethod: normalizedMethod,
    subtotal: cleanSubtotal,
    deliveryFee,
    extraFees,
    discounts,
    total,
    amountToCollect,
    collectedAmount,
    collectionRequired,
    cashResponsibleRole,
    cashResponsibleActorId,
    financialSnapshot,
    financialUpdatedAt: admin.firestore.FieldValue.serverTimestamp(),
    financialNotes,
  };
}

function normalizePaymentMethod(value) {
  const clean = cleanText(value).toLowerCase();
  if (["cash", "efectivo", "efectivo al recibir", "pagar al retirar", "pago al retirar"].includes(clean)) {
    return PAYMENT_METHOD_CASH;
  }
  if (["transfer", "transferencia", "transferencia bancaria"].includes(clean)) {
    return PAYMENT_METHOD_TRANSFER;
  }
  if (["already_paid", "paid", "ya esta pago", "ya está pago", "pagado"].includes(clean)) {
    return PAYMENT_METHOD_ALREADY_PAID;
  }
  if (["card", "tarjeta", "credit_card", "debit_card"].includes(clean)) {
    throw new HttpsError("failed-precondition", "Tarjeta no está disponible: no hay pasarela de pago activa.");
  }
  throw new HttpsError("invalid-argument", "Elegí una forma de pago válida.");
}

function financialStatusForPaymentMethod(paymentMethod) {
  if (paymentMethod === PAYMENT_METHOD_CASH) return FINANCIAL_STATUS_COLLECT_ON_DELIVERY;
  if (paymentMethod === PAYMENT_METHOD_TRANSFER) return FINANCIAL_STATUS_TRANSFER_DECLARED;
  if (paymentMethod === PAYMENT_METHOD_ALREADY_PAID) return FINANCIAL_STATUS_PAID_DECLARED;
  return FINANCIAL_STATUS_PENDING;
}

function financialNotesForPaymentMethod(paymentMethod) {
  if (paymentMethod === PAYMENT_METHOD_CASH) return "Cobro en entrega pendiente de rendición operativa.";
  if (paymentMethod === PAYMENT_METHOD_TRANSFER) return "Transferencia declarada por el usuario; no validada bancariamente.";
  if (paymentMethod === PAYMENT_METHOD_ALREADY_PAID) return "Pago declarado por el usuario; no confirmado por pasarela externa.";
  return "Revisión financiera pendiente.";
}

function assertValidMoneyAmount(value, label) {
  const number = Number(value);
  if (!Number.isFinite(number) || !Number.isInteger(number) || number < 0) {
    throw new HttpsError("invalid-argument", `El monto ${label} no es válido.`);
  }
  return number;
}

function parsePublicAmountToCents(value) {
  const clean = cleanText(value);
  if (!clean) return 0;
  const normalized = clean.replace(/[^\d,.-]/g, "").replace(/\./g, "").replace(",", ".");
  const amount = Number(normalized);
  if (!Number.isFinite(amount) || amount < 0) {
    throw new HttpsError("invalid-argument", "El monto informado no es válido.");
  }
  return Math.round(amount * 100);
}

function cleanPlusItem(item) {
  return {
    name: cleanText(item.name),
    detail: cleanText(item.detail),
  };
}

function plusOrderData(clean, trackingNumber, now, idempotencyKey) {
  const orderType = clean.requestType === "buy" ? "direct_purchase" : "pickup_shipping";
  const subtotal = parsePublicAmountToCents(clean.amount);
  const finance = buildFinancialContract({
    paymentMethod: clean.paymentMethod,
    subtotal,
    source: clean.source,
    orderType,
  });
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
      financialSnapshot: finance.financialSnapshot,
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
    amount: clean.amount,
    ...finance,
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
  const normalizedSnapshot = liveOrderSnapshot({orderType, source, trackingNumber, snapshot});
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
    communicationStatus: COMMUNICATION_STATUS_PREPARED,
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
    liveSnapshot: normalizedSnapshot,
    initialSnapshot: normalizedSnapshot,
    timeoutPolicy: INITIAL_TIMEOUT_POLICY,
    fallbackPolicy: INITIAL_FALLBACK_POLICY,
    version: LIVE_ORDER_VERSION,
    idempotencyKey,
    trackingNumber,
    publicOrderNumber: trackingNumber,
    isPublicCreated: true,
  };
}

function liveOrderSnapshot({orderType, source, trackingNumber, snapshot}) {
  return {
    schemaVersion: 1,
    orderType,
    source,
    trackingNumber,
    publicSummary: publicSnapshotSummary(source, snapshot),
    payload: snapshot,
  };
}

function publicSnapshotSummary(source, snapshot) {
  if (!snapshot || typeof snapshot !== "object") return "";
  if (source === LOCAL_SOURCE && Array.isArray(snapshot.items)) {
    return snapshot.items.map((item) => cleanText(item.name)).filter(Boolean).slice(0, 3).join(", ");
  }
  if (source === PLUS_BUY_SOURCE && Array.isArray(snapshot.items)) {
    return snapshot.items.map((item) => cleanText(item.name)).filter(Boolean).slice(0, 3).join(", ");
  }
  if (source === PLUS_PICKUP_SHIPPING_SOURCE && Array.isArray(snapshot.items)) {
    return cleanText(snapshot.items[0] && snapshot.items[0].name);
  }
  return "";
}

async function createOrderWithInitialEvent(orderRef, orderData, now) {
  const eventRef = orderRef.collection("events").doc("initial");
  await db.runTransaction(async (tx) => {
    const existing = await tx.get(orderRef);
    if (existing.exists) return;
    const communicationRecords = communicationRecordsForOrder({
      orderId: orderRef.id,
      order: orderData,
      eventType: "order_created",
      triggeredByRole: "public_user",
      triggeredByActorId: "",
      sourceEventId: "initial",
      now,
    });
    const assistedDecision = assistedDecisionForOrder({
      orderId: orderRef.id,
      order: orderData,
      sourceEventId: "initial",
      scope: "order_created",
      now,
    });

    tx.create(orderRef, {
      ...orderData,
      ...assistedDecisionOrderPatch(assistedDecision),
    });
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
      previousVersion: 0,
      nextVersion: orderData.version,
      idempotencyKey: orderData.idempotencyKey,
      result: {
        status: orderData.status,
        publicStatus: orderData.publicStatus,
        operationalStatus: orderData.operationalStatus,
        responsibleRole: orderData.responsibleRole,
        currentResponsibleRole: orderData.currentResponsibleRole,
        archiveStatus: orderData.archiveStatus,
        version: orderData.version,
        nextAllowedActions: orderData.nextAllowedActions,
        idempotent: false,
      },
      audit: {
        orderType: orderData.orderType,
        source: orderData.source,
        previousResponsibleRole: "",
        nextResponsibleRole: orderData.currentResponsibleRole,
        previousArchiveStatus: "",
        nextArchiveStatus: orderData.archiveStatus,
      },
      createdAt: now,
    });
    writeOrderCommunications(tx, orderRef, communicationRecords);
    writeAssistedDecision(tx, orderRef, assistedDecision);
  });
}

function buildOperationalHealthReport({orders = [], publicClaims = [], orderRelated = {}, generatedAt = ""}) {
  const metrics = operationalHealthMetrics(orders, publicClaims, orderRelated);
  const alerts = orders.flatMap((order) => orderConsistencyWarnings(order, orderRelated[order.id] || {}));
  const criticalEvents = recentCriticalEvents(orders, orderRelated);
  const healthStatus = alerts.some((item) => item.severity === HEALTH_STATUSES.CRITICAL)
    ? HEALTH_STATUSES.CRITICAL
    : alerts.length > 0 || metrics.requiresAttention > 0
      ? HEALTH_STATUSES.WARNING
      : HEALTH_STATUSES.OK;

  return {
    healthStatus,
    severity: healthStatus,
    scope: "internal_admin",
    source: "calculated_read_model",
    generatedAt,
    metrics,
    modules: MODULE_HEALTH.map((module) => ({
      ...module,
      healthStatus: module.moduleStatus,
      severity: module.moduleStatus === HEALTH_STATUSES.DISABLED ? HEALTH_STATUSES.DISABLED : HEALTH_STATUSES.WARNING,
      requiresAdminReview: false,
      warningMessage: moduleHealthMessage(module),
    })),
    alerts,
    criticalEvents,
    auditSummary: {
      ordersWithEvents: orders.filter((order) => (orderRelated[order.id]?.events || []).length > 0).length,
      orderEventRecords: sumRelated(orderRelated, "events"),
      incidentRecords: sumRelated(orderRelated, "incidents"),
      claimRecords: sumRelated(orderRelated, "claims"),
      communicationRecords: sumRelated(orderRelated, "communications"),
      aiDecisionRecords: sumRelated(orderRelated, "aiDecisions"),
      publicClaimRecords: publicClaims.length,
      exposesPublicAudit: false,
      correctiveActionsExecuted: false,
    },
    securitySignals: [
      securitySignal("orders_client_write_denied", "orders", "Escritura directa cliente sobre /orders denegada por rules."),
      securitySignal("critical_subcollections_backend_only", "orders", "events/incidents/claims/communications/ai_decisions tienen escritura directa denegada."),
      securitySignal("admin_only_internal_health", "health", "El tablero global de salud se entrega solo por callable Admin activo."),
      securitySignal("public_audit_not_exposed", "public", "Tracking público no expone auditoría ni diagnósticos internos."),
    ],
  };
}

function operationalHealthMetrics(orders, publicClaims, orderRelated) {
  const liveOrders = orders.filter((order) => !isTerminalStatus(order.status) && cleanText(order.archiveStatus || ARCHIVE_STATUS_LIVE) === ARCHIVE_STATUS_LIVE);
  const actionsByRole = {};
  for (const order of orders) {
    const role = cleanText(order.currentResponsibleRole || order.responsibleRole) || "unknown";
    const actions = Array.isArray(order.nextAllowedActions) ? order.nextAllowedActions.length : 0;
    actionsByRole[role] = (actionsByRole[role] || 0) + actions;
  }

  return {
    liveOrders: liveOrders.length,
    pendingReviewOrders: orders.filter((order) => cleanText(order.operationalStatus).includes("review") || order.needsAttention === true).length,
    openIncidentOrders: orders.filter((order) => order.activeIncident === true || cleanText(order.incidentStatus) === "open").length,
    cancelledOrders: orders.filter((order) => ["cancelled", "canceled"].includes(cleanText(order.status).toLowerCase())).length,
    closedOrders: orders.filter((order) => isTerminalStatus(order.status)).length,
    failedCommunicationOrders: orders.filter((order) => cleanText(order.communicationStatus) === "failed").length,
    preparedCommunicationOrders: orders.filter((order) => cleanText(order.communicationStatus) === COMMUNICATION_STATUS_PREPARED).length,
    disabledCommunicationOrders: orders.filter((order) => cleanText(order.communicationStatus) === COMMUNICATION_STATUS_DISABLED).length,
    financialReviewOrders: orders.filter((order) => order.financialReviewRequired === true || cleanText(order.financialStatus) === FINANCIAL_STATUS_PENDING).length,
    pendingAiSuggestionOrders: orders.filter((order) => order.aiRequiresHumanReview === true).length,
    publicClaimsReceived: publicClaims.length,
    linkedPublicClaims: publicClaims.filter((claim) => cleanText(claim.orderId)).length,
    unlinkedPublicClaims: publicClaims.filter((claim) => !cleanText(claim.orderId)).length,
    actionsPendingByRole: actionsByRole,
    requiresAttention: orders.filter((order) => order.needsAttention === true).length,
    collectOnDeliveryOrders: orders.filter((order) => cleanText(order.financialStatus) === FINANCIAL_STATUS_COLLECT_ON_DELIVERY || cleanText(order.paymentMethod) === PAYMENT_METHOD_CASH).length,
    transferDeclaredPending: orders.filter((order) => cleanText(order.financialStatus) === FINANCIAL_STATUS_TRANSFER_DECLARED).length,
    paidDeclaredUnconfirmed: orders.filter((order) => cleanText(order.financialStatus) === FINANCIAL_STATUS_PAID_DECLARED).length,
    collectionPendingOrders: orders.filter((order) => order.collectionRequired === true && Number(order.amountToCollect) > 0).length,
    openIncidents: sumRelatedWhere(orderRelated, "incidents", (incident) => cleanText(incident.status) === "open"),
    resolvedIncidents: sumRelatedWhere(orderRelated, "incidents", (incident) => cleanText(incident.status) === "resolved"),
    unresolvedIncidents: sumRelatedWhere(orderRelated, "incidents", (incident) => cleanText(incident.status) !== "resolved"),
    aiSuggested: sumRelatedWhere(orderRelated, "aiDecisions", (decision) => cleanText(decision.status) === AI_DECISION_STATUSES.SUGGESTED),
    aiAccepted: sumRelatedWhere(orderRelated, "aiDecisions", (decision) => cleanText(decision.status) === AI_DECISION_STATUSES.ACCEPTED),
    aiRejected: sumRelatedWhere(orderRelated, "aiDecisions", (decision) => cleanText(decision.status) === AI_DECISION_STATUSES.REJECTED),
    aiNotApplicable: sumRelatedWhere(orderRelated, "aiDecisions", (decision) => cleanText(decision.status) === AI_DECISION_STATUSES.NOT_APPLICABLE),
    highRiskAi: orders.filter((order) => ["high", "critical"].includes(cleanText(order.aiRiskLevel))).length,
    whatsappDisabled: true,
    pushDisabled: true,
    externalAiDisabled: true,
    engineVersion: ASSISTED_ENGINE_VERSION,
    providerStatus: AI_PROVIDER_STATUS_DISABLED,
  };
}

function orderConsistencyWarnings(order, related) {
  const warnings = [];
  const push = (severity, metricKey, warningCode, warningMessage) => {
    warnings.push({
      healthStatus: severity === HEALTH_STATUSES.CRITICAL ? HEALTH_STATUSES.CRITICAL : HEALTH_STATUSES.WARNING,
      severity,
      scope: "order",
      source: "live_order_consistency",
      metricKey,
      metricValue: cleanText(order[metricKey]) || String(order[metricKey] ?? ""),
      warningCode,
      warningMessage,
      requiresAdminReview: true,
      relatedOrderId: order.id || "",
      lastEventAt: order.lastOperationEvent?.createdAt || "",
    });
  };

  if (isTerminalStatus(order.status) && cleanText(order.archiveStatus) === ARCHIVE_STATUS_LIVE) {
    push(HEALTH_STATUSES.CRITICAL, "archiveStatus", "TERMINAL_ORDER_STILL_LIVE", "Pedido terminal con archiveStatus live.");
  }
  if (!isTerminalStatus(order.status) && (!Array.isArray(order.nextAllowedActions) || order.nextAllowedActions.length === 0)) {
    push(HEALTH_STATUSES.WARNING, "nextAllowedActions", "ACTIVE_ORDER_WITHOUT_ACTIONS", "Pedido activo sin próximas acciones permitidas.");
  }
  if (order.activeIncident === true && cleanText(order.incidentStatus) === INCIDENT_STATUS_NONE) {
    push(HEALTH_STATUSES.CRITICAL, "incidentStatus", "ACTIVE_INCIDENT_WITH_NONE_STATUS", "activeIncident true con incidentStatus none.");
  }
  if (cleanText(order.incidentStatus) === "open" && order.activeIncident !== true) {
    push(HEALTH_STATUSES.CRITICAL, "activeIncident", "OPEN_INCIDENT_FLAG_FALSE", "incidentStatus open con activeIncident false.");
  }
  if (order.financialReviewRequired === true && !cleanText(order.financialReviewReason || order.financialNotes)) {
    push(HEALTH_STATUSES.WARNING, "financialReviewRequired", "FINANCIAL_REVIEW_WITHOUT_REASON", "Revisión financiera requerida sin nota o motivo.");
  }
  if (cleanText(order.communicationStatus) === "failed" && !(related.communications || []).some((item) => cleanText(item.status) === "failed")) {
    push(HEALTH_STATUSES.WARNING, "communicationStatus", "FAILED_COMMUNICATION_WITHOUT_RECORD", "communicationStatus failed sin comunicación fallida registrada.");
  }
  if (order.aiRequiresHumanReview === true && (related.aiDecisions || []).length === 0) {
    push(HEALTH_STATUSES.WARNING, "aiRequiresHumanReview", "AI_REVIEW_WITHOUT_DECISION", "Revisión humana IA requerida sin ai_decision vinculada.");
  }
  if (!isTerminalStatus(order.status) && !cleanText(order.currentResponsibleRole || order.responsibleRole)) {
    push(HEALTH_STATUSES.WARNING, "currentResponsibleRole", "ACTIVE_ORDER_WITHOUT_RESPONSIBLE_ROLE", "Pedido activo sin currentResponsibleRole.");
  }
  if (cleanText(order.currentResponsibleRole || order.responsibleRole) === "driver" && !cleanText(order.driverId) && cleanText(order.assignedActorId)) {
    push(HEALTH_STATUSES.WARNING, "driverId", "DRIVER_ASSIGNED_WITHOUT_DRIVER_ID", "Pedido asignado a Driver sin driverId.");
  }
  if (cleanText(order.source) === LOCAL_SOURCE && !cleanText(order.storeId)) {
    push(HEALTH_STATUSES.CRITICAL, "storeId", "LOCAL_ORDER_WITHOUT_STORE_ID", "Pedido Store sin storeId.");
  }
  if (order.collectionRequired === true && Number(order.amountToCollect) <= 0) {
    push(HEALTH_STATUSES.CRITICAL, "amountToCollect", "COLLECTION_REQUIRED_INVALID_AMOUNT", "collectionRequired true con amountToCollect inválido.");
  }
  if (Object.hasOwn(order, "amountToCollect") && Number(order.amountToCollect) < 0) {
    push(HEALTH_STATUSES.CRITICAL, "amountToCollect", "NEGATIVE_AMOUNT_TO_COLLECT", "amountToCollect incoherente.");
  }
  if (Object.hasOwn(order, "total") && (!Number.isFinite(Number(order.total)) || Number(order.total) < 0)) {
    push(HEALTH_STATUSES.WARNING, "total", "INVALID_TOTAL", "Total inválido o ausente cuando corresponde.");
  }

  return warnings;
}

function recentCriticalEvents(orders, orderRelated) {
  const criticalTypes = new Set(["cancel_order", "cancel_by_admin", "open_incident", "mark_incident", "communication_failed", "assisted_decision_resolved"]);
  return orders
    .flatMap((order) => (orderRelated[order.id]?.events || []).map((event) => ({
      relatedOrderId: order.id,
      source: "orders/events",
      type: cleanText(event.type),
      summary: cleanText(event.summary || event.reason),
      actorRole: cleanText(event.actorRole),
      previousStatus: cleanText(event.previousStatus),
      nextStatus: cleanText(event.nextStatus),
      createdAt: event.createdAt || "",
      severity: criticalTypes.has(cleanText(event.type)) ? HEALTH_STATUSES.WARNING : HEALTH_STATUSES.OK,
    })))
    .filter((event) => event.severity !== HEALTH_STATUSES.OK)
    .slice(0, 10);
}

function securitySignal(metricKey, scope, warningMessage) {
  return {
    healthStatus: HEALTH_STATUSES.OK,
    severity: HEALTH_STATUSES.OK,
    scope,
    source: "firestore_rules",
    metricKey,
    metricValue: "enforced",
    warningCode: "",
    warningMessage,
    requiresAdminReview: false,
  };
}

function moduleHealthMessage(module) {
  if (module.moduleStatus === HEALTH_STATUSES.DISABLED) {
    return `${module.label} deshabilitado: no hay proveedor real activo.`;
  }
  return `${module.label} no debe tratarse como listo para producción.`;
}

function sumRelated(orderRelated, key) {
  return Object.values(orderRelated).reduce((sum, related) => sum + ((related && related[key]) || []).length, 0);
}

function sumRelatedWhere(orderRelated, key, predicate) {
  return Object.values(orderRelated).reduce((sum, related) => {
    return sum + ((related && related[key]) || []).filter(predicate).length;
  }, 0);
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

function isValidTrackingNumber(value) {
  return PUBLIC_TRACKING_PATTERN.test(cleanText(value)) && !isPlaceholder(value);
}

function publicTrackingResponse(order, requestedTrackingNumber) {
  const hasOperationalProblem = order.activeIncident === true || cleanText(order.incidentStatus) === "open" || order.needsAttention === true;
  const hasCommunicationIssue = cleanText(order.communicationStatus) === "failed";
  const hasAssistedReview = order.aiRequiresHumanReview === true;
  const status = (hasOperationalProblem || hasCommunicationIssue || hasAssistedReview) && !isTerminalStatus(order.status) ? "UNDER_REVIEW" : publicStatusCode(order.status);
  const terminal = isTerminalStatus(order.status);
  const trackingNumber = cleanText(order.trackingNumber || order.publicOrderNumber || requestedTrackingNumber);
  const publicStatus = terminal ? "Pedido cerrado" : (hasOperationalProblem || hasCommunicationIssue || hasAssistedReview) ? "Pedido en revisión operativa" : cleanText(order.publicStatus) || PUBLIC_STATUS;

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
      paymentLabel: publicPaymentLabel(order),
      publicTotal: publicMoneyLabel(order.total),
      collectionMessage: publicCollectionMessage(order),
      isClosed: true,
    };
  }

  return {
    found: true,
    trackingNumber,
    publicStatus,
    status,
    humanMessage: hasOperationalProblem || hasCommunicationIssue || hasAssistedReview
      ? "Estamos revisando el pedido. Te mostramos solo información segura del seguimiento."
      : "Estamos siguiendo tu pedido con el estado actual disponible.",
    orderType: publicOrderType(order),
    storeName: cleanText(order.storeName),
    summary: publicOrderSummary(order),
    paymentLabel: publicPaymentLabel(order),
    publicTotal: publicMoneyLabel(order.total),
    collectionMessage: publicCollectionMessage(order),
    isClosed: false,
  };
}

function publicPaymentLabel(order) {
  const method = cleanText(order.paymentMethod).toLowerCase();
  if (method === PAYMENT_METHOD_CASH) return "Efectivo al recibir";
  if (method === PAYMENT_METHOD_TRANSFER) return "Transferencia declarada";
  if (method === PAYMENT_METHOD_ALREADY_PAID) return "Pago declarado";
  return "Forma de pago en revisión";
}

function publicCollectionMessage(order) {
  if (order.collectionRequired === true && Number(order.amountToCollect) > 0) {
    return `Monto a pagar al recibir: ${publicMoneyLabel(order.amountToCollect)}`;
  }
  if (cleanText(order.paymentMethod) === PAYMENT_METHOD_TRANSFER) {
    return "Transferencia pendiente de revisión; no se valida automáticamente.";
  }
  if (cleanText(order.paymentMethod) === PAYMENT_METHOD_ALREADY_PAID) {
    return "Pago declarado; no confirmado por pasarela externa.";
  }
  return "Sin cobro al recibir informado.";
}

function publicMoneyLabel(value) {
  const cents = Number(value);
  if (!Number.isFinite(cents) || cents < 0) return "Sin total informado";
  return `$${Math.round(cents / 100).toLocaleString("es-AR")}`;
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
  const phone = cleanText(value);
  const digits = phone.replace(/\D/g, "").length;
  return digits >= 8 && digits <= 15 && (phone.match(/\+/g) || []).length <= 1 && (!phone.includes("+") || phone.startsWith("+"));
}

function isPlaceholder(value) {
  return ["nombre", "tu nombre", "telefono", "teléfono", "direccion", "dirección", "pedido", "producto", "paquete"]
    .includes(cleanText(value).toLowerCase());
}

function hasPlaceholder(values) {
  return values.some(isPlaceholder);
}
