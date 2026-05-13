import {FieldValue, Firestore} from "firebase-admin/firestore";
import {HttpsError} from "firebase-functions/v2/https";
import {writeEvent, writeIncident} from "./events";
import {Role} from "./validators";
import {
  Action,
  OrderDoc,
  Status,
  optionalText,
  parseItems,
  publicOrderLimits,
  requireText,
  terminalStatuses,
} from "./validators";

type Transition = {
  action: Action;
  roles: Role[];
  from: Status[];
  to: Status;
};
type OperatorAction = Action | "assign_driver";

const transitions: Transition[] = [
  {action: "mark_picked_up", roles: ["driver"], from: ["assigned_to_driver"], to: "picked_up"},
  {action: "mark_on_the_way", roles: ["driver"], from: ["picked_up"], to: "on_the_way"},
  {action: "mark_delivered", roles: ["driver"], from: ["on_the_way"], to: "delivered"},
  {action: "cancel_order", roles: ["admin"], from: ["created", "assigned_to_driver", "picked_up", "on_the_way", "problem"], to: "cancelled"},
  {action: "report_problem", roles: ["store", "driver", "admin"], from: ["created", "assigned_to_driver", "picked_up", "on_the_way"], to: "problem"},
];

export async function createOrderFlow(db: Firestore, data: unknown): Promise<{orderId: string}> {
  const input = data as Record<string, unknown> | undefined;
  const requesterName = requireText(input?.requesterName, "requesterName", {max: publicOrderLimits.nameMax});
  const itemsText = requireText(input?.itemsText, "itemsText", {
    min: publicOrderLimits.itemsMin,
    max: publicOrderLimits.itemsMax,
  });
  const deliveryAddress = requireText(input?.deliveryAddress, "deliveryAddress", {
    min: publicOrderLimits.addressMin,
    max: publicOrderLimits.addressMax,
  });
  const contactPhone = requireText(input?.contactPhone, "contactPhone", {
    min: publicOrderLimits.phoneMin,
    max: publicOrderLimits.phoneMax,
    digitsOnly: true,
  });
  const note = optionalText(input?.note, publicOrderLimits.noteMax);

  const orderRef = db.collection("orders").doc();
  const baseOrder: OrderDoc = {
    orderType: "public",
    status: "created",
    storeId: null,
    driverId: null,
    beforeProblemStatus: null,
    problemNote: null,
  };
  const order = {
    ...baseOrder,
    requesterName,
    items: parseItems(itemsText),
    deliveryAddress,
    contactPhone,
    note,
    availableActionsByRole: actionsFor(baseOrder),
    adminAllowedStatuses: adminAllowedStatusesFor(baseOrder),
    createdAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp(),
  };

  await db.runTransaction(async (tx) => {
    tx.set(orderRef, order);
    writeEvent(tx, orderRef, {
      actorId: "public",
      actorRole: "public",
      type: "order_created",
      fromStatus: null,
      toStatus: "created",
      note,
    });
  });

  return {orderId: orderRef.id};
}

export async function transitionOrderFlow(
  db: Firestore,
  actorId: string,
  actorRole: Role,
  orderId: string,
  action: Action,
  note: string
): Promise<{ok: true}> {
  const orderRef = db.collection("orders").doc(orderId);
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(orderRef);
    if (!snap.exists) throw new HttpsError("not-found", "Pedido inexistente.");
    const order = snap.data() as OrderDoc;
    authorizeActor(actorId, actorRole, order);
    const nextStatus = nextStatusFor(action, actorRole, order);
    const update: Record<string, unknown> = {
      status: nextStatus,
      updatedAt: FieldValue.serverTimestamp(),
    };

    if (action === "report_problem") {
      update.beforeProblemStatus = order.status;
      update.problemNote = note || "Problema reportado";
      writeIncident(tx, orderRef, {
        actorId,
        actorRole,
        note: update.problemNote as string,
        status: order.status,
      });
    }
    if (action === "resolve_problem") {
      update.beforeProblemStatus = null;
      update.problemNote = null;
    }

    const updatedOrder = {...order, ...update, status: nextStatus} as OrderDoc;
    update.availableActionsByRole = actionsFor(updatedOrder);
    update.adminAllowedStatuses = adminAllowedStatusesFor(updatedOrder);

    tx.update(orderRef, update);
    writeEvent(tx, orderRef, {
      actorId,
      actorRole,
      type: action,
      fromStatus: order.status,
      toStatus: nextStatus,
      note,
    });
  });
  return {ok: true};
}

