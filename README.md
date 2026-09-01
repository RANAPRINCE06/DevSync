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

### Step 2B — Daily Progress & Accountability Domain
- **Daily Progress Entity**: `DailyProgress` entity mapping `daily_progress` table with `@ManyToOne(fetch = FetchType.LAZY)` links to `User` and `Team`
- **Progress Status**: Enum `ProgressStatus` (`IN_PROGRESS`, `COMPLETED`, `PARTIAL`)
- **Database Uniqueness & Indexing**: `UNIQUE(user_id, team_id, progress_date)` constraint enforcing one entry per user/team per date in Flyway migration (`V3__create_daily_progress_schema.sql`)
- **Business Validation**: Active team membership verification, `studyMinutes` limit (0-1440 mins), max 2000 text length constraints, and date range validation (`fromDate <= toDate`)
- **Dynamic Paginated Filtering**: Spring Data JPA Specification (`DailyProgressSpecification`) supporting flexible query combinations (`userId`, `teamId`, `date`, `fromDate`, `toDate`, `status`) with Pageable sorting defaults

### Step 2C — Goals & Task Management Domain
- **Goal Domain**: `Goal` entity mapping `goals` table with `GoalStatus` (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `ON_HOLD`, `CANCELLED`) and `GoalPriority` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- **Goal Business Rules**: Owner active team membership validation, date range validation (`startDate <= targetDate`), 0-100 progress percentage range, automatic transition to `COMPLETED` when progress reaches 100%, and soft deletion (`active = false`).
- **Task Domain**: `Task` entity mapping `tasks` table with `TaskStatus` (`TODO`, `IN_PROGRESS`, `COMPLETED`, `BLOCKED`, `CANCELLED`) and `TaskPriority` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- **Task Business Rules**: Direct derivation of Team from Goal, active assignee team membership verification, `dueDate` validation against Goal `startDate`, minute constraints (0-1440), automatic `completedAt` timestamp lifecycle management, and soft deletion (`active = false`).
- **Database Migrations**: Flyway migrations `V4__create_goals_schema.sql` and `V5__create_tasks_schema.sql` with check constraints and performance indexes.
- **Dynamic Paginated Filtering**: Spring Data JPA Specifications (`GoalSpecification`, `TaskSpecification`) with Pageable sorting defaults.

### Step 2D — Notification & Reminder Domain
- **Notification Domain (`com.devsync.notification`)**: `Notification` entity mapping `notifications` table with `NotificationType` (`DAILY_REMINDER`, `GOAL_REMINDER`, `TASK_REMINDER`, `SYSTEM`, `ACHIEVEMENT`) and `NotificationStatus` (`UNREAD`, `READ`).
- **Notification Business Rules**: Validates user existence, starts as `UNREAD`, records `readAt` upon transition to `READ` (idempotent), batch `markAllAsRead`, count unread notifications, and paginated dynamic filtering.
- **Reminder Domain (`com.devsync.reminder`)**: `Reminder` entity mapping `reminders` table with `ReminderType` (`DAILY_PROGRESS`, `GOAL`, `TASK`) and `ReminderStatus` (`ACTIVE`, `PAUSED`, `COMPLETED`, `CANCELLED`).
- **Reminder Business Rules**: Validates user existence, validates team existence and active membership in that team, validates IANA timezone (`ZoneId.of()`), validates date range (`startDate <= endDate`), starts as `ACTIVE` and `active = true`, immutable ownership fields, and soft deletion (`active = false`, `status = CANCELLED`).
- **Database Migrations**: Flyway migrations `V6__create_notifications_schema.sql` and `V7__create_reminders_schema.sql` with table constraints and performance indexes.

### Step 2E — Achievement, Leaderboard & Coding Profile Domain
- **Coding Profile Domain (`com.devsync.codingprofile`)**: `CodingProfile` entity mapping `coding_profiles` table with `CodingPlatform` enum (`LEETCODE`, `CODEFORCES`, `CODECHEF`, `HACKERRANK`, `GITHUB`, `OTHER`). Supports rating, problems solved, contests participated, rank, URL validation, duplicate active profile prevention per user/platform, and soft deletion (`active = false`).
- **Achievement Domain (`com.devsync.achievement`)**: `Achievement` entity mapping `achievements` table with `AchievementType` enum (`STREAK`, `DSA`, `GOAL`, `TASK`, `CODING_PLATFORM`, `TEAM`, `SPECIAL`). Supports point tracking, dynamic filtering, point aggregation, and soft deletion (`active = false`).
- **Team Leaderboard Domain (`com.devsync.leaderboard`)**: Real-time deterministic score and ranking computation for active team members.
  - **Deterministic Scoring Rules**:
    - Daily Progress Entry: `+10` points
    - Completed Task: `+20` points
    - Completed Goal: `+50` points
    - Achievement Points: `+` actual active achievement points
  - **Deterministic Tie-Breaker Ordering**:
    1. `score` DESC
    2. `completedTasks` DESC
    3. `progressEntries` DESC
    4. `userName` ASC
  - **Periods**: `DAILY` (today), `WEEKLY` (Monday to now), `MONTHLY` (1st of month to now), `ALL_TIME` (all records).
