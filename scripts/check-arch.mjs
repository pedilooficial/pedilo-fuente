import { readFileSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = join(__dirname, "..");

const files = [
  "src/app/render.js",
  "src/app/state.js",
  "src/services/whatsapp.js"
];

for (const rel of files) {
  const path = join(root, rel);
  readFileSync(path, "utf8");
}

const renderSrc = readFileSync(join(root, "src/app/render.js"), "utf8");
if (!renderSrc.includes("bindGlobalEvents")) {
  console.error("check-arch: render.js debe exportar bindGlobalEvents");
  process.exit(1);
}
if (!renderSrc.includes("routeClick")) {
  console.error("check-arch: render.js debe definir routeClick / delegación");
  process.exit(1);
}

console.log("check-arch: ok");
