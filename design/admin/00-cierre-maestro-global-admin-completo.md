# Cierre maestro global técnico-funcional — Admin completo

## 1. Propósito del documento

Este documento cierra el universo **Admin completo** de Pédilo como plano técnico-funcional global.

Su objetivo es integrar y auditar los tres universos principales del Admin:

```text
Admin
├── Operación
├── Configuración
└── Alta de roles
```

Este cierre no define UI final, estilo visual, componentes, código, backend ni implementación.  
Su función es dejar establecido cómo se separan los universos, qué gobierna cada uno, dónde convergen, qué no se debe mezclar y qué reglas globales deben respetarse para que el Admin pueda construirse más adelante sin contradicciones.

Las referencias visuales, ZIPs e imágenes generadas hasta ahora son planos de navegación, flujo y comportamiento. No son una UI final literal para copiar.

---

## 2. Estado actual que debe respetar Admin

El Admin debe proyectarse sobre la app real existente, no sobre una app vacía.

Actualmente Pédilo ya cuenta con:

- usuario público real cerrado;
- catálogo real;
- pedidos reales desde Local;
- pedidos reales desde Botón + Comprar;
- pedidos reales desde Botón + Retiro / Envío;
- ticket real con número real;
- seguimiento público real por número;
- Login Equipo funcionando;
- roles reales: `admin`, `store`, `driver`;
- Admin actual todavía en etapa de proyección por bloques.

Este cierre global no implementa nada.  
Solo ordena el comportamiento del Admin para futuras etapas.

---

## 3. Los tres universos principales del Admin

### 3.1 Operación

**Operación trabaja sobre lo vivo.**

Responde:

> “¿Qué está pasando ahora y qué necesita atención?”

Incluye:

- Pedidos del día;
- Pedidos activos;
- Pedidos con problemas;
- Repartidores activos;
- Locales activos;
- Pedido #____;
- Solucionar;
- Local operativo concreto;
- Repartidor operativo concreto.

Operación puede leer estados vivos, clasificar situaciones, llegar a entidades concretas y activar resolución guiada cuando corresponde.

Operación no configura reglas generales, no edita productos, no edita usuarios, no cambia roles y no administra accesos.

---

### 3.2 Configuración

**Configuración trabaja sobre estructura editable.**

Responde:

> “¿Cómo queda preparada la app para funcionar correctamente hacia adelante?”

Incluye:

- Usuario público;
- Locales;
- Catálogo y productos;
- Pedidos;
- Comunicación;
- Operación;
- Reglas y validaciones;
- Auditoría;
- Emergencias;
- General.

Configuración edita estructura, reglas, criterios, mensajes, productos, datos configurables y parámetros controlados.

Configuración no resuelve operación viva, no abre Pedido #____ como caso operativo, no cambia accesos, no edita usuarios y no toca active/inactive.

---

### 3.3 Alta de roles

**Alta de roles trabaja sobre accesos.**

Responde:

> “¿Quién puede entrar al sistema, con qué rol, en qué estado y bajo qué vínculo operativo?”

Incluye:

- Usuarios del equipo;
- Administradores;
- Locales store;
- Repartidores driver;
- Altas pendientes;
- Usuarios inactivos;
- Vinculaciones pendientes;
- Cuenta concreta;
- Alta de cuenta;
- Editor de acceso;
- Cambio de rol;
- Activar / desactivar;
- Vincular entidad;
- Impacto;
- Confirmación sensible;
- Resultado.

Alta de roles es dueño único de usuarios, cuentas, roles, accesos, active/inactive y vínculos de cuenta.

Alta de roles no opera pedidos, no configura locales, no edita productos y no resuelve incidencias.

---

## 4. Regla global de dueño único

Regla madre:

> Una entidad puede aparecer como referencia en varios lugares, pero solo se edita desde su dueño.

Referencia no significa edición.

Esta regla evita doble dueño, loops peligrosos y contradicciones silenciosas.

### Matriz global de dueño

