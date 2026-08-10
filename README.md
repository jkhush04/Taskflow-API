# TaskFlow API

Role-Based Project & Task Management System — Spring Boot 3 / Java 21 / PostgreSQL / JWT.

## One-time cleanup (only if you have old containers from a previous attempt)

```bash
docker rm -f taskflow-pg taskflow-postgres 2>/dev/null    # remove old containers if present
docker volume prune -f                                     # remove orphaned volumes (optional)
```

## Quick start (dev)

```bash
# 1. Start Postgres via docker-compose — this creates a container named
#    "taskflow-postgres" with user/pass/db that already match application-dev.yml.
docker compose up -d

# 2. Confirm it's healthy before starting the app
docker compose ps          # STATUS should say "healthy"

# 3. Run the app (defaults to the "dev" profile — see application.yml)
mvn spring-boot:run
```

No environment variables are required for dev — `application-dev.yml` ships with
matching defaults for `docker-compose.yml`. Flyway migrates the schema (and seeds
the three RBAC roles) automatically on boot.

```bash
# 4. Register a user (defaults to ROLE_MEMBER; pass "role": "ROLE_MANAGER" or "ROLE_ADMIN")
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","email":"ada@taskflow.dev","password":"SecurePass123!","role":"ROLE_MANAGER"}'

# 5. Use the returned token
curl http://localhost:8080/api/projects -H "Authorization: Bearer <token>"
```

## Running with the prod profile

The `prod` profile has **no fallback defaults** for `DB_USERNAME`, `DB_PASSWORD`,
or `JWT_SECRET` — the app refuses to start unless they're set explicitly. This is
intentional: it prevents ever accidentally deploying with dev credentials or a
guessable JWT secret.

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_HOST=your-prod-host DB_NAME=taskflow \
DB_USERNAME=your_prod_user DB_PASSWORD=your_prod_password \
JWT_SECRET=$(openssl rand -base64 48) \
mvn spring-boot:run
```

## Run tests

```bash
mvn clean install   # unit (Mockito) + integration (@SpringBootTest + H2) — no external DB needed
```

Tests run against an in-memory H2 database (`application-test.yml`), fully
isolated from whatever Postgres container you have running locally.

## Config file map

| File | Purpose |
|---|---|
| `application.yml` | Shared settings; active profile defaults to `dev` |
| `application-dev.yml` | Local dev: matches `docker-compose.yml` credentials, verbose SQL/logging |
| `application-prod.yml` | Production: no credential fallbacks, quiet logging |
| `src/test/resources/application-test.yml` | H2 in-memory DB for the test suite |
| `docker-compose.yml` | Local Postgres 16, matches `application-dev.yml` exactly |

## Key environment variables

| Variable | Dev default | Prod |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `taskflow` | required |
| `DB_USERNAME` / `DB_PASSWORD` | `taskflow_user` / `taskflow_pass` | **required, no fallback** |
| `JWT_SECRET` | dev placeholder | **required, no fallback** |
| `JWT_EXPIRATION_MS` | 86400000 (24h) | same |
| `REMINDER_CRON` | `0 0 * * * *` (hourly) | same |

## Endpoint summary

| Method | Path | Roles |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| POST/PUT/DELETE | `/api/projects/**` | ADMIN, MANAGER |
| GET | `/api/projects/**` | ADMIN, MANAGER, MEMBER |
| POST/DELETE | `/api/tasks/**` | ADMIN, MANAGER |
| GET, PATCH | `/api/tasks/**` | ADMIN, MANAGER, MEMBER |
| GET | `/api/tasks?status=&projectId=&assigneeId=&page=&size=&sort=deadline,asc` | paginated/filtered/sorted task search |
