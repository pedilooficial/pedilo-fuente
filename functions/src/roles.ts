import {Firestore} from "firebase-admin/firestore";
import {HttpsError} from "firebase-functions/v2/https";
import {Role} from "./validators";

export async function roleFor(db: Firestore, uid: string): Promise<Role> {
  const snap = await db.collection("users").doc(uid).get();
  const role = snap.get("role");
  if (role !== "store" && role !== "driver" && role !== "admin") {
    throw new HttpsError("permission-denied", "Usuario sin rol operativo.");
  }
  return role;
}

export function requireAuth(uid: string | undefined): string {
  if (!uid) throw new HttpsError("unauthenticated", "Necesitás iniciar sesión como operador.");
  return uid;
}
