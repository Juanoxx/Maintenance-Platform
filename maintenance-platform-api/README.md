# Maintenance Platform API

Backend Spring Boot para el sistema de gestion de mantenciones de edificios.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL + Flyway
- OpenAPI (Swagger UI)
- Docker Compose

## Ejecutar local

1. Crear archivo `.env` desde `.env.example`.

2. Levantar PostgreSQL:

```bash
docker compose up -d
```

3. Ejecutar backend:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Accesos:

- API base: `http://localhost:8090/api/v1`
- Health: `http://localhost:8090/api/v1/actuator/health`
- Swagger: `http://localhost:8090/api/v1/swagger`

## Bootstrap de usuarios

- El **primer registro** en `/auth/register` se crea como `ADMIN`.
- Los siguientes registros se crean como `RESIDENT`.

Ejemplo de primer registro (admin):

```json
{
  "fullName": "Admin Principal",
  "email": "admin@demo.com",
  "password": "Admin1234!",
  "buildingId": null
}
```

## Variables importantes

Revisa `.env.example`:

- `DB_PORT=5433` (para no chocar con Postgres local en 5432)
- `SERVER_PORT=8090`
- `JWT_SECRET`, `JWT_ACCESS_EXP_MIN`, `JWT_REFRESH_EXP_DAYS`

## Comandos utiles

```bash
./mvnw test
./mvnw -DskipTests package
docker compose down -v
```

## Endpoints principales

Auth

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

Incidencias

- `GET /incidents` (con filtros opcionales)
- `GET /incidents/{id}`
- `POST /incidents`
- `PATCH /incidents/{id}/assign`
- `PATCH /incidents/{id}/status`
- `PATCH /incidents/{id}/priority` (admin)
- `GET /incidents/{id}/comments`
- `POST /incidents/{id}/comments`
- `GET /incidents/{id}/attachments`
- `POST /incidents/{id}/attachments`
- `GET /incidents/{id}/history`

Reportes

- `GET /reports/incidents.csv` (con filtros opcionales)

Dashboard

- `GET /dashboard/admin`
- `GET /dashboard/technician`
- `GET /dashboard/resident`

## Filtros disponibles en incidencias y CSV

Parámetros opcionales:

- `buildingId`
- `status` (`PENDIENTE`, `ASIGNADA`, `EN_PROCESO`, `RESUELTA`, `CERRADA`, `REABIERTA`, `CANCELADA`)
- `priority` (`BAJA`, `MEDIA`, `ALTA`, `CRITICA`)
- `category` (`ELECTRICIDAD`, `GASFITERIA`, `SEGURIDAD`, `LIMPIEZA`, `ASCENSOR`, `RUIDO`, `OTRO`)
- `createdFrom` (`yyyy-MM-dd`)
- `createdTo` (`yyyy-MM-dd`)
- `technicianId`
- `overdue` (`true`/`false`)

Ejemplo:

`GET /api/v1/incidents?buildingId=1&status=PENDIENTE&createdFrom=2026-03-01&createdTo=2026-03-31`
