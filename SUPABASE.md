# Integración Supabase en Pédilo

## 📋 Descripción

Pédilo integra **Supabase** para lectura de datos con **fallback automático a mock**.

### ✨ Características
- ✅ Lectura desde Supabase si está configurado
- ✅ Fallback automático a datos mock si falla Supabase
- ✅ Sin romper la arquitectura (State agnóstico a data source)
- ✅ Seguro: usa clave ANONIMA del frontend

---

## 🚀 Setup Supabase

### 1. Crear proyecto en Supabase

1. Ve a https://app.supabase.com
2. Crea un nuevo proyecto (o usa uno existente)
3. Espera a que esté listo (~2 min)

### 2. Obtener credenciales

En **Project Settings → API**:
- Copia `Project URL` → `VITE_SUPABASE_URL`
- Copia `anon public key` → `VITE_SUPABASE_ANON_KEY`

⚠️ **Importante**: Usa solo la clave `anon` (anonima), NUNCA la clave de servicio.

### 3. Configurar variables de entorno

```bash
# Copia .env.example a .env.local
cp .env.example .env.local

# Edita .env.local con tus credenciales
VITE_SUPABASE_URL=https://xxxxx.supabase.co
VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

> **Nota**: Si no configuras estas variables, la app usa **mock automáticamente**.

---

## 📊 Esquema de base de datos

Crea estas tablas en tu proyecto Supabase:

### `categorias`
```
- id (uuid, pk)
- nombre (text)
- descripcion (text)
```

### `subcategorias`
```
- id (uuid, pk)
- nombre (text)
- categoria_id (uuid, fk → categorias.id)
```

### `locales`
```
- id (uuid, pk)
- nombre (text)
- subcategoria_id (uuid, fk → subcategorias.id)
- descripcion (text)
- imagen_url (text, nullable)
- oferta (text, nullable)
```

### `productos`
```
- id (uuid, pk)
- nombre (text)
- precio (numeric)
- local_id (uuid, fk → locales.id)
- descripcion (text)
- imagen_url (text, nullable)
- oferta (text, nullable)
```

---

## 🔐 Row Level Security (RLS)

Todos los datos son **públicos de lectura**. Ejemplo para `categorias`:

```sql
-- Habilitar RLS
ALTER TABLE categorias ENABLE ROW LEVEL SECURITY;

-- Política de lectura pública
CREATE POLICY "categorias_read_public" ON categorias
  FOR SELECT
  USING (true);
```

Repite para: `subcategorias`, `locales`, `productos`.

---

## 🧪 Testing

Los tests usan **mock data** automáticamente (sin Supabase).

```bash
npm test
```

No necesitan variables de entorno configuradas.

---

## 🔄 Flujo de datos

```
app.js
  ↓
cargarDatos() [api.js]
  ↓
┌─────────────────────────┐
│ Intenta Supabase (si    │
│ está configurado)       │
└────────────┬────────────┘
             ↓ (falla o no configurado)
┌─────────────────────────┐
│ Fallback a mock data    │
└────────────┬────────────┘
             ↓
       INIT_DATA
             ↓
        state.js
             ↓
     Screens puras
```

---

## 📝 Archivos relacionados

- `src/services/supabase.js` - Cliente Supabase + queries
- `src/services/api.js` - Orquestación de datos (Supabase + fallback)
- `.env.example` - Variables de entorno (copiar a `.env.local`)
- `tests/flow.test.js` - Tests con mock data

---

## 🐛 Debugging

### Ver logs de inicialización

Abre la consola del navegador (F12):

```
✅ Supabase inicializado
✅ Categorías desde Supabase
...
✅ Usando datos de Supabase
```

o fallback:

```
⚠️ Supabase no configurado. Usando mock.
📦 Usando datos mock (fallback)
```

### Verificar configuración

En la consola del navegador:

```js
import { isSupabaseReady } from './src/services/supabase.js';
console.log(isSupabaseReady());
```

---

## ✅ Próximos pasos

- [ ] Crear tablas en Supabase
- [ ] Configurar RLS (lectura pública)
- [ ] Agregar variables de entorno
- [ ] Testear lectura de Supabase
- [ ] Implementar escritura (órdenes) en Supabase
