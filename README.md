# DevSync — Developer Accountability & Growth Platform

DevSync is a private developer accountability and growth platform designed to empower teams of software engineers through daily goal setting, team synchronization, and growth tracking.

---

## Architecture & Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.3 (Java 21)
- **Database**: PostgreSQL with Flyway migrations (`V1` through `V9`)
- **Testing**: H2 in-memory DB test profile with 151+ unit & WebMvc tests
- **Documentation**: OpenAPI / Swagger UI (`/swagger-ui.html`, `/v3/api-docs`)
- **Monitoring**: Spring Boot Actuator (`/actuator/health`)

### Frontend
- **Framework**: React 18 with TypeScript & Vite
- **Styling**: Tailwind CSS (Dark theme first, custom palette, slate/violet accents)
- **Routing**: React Router v7
- **Server State**: TanStack Query (React Query v5) with optimistic updates and caching
- **HTTP Client**: Centralized Axios client with standard error interceptors
- **Icons**: Lucide React

---

## Implemented Modules

### Step 1 — Project Foundation
- Spring Boot 3.4.3 + Java 21 foundation, PostgreSQL database with Hikari pool, Flyway migrations (`V1__init_schema.sql`), Actuator, Swagger/OpenAPI.

### Step 2A — Identity & Team Management
- `User`, `Team`, and `TeamMember` domains with UUID primary keys, IANA timezone validation, and role authorization (`OWNER`, `ADMIN`, `MEMBER`). Flyway `V2__create_user_team_schema.sql`.

### Step 2B — Daily Progress & Accountability
- `DailyProgress` entity mapping daily learning and task progress with unique `(user_id, team_id, progress_date)` constraints. Flyway `V3__create_daily_progress_schema.sql`.

### Step 2C — Goals & Task Management
- `Goal` and `Task` domains with status/priority enums, automatic goal completion, and team relationship derivations. Flyway `V4__create_goals_schema.sql` and `V5__create_tasks_schema.sql`.

### Step 2D — Notification & Reminder Domain
- In-app `Notification` domain with read/unread tracking and batch mark-as-read.
- Scheduled `Reminder` domain with IANA timezone validation and soft deletion. Flyway `V6` and `V7`.

### Step 2E — Achievement, Leaderboard & Coding Profile Domain
- `CodingProfile` domain for LeetCode, Codeforces, CodeChef, GitHub, HackerRank.
- `Achievement` domain with active point aggregation.
- Real-time `Leaderboard` calculation for `DAILY`, `WEEKLY`, `MONTHLY`, and `ALL_TIME` periods. Flyway `V8` and `V9`.

### Step 3A — Frontend Foundation
- **Application Shell**: Modern developer-focused layout with collapsible sidebar, mobile drawer, navbar with workspace/team switcher, user switcher, and notification badges.
- **Developer Dashboard (`/dashboard`)**: Focus time metrics, active goals summary, upcoming tasks with status badges, team leaderboard top 3 preview, recent badges, and quick-add progress modal.
- **Typed API Clients & Hooks**: End-to-end typed Axios modules and React Query hooks for all 10 backend domains.
- **Design System & UI Library**: Custom reusable `Button`, `Card`, `Badge`, `Modal`, `Input`, `Select`, `Skeleton`, `EmptyState`, `Toast`, `Tabs`, and `Pagination` components.
- **Route Shells**: Foundation routes for `/dashboard`, `/progress`, `/goals`, `/tasks`, `/team`, `/leaderboard`, `/achievements`, `/coding-profiles`, `/notifications`, and `/settings`.

---

## Running the Application

### 1. Backend Setup & Tests
```bash
# Run complete test suite (151 tests)
./mvnw clean test

# Run Spring Boot backend locally on port 8080
./mvnw spring-boot:run
```

### 2. Frontend Setup & Execution
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Copy environment configuration
cp .env.example .env

# Run Vite development server on http://localhost:5173
npm run dev

# Build production bundle
npm run build
```

### 3. Environment Variables (`frontend/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### 4. Interactive API Documentation
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Actuator Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
