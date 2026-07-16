# guide-service — Guías de Remisión Electrónica SUNAT

**Microservicio del sistema TMS**  
**Responsable:** Diego Arturo Huaman Bonilla — Integrante 1  
**Módulo:** 3 — Guías de Remisión Electrónica SUNAT  
**Tema individual:** Integración con APIs de terceros

---

## Stack tecnológico

| Capa       | Tecnología               |
|------------|--------------------------|
| Lenguaje   | Java 21                  |
| Framework  | Spring Boot 3.2.5        |
| ORM        | Spring Data JPA / Hibernate |
| BD         | MySQL 8.x (local)        |
| Build      | Maven                    |

---

## Configuración local

### 1. Crear la base de datos

```sql
-- Ejecutar el script:
mysql -u root -p < sql/01_schema.sql
```

### 2. Ajustar credenciales

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tms_guide_db?...
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

### 3. Levantar el servicio

```bash
mvn spring-boot:run
# Puerto: 8083
```

---

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST   | `/api/v1/guias` | Crear guía (estado PENDIENTE) |
| GET    | `/api/v1/guias/{uuid}` | Consultar guía por UUID |
| GET    | `/api/v1/guias/orden/{ordenId}` | Listar guías de una orden |
| POST   | `/api/v1/guias/{uuid}/enviar` | Enviar al OSE/SUNAT |
| POST   | `/api/v1/guias/{uuid}/anular?motivo=...` | Anular guía |

---

## Ciclo de vida de la guía

```
PENDIENTE → EN_PROCESO → ACEPTADA
                       → RECHAZADA
          → ANULADA (desde cualquier estado)
```

Si SUNAT no responde, la guía vuelve a **PENDIENTE** con un `proximo_reenvio`
programado a +5 minutos. El scheduler la reintenta automáticamente cada 5 minutos
(máximo 5 intentos).

---

## Estructura del proyecto

```
guide-service/
├── sql/
│   └── 01_schema.sql              # Script de creación MySQL
└── src/main/java/com/tms/guideservice/
    ├── controller/
    │   └── GuiaController.java
    ├── service/
    │   ├── GuiaService.java        # Lógica de negocio
    │   ├── GREBuilder.java         # Construye el XML (XSD SUNAT)
    │   └── SunatGateway.java       # Comunicación con OSE/SUNAT
    ├── repository/
    │   ├── GuiaRemisionRepository.java
    │   └── AuditoriaGuiaRepository.java
    ├── model/
    │   ├── entity/                 # GuiaRemision, GuiaDetalle, AuditoriaGuia
    │   ├── dto/request/            # CrearGuiaRequest
    │   ├── dto/response/           # GuiaResponse, ApiResponse
    │   └── enums/                  # EstadoGuia
    ├── exception/                  # GuiaException, GlobalExceptionHandler
    ├── config/                     # ReenvioScheduler
    └── util/                       # GuiaMapper, NumeroGuiaGenerator
```

---

## Interacciones con otros microservicios

| Servicio         | Tipo       | Descripción |
|------------------|------------|-------------|
| order-service    | Asíncrono  | Recibe evento al cerrar una orden |
| fleet-service    | Síncrono   | Consulta placa y SOAT del vehículo |
| staff-service    | Síncrono   | Consulta licencia vigente del conductor |
| billing-service  | Asíncrono  | Notifica guía aceptada para factura |
