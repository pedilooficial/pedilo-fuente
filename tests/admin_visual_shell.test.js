const fs = require("node:fs");
const test = require("node:test");
const assert = require("node:assert/strict");

const admin = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicAdmin.kt";
const app = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";

test("admin role opens visual admin shell instead of empty placeholder", () => {
  const appSource = fs.readFileSync(app, "utf8");
  const adminSource = fs.readFileSync(admin, "utf8");

  assert.match(appSource, /role == TeamRole\.Admin/);
  assert.match(appSource, /AdminApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
  assert.match(adminSource, /fun AdminApp/);
  assert.match(adminSource, /"Pédilo Admin"/);
});

test("admin has separated root navigation and no public bottom labels", () => {
  const source = fs.readFileSync(admin, "utf8");

  assert.match(source, /Operation\("Operación"\)/);
  assert.match(source, /Configuration\("Configuración"\)/);
  assert.match(source, /RoleAccess\("Alta de roles"\)/);
  assert.match(source, /AdminBottomBar/);
  assert.doesNotMatch(source, /"Inicio"|"\\+"|"Tienda"|"Casa"|"Salir de Pedilo"/);
});

test("admin operation root exposes visual operation cards only", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Pedidos del día",
    "Pedidos activos",
    "Pedidos con problemas",
    "Repartidores activos",
    "Locales activos",
  ].forEach((label) => assert.match(source, new RegExp(label)));
  assert.match(source, /Seguimiento operativo|Resumen operativo|Revisión pendiente/);
});

test("admin configuration and role access roots expose their planned entries", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Usuario público",
    "Locales",
    "Catálogo y productos",
    "Pedidos",
    "Comunicación",
    "Operación",
    "Reglas y validaciones",
    "Auditoría",
    "Emergencias",
    "General",
    "Usuarios del equipo",
    "Administradores",
    "Locales store",
    "Repartidores driver",
    "Altas pendientes",
    "Usuarios inactivos",
    "Vinculaciones pendientes",
  ].forEach((label) => assert.match(source, new RegExp(label)));
});

test("admin visual shell does not touch real data or operational systems", () => {
  const source = fs.readFileSync(admin, "utf8");

  assert.doesNotMatch(source, /Firebase|Firestore|collection\(|orders|createLocalOrder|createPlusOrder|getPublicOrderTracking|payments|WhatsApp|whatsapp|driverId/);
  assert.doesNotMatch(source, /Sin datos conectados|Estructura visual futura|Acceso visual futuro/);
  assert.match(source, /"¿Querés cerrar sesión\?"/);
  assert.match(source, /Text\("No"\)/);
  assert.match(source, /Text\("Sí"\)/);
});
