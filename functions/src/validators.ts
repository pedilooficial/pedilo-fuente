import {HttpsError} from "firebase-functions/v2/https";

export type Role = "store" | "driver" | "admin";
export type ActorRole = Role | "public";
export type Status =
  | "created"
  | "assigned_to_driver"
  | "picked_up"
  | "on_the_way"
  | "delivered"
  | "cancelled"
  | "problem";
export type Action =
  | "mark_picked_up"
  | "mark_on_the_way"
  | "mark_delivered"
  | "cancel_order"
  | "report_problem"
  | "resolve_problem";

export type OrderDoc = {
  status: Status;
  orderType: "public";
  storeId?: string | null;
  driverId?: string | null;
  beforeProblemStatus?: Status | null;
  problemNote?: string | null;
};

export const liveStatuses: Status[] = [
  "created",
  "assigned_to_driver",
  "picked_up",
  "on_the_way",
  "problem",
];

export const terminalStatuses: Status[] = ["delivered", "cancelled"];

export const publicOrderLimits = {
  nameMax: 80,
  phoneMin: 8,
  phoneMax: 15,
  addressMin: 5,
  addressMax: 180,
  itemsMin: 3,
  itemsMax: 1200,
  noteMax: 300,
};

export function requireText(value: unknown, field: string, options: {
  min?: number;
  max?: number;
  digitsOnly?: boolean;
} = {}): string {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new HttpsError("invalid-argument", `Falta ${field}.`);
  }
  const text = value.trim();
  if (options.min !== undefined && text.length < options.min) {
    throw new HttpsError("invalid-argument", `${field} es demasiado corto.`);
  }
  if (options.max !== undefined && text.length > options.max) {
    throw new HttpsError("invalid-argument", `${field} supera el máximo permitido.`);
  }
  if (options.digitsOnly === true && /\D/.test(text)) {
    throw new HttpsError("invalid-argument", `${field} debe contener solo números.`);
  }
  return text;
}

export function optionalText(value: unknown, max = Number.MAX_SAFE_INTEGER): string {
  const text = typeof value === "string" ? value.trim() : "";
  if (text.length > max) {
    throw new HttpsError("invalid-argument", "La nota supera el máximo permitido.");
  }
  return text;
}

export function requireAction(value: unknown): Action {
  const actions: Action[] = [
    "mark_picked_up",
    "mark_on_the_way",
    "mark_delivered",
    "cancel_order",
    "report_problem",
    "resolve_problem",
  ];
  if (typeof value === "string" && actions.includes(value as Action)) return value as Action;
  throw new HttpsError("invalid-argument", "Acción inválida.");
}

export function requireStatus(value: unknown): Status {
  const statuses: Status[] = [
    "created",
    "assigned_to_driver",
    "picked_up",
    "on_the_way",
    "delivered",
    "cancelled",
    "problem",
  ];
  if (typeof value === "string" && statuses.includes(value as Status)) return value as Status;
  throw new HttpsError("invalid-argument", "Estado inválido.");
}

export function parseItems(itemsText: string): Array<{name: string; quantity: number; note: string}> {
  return itemsText
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => ({name: line, quantity: 1, note: ""}));
}
