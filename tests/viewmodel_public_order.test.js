const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

test("ViewModel prevents double submit and preserves form ownership in UI", () => {
  const source = fs.readFileSync("app/src/main/java/com/pedilo/app/ui/PediloViewModel.kt", "utf8");
  assert.match(source, /if \(_state\.value\.isSubmittingOrder\) return@launch/);
  assert.match(source, /isSubmittingOrder = true, error = null/);
  assert.match(source, /publicOrderId = orderId/);
  assert.match(source, /onFailure \{ showError\(it\) \}/);
  assert.doesNotMatch(source, /fun publicOrderPhase/);
});

test("public UI clears form only after success and keeps invalid submits blocked", () => {
  const source = fs.readFileSync("app/src/main/java/com/pedilo/app/ui/PediloScreen.kt", "utf8");
  assert.match(source, /LaunchedEffect\(orderId\)/);
  assert.match(source, /if \(orderId != null\)/);
  assert.match(source, /enabled = canSubmit && !isSubmitting && !isSent/);
  assert.match(source, /if \(validation\.isValid\) onCreate\(form\.toDraft\(\)\)/);
});
