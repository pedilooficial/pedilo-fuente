# 04 - Auditoria de flujos reales

## Flujos certificados por lectura y tests locales

1. Pedido local:
   - UI arma draft.
   - Adapter llama `createLocalOrder`.
   - Backend valida store/productos/payload.
   - Pedido nace en `/orders`.
   - Tracking publico lee estado seguro.
   - Admin/Store lo ven segun rules.
   - Driver no lo ve hasta estado/responsable driver.

2. Boton + compra:
   - UI arma draft.
   - Adapter llama `createPlusOrder`.
   - Backend valida compra, contacto, productos y pago.
   - Pedido nace con tracking y finanzas coherentes.

3. Boton + retiro/envio:
   - UI arma draft.
   - Adapter llama `createPlusOrder`.
   - Backend valida pickup/shipping.
   - Tracking funciona como pedido publico.

4. Store:
   - Observa propios.
   - Acepta, prepara, marca listo, cancela o abre incidencia por `operateLiveOrder`.
   - No opera ajeno por adapter, rules y backend.

5. Driver:
   - Observa disponibles/asignados.
   - Toma, retira y entrega por `operateLiveOrder`.
   - No opera ajeno por adapter, rules y backend.

6. Incidencia:
   - Store/Driver/Admin abre problema con motivo.
   - Se registra evento, comunicacion e IA asistida.
   - Tracking publico queda seguro.
   - Health lo cuenta.

7. Reclamo publico:
   - Reclamo persiste en `public_claims`.
   - Si hay tracking, vincula subcoleccion `orders/{id}/claims`.
   - No muta Pedido Vivo automaticamente.
   - Comunicacion preparada/disabled e IA asistida quedan registradas.
   - Health lo cuenta.

8. Cancelacion:
   - Requiere motivo.
   - Terminal no permite nuevas acciones.
   - Audita.
   - Marca revision financiera si hay pago/cobro.
   - Tracking cierra seguro.

9. Comunicacion:
   - No WhatsApp falso.
   - No push falso.
   - Prepared/disabled coherente.

10. IA asistida:
   - No proveedor externo.
   - No accion critica autonoma.
   - Resolucion Admin es auditoria.

11. Salud:
   - Cuenta alertas.
   - No corrige automaticamente.

## Tests agregados para esta auditoria

- `tests/final_pre_p_source_audit.test.js`

Cobertura agregada:

- exports y validadores criticos;
- flujo local completo Store/Driver hasta entrega;
- cadena incidencia/comunicacion/IA/health;
- copy seguro para comunicacion `sent`;
- no deploy/release/provider/direct order writes en runtime.
