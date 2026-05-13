import * as admin from "firebase-admin";
import {onCall} from "firebase-functions/v2/https";
import {
  adminSetStatusFlow,
  assignDriverFlow,
  createOrderFlow,
  transitionOrderFlow,
} from "./orderFlow";
import {requireAuth, roleFor} from "./roles";
import {optionalText, requireAction, requireStatus, requireText} from "./validators";

admin.initializeApp();

const db = admin.firestore();

export const createOrder = onCall(async (request) => {
  return createOrderFlow(db, request.data);
});

export const transitionOrder = onCall(async (request) => {
  const actorId = requireAuth(request.auth?.uid);
  const actorRole = await roleFor(db, actorId);
  const orderId = requireText(request.data?.orderId, "orderId");
  const action = requireAction(request.data?.action);
  const note = optionalText(request.data?.note);
  return transitionOrderFlow(db, actorId, actorRole, orderId, action, note);
});

export const assignDriver = onCall(async (request) => {
  const actorId = requireAuth(request.auth?.uid);
  const actorRole = await roleFor(db, actorId);
  const orderId = requireText(request.data?.orderId, "orderId");
  const driverId = requireText(request.data?.driverId, "driverId");
  return assignDriverFlow(db, actorId, actorRole, orderId, driverId);
});

export const adminSetStatus = onCall(async (request) => {
  const actorId = requireAuth(request.auth?.uid);
  const actorRole = await roleFor(db, actorId);
  const orderId = requireText(request.data?.orderId, "orderId");
  const toStatus = requireStatus(request.data?.toStatus);
  const note = optionalText(request.data?.note);
  return adminSetStatusFlow(db, actorId, actorRole, orderId, toStatus, note);
});
