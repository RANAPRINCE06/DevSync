# DevSync — Developer Accountability & Growth Platform

DevSync is a private developer accountability and growth platform designed to empower teams of software engineers through daily goal setting, team synchronization, and growth tracking.

---

## Implemented Features

### Step 1 — Project Foundation
- Spring Boot 3.4.3 + Java 21 foundation
- PostgreSQL database configuration with Hikari connection pool
- In-memory H2 database configuration for testing
- Database migrations with Flyway (`V1__init_schema.sql`)
- OpenAPI / Swagger documentation (`/swagger-ui.html`, `/v3/api-docs`)
- Spring Boot Actuator endpoints (`/actuator/health`)
- Environment variable configuration via `.env.example`
- Multi-stage Docker build & Docker Compose setup

### Step 2A — Identity & Team Management
- **User Domain**: `User` entity with UUID primary keys, IANA timezone validation, and active status tracking
- **Team Domain**: `Team` entity with UUID primary keys and transactional creator `OWNER` role initialization
- **Team Membership**: Explicit `TeamMember` entity with `TeamRole` enum (`OWNER`, `ADMIN`, `MEMBER`)
- **Database Migrations**: Flyway migration (`V2__create_user_team_schema.sql`) with UUID columns, unique constraints, and performance indexes
- **Validation**: Input validation for name length, email format, duplicate detection, and valid IANA ZoneId timezones
- **Standardized API Response & Exception Handling**: Centralized `ApiResponse<T>` wrapper and `@RestControllerAdvice` exception handler

---

## API Endpoints

### User APIs (`/api/v1/users`)
- `POST /api/v1/users` — Create a user profile
- `GET /api/v1/users/{id}` — Get user profile by UUID
- `GET /api/v1/users` — Get paginated list of users (`page`, `size`, `sort`)
- `PUT /api/v1/users/{id}` — Update user name, avatar URL, or timezone

### Team APIs (`/api/v1/teams`)
- `POST /api/v1/teams` — Create a team (assigns creator as `OWNER`)
- `GET /api/v1/teams/{id}` — Get team by UUID
- `GET /api/v1/teams` — Get paginated list of teams
- `PUT /api/v1/teams/{id}` — Update team name or description
- `POST /api/v1/teams/{teamId}/members/{userId}` — Add a user to a team (`MEMBER` role)
- `GET /api/v1/teams/{teamId}/members` — Get paginated team members

---

## Running the Application

### 1. Execute Unit & Integration Tests
```bash
./mvnw clean test
```

### 2. Local Execution
Copy `.env.example` to `.env` and set environment variables:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=devsync
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export SERVER_PORT=8080

./mvnw spring-boot:run
```

### 3. Docker Compose Execution
```bash
docker-compose up -d
```

### 4. Interactive API Documentation
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
