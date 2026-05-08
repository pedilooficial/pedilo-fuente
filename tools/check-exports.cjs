const fs = require("fs");
const path = require("path");

const errores = [];

function read(file) {
  return fs.readFileSync(file, "utf8");
}

function resolveImport(fromFile, importPath) {
  const base = path.dirname(fromFile);
  let full = path.normalize(path.join(base, importPath));
  if (!full.endsWith(".js")) full += ".js";
  return full;
}

function getNamedExports(content) {
  const exports = new Set();

  const patterns = [
    /export\s+(?:async\s+)?function\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/g,
    /export\s+const\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/g,
    /export\s+let\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/g,
    /export\s+var\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/g,
    /export\s+class\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/g
  ];

  for (const pattern of patterns) {
    let match;
    while ((match = pattern.exec(content)) !== null) {
      exports.add(match[1]);
    }
  }

  const exportBlockRegex = /export\s*\{\s*([^}]+)\s*\}/g;
  let blockMatch;

  while ((blockMatch = exportBlockRegex.exec(content)) !== null) {
    const names = blockMatch[1]
      .split(",")
      .map(x => x.trim())
      .filter(Boolean);

    for (const item of names) {
      const parts = item.split(/\s+as\s+/).map(x => x.trim());
      const exportedName = parts[1] || parts[0];

      if (exportedName) {
        exports.add(exportedName);
      }
    }
  }

  return exports;
}

function scanFile(file) {
  const content = read(file);

  const importRegex = /import\s+\{\s*([^}]+)\s*\}\s+from\s+["']([^"']+)["']/g;

  let match;

  while ((match = importRegex.exec(content)) !== null) {
    const names = match[1]
      .split(",")
      .map(x => {
        const parts = x.trim().split(/\s+as\s+/);
        return parts[0].trim();
      })
      .filter(Boolean);

    const importPath = match[2];
    const target = resolveImport(file, importPath);

    if (!fs.existsSync(target)) {
      errores.push({
        archivo: file,
        problema: `Importa desde archivo inexistente: ${importPath}`,
        destino: target
      });
      continue;
    }

    const targetContent = read(target);
    const namedExports = getNamedExports(targetContent);

    for (const name of names) {
      if (!namedExports.has(name)) {
        errores.push({
          archivo: file,
          problema: `Importa "${name}" pero no existe export con ese nombre`,
          destino: target,
          exportsDisponibles: Array.from(namedExports)
        });
      }
    }
  }
}

function scanDir(dir) {
  if (!fs.existsSync(dir)) return;

  for (const item of fs.readdirSync(dir)) {
    const full = path.join(dir, item);
    const stat = fs.statSync(full);

    if (stat.isDirectory()) {
      scanDir(full);
    } else if (full.endsWith(".js")) {
      scanFile(full);
    }
  }
}

scanDir("src");
scanDir("tests");

if (errores.length === 0) {
  console.log("CHECK EXPORTS: OK");
} else {
  console.log("CHECK EXPORTS: ERROR\n");

  for (const e of errores) {
    console.log(`Archivo: ${e.archivo}`);
    console.log(`Problema: ${e.problema}`);
    console.log(`Destino: ${e.destino}`);
    if (e.exportsDisponibles) {
      console.log(`Exports disponibles: ${e.exportsDisponibles.join(", ") || "ninguno"}`);
    }
    console.log("");
  }

  process.exitCode = 1;
}
