import {DocumentReference, FieldValue, Transaction} from "firebase-admin/firestore";
import {ActorRole, Status} from "./validators";

export function eventData(input: {
  actorId: string;
  actorRole: ActorRole;
  type: string;
  fromStatus: Status | null;
  toStatus: Status | null;
  note: string;
}): Record<string, unknown> {
  return {
    ...input,
    createdAt: FieldValue.serverTimestamp(),
  };
}

export function writeEvent(tx: Transaction, orderPath: DocumentReference, input: {
  actorId: string;
  actorRole: ActorRole;
  type: string;
  fromStatus: Status | null;
  toStatus: Status | null;
  note: string;
}): void {
  tx.create(orderPath.collection("events").doc(), eventData(input));
}

export function writeIncident(tx: Transaction, orderPath: DocumentReference, input: {
  actorId: string;
  actorRole: ActorRole;
  note: string;
  status: Status;
}): void {
  tx.create(orderPath.collection("incidents").doc(), {
    ...input,
    createdAt: FieldValue.serverTimestamp(),
  });
}
