#!/usr/bin/env node
"use strict";

const CONFIRM_ENV = "PEDILO_CONFIRM_SEED";
const REQUIRED_CONFIRMATION = "YES";
const STORE_ID = "pizzeria-roma";

const products = [
  {
    id: "muzzarella",
    name: "Pizza muzzarella",
    description: "Salsa de tomate, muzzarella y aceitunas.",
    priceCents: 620000,
  },
  {
    id: "napolitana",
    name: "Pizza napolitana",
    description: "Tomate fresco, ajo y muzzarella.",
    priceCents: 690000,
  },
  {
    id: "empanadas",
    name: "Empanadas",
    description: "Empanadas surtidas al horno.",
    priceCents: 120000,
  },
  {
    id: "gaseosa",
    name: "Gaseosa / bebida",
    description: "Bebida fría para acompañar el pedido.",
    priceCents: 180000,
  },
  {
    id: "promo-dia",
    name: "Promo del día",
    description: "Pizza grande con bebida con precio especial.",
    priceCents: 790000,
  },
];

const store = {
  id: STORE_ID,
  name: "Pizzería Roma",
  category: "Pizzas / Comida",
  mainCategory: "Pizzas",
  description: "Pizzería de barrio con pizzas clásicas y empanadas.",
  address: "Dirección inicial controlada",
  phone: "Teléfono inicial controlado",
  openingHours: "hasta 00:30",
  visible: true,
  operational: true,
  acceptsOrders: true,
  isOpen: true,
};

async function main() {
  if (process.env[CONFIRM_ENV] !== REQUIRED_CONFIRMATION) {
    console.log(`Modo seguro: no se escribe catálogo. Para ejecutar, usar ${CONFIRM_ENV}=${REQUIRED_CONFIRMATION}.`);
    return;
  }

  const admin = loadFirebaseAdmin();

  if (admin.apps.length === 0) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault(),
    });
  }

  const db = admin.firestore();
  const now = admin.firestore.FieldValue.serverTimestamp();
  const storeRef = db.collection("stores").doc(store.id);
  const {id, ...storeData} = store;

  await storeRef.set(
    {
      ...storeData,
      updatedAt: now,
      createdAt: now,
    },
    {merge: true},
  );

  for (const product of products) {
    const {id: productId, ...productData} = product;
    await storeRef.collection("products").doc(productId).set(
      {
        ...productData,
        storeId: store.id,
        visible: true,
        available: true,
        updatedAt: now,
        createdAt: now,
      },
      {merge: true},
    );
  }

  console.log(`Catálogo inicial cargado: stores/${store.id} con ${products.length} productos.`);
}

main().catch((error) => {
  console.error(error.message || error);
  process.exit(1);
});

function loadFirebaseAdmin() {
  try {
    return require("firebase-admin");
  } catch (error) {
    throw new Error("Firebase Admin SDK no está disponible en este entorno local. No se escribió catálogo.");
  }
}
