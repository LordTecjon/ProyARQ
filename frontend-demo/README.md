# Frontend Demo - order-service + cost-service

Flujo para exposicion:

1. Crear una orden en `order-service`.
2. Tomar el `id` de la orden creada.
3. Registrar costo estimado, costo real y gastos en `cost-service`.
4. Consultar el resumen de costos de la orden.

## Puertos usados

- `order-service`: http://localhost:8081
- `cost-service`: http://localhost:8082
- `billing-service`: http://localhost:8087
- `frontend-demo`: http://localhost:5173

El frontend usa un proxy Node sin dependencias para evitar problemas de CORS:

- `/order-api/*` -> `order-service`
- `/cost-api/*` -> `cost-service`
- `/billing-api/*` -> `billing-service`

## Como levantar para la demo

Terminal 1:

```bash
cd order-service
mvn spring-boot:run
```

Terminal 2:

```bash
cd cost-service
mvn spring-boot:run
```

Terminal 3:

```bash
cd billing-service
mvn spring-boot:run
```

Terminal 4:

```bash
cd frontend-demo
node server.js
```

Abrir:

```text
http://localhost:5173
```

## Flujo en pantalla

1. Click en `Crear orden`.
2. Verificar que se llenen `Orden ID` y `Codigo OT`.
3. Click en `Registrar costos`.
4. Click en `Consultar resumen`.
5. En la seccion 3, click en `Emitir comprobante` (factura la orden en billing-service).
6. Click en `Registrar pago total` para ver el saldo en cero.
