# customer-service — Módulo 4: Gestión de Clientes

Microservicio del TMS LogiTech responsable del registro y administración de la ficha maestra de clientes.
Cubre los requisitos funcionales **RF4.1 – RF4.10** definidos en la documentación de arquitectura.

---

## Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Java | 21 |
| Maven | 3.9+ (incluido en `mvnw`) |
| Docker + Docker Compose | Cualquier versión reciente |

---

## Levantar el servicio

### Paso 1 — Iniciar la base de datos

```bash
cd customer-service
docker compose up -d
```

Espera a que el contenedor esté `healthy` (unos 10 segundos):

```bash
docker compose ps
# Estado esperado: tms_customer_db   Up (healthy)
```

### Paso 2 — Arrancar la aplicación

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

El servicio levanta en **http://localhost:8084**

Flyway crea automáticamente las tablas en el primer arranque. Verás en el log:

```
Flyway Community Edition ... by Redgate
Database: jdbc:postgresql://localhost:5432/tms_customer_db
Successfully applied 1 migration to schema "public"
Started CustomerServiceApplication in X.XXX seconds
```

---

## Documentación interactiva (Swagger UI)

Una vez levantado, abre:

```
http://localhost:8084/swagger-ui.html
```

Desde ahí puedes probar **todos los endpoints** sin necesidad de Postman.

---

## Endpoints disponibles

| Método | URL | RF | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/customers` | RF4.1 | Registrar nuevo cliente |
| `GET` | `/api/v1/customers` | RF4.2 | Listar clientes (paginado + filtros) |
| `GET` | `/api/v1/customers/{id}` | RF4.3 | Detalle completo del cliente |
| `PUT` | `/api/v1/customers/{id}` | RF4.4 | Actualizar datos (RUC no modificable) |
| `PATCH` | `/api/v1/customers/{id}/deactivate` | RF4.5 | Inactivar cliente |
| `POST` | `/api/v1/customers/{id}/incidents` | RF4.7 | Registrar incidencia |
| `GET` | `/api/v1/customers/frequent` | RF4.8 | Clientes frecuentes |
| `PUT` | `/api/v1/customers/{id}/payment-conditions` | RF4.9 | Gestionar condición de pago |
| `POST` | `/api/v1/customers/{id}/tariffs` | RF4.10 | Asignar tarifa comercial |
| `POST` | `/api/v1/customers/batch` | Batch | Consulta masiva para Módulo 7 |

---

## Pruebas con curl

### Registrar un cliente (RF4.1)

```bash
curl -s -X POST http://localhost:8084/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "razonSocial": "Transportes Rápido SAC",
    "ruc": "20601234567",
    "direccionFiscal": "Av. Industrial 123, Lima",
    "telefono": "01-5551234",
    "correo": "contacto@rapidosac.pe",
    "personaContacto": "Juan Pérez"
  }' | python -m json.tool
```

Respuesta esperada (`HTTP 201`):
```json
{
  "success": true,
  "message": "Cliente registrado exitosamente.",
  "data": {
    "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "codigoCliente": "CLI-00001",
    "razonSocial": "Transportes Rápido SAC",
    "ruc": "20601234567",
    "estado": "ACTIVO",
    ...
  }
}
```

### Listar clientes con filtro (RF4.2)

```bash
curl -s "http://localhost:8084/api/v1/customers?estado=ACTIVO&size=5" | python -m json.tool
```

### Registrar incidencia (RF4.7)

```bash
curl -s -X POST http://localhost:8084/api/v1/customers/{ID}/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "tipoIncidencia": "RECLAMO",
    "descripcion": "El cliente reporta demora en entrega del 2024-06-01",
    "registradoPor": "operador1"
  }' | python -m json.tool
```

### Consultar duplicado de RUC (debe retornar 409)

```bash
curl -s -X POST http://localhost:8084/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{"razonSocial":"Otra Empresa","ruc":"20601234567"}' | python -m json.tool
# Esperado: HTTP 409, "Ya existe un cliente registrado con el RUC: 20601234567"
```

### Endpoint batch para Módulo 7

```bash
curl -s -X POST http://localhost:8084/api/v1/customers/batch \
  -H "Content-Type: application/json" \
  -d '{"ids": ["uuid-1", "uuid-2"]}' | python -m json.tool
```

---

## Detener el servicio

```bash
# Detener la app: Ctrl+C en la terminal donde corre mvnw

# Detener la base de datos
docker compose down

# Detener y eliminar volúmenes (borra todos los datos)
docker compose down -v
```

---

## Estructura del proyecto

```
customer-service/
├── docker-compose.yml
├── pom.xml
└── src/main/
    ├── java/com/tms/logistica/customerservice/
    │   ├── controller/        # CustomerController — endpoints REST
    │   ├── domain/
    │   │   ├── entity/        # Customer, PaymentCondition, CustomerTariff,
    │   │   │                  # CustomerIncident, CustomerAudit
    │   │   ├── enums/         # CustomerStatus, BillingType, IncidentType, IncidentStatus
    │   │   └── repository/    # JPA repositories
    │   ├── dto/
    │   │   ├── request/       # DTOs de entrada con validaciones
    │   │   └── response/      # DTOs de salida (Summary, Detail, ApiResponse)
    │   ├── exception/         # Excepciones de dominio + GlobalExceptionHandler
    │   └── service/           # CustomerService (interfaz) + CustomerServiceImpl
    └── resources/
        ├── application.properties
        └── db/migration/
            └── V1__create_customer_schema.sql   # Flyway — crea todas las tablas
```

---

## Notas de arquitectura

- **Bloqueo optimista:** la entidad `Customer` usa `@Version` (JPA). Modificaciones concurrentes retornan `HTTP 409`.
- **Auditoría:** cambios en `direccionFiscal`, `telefono`, `correo`, `personaContacto` y estado se registran automáticamente en `auditoria_cliente`.
- **RUC no modificable:** el endpoint `PUT /{id}` no expone el campo `ruc` en el request.
- **Incidencias no eliminables:** solo pueden cambiar a estado `ANULADA` (no hay `DELETE`).
- **Endpoint batch:** `POST /batch` resuelve hasta 500 IDs en una sola query `WHERE id IN (...)` — táctica de rendimiento ESC-M4-02.
