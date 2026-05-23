# Estado actual — Pédilo usuario público

## Estado seguro actual

Commits seguros registrados:

```text
d452d79 Recover clean architecture and remove mockup-runtime UI
a719d1e Build real public Home in Compose
```

## Estado de trabajo

- Home público real en Compose: aprobado y commiteado.
- Tienda principal: construida por Codex, pendiente de decisión visual final y commit.
- No avanzar a nuevas fases de Codex hasta decidir Tienda principal.
- No usar referencias viejas ni screenshots runtime.
- No usar `plan_*.png` como recurso runtime.

## Bloques visuales reorganizados

Se refinó y ordenó el material visual del usuario público en:

```text
home/
tienda/
convenciones/
boton_mas/
local/
Splash/
```

## Próxima decisión al retomar Codex

```text
Revisar Tienda principal ya construida.
Si se acepta: commit "Build real public Shop in Compose".
Si no se acepta: realizar ajustes visuales controlados antes del commit.
```
