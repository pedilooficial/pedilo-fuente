const fs = require("fs");
const path = require("path");

const errores = [];
const warnings = [];
const info = [];

const OUTPUT_DIR = "docs_ia";
const FILE_DIAG = `${OUTPUT_DIR}/01_DIAGNOSTICO_ACTUAL.md`;
const FILE_ERRORS = `${OUTPUT_DIR}/02_ERRORES_Y_ADVERTENCIAS.md`;

// ======================
// SCAN
// ======================

function scanDir(dir) {
  const files = fs.readdirSync(dir);

  files.forEach(file => {
    const full = path.join(dir, file);

    if (fs.statSync(full).isDirectory()) {
      scanDir(full);
    } else if (full.endsWith(".js")) {
      const content = fs.readFileSync(full, "utf8");

      analizarArchivo(full, content);
    }
  });
}

// ======================
// REGLAS
// ======================

function analizarArchivo(file, content) {
  // ERRORES CRÍTICOS
  if (content.includes("state.carrito")) {
    errores.push({
      archivo: file,
      regla: "Uso de state.carrito (PROHIBIDO)",
      capa: "STATE",
      impacto: "Rompe contrato oficial"
    });
  }

  if (content.includes("state.cliente")) {
    errores.push({
      archivo: file,
      regla: "Uso directo de state.cliente (PROHIBIDO)",
      capa: "STATE",
      impacto: "Debe usarse state.pedido.cliente"
    });
  }

  if (content.includes("localStorage")) {
    errores.push({
      archivo: file,
      regla: "Uso de localStorage (PROHIBIDO en V1)",
      capa: "PERSISTENCIA",
      impacto: "Rompe reglas de arquitectura"
    });
  }

  // WARNINGS
  if (content.includes("window.") && !file.includes("whatsapp")) {
    warnings.push({
      archivo: file,
      regla: "Uso de window.* fuera de services",
      capa: "UI",
      impacto: "Puede mezclar capas"
    });
  }

  // INFO
  if (file.includes("screens")) {
    info.push({
      archivo: file,
      tipo: "screen detectada"
    });
  }

  if (file.includes("services")) {
    info.push({
      archivo: file,
      tipo: "service detectado"
    });
  }
}

// ======================
// INFORMES
// ======================

function generarDiagnostico() {
  const fecha = new Date().toISOString();

  let md = `# DIAGNÓSTICO ACTUAL — PÉDILO

Generado: ${fecha}

## Resultado
`;

  if (errores.length === 0 && warnings.length === 0) {
    md += `OK\n`;
  } else if (errores.length === 0) {
    md += `REVISAR\n`;
  } else {
    md += `ERROR\n`;
  }

  md += `
Errores críticos: ${errores.length}
Advertencias: ${warnings.length}
Archivos analizados: ${info.length}

---

## Veredicto
`;

  if (errores.length > 0) {
    md += `NO APTO PARA AVANZAR\n`;
  } else {
    md += `APTO PARA REVISIÓN HUMANA\n`;
  }

  fs.writeFileSync(FILE_DIAG, md);
}

function generarErrores() {
  const fecha = new Date().toISOString();

  let md = `# ERRORES Y ADVERTENCIAS — PÉDILO

Generado: ${fecha}

## Errores críticos
`;

  if (errores.length === 0) {
    md += `Sin errores críticos\n`;
  } else {
    errores.forEach((e, i) => {
      md += `
### Error ${i + 1}
Archivo: ${e.archivo}
Regla: ${e.regla}
Capa: ${e.capa}
Impacto: ${e.impacto}
`;
    });
  }

  md += `

## Advertencias
`;

  if (warnings.length === 0) {
    md += `Sin advertencias\n`;
  } else {
    warnings.forEach((w, i) => {
      md += `
### Warning ${i + 1}
Archivo: ${w.archivo}
Regla: ${w.regla}
Capa: ${w.capa}
Impacto: ${w.impacto}
`;
    });
  }

  fs.writeFileSync(FILE_ERRORS, md);
}

// ======================
// MAIN
// ======================

fs.mkdirSync(OUTPUT_DIR, { recursive: true });

scanDir("./src");

generarDiagnostico();
generarErrores();

// ======================
// CONSOLA
// ======================

if (errores.length === 0 && warnings.length === 0) {
  console.log("DIAGNOSTICO: OK");
} else if (errores.length === 0) {
  console.log("DIAGNOSTICO: REVISAR");
  console.log(`Warnings: ${warnings.length}`);
} else {
  console.log("DIAGNOSTICO: ERROR");
  console.log(`Errores: ${errores.length}`);
  process.exitCode = 1;
}

console.log("Informes generados en docs_ia/");
