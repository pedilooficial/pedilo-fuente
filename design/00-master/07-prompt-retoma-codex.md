# Prompt maestro de retoma para Codex

Usar este prompt cuando vuelva el crédito y se retome el trabajo.

```text
Antes de tocar código, leé obligatoriamente:

design/00-master/00-estado-actual.md
design/00-master/01-reglas-globales.md
design/00-master/02-mapa-de-fases.md
design/00-master/03-criterios-validacion.md
design/00-master/05-revision-visual-pendiente.md
design/00-master/06-indice-visual-aprobado.md

No uses memoria implícita.
No uses referencias viejas.
No uses screenshots como runtime.
No uses plan_*.png.
No uses R.drawable.plan_.
No uses PlanScreen, PlanPhoneScreen ni TapZone.
No uses hotspots transparentes.
No toques Firebase productivo.
No hagas deploy.
No mezcles roles.
No avances a otra fase.

Estado actual:
- Home real está aprobado y commiteado en a719d1e.
- Tienda principal fue construida pero está pendiente de decisión visual y commit.
- No continuar subcategorías, buscador, seguimiento ni otros flujos hasta cerrar Tienda principal.

Tarea inmediata:
Revisar el estado real de Tienda principal frente a:
design/public-user-approved-mockups/tienda/08.0-tienda.png

Decidir y reportar:
A) si Tienda puede commitearse como "Build real public Shop in Compose";
B) si requiere ajustes visuales menores;
C) si debe reconstruirse solo Tienda principal.

Ejecutar validaciones:
bash tools/guards/check_architecture.sh
bash tools/guards/check_ui_quality.sh
node --test tests
./gradlew compileDebugKotlin
./gradlew assembleDebug
git diff --check

No hacer commit sin aprobación explícita.
```