| Entidad / asunto | Dueño funcional |
|---|---|
| Pedido vivo / Pedido #____ | Operación |
| Resolución guiada de pedido | Operación → Pedido #____ → Solucionar |
| Datos estructurales del local | Configuración → Locales |
| Estado vivo del local | Operación → Locales activos |
| Cuenta store del local | Alta de roles |
| Producto / precio / imagen / categoría | Configuración → Catálogo y productos |
| Experiencia pública configurable | Configuración → Usuario público |
| Texto visible de pantalla pública | Configuración → Usuario público |
| Mensaje enviado / aviso / plantilla | Configuración → Comunicación |
| Reglas futuras de pedido | Configuración → Pedidos |
| Criterios operativos | Configuración → Operación |
| Validaciones generales | Configuración → Reglas y validaciones |
| Registros administrativos | Configuración → Auditoría |
| Emergencias globales | Configuración → Emergencias |
| Usuarios / roles / accesos / active-inactive | Alta de roles |
| Estado operativo de repartidor | Operación → Repartidores activos |
| Cuenta driver del repartidor | Alta de roles |

---

## 5. Convergencias globales del Admin

### 5.1 Pedido #____

Es la convergencia principal del pedido concreto.

Debe mostrar primero el estado general del pedido.  
Solo muestra necesidades, problemas o acciones si el estado actual lo requiere.

No se llama “Pedido vivo” ni “Detalle del pedido” como identidad principal.

Pedido #____ no pertenece a Configuración ni a Alta de roles.

---

### 5.2 Solucionar

Es resolución guiada dentro del contexto correcto de Pedido #____.

No aparece desde raíces sueltas.  
No aparece desde Configuración.  
No aparece desde Alta de roles.

Solo se muestra cuando el pedido concreto necesita una resolución.

---

### 5.3 Local operativo concreto

Pertenece a Operación.

Responde qué pasa ahora con un local desde el punto de vista operativo.

No edita datos estructurales del local.  
No edita productos.  
No edita cuenta store.

---

### 5.4 Repartidor operativo concreto

Pertenece a Operación.

Responde qué pasa ahora con un repartidor desde el punto de vista operativo.

No edita cuenta driver.  
No cambia rol.  
No administra acceso.

---

### 5.5 Entidad configurable

Pertenece a Configuración.

Puede ser:

- local configurable;
- producto configurable;
- categoría configurable;
- regla configurable;
- mensaje configurable;
- criterio configurable;
- parámetro configurable.

Debe pasar por editor, impacto, confirmación si aplica y resultado.

---

### 5.6 Cuenta concreta

Pertenece a Alta de roles.

Permite revisar una cuenta, su rol, su estado, si puede ingresar y su vínculo operativo.

No muestra credenciales sensibles.  
No opera pedidos.  
No edita productos ni locales.

---

## 6. Flujo global correcto por universo

### Operación

```text
Raíz operativa
→ submundo / lista
→ entidad concreta
→ estado actual
→ necesidad si existe
→ acción contextual si corresponde
→ resultado operativo
```

Operación no debe mostrar todas las acciones juntas ni resolver fuera de contexto.

---

### Configuración

```text
Raíz de Configuración
→ grupo / listado
→ entidad configurable
→ estado actual
→ editar
→ preview o revisión
→ impacto
→ confirmación si corresponde
→ resultado
→ volver
```

Configuración no debe saltar directo a cambios con impacto.

---

### Alta de roles

```text
Raíz Alta de roles
→ listado / grupo
→ cuenta concreta
→ acción disponible
→ editor o flujo sensible
→ impacto
→ confirmación si corresponde
→ resultado
→ volver
```

Alta de roles no debe activar, desactivar, cambiar rol ni vincular sin impacto y confirmación.

---

## 7. Acciones sensibles globales

Toda acción sensible debe pasar por:

```text
Impacto
→ Confirmación
→ Resultado
```

### Acciones sensibles en Operación

- Solucionar un pedido.
- Ejecutar acción manual Admin.
- Intervenir bajo excepción.
- Resolver una incidencia.

Además, en Operación la acción sensible solo puede aparecer dentro del contexto correcto.

---

### Acciones sensibles en Configuración

- Publicar.
- Ocultar.
- Desactivar.
- Cambiar regla.
- Cambiar criterio operativo.
- Activar emergencia.
- Cambiar validación sensible.
- Modificar algo con impacto público o funcional.

---

### Acciones sensibles en Alta de roles

- Crear cuenta activa.
- Activar cuenta.
- Desactivar cuenta.
- Cambiar rol.
- Cambiar vínculo.
- Bloquear acceso.

---

## 8. Auditoría de separación de universos

### Operación vs Configuración

Ley:

```text
Operación muestra lo vivo.
Configuración define estructura, reglas y criterios.
```

