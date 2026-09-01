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
- **Framework**: React 18 with TypeScript & Vite 6
- **Styling**: Tailwind CSS (Dark / Light / System theme modes, custom slate & violet accents)
- **Routing & Guards**: React Router v7 with `ProtectedRoute` and `PublicRoute` session protection
- **Authentication & State**: Centralized `AuthContext` with session token persistence, Axios interceptors, and `AppContext` synchronization
- **Server State**: TanStack Query (React Query v5) with optimistic updates and caching
- **HTTP Client**: Centralized Axios client with automatic Bearer token injection and global 401 redirect handling
- **Icons**: Lucide React

---

## Implemented Modules & Milestones

### Step 1 — Project Foundation
- Spring Boot 3.4.3 + Java 21 foundation, PostgreSQL database with Hikari pool, Flyway migrations (`V1__init_schema.sql`), Actuator, Swagger/OpenAPI.

### Step 2A — Identity & Team Management
- `User`, `Team`, and `TeamMember` domains with UUID primary keys, IANA timezone validation, and role authorization (`OWNER`, `ADMIN`, `MEMBER`). Flyway `V2`.

### Step 2B — Daily Progress & Accountability
- `DailyProgress` entity mapping daily learning and task progress with unique `(user_id, team_id, progress_date)` constraints. Flyway `V3`.

### Step 2C — Goals & Task Management
- `Goal` and `Task` domains with status/priority enums, automatic goal completion at 100%, and team relationship derivations. Flyway `V4` and `V5`.

### Step 2D — Notification & Reminder Domain
- In-app `Notification` domain with read/unread tracking and batch mark-as-read.
- Scheduled `Reminder` domain with IANA timezone validation and soft deletion. Flyway `V6` and `V7`.

### Step 2E — Achievement, Leaderboard & Coding Profile Domain
- `CodingProfile` domain for LeetCode, Codeforces, CodeChef, GitHub, HackerRank.
- `Achievement` domain with active point aggregation.
- Real-time `Leaderboard` calculation for `DAILY`, `WEEKLY`, `MONTHLY`, and `ALL_TIME` periods. Flyway `V8` and `V9`.

### Step 3A — Frontend Foundation
- Modern application shell with collapsible sidebar, mobile drawer, navbar, and foundational typed API client layer.

### Step 3B — Functional Productivity Modules & Complete UI/UX
- Complete interactive pages for `/dashboard`, `/progress`, `/goals`, `/tasks` (Kanban & List views), `/team`, `/leaderboard`, `/achievements`, `/coding-profiles`, `/notifications`, and `/settings`.

### Step 3C — Authentication, User Session & Production UX
- **Authentication Flow**: Login (`/login`), Registration with password strength meter (`/register`), and Password Recovery (`/forgot-password`).
- **Route Protection**: `ProtectedRoute` wrapper guarding all productivity pages with session resolution spinners and redirect preservation.
- **Session Management**: Persistent authentication token and user context in `localStorage` / `sessionStorage` with automatic Axios Bearer token attachment and global 401 handling.
- **Theme System**: Dark, Light, and System theme synchronization with local storage persistence.
- **Command Palette (`Ctrl+K` / `Cmd+K`)**: Keyboard arrow navigation, enter selection, quick actions (Create Goal, Create Task, Log Progress, Link Profile), and universal search.
- **Navbar Popover**: Live notification bell popover with recent unread alerts and one-click mark all as read.
- **Settings Center**: Profile updater, password change form, theme preferences, and danger zone account deactivation.

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
