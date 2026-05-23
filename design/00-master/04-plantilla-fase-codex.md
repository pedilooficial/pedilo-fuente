# Plantilla para fase de Codex

## FASE X — Nombre de la fase

### Objetivo

Construir o ajustar solamente:

```text
[describir pantalla o flujo puntual]
```

### Referencias obligatorias

Leer primero:

```text
design/00-master/00-estado-actual.md
design/00-master/01-reglas-globales.md
design/00-master/02-mapa-de-fases.md
design/00-master/03-criterios-validacion.md
design/00-master/06-indice-visual-aprobado.md
```

Referencia visual específica:

```text
[agregar ruta de imagen aprobada]
```

### Alcance permitido

```text
[qué puede tocar]
```

### Prohibido

```text
No avanzar a otra fase.
No usar screenshots runtime.
No usar plan_*.png.
No tocar Firebase productivo.
No hacer deploy.
No mezclar roles.
No crear flujos no definidos.
```

### Validaciones obligatorias

```bash
bash tools/guards/check_architecture.sh
bash tools/guards/check_ui_quality.sh
node --test tests
./gradlew compileDebugKotlin
./gradlew assembleDebug
git diff --check
```

### Entrega esperada

Reportar:

```text
archivos modificados
pantallas construidas
capturas generadas
validaciones ejecutadas
pendientes
dictamen
```

No hacer commit sin aprobación del usuario.
