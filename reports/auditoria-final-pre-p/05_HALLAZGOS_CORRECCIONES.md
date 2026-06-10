# 05 - Hallazgos y correcciones

## Hallazgos criticos

- Ninguno.

## Hallazgos medios

- UI Admin/Store/Driver mostraba `communicationStatus == "sent"` como "Enviada por canal real".
- Motivo: aunque el backend actual no produce `sent` para proveedores externos, el texto podia sugerir envio real si aparecia ese estado en datos futuros/manuales.

## Hallazgos menores

- No existe infraestructura de tests JVM/Android locales dedicada; se valida Android con build/lint offline.
- `app/google-services.json` y `local.properties` existen localmente como configuracion sensible no trackeada; no se modificaron.

## Correcciones aplicadas

- `app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt`: label `sent` corregido a "Registrada como enviada; verificar canal".
- `app/src/main/java/com/pedilo/app/ui/store/StoreApp.kt`: label `sent` corregido a "Registrada como enviada; verificar canal".
- `app/src/main/java/com/pedilo/app/ui/driver/DriverApp.kt`: label `sent` corregido a "Registrada como enviada; verificar canal".
- `tests/final_pre_p_source_audit.test.js`: agregado para cubrir la correccion y flujos finales pre-P.

## Fallas no corregidas

- Ninguna dentro del alcance.

## Bloqueos reales

- Ninguno para iniciar Bloque P.

## Fuera de alcance no ejecutado

- No se ejecuto Bloque P.
- No se publico.
- No se genero AAB release firmado.
- No se ejecuto deploy.
- No se ejecuto seed productivo.
- No se tocaron credenciales ni providers externos.
