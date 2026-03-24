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

1. Levantar PostgreSQL:

```bash
docker compose up -d
```

2. Ejecutar backend:

```bash
./mvnw spring-boot:run
```

3. Accesos:

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
