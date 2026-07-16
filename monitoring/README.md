# Observabilidad y Monitoreo — Tema Individual 0.7.4

Stack de monitoreo para el TMS LogiTech: **Prometheus** recolecta las métricas de
`customer-service` (Módulo 4) y `auth-service` (Módulo 8), y **Grafana** las
visualiza con un dashboard basado en el método **RED** (Rate, Errors, Duration).

## Requisitos

- Docker + Docker Compose.
- Los microservicios corriendo en el host (`customer-service` en `:8084`,
  `auth-service` en `:8088`). Cada uno ya expone `/actuator/prometheus`.

## Cómo levantar

```bash
# 1. Arranca los servicios (en sus carpetas), por ejemplo:
cd customer-service && docker compose up -d && ./mvnw spring-boot:run
cd auth-service    && docker compose up -d && ./mvnw spring-boot:run

# 2. Arranca el stack de monitoreo
cd monitoring
docker compose up -d
```

- **Prometheus:** <http://localhost:9090> — en *Status → Targets* deben verse
  `customer-service` y `auth-service` en estado **UP**.
- **Grafana:** <http://localhost:3000> — el dashboard *"TMS LogiTech — Observabilidad (RED)"*
  se provisiona automáticamente (acceso anónimo habilitado).

## Qué muestra el dashboard

| Panel | Métrica (PromQL) | Pilar |
|---|---|---|
| Rate | `rate(http_server_requests_seconds_count[1m])` | Tráfico |
| Errors | `rate(http_server_requests_seconds_count{status=~"5.."}[1m])` | Errores 5xx |
| Duration | `histogram_quantile(0.95, ... http_server_requests_seconds_bucket ...)` | Latencia P95 |
| Materialized View | `histogram_quantile(0.95, ... tms_frequent_report_seconds_bucket ...)` | Latencia del reporte por fuente |

El último panel conecta este tema con el de **Materialized View (0.8.4)**: al
comparar la fuente `realtime` vs `materialized` se ve en vivo la diferencia de
latencia del patrón.

## Cómo generar tráfico para la demo

Con `customer-service` arriba, ejecuta el flujo del reporte (ver
`customer-service/MATERIALIZED_VIEW.md`) o lanza varias peticiones a los
endpoints para poblar las gráficas.

> Nota: `host.docker.internal` permite que Prometheus (en contenedor) alcance
> los servicios del host. En Docker Desktop (Windows/Mac) funciona out-of-the-box;
> en Linux se resuelve con el `extra_hosts: host-gateway` del compose.
