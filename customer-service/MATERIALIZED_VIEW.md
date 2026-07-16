# Demo — Patrón Materialized View (Tema Individual 0.8.4)

Aplicado al reporte de **clientes frecuentes** (RF4.8) del Módulo 4. Contrasta una
consulta de agregación "en caliente" contra una **vista materializada**
pre-calculada.

## Qué se agregó (sobre el customer-service base)

- **`V2__materialized_view_clientes_frecuentes.sql`**
  - Tabla `orden_transporte` (versión mínima local, solo para el demo; en
    producción vendría del `order-service`/Módulo 1).
  - Vista materializada `mv_cliente_frecuente` = conteo de órdenes ENTREGADAS y
    monto total por cliente.
- **Paquete `report/`**: DTOs, repositorio (SQL nativo con `JdbcTemplate`),
  servicio (cronometra cada consulta como métrica Micrometer) y controlador.

## Endpoints

Base: `/api/v1/reports/frequent-customers`

| Método | Ruta | Rol en el patrón |
|---|---|---|
| `POST` | `/seed?clientes=200&ordenes=50` | Genera datos de demo (≈10.000 órdenes) |
| `GET`  | `/realtime?limit=10` | Consulta **sin patrón**: JOIN + GROUP BY en caliente |
| `POST` | `/refresh` | Proceso de actualización: `REFRESH MATERIALIZED VIEW` |
| `GET`  | `/materialized?limit=10` | Lectura **con patrón**: desde la vista materializada |

Cada `GET` devuelve `source`, `elapsedMs` y `count`, además de los datos — así se
compara la latencia directamente.

## Flujo de la demo

```bash
# 0. Base de datos + servicio
docker compose up -d
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run

# 1. Generar datos
curl -X POST "http://localhost:8084/api/v1/reports/frequent-customers/seed?clientes=200&ordenes=50"

# 2. Consulta en caliente (lenta) — observa elapsedMs
curl "http://localhost:8084/api/v1/reports/frequent-customers/realtime?limit=10"

# 3. Refrescar la vista materializada
curl -X POST "http://localhost:8084/api/v1/reports/frequent-customers/refresh"

# 4. Consulta materializada (rápida) — compara elapsedMs con el paso 2
curl "http://localhost:8084/api/v1/reports/frequent-customers/materialized?limit=10"
```

También puedes probarlo todo desde **Swagger UI**: <http://localhost:8084/swagger-ui.html>.

## Enlace con Observabilidad (0.7.4)

Cada consulta publica la métrica `tms_frequent_report_seconds` (etiqueta
`source=realtime|materialized`), visible en Prometheus/Grafana. El dashboard RED
(`monitoring/`) grafica la diferencia de latencia entre ambas fuentes.
