const fs = require("node:fs");
const test = require("node:test");
const assert = require("node:assert/strict");

const app = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";
const home = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicHome.kt";
const screen = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicTeamAccess.kt";
const store = "app/src/main/java/com/pedilo/app/ui/publicuser/TeamSessionStore.kt";
const runtime = "app/src/main/java/com/pedilo/app/core/runtime/TeamAccessRuntime.kt";
const model = "app/src/main/java/com/pedilo/app/core/model/TeamAccess.kt";

test("team button opens login without changing public home navigation", () => {
  const appSource = fs.readFileSync(app, "utf8");
  const homeSource = fs.readFileSync(home, "utf8");

  assert.match(homeSource, /onTeam: \(\) -> Unit/);
  assert.match(homeSource, /clickable\(role = Role\.Button, onClick = onTeam\)/);
  assert.match(appSource, /data object TeamLogin : PublicRoute/);
  assert.match(appSource, /navigateTo\(PublicRoute\.TeamLogin\)/);
});

test("team access has role placeholders only and sign out confirmation", () => {
  const source = fs.readFileSync(screen, "utf8");
  const modelSource = fs.readFileSync(model, "utf8");

  assert.match(modelSource, /Admin\("admin", "Pantalla Admin"\)/);
  assert.match(modelSource, /Local\("local", "Pantalla Local"\)/);
  assert.match(modelSource, /Driver\("repartidor", "Pantalla Repartidor"\)/);
  assert.match(source, /role\.screenTitle/);
  assert.match(source, /"Cerrar sesión"/);
  assert.match(source, /"¿Querés cerrar sesión\?"/);
  assert.match(source, /Text\("No"\)/);
  assert.match(source, /Text\("Sí"\)/);
  assert.doesNotMatch(source, /pedido|métrica|dashboard|estado operativo|driverId|WhatsApp|whatsapp/i);
});

test("persisted session redirects to the saved role and back does not clear it", () => {
  const appSource = fs.readFileSync(app, "utf8");
  const storeSource = fs.readFileSync(store, "utf8");

  assert.match(storeSource, /readPersistedSession\(\)/);
  assert.match(storeSource, /putString\(KEY_ROLE, session\.role\.wireName\)/);
  assert.match(storeSource, /fun clear\(\)/);
  assert.match(appSource, /activeTeamSession\?\.let \{ PublicRoute\.TeamRolePlaceholder\(it\.role\) \}/);
  assert.match(appSource, /is PublicRoute\.TeamRolePlaceholder -> null/);
  assert.match(appSource, /teamSessionStore\.clear\(\)/);
});

test("login failure stays closed and exposes pediloapp shop link", () => {
  const appSource = fs.readFileSync(app, "utf8");
  const screenSource = fs.readFileSync(screen, "utf8");
  const runtimeSource = fs.readFileSync(runtime, "utf8");

  assert.match(runtimeSource, /MissingSecureProvider/);
  assert.match(appSource, /No encontramos un acceso activo/);
  assert.match(appSource, /todavía no está habilitado/);
  assert.match(screenSource, /"¿Querés formar parte de Pédilo\?"/);
  assert.match(screenSource, /"Ir a pediloapp\.shop"/);
  assert.match(screenSource, /https:\/\/pediloapp\.shop/);
});

test("team access does not hardcode credentials or touch operational domains", () => {
  const joined = [home, screen, store, runtime, model]
    .map((file) => fs.readFileSync(file, "utf8"))
    .join("\n");
  const appSource = fs.readFileSync(app, "utf8");
  const forbiddenSamples = new RegExp(
    [
      ["admin", "123"].join(""),
      ["contraseña", "123"].join(""),
      ["repartidor", "123"].join(""),
      ["local", "123"].join(""),
    ].join("|"),
  );

  assert.doesNotMatch(joined, forbiddenSamples);
  assert.doesNotMatch(joined, /collection\("orders"\)|collection\('orders'\)|payments|order_tracking|createLocalOrder|createPlusOrder|getPublicOrderTracking/);
  assert.match(appSource, /teamAccess\.login/);
  assert.doesNotMatch(appSource, forbiddenSamples);
});
