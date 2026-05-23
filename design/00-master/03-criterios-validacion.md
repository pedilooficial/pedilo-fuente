# Criterios de validación

## Validación técnica mínima

Antes de aceptar una fase:

```bash
bash tools/guards/check_architecture.sh
bash tools/guards/check_ui_quality.sh
node --test tests
./gradlew compileDebugKotlin
./gradlew assembleDebug
git diff --check
```

## Validación anti-maqueta

Debe fallar si aparece:

```text
app/src/main/res/drawable-nodpi/plan_*.png
R.drawable.plan_
PlanScreen
PlanPhoneScreen
TapZone
hotspots transparentes con offset/size/clickable
screenshots/mockups como recursos runtime
```

## Validación visual

Una fase visual se acepta si:

- se ve como app real;
- respeta identidad negra/naranja;
- no parece Material genérico;
- no parece formulario básico;
- no parece lista Excel;
- no mezcla pantallas;
- no avanza a flujos no autorizados;
- respeta bottom nav: Inicio, +, Tienda;
- no muestra “Casa”.

## Validación de alcance

Cada fase debe declarar:

```text
qué construyó
qué no construyó
qué archivos modificó
qué validaciones ejecutó
qué quedó pendiente
```

## Commit

No commitear hasta que el usuario acepte visualmente la fase.
