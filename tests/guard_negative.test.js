const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const {spawnSync} = require("node:child_process");

function copyProjectForGuard() {
  const tmpRoot = fs.mkdtempSync(path.join(os.tmpdir(), "pedilo-guard-"));
  for (const item of ["app", "functions", "firestore.rules", "firestore.indexes.json", "README.md", "tools"]) {
    fs.cpSync(item, path.join(tmpRoot, item), {recursive: true});
  }
  fs.rmSync(path.join(tmpRoot, "functions", "node_modules"), {recursive: true, force: true});
  return tmpRoot;
}

function runGuard(root) {
  return spawnSync("bash", ["tools/guards/check_architecture.sh"], {
    cwd: root,
    encoding: "utf8",
  });
}

test("architecture guard fails if legacy public identity returns", () => {
  const root = copyProjectForGuard();
  const target = path.join(root, "app/src/main/java/com/pedilo/app/domain/UserRole.kt");
  fs.appendFileSync(target, "\n// signIn" + "Anonymously\n");
  const result = runGuard(root);
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /forbidden pattern/);
});

test("architecture guard fails if Android writes directly to orders", () => {
  const root = copyProjectForGuard();
  const target = path.join(root, "app/src/main/java/com/pedilo/app/data/FirebasePediloRepository.kt");
  fs.appendFileSync(target, "\n// db.collection(\"orders\").document(\"x\").set(mapOf())\n");
  const result = runGuard(root);
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /forbidden pattern|Android must not write directly/);
});

test("architecture guard fails if createOrder reads auth", () => {
  const root = copyProjectForGuard();
  const target = path.join(root, "functions/src/index.ts");
  const source = fs.readFileSync(target, "utf8");
  fs.writeFileSync(
    target,
    source.replace(
      "return createOrderFlow(db, request.data);",
      "const uid = request.auth?.uid;\n  return createOrderFlow(db, {...request.data, uid});"
    )
  );
  const result = runGuard(root);
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /createOrder must not depend on auth/);
});
