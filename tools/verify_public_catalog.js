#!/usr/bin/env node
"use strict";

const STORE_ID = "pizzeria-roma";
const PRODUCT_IDS = ["muzzarella", "napolitana", "empanadas", "gaseosa", "promo-dia"];

async function main() {
  const admin = loadFirebaseAdmin();

  if (admin.apps.length === 0) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault(),
    });
  }

  const db = admin.firestore();
  const storeRef = db.collection("stores").doc(STORE_ID);
  const store = await storeRef.get();
  const products = await storeRef.collection("products").get();
  const productIds = products.docs.map((doc) => doc.id).sort();

  console.log(`store:${STORE_ID}:exists=${store.exists}`);
  console.log(`products:${productIds.length}:${productIds.join(",")}`);
  console.log(`expected-products-present=${PRODUCT_IDS.every((id) => productIds.includes(id))}`);
}

main().catch((error) => {
  console.error(error.message || error);
  process.exit(1);
});

function loadFirebaseAdmin() {
  try {
    return require("firebase-admin");
  } catch (error) {
    throw new Error("Firebase Admin SDK no está disponible en este entorno local.");
  }
}
