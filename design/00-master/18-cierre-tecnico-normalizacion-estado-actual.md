# 18 — Cierre técnico: normalización del estado actual

Fecha: 2026-05-30  
Rama: `temp-qwen-admin-readonly-ui`  
Base previa: `ea78fc6` (Enhance admin order operation mapping…)

---

## 1. Objetivo cumplido

Normalización técnica del repo sin construir funcionalidades nuevas ni avanzar operación viva (Admin operativo, Local, Driver, Pedido Vivo Universal).

---

## 2. Estado confirmado al cierre

### Código compilable y testeable

| Validación | Resultado |
|------------|-----------|
| `node --test tests/*.test.js` | **96/96 OK** |
| `bash tools/guards/check_architecture.sh` | **OK** |
| `./gradlew :app:compileDebugKotlin` | **BUILD SUCCESSFUL** |
| `cd functions && npm run build` | **OK** |
| `git status --short` | Ver sección 8 |

### Funcionalidad real en producción de código

- Usuario público completo (Compose + Firestore catálogo + pedidos vía Functions + tracking).
- Login equipo Firebase Auth con roles `admin` / `store` / `driver`.
- Admin: shell visual read-only con lectura de pedidos vía `AdminOrdersUseCase`.
- Functions exportadas: `createLocalOrder`, `createPlusOrder`, `getPublicOrderTracking`.
- Núcleo `core/` con clasificador `AdminOperationOrderClassification` y adapter read-only.

---

## 3. Cambios realizados en este bloque

### A. `PublicAdmin.kt`

- **Imports** de tipos de clasificación operativa (`AdminOperationOrderClassification`, buckets, signals).
- **Clasificación de buckets** corregida: antes llamaba métodos inexistentes en `AdminOperationOrderSignals` (`signals.todayBucket(...)`); ahora usa `AdminOperationOrderClassification.todayBucket/activeBucket/problemBucket` con agrupación conservadora (solo pedidos con señal real).
- **Textos técnicos eliminados** de UI visible:
  - `status=created`, `publicStatus=…`, `Sin criterio real actualmente`, `Sin señal real actualmente`, `Lectura read-only del pedido`.
  - Reemplazados por copy operativo legible para Admin.
- **Copy de Pedidos del día** alineado al contrato visual aprobado (categorías Cancelados/Demorados/Con problemas y subentradas de cancelación).
- Evitado falso positivo del test de hardening: la palabra `demora` en minúsculas contenía el substring `demo`.

### B. Tests

- Sin debilitar tests: se corrigió código para cumplir contrato existente.
- Tests que fallaban y quedaron verdes:
  - `admin_visual_shell.test.js` (copy de categorías del día).
  - `public_input_hardening.test.js` (textos técnicos / substring `demo`).
  - `architecture_guard.test.js` y tests negativos asociados (guard sin `rg`).

### C. `README.md`

- Actualizado al backend real: ya no documenta `createOrder`, `transitionOrder`, `assignDriver`, `adminSetStatus` ni `/events` como actuales.
- Tabla clara de **confirmado / parcial / pendiente / no implementado**.

### D. `tools/guards/check_architecture.sh`

- Fallback a `grep` cuando `rg` no está instalado (entornos CI/dev sin ripgrep).
- Misma semántica de reglas; elimina falsos fallos por `rg: orden no encontrada`.

### E. Archivos `design/` y `design.zip`

- `design/` ya trackeado en Git (documentación fuente + mockups).
- `design.zip` **no presente** en el árbol de trabajo; no requiere acción.
- `app/google-services.json` sigue en `.gitignore` (correcto).

---

## 4. Qué quedó parcial

| Área | Detalle |
|------|---------|
| Admin Operación | Shell visual + lectura read-only de pedidos; contadores en raíz operativa; sin listados completos por bucket ni acciones de resolución |
| Clasificación operativa | Solo señales reales actuales (`created` + `Pedido recibido`, `cancelled`, reclamo en `publicStatus`); demoras y estados intermedios devuelven `false` / no mapean |
| Store / Driver | Entrada por rol sin operación viva |
| Subcolecciones de pedido | No expuestas en Functions ni UI |

---

## 5. Qué quedó pendiente (fuera de alcance)

- Pedido Vivo Universal (transiciones, eventos, asignación repartidor).
- Local interno operativo y Driver/Repartidor operativo.
- Resolución real Admin (solucionar, reasignar, intervenir).
- Deploy de Functions / cambios en Firestore Rules.
- Conectar listados Admin por bucket con todos los pedidos (hoy `orderDetailEntriesFor` usa `firstOrNull()` como vista parcial).

---

## 6. Riesgos eliminados

- Admin **no compilaba** por referencias incorrectas a clasificación/buckets.
- Tests rotos por copy técnico y expectativas visuales desalineadas.
- README contradecía Functions reales (riesgo de retomar arquitectura vieja).
- Guard de arquitectura fallaba en entornos sin `ripgrep`.
- Textos técnicos visibles al usuario Admin.

---

## 7. Riesgos que siguen abiertos

- `google-services.json` debe configurarse localmente (no versionado).
- Documentos viejos en `design/00-master/` (p. ej. `00-estado-actual.md`) pueden contradecir el código; **mandan Git + código + tests + este documento**.
- Admin muestra conteos reales en raíz operativa pero subsecciones siguen siendo mayormente visuales.
- Señales de demora/problema operativo aún no existen en datos reales → buckets vacíos es comportamiento esperado.

---

## 8. Git al cierre

```
# Cambios pendientes de commit (post-normalización):
 M README.md
 M app/src/main/java/com/pedilo/app/ui/publicuser/PublicAdmin.kt
 M tools/guards/check_architecture.sh
?? design/00-master/18-cierre-tecnico-normalizacion-estado-actual.md
```

**Commit sugerido** (no aplicado automáticamente):

```
Normalizar estado técnico: Admin read-only compilable, tests verdes y README alineado al backend real.
```

---

## 9. Jerarquía de verdad aplicada

1. Git y commits actuales → código fuente de verdad.
2. Código + tests + build → mandan sobre documentos viejos.
3. Auditoría maestra inicial → diagnóstico de partida.
4. `design/00-master/` → referencia conceptual; distinguir docs frescos (09–18) de docs históricos (00–08).
5. README → describe estado real, no aspiracional.

---

## 10. Recomendación del próximo bloque

**Admin operación read-only ampliada** (sin escritura):

1. Listar pedidos reales por bucket usando `AdminOperationOrderClassification` en subsecciones (no solo `firstOrNull()`).
2. Conectar contadores de subcategorías del día a datos read-only.
3. Mantener prohibición de acciones operativas reales hasta bloque Pedido Vivo Universal.

Alternativa si la prioridad es operación: **Pedido Vivo Universal mínimo** — definir contrato de eventos y primera Function de transición antes de UI operativa Store/Driver.

---

## 11. Comandos de re-validación

```bash
git status --short
node --test tests/*.test.js
bash tools/guards/check_architecture.sh
./gradlew :app:compileDebugKotlin
cd functions && npm run build
```
