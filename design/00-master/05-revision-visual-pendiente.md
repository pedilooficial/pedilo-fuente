# Revisión visual pendiente — Tienda principal

## Estado

La Tienda principal fue construida por Codex en FASE 3, pero todavía no fue commiteada.

Se revisó video manual de la app funcionando.

## Video revisado

```text
pedilo_20260522_182410.mp4
```

## Dictamen visual actual

```text
B) Tienda encaminada, requiere decisión final antes de commit
```

## Observaciones positivas

- La pantalla Tienda no parece maqueta pegada.
- Se ve como UI real.
- Mantiene identidad negra/naranja.
- La navegación Home → Tienda funciona.
- Aparecen buscador, consulta de pedido, categorías y bottom bar.
- No aparece “Casa”.
- No se ven `plan_*.png`, `TapZone` ni screenshots runtime en la experiencia visible.

## Observaciones a revisar antes del commit

- El buscador abre teclado; decidir si eso queda aceptado como input real o si debe comportarse como placeholder hasta construir el flujo.
- Revisar si la densidad de cards/categorías se acerca lo suficiente al mockup.
- Revisar safe area/bottom bar contra navegación del sistema.
- Confirmar que Tienda no abre local, productos, carrito, pedido ni ticket.

## Decisión pendiente

Al retomar trabajo con Codex, elegir una de estas opciones:

### A) Aceptar Tienda principal y commitear

```text
Build real public Shop in Compose
```

### B) Pedir ajuste visual menor antes del commit

Usar una fase controlada de ajustes visuales de Tienda principal.

### C) Rechazar Tienda y reconstruir solo Tienda principal

No avanzar a flujos si la pantalla principal no queda aceptada.

## Regla

No avanzar a Tienda subcategoría, Tienda buscador, Tienda seguimiento ni otros flujos hasta cerrar esta decisión.