Ejemplo:

- Operación muestra un pedido demorado.
- Configuración define cuándo algo se considera demorado.

Riesgo:

- Confundir Configuración → Operación con Admin → Operación.

Solución:

- Configuración → Operación configura criterios.
- Admin → Operación muestra casos reales.

Dictamen: separación correcta.

---

### Operación vs Alta de roles

Ley:

```text
Operación ve responsables y estados vivos.
Alta de roles edita cuentas y accesos.
```

Ejemplo:

- Operación ve que un repartidor está pendiente de respuesta.
- Alta de roles administra si la cuenta driver puede ingresar.

Riesgo:

- Desde cuenta driver querer operar repartidor.

Solución:

- Alta de roles puede mostrar referencia del repartidor, pero no asigna pedidos ni resuelve entregas.

Dictamen: separación correcta.

---

### Configuración vs Alta de roles

Ley:

```text
Configuración edita entidades y reglas.
Alta de roles edita cuentas y accesos.
```

Ejemplo:

- Configuración → Locales edita datos estructurales del comercio.
- Alta de roles → Locales store edita la cuenta que accede como local.

Riesgo:

- Configuración muestra “usuario asociado” y parece editable.

Solución:

- Usuario asociado es referencia.
- Cuenta se edita solo en Alta de roles.

Dictamen: separación correcta.

---

## 9. Auditoría de puntos peligrosos

### 9.1 Local

El local aparece en tres universos:

```text
Operación → Locales activos
Configuración → Locales
Alta de roles → Locales store
```

Separación correcta:

- Operación: qué está pasando ahora con el local.
- Configuración: datos estructurales del local.
- Alta de roles: cuenta de acceso del local.

Dictamen: no colapsa.

---

### 9.2 Repartidor

El repartidor aparece en:

```text
Operación → Repartidores activos
Alta de roles → Repartidores driver
```

Separación correcta:

- Operación: estado vivo del repartidor.
- Alta de roles: cuenta de acceso driver.

No se inventa Configuración → Repartidores en esta etapa.

Dictamen: no colapsa.

---

### 9.3 Pedido

El pedido aparece en:

```text
Operación → Pedido #____
Configuración → Pedidos
Configuración → Reglas y validaciones
Configuración → Comunicación
Configuración → Operación
```

Separación correcta:

- Operación → Pedido #____: pedido concreto vivo.
- Configuración → Pedidos: reglas futuras del pedido.
- Reglas y validaciones: condiciones generales de integridad.
- Comunicación: mensajes futuros o plantillas.
- Configuración → Operación: criterios de clasificación.

Regla global:

```text
Configuración nunca abre ni modifica Pedido #____.
```

Dictamen: punto delicado pero controlado.

---

### 9.4 Usuario público

El usuario público aparece relacionado con:

```text
Configuración → Usuario público
Configuración → Catálogo y productos
Configuración → Comunicación
Configuración → Pedidos
```

Separación correcta:

- Usuario público: experiencia visible configurable.
- Catálogo: productos como entidad vendible.
- Comunicación: mensajes enviados.
- Pedidos: reglas futuras del pedido o seguimiento.

Regla:

```text
El usuario público ya cerrado se respeta.
Configuración no lo reabre como rediseño libre.
```

Dictamen: correcto.

---

### 9.5 Emergencias

Riesgo:

- Emergencias puede convertirse en botón destructivo.

Solución:

```text
Emergencias configura estados excepcionales globales.
No resuelve pedidos puntuales.
No reemplaza Solucionar.
Toda emergencia requiere alcance, impacto, confirmación, resultado y auditoría posterior.
```

Dictamen: correcto.

---

### 9.6 General

Riesgo:

- General puede convertirse en depósito de opciones.

Solución:

```text
General solo contiene parámetros sin dueño específico.
Si algo tiene dueño, deriva al bloque dueño.
```

Dictamen: correcto.

---

## 10. Auditoría de loops

### Loop posible 1

```text
Configuración → General → pendiente → otro bloque → General
```

Riesgo: General se vuelve centro de edición.

Solución: General solo orienta, no edita lo que tiene dueño.

Dictamen: controlado.

---

### Loop posible 2

```text
Alta de roles → cuenta store → local vinculado → Configuración Locales → usuario asociado → Alta de roles
```

Riesgo: ciclo entre cuenta y local.