- **Database Migrations**: Flyway migrations `V8__create_coding_profiles_schema.sql` and `V9__create_achievements_schema.sql` with check constraints and performance indexes.

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

### Daily Progress APIs (`/api/v1/progress`)
- `POST /api/v1/progress` — Record daily learning & task progress
- `GET /api/v1/progress/{id}` — Get daily progress entry by UUID
- `PUT /api/v1/progress/{id}` — Update progress details (what studied, completed, study minutes, status)
- `GET /api/v1/progress` — Paginated & filtered list (`userId`, `teamId`, `date`, `fromDate`, `toDate`, `status`, `page`, `size`, `sort`)

### Goal APIs (`/api/v1/goals`)
- `POST /api/v1/goals` — Create a learning/development goal for a team
- `GET /api/v1/goals/{id}` — Get goal details by UUID
- `PUT /api/v1/goals/{id}` — Update goal title, description, dates, priority, progress, or status
- `DELETE /api/v1/goals/{id}` — Soft delete (deactivate) goal
- `GET /api/v1/goals` — Paginated & filtered list (`ownerId`, `teamId`, `status`, `priority`, `active`, `startDate`, `targetDate`, `page`, `size`, `sort`)

### Task APIs (`/api/v1/tasks`)
- `POST /api/v1/tasks` — Create a task under an active goal (team derived automatically)
- `GET /api/v1/tasks/{id}` — Get task details by UUID
- `PUT /api/v1/tasks/{id}` — Update task details, priority, due date, status, or actual minutes
- `DELETE /api/v1/tasks/{id}` — Soft delete (deactivate) task
- `GET /api/v1/tasks` — Paginated & filtered list (`goalId`, `assigneeId`, `teamId`, `status`, `priority`, `dueDate`, `active`, `page`, `size`, `sort`)

### Notification APIs (`/api/v1/notifications`)
- `POST /api/v1/notifications` — Create an in-app notification
- `GET /api/v1/notifications/{id}` — Get notification details by UUID
- `GET /api/v1/notifications` — Paginated & filtered list (`userId`, `type`, `status`, `page`, `size`, `sort`)
- `GET /api/v1/notifications/unread` — Paginated list of unread notifications
- `GET /api/v1/notifications/unread/count` — Get total unread count for a user
- `PATCH /api/v1/notifications/{id}/read` — Mark a notification as read (idempotent)
- `PATCH /api/v1/notifications/read-all` — Mark all unread notifications for a user as read
- `DELETE /api/v1/notifications/{id}` — Delete notification by UUID

### Reminder APIs (`/api/v1/reminders`)
- `POST /api/v1/reminders` — Configure a scheduled reminder
- `GET /api/v1/reminders/{id}` — Get reminder details by UUID
- `PUT /api/v1/reminders/{id}` — Update reminder title, message, time, timezone, dates, or status
- `DELETE /api/v1/reminders/{id}` — Soft delete (deactivate) reminder (`active = false`, `status = CANCELLED`)
- `GET /api/v1/reminders` — Paginated & filtered list (`userId`, `teamId`, `type`, `status`, `active`, `startDate`, `endDate`, `page`, `size`, `sort`)

### Coding Profile APIs (`/api/v1/coding-profiles`)
- `POST /api/v1/coding-profiles` — Link a coding profile
- `GET /api/v1/coding-profiles/{id}` — Get coding profile details by UUID
- `PUT /api/v1/coding-profiles/{id}` — Update coding profile stats or active state
- `DELETE /api/v1/coding-profiles/{id}` — Soft delete coding profile (`active = false`)
- `GET /api/v1/coding-profiles` — Paginated & filtered list (`userId`, `platform`, `active`, `page`, `size`, `sort`)
- `GET /api/v1/coding-profiles/user/{userId}` — Get all coding profiles for a user

### Achievement APIs (`/api/v1/achievements`)
- `POST /api/v1/achievements` — Award/create an achievement
- `GET /api/v1/achievements/{id}` — Get achievement details by UUID
- `PUT /api/v1/achievements/{id}` — Update achievement title, points, icon, or status
- `DELETE /api/v1/achievements/{id}` — Soft delete achievement (`active = false`)
- `GET /api/v1/achievements` — Paginated & filtered list (`userId`, `type`, `active`, `earnedFrom`, `earnedTo`, `page`, `size`, `sort`)
- `GET /api/v1/achievements/user/{userId}` — Get all achievements for a user
- `GET /api/v1/achievements/user/{userId}/points` — Get aggregate active achievement points for a user

### Leaderboard APIs (`/api/v1/leaderboard`)
- `GET /api/v1/leaderboard/teams/{teamId}` — Get ranked leaderboard for a team (`period=DAILY|WEEKLY|MONTHLY|ALL_TIME`, `page`, `size`, `sort`)
- `GET /api/v1/leaderboard/teams/{teamId}/me/{userId}` — Get specific member ranking and score breakdown in a team
- `GET /api/v1/leaderboard/teams/{teamId}/period/{period}` — Get leaderboard for explicit period

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
