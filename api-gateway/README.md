# API Gateway Demo

Gateway local para la demo de calidad:

- Entrada unica: `http://localhost:8080`
- `/api/orders/**` -> `order-service:8081`
- `/api/trip-costs/**` -> `cost-service:8082`

Ejecutar:

```bash
node server.js
```