Solución:

- Alta de roles edita cuenta.
- Configuración Locales edita local.
- Las referencias cruzadas no editan desde el lugar equivocado.

Dictamen: controlado.

---

### Loop posible 3

```text
Operación → Pedido #____ → Local relacionado → Local operativo → Pedido relacionado
```

Riesgo: navegación circular entre pedido y local.

Solución:

- Pedido #____ mantiene identidad de pedido.
- Local operativo mantiene identidad de local.
- No se resuelve pedido desde Local operativo.

Dictamen: controlado.

---

### Loop posible 4

```text
Configuración → Pedidos → Reglas y validaciones → Pedidos
```

Riesgo: duplicar reglas.

Solución:

- Pedidos define comportamiento del pedido.
- Reglas y validaciones define integridad general.

Dictamen: controlado.

---

## 11. Huecos conscientes

### Repartidor como entidad configurable

No se definió Configuración → Repartidores.

No debe inventarse ahora.

Actualmente:

- cuenta driver → Alta de roles;
- estado vivo driver → Operación.

Dictamen: hueco consciente, no bloqueante.

---

### Permisos granulares

Solo existen roles:

```text
admin
store
driver
```

No se definen permisos finos.

Dictamen: no inventar. No bloqueante.

---

### Invitaciones reales

Alta de roles no asume emails, WhatsApp ni invitaciones reales.

Dictamen: no bloqueante.

---

### Backend / núcleo de acciones

Muchas acciones dependen del núcleo futuro.

Esta etapa no implementa núcleo.

Dictamen: no bloqueante.

---

### Auditoría real backend

Auditoría fue proyectada como lectura administrativa, no como implementación real de logs.

Dictamen: no bloqueante.

---

## 12. Contradicciones detectadas

No se detecta contradicción estructural grave entre:

- Operación y Configuración;
- Configuración y Alta de roles;
- Alta de roles y Operación;
- Pedido #____ y Configuración → Pedidos;
- Local operativo y Configuración → Locales;
- Cuenta store y Local configurable;
- Repartidor driver y Repartidor operativo.

El riesgo transversal real es:

```text
Interpretar referencia como edición.
```

Solución global:

```text
Referencia no significa dueño de edición.
```

---

## 13. Reglas globales obligatorias

1. Admin se divide en tres universos principales: Operación, Configuración y Alta de roles.
2. Operación trabaja lo vivo.
3. Configuración trabaja lo estructural editable.
4. Alta de roles trabaja accesos.
5. Pedido #____ es la identidad del pedido concreto.
6. Solucionar solo existe dentro del contexto correcto.
7. Una entidad puede verse en varios lugares, pero se edita desde un solo dueño.
8. Referencia no significa edición.
9. Toda acción sensible requiere impacto, confirmación y resultado.
10. Configuración no abre ni modifica Pedido #____.
11. Alta de roles no opera pedidos.
12. Operación no cambia roles ni accesos.
13. Configuración no cambia usuarios ni active/inactive.
14. General no absorbe funciones con dueño.
15. Emergencias no reemplaza Operación ni Solucionar.
16. No se inventan roles nuevos.
17. No se asumen WhatsApp, pagos, invitaciones ni notificaciones reales.
18. Las referencias visuales no son UI final literal.
19. Este mapa no implementa backend.
20. Admin debe respetar usuario público, catálogo real, pedidos reales, tickets y seguimiento ya existentes.
21. Desactivar cuenta corta ingreso, no borra historial ni pedidos.
22. Catálogo no modifica pedidos ya creados.
23. Comunicación no envía mensajes reales.
24. Operación no muestra acciones fuera de contexto.
25. Configuración no se convierte en panel de todo.

---

## 14. Dictamen final

**Admin completo queda cerrado como plano técnico-funcional global.**

Los tres universos principales quedan ordenados:

```text
Admin
├── Operación
├── Configuración
└── Alta de roles
```

No se detectan contradicciones estructurales graves.  
No se detecta doble dueño si se aplica la regla de dueño único.  
No se detectan loops imposibles si las referencias cruzadas se mantienen como lectura o navegación controlada.

Este cierre no implementa todavía.  
No define backend.  
No define UI final.  
No prepara Codex todavía como instrucción directa de construcción.

Este documento gobierna los cierres anteriores y debe usarse como referencia superior para cualquier etapa futura del Admin real.