export async function assignDriverFlow(
  db: Firestore,
  actorId: string,
  actorRole: Role,
  orderId: string,
  driverId: string
): Promise<{ok: true}> {
  if (actorRole !== "admin") {
    throw new HttpsError("permission-denied", "Solo admin puede asignar repartidor.");
  }
  const orderRef = db.collection("orders").doc(orderId);
  const driverRef = db.collection("users").doc(driverId);
  await db.runTransaction(async (tx) => {
    const driver = await tx.get(driverRef);
    if (!driver.exists || driver.get("role") !== "driver") {
      throw new HttpsError("failed-precondition", "El repartidor no existe o no tiene rol repartidor.");
    }
    const snap = await tx.get(orderRef);
    if (!snap.exists) throw new HttpsError("not-found", "Pedido inexistente.");
    const order = snap.data() as OrderDoc;
    if (order.status !== "created") {
      throw new HttpsError("failed-precondition", "El pedido debe estar creado para asignar repartidor.");
    }
    const updatedOrder: OrderDoc = {...order, driverId, status: "assigned_to_driver"};
    tx.update(orderRef, {
      driverId,
      status: "assigned_to_driver",
      availableActionsByRole: actionsFor(updatedOrder),
      adminAllowedStatuses: adminAllowedStatusesFor(updatedOrder),
      updatedAt: FieldValue.serverTimestamp(),
    });
    writeEvent(tx, orderRef, {
      actorId,
      actorRole,
      type: "assign_driver",
      fromStatus: order.status,
      toStatus: "assigned_to_driver",
      note: `driver:${driverId}`,
    });
  });
  return {ok: true};
}

export async function adminSetStatusFlow(
  db: Firestore,
  actorId: string,
  actorRole: Role,
  orderId: string,
  toStatus: Status,
  note: string
): Promise<{ok: true}> {
  if (actorRole !== "admin") {
    throw new HttpsError("permission-denied", "Solo admin puede cambiar estados.");
  }
  const orderRef = db.collection("orders").doc(orderId);
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(orderRef);
    if (!snap.exists) throw new HttpsError("not-found", "Pedido inexistente.");
    const order = snap.data() as OrderDoc;
    if (!isAllowedAdminStatusChange(order, toStatus)) {
      throw new HttpsError("failed-precondition", "Transición inválida.");
    }
    const update: Record<string, unknown> = {
      status: toStatus,
      updatedAt: FieldValue.serverTimestamp(),
    };
    if (toStatus === "problem") {
      update.beforeProblemStatus = order.status;
      update.problemNote = note || "Problema registrado por admin";
      writeIncident(tx, orderRef, {
        actorId,
        actorRole,
        note: update.problemNote as string,
        status: order.status,
      });
    } else {
      update.beforeProblemStatus = null;
      update.problemNote = null;
    }
    const updatedOrder = {...order, ...update, status: toStatus} as OrderDoc;
    update.availableActionsByRole = actionsFor(updatedOrder);
    update.adminAllowedStatuses = adminAllowedStatusesFor(updatedOrder);
    tx.update(orderRef, update);
    writeEvent(tx, orderRef, {
      actorId,
      actorRole,
      type: "admin_set_status",
      fromStatus: order.status,
      toStatus,
      note,
    });
  });
  return {ok: true};
}

export function actionsFor(order: OrderDoc): Record<Role, OperatorAction[]> {
  const result: Record<Role, OperatorAction[]> = {
    store: [],
    driver: [],
    admin: [],
  };
  for (const transition of transitions) {
    if (!transition.from.includes(order.status)) continue;
    for (const role of transition.roles) result[role].push(transition.action);
  }
  if (order.status === "created") result.admin.push("assign_driver");
  if (order.status === "problem") result.admin.push("resolve_problem");
  if (terminalStatuses.includes(order.status)) {
    return {store: [], driver: [], admin: []};
  }
  return result;
}

function nextStatusFor(action: Action, actorRole: Role, order: OrderDoc): Status {
  if (action === "resolve_problem") {
    if (actorRole !== "admin" || order.status !== "problem") {
      throw new HttpsError("failed-precondition", "Solo admin puede resolver problemas.");
    }
    return order.beforeProblemStatus ?? "created";
  }
  const transition = transitions.find((item) =>
    item.action === action &&
    item.roles.includes(actorRole) &&
    item.from.includes(order.status)
  );
  if (!transition) {
    throw new HttpsError("failed-precondition", "Transición inválida para este rol y estado.");
  }
  return transition.to;
}

function authorizeActor(actorId: string, actorRole: Role, order: OrderDoc): void {
  const allowed =
    actorRole === "admin" ||
    (actorRole === "store" && !!order.storeId && order.storeId === actorId) ||
    (actorRole === "driver" && order.driverId === actorId);
  if (!allowed) {
    throw new HttpsError("permission-denied", "Este pedido no pertenece a tu operación.");
  }
}

function isAllowedAdminStatusChange(order: OrderDoc, to: Status): boolean {
  if (order.status === to) return false;
  if (terminalStatuses.includes(order.status)) return false;
  if (to === "cancelled" || to === "problem") return true;
  return transitions.some((transition) => transition.from.includes(order.status) && transition.to === to);
}

function adminAllowedStatusesFor(order: OrderDoc): Status[] {
  if (terminalStatuses.includes(order.status)) return [];
  const direct = transitions
    .filter((transition) => transition.from.includes(order.status))
    .map((transition) => transition.to);
  const candidates: Status[] = [...direct, "cancelled", "problem"];
  return Array.from(new Set(candidates))
    .filter((status) => status !== order.status && isAllowedAdminStatusChange(order, status));
}
