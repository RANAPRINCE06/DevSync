# DevSync
> **Private Developer Productivity, Accountability & Growth Platform**

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.4-blue.svg?style=flat-square&logo=springsecurity)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations%20(V1--V11)-red.svg?style=flat-square&logo=flyway)](https://flywaydb.org/)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg?style=flat-square&logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6.svg?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-6.x-646CFF.svg?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.x-38B2AC.svg?style=flat-square&logo=tailwind-css)](https://tailwindcss.com/)
[![Tests](https://img.shields.io/badge/Tests-348%20Passed-success.svg?style=flat-square)](https://github.com/RANAPRINCE06/DevSync)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg?style=flat-square)](LICENSE)

---

## 1. Project Overview

**DevSync** is a private developer productivity, accountability, and technical growth platform engineered for individual software engineers, competitive programmers, and small collaborative squads.

Developers routinely struggle with:
- Maintaining consistent daily programming and study habits.
- Tracking learning velocity across multiple technical roadmaps.
- Breaking long-term career or contest milestones into actionable daily tasks.
- Maintaining mutual accountability within coding peer groups.
- Consolidating competitive programming metrics (LeetCode, Codeforces, GitHub) into a single dashboard.

Rather than functioning as a generic task-management tool, **DevSync** combines **developer accountability**, **hierarchical goal & task planning**, **daily progress tracking**, **team workspaces**, **gamified XP & leaderboards**, and **deep security & IDOR protection**.

---

## 2. System Architecture

DevSync is designed as a **Modular Monolithic Architecture**. The backend is maintained as a cohesive Spring Boot 3.4.3 application divided into decoupled business modules, communicating via strict service contracts and type-safe DTOs without premature distributed-system overhead.

### 2.1 High-Level Architecture (Full-Stack Request Lifecycle)

The end-to-end execution flow of an authenticated developer interaction traversing the client, gateway filter chain, modular monolith backend, and persistence layer:

```mermaid
sequenceDiagram
    autonumber
    actor Dev as Developer (Browser)
    participant UI as React UI (Tailwind & Recharts)
    participant Query as TanStack Query (Server State)
    participant Axios as Axios Client (Bearer Interceptor)
    participant Filter as JwtAuthenticationFilter
    participant SecCtx as SecurityContextHolder
    participant Ctrl as Spring REST Controller (@Valid)
    participant Svc as Business Service (@Transactional)
    participant CUP as CurrentUserProvider
    participant Repo as Ownership JPA Repository
    participant DB as PostgreSQL 17 Database

    Note over Dev,Axios: Client-Side Interaction & State Management
    Dev->>UI: Trigger action (e.g. view tasks or log progress)
    UI->>Query: useQuery() / useMutation()
    Query->>Axios: Invoke HTTP request
    Axios->>Axios: Attach Authorization: Bearer JWT header
    Axios->>Filter: HTTPS REST Request

    Note over Filter,SecCtx: Gateway & Security Pipeline
    Filter->>Filter: Validate JWT signature & claims (HS256)
    Filter->>SecCtx: Populate DevSyncPrincipal authentication
    Filter->>Ctrl: Forward authenticated request

    Note over Ctrl,CUP: Controller & Service Orchestration
    Ctrl->>Ctrl: Validate payload constraints (@Valid)
    Ctrl->>Svc: Invoke business operation
    Svc->>CUP: getCurrentUserId()
    CUP-->>Svc: Authenticated User UUID
    Svc->>Svc: Enforce business logic & ownership policies

    Note over Svc,DB: Persistence & Database Layer
    Svc->>Repo: Query with ownership filter (id, ownerId)
    Repo->>DB: Execute SQL query with WHERE constraints
    DB-->>Repo: Return entity record / result set
    Repo-->>Svc: Return entity / aggregate

    Note over Svc,Dev: Response Propagation & State Synchronization
    Svc-->>Ctrl: Return Response DTO
    Ctrl-->>Axios: 200 OK / 201 Created (ApiResponse DTO)
    Axios-->>Query: Resolve promise & update query cache
    Query-->>UI: Re-render components with fresh data
    UI-->>Dev: Display updated view & metrics
```

### 2.2 Layered Backend Architecture Execution Lifecycle

Every backend module adheres to strict layer separation, executing within a transactional, security-validated request lifecycle:

```mermaid
sequenceDiagram
    autonumber
    actor Client as HTTP Client (Frontend)
    participant Filter as Security Filter Chain
    participant Controller as 1. Controller Layer (@Valid)
    participant Service as 2. Service Layer (@Transactional)
    participant Provider as CurrentUserProvider
    participant Repo as 3. Repository Layer (JPQL)
    participant DB as 4. PostgreSQL Database Layer

    Note over Client,Filter: 1. Request Ingestion & Security Guard
    Client->>Filter: HTTPS Request + Bearer JWT
    Filter->>Filter: Verify signature and extract Principal
    Filter->>Controller: Dispatch to matching @RequestMapping

    Note over Controller,Service: 2. Validation & Business Invariants
    Controller->>Controller: Validate incoming request body (@Valid)
    Controller->>Service: Execute business method with validated DTO
    Service->>Provider: getCurrentUserId()
    Provider-->>Service: Return authenticated caller UUID
    Service->>Service: Enforce domain invariants & role checks

    Note over Service,DB: 3. Ownership-Constrained Persistence
    Service->>Repo: findByIdAndOwnerId(id, ownerId)
    Repo->>DB: SELECT id, title FROM entity WHERE id = ? AND owner_id = ?
    DB-->>Repo: Return record
    Repo-->>Service: Return Optional entity

    Note over Service,Client: 4. Response Serialization & Output
    Service->>Service: Map Entity to Response DTO
    Service-->>Controller: Return Response DTO
    Controller-->>Client: 200 OK / 201 Created (ApiResponse DTO)
```

### 2.3 Frontend Client Architecture & State Lifecycle

The frontend single-page application manages client state, routing guards, asynchronous server synchronization, and optimistic mutations:

```mermaid
sequenceDiagram
    autonumber
    actor User as Developer (User)
    participant Router as React Router (ProtectedRoute)
    participant UI as Page Component (View Layer)
    participant Query as TanStack Query Cache
    participant Axios as Axios Interceptor
    participant API as Backend REST API

    Note over User,Router: 1. Route Guarding & Authentication Check
    User->>Router: Navigate to protected URL (e.g. /tasks)
    Router->>Router: Verify JWT token presence & expiration
    Router->>UI: Mount & render protected component

    Note over UI,Query: 2. Server State Inspection
    UI->>Query: useQuery(queryKey, queryFn)
    alt Cache hit & data is fresh
        Query-->>UI: Serve cached data instantly (0ms latency)
    else Cache miss or stale
        Query->>Axios: Execute queryFn() HTTP call
        Note over Axios,API: 3. Automatic Bearer Injection
        Axios->>Axios: Intercept & attach Authorization header
        Axios->>API: GET /api/v1/tasks
        API-->>Axios: 200 OK (Task list JSON)
        Axios-->>Query: Resolve data & update cache
        Query-->>UI: Re-render with server data
    end

    Note over User,UI: 4. Optimistic UI Mutation
    User->>UI: Toggle task completion status
    UI->>Query: useMutation() with onMutate callback
    Query->>Query: Optimistically update local cache
    Query-->>UI: Instant visual update on screen
    Query->>Axios: PATCH /api/v1/tasks/:id/status
    Axios->>API: Send mutation request
    API-->>Axios: 200 OK (Confirmed by server)
    Axios-->>Query: Reconcile cache & invalidate related queries
```

### 2.4 Authentication & Session Lifecycle Architecture

Stateless, cryptographic authentication powered by PBKDF2 password hashing and HS256 signed JSON Web Tokens:

```mermaid
sequenceDiagram
    autonumber
    actor User as Developer (Client)
    participant UI as Login / Register UI
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthServiceImpl
    participant Encoder as PasswordEncoder (PBKDF2)
    participant JWT as JwtTokenProvider
    participant Repo as UserRepository
    participant DB as PostgreSQL Database

    Note over User,AuthCtrl: 1. Submission & Ingestion
    User->>UI: Enter credentials (email, password)
    UI->>AuthCtrl: POST /api/v1/auth/login (AuthRequest DTO)
    AuthCtrl->>AuthCtrl: Validate email format & password presence

    Note over AuthCtrl,DB: 2. Password Verification
    AuthCtrl->>AuthSvc: login(AuthRequest)
    AuthSvc->>Repo: findByEmailAndActiveTrue(email)
    Repo->>DB: SELECT id, password_hash FROM users WHERE email = ?
    DB-->>Repo: Return user record
    Repo-->>AuthSvc: User entity
    AuthSvc->>Encoder: matches(rawPassword, storedHash)
    Encoder->>Encoder: Compute PBKDF2 hash with stored salt
    Encoder-->>AuthSvc: Credentials verified (true)

    Note over AuthSvc,UI: 3. Token Generation & Session Storage
    AuthSvc->>JWT: generateToken(UserPrincipal)
    JWT->>JWT: Sign HS256 JWT with 24-hour expiration
    JWT-->>AuthSvc: Signed compact JWT string
    AuthSvc-->>AuthCtrl: AuthResponse (JWT, UserProfile)
    AuthCtrl-->>UI: 200 OK (ApiResponse with Bearer Token)
    UI->>UI: Persist JWT in secure storage & update AuthContext
    UI-->>User: Seamless redirect to /dashboard
```

---

## 3. Technology Stack

### 3.1 Backend Technologies

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 21 (LTS) | Modern Java runtime (records, pattern matching, virtual threads ready) |
| **Spring Boot** | 3.4.3 | Application framework and dependency injection |
| **Spring Security** | 6.4 | Stateless authentication, authorization filter chain, method security |
| **Spring Data JPA** | 3.4.3 | Repository persistence, dynamic Specifications, JPQL queries |
| **Hibernate** | 6.6 | Object-Relational Mapping (ORM) and entity lifecycle management |
| **PostgreSQL** | 17 | Production relational database engine |
| **Flyway** | 10.x | Schema versioning and reproducible database migrations |
| **JJWT** | 0.12.6 | RFC 7519 cryptographically signed HMAC-SHA256 (HS256) JWTs |
| **Lombok** | 1.18 | Boilerplate reduction for builders and accessors |
| **Springdoc OpenAPI** | 2.8 | Interactive OpenAPI v3 specification and Swagger UI |
| **Spring Boot Actuator**| 3.4.3 | Application health metrics (`/actuator/health`, `/actuator/info`) |
| **JUnit 5 & Mockito** | Latest | Unit testing, WebMvc slices, and cross-user integration tests |
| **Maven** | 3.9+ | Build and dependency lifecycle automation |

### 3.2 Frontend Technologies

| Technology | Version | Purpose |
|---|---|---|
| **React** | 18.3 | Declarative component UI library |
| **TypeScript** | 5.x | End-to-end static typing and interface definitions |
| **Vite** | 6.x | Fast dev server and optimized Rollup production bundler |
| **Tailwind CSS** | 3.4 | Utility-first CSS styling with dark/light theme support |
| **TanStack React Query**| v5 | Asynchronous server-state synchronization and caching |
| **React Router** | v7 | Single-page application client routing and route guards |
| **Axios** | 1.7 | HTTP client with automatic Authorization header injection |
| **Lucide React** | Latest | Clean, accessible icon system |
| **Recharts** | 2.x | Responsive SVG charts and productivity visualizations |

---

## 4. Core Application Modules Interaction Architecture

The modular monolith facilitates decoupled, cohesive cross-module communication via strict service interfaces and type-safe events:

```mermaid
sequenceDiagram
    autonumber
    actor Dev as Developer Workspace
    participant Auth as Auth & User Module
    participant Goals as Goal Planning Module
    participant Tasks as Task Execution Module
    participant Progress as Daily Progress Tracker
    participant Teams as Team Workspaces & RBAC
    participant Game as Gamified XP & Leaderboard

    Note over Dev,Tasks: 1. Personal Productivity Workflow
    Dev->>Goals: Create quarterly milestones (Goal Planning)
    Goals->>Tasks: Break milestone into actionable Tasks
    Dev->>Tasks: Execute & update Task status (Kanban Board)
    Dev->>Progress: Log daily standup, study minutes & challenges

    Note over Progress,Game: 2. Automated Gamification & Growth
    Progress->>Game: Trigger XP & streak calculation
    Tasks->>Game: Award completion XP & badges
    Game->>Game: Update personal streak & calculate leaderboard rank

    Note over Dev,Teams: 3. Team Collaboration Workflow
    Dev->>Teams: Access Squad Workspace
    Teams->>Goals: View & manage Collaborative Team Goals
    Teams->>Tasks: Assign collaborative squad tasks to teammates
    Teams->>Game: View team-wide productivity rankings
```

### 4.1 Authentication & User Management
- User registration with real-time client & server password validation.
- Secure login verifying PBKDF2 password hashes.
- Stateless HS256 JWT generation with 24-hour expiration.
- Centralized `CurrentUserProvider` extracting verified `DevSyncPrincipal` from `SecurityContextHolder`.

### 4.2 Dashboard Center
- Aggregated productivity telemetry: today's focus minutes, task completion velocity score (0–100), active goal milestones, and squad rankings.
- 7-day trailing focus activity charts and quick-log progress action.

### 4.3 Goals Management
- Hierarchical technical goal setting with team associations, target dates, priorities (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), and progress percentage tracking.
- Automatic status transition to `COMPLETED` when progress reaches 100%.
- Strict object-level ownership checks preventing foreign access.

### 4.4 Task Management
- Actionable work items linked directly to parent goals.
- Status management (`TODO`, `IN_PROGRESS`, `BLOCKED`, `COMPLETED`), priorities, estimated vs. actual minutes, and due dates.
- Interactive Kanban Board and searchable list views.
- Automatically tracks `completedAt` timestamp upon transition.

### 4.5 Daily Progress & Accountability
- Structured daily developer standup: what was studied, completed work, study duration (minutes), challenges/blockers encountered, improvement areas, and tomorrow's plan.
- Enforces unique `(user_id, team_id, progress_date)` constraints to ensure daily consistency.

### 4.6 Team Workspaces
- Collaboration squads with member roles (`OWNER`, `ADMIN`, `MEMBER`).
- Shared team goals, team task visibility, and collective progress tracking.

### 4.7 Leaderboard & Gamification
- Dynamic ranking engines calculated across `DAILY`, `WEEKLY`, `MONTHLY`, and `ALL_TIME` horizons.
- Weighted scoring based on task completion, study minutes, goal milestones, and streak consistency.

### 4.8 Coding Profiles
- Centralized dashboard tracking external competitive programming and open-source profiles: **LeetCode**, **Codeforces**, **CodeChef**, **HackerRank**, and **GitHub**.

### 4.9 Notifications & Reminders
- In-app notification center with read/unread tracking and batch mark-as-read.
- Developer deadline and study reminders with IANA timezone validation.

### 4.10 Command Palette (`Ctrl + K` / `Cmd + K`)
- Fast keyboard-first search, navigation, and modal actions (Create Goal, Create Task, Log Daily Progress, Link Profile).

---

## 5. Security & IDOR Protection Architecture

DevSync implements **Backend-First Defense-in-Depth** to completely eliminate **Broken Object Level Authorization (BOLA)** and **Insecure Direct Object Reference (IDOR)** vulnerabilities.

```mermaid
sequenceDiagram
    autonumber
    actor Alice as Alice (Authenticated User)
    actor Bob as Bob (Unauthorized Attacker)
    participant Filter as JwtAuthenticationFilter
    participant Controller as TaskController
    participant Service as TaskServiceImpl
    participant Provider as CurrentUserProvider
    participant Repo as TaskRepository
    participant DB as PostgreSQL DB

    Note over Alice,Filter: Legitimate User Request
    Alice->>Filter: GET /api/v1/tasks/{id} (Token A)
    Filter->>Filter: Verify signature and claims
    Filter->>Controller: SecurityContext populated with Alice UUID
    Controller->>Service: getTaskById(taskId)
    Service->>Provider: getCurrentUserId() returns Alice UUID
    Service->>Repo: findByIdAndGoalOwnerId(taskId, Alice UUID)
    Repo->>DB: SELECT id, title FROM tasks WHERE id = ? AND goal.owner_id = ?
    DB-->>Repo: Task Record Found
    Repo-->>Service: Task Entity
    Service-->>Controller: TaskResponse DTO
    Controller-->>Alice: 200 OK (Task Payload)

    Note over Bob,Filter: Malicious IDOR or BOLA Attempt
    Bob->>Filter: GET /api/v1/tasks/{id} (Token B)
    Filter->>Filter: Verify signature and claims
    Filter->>Controller: SecurityContext populated with Bob UUID
    Controller->>Service: getTaskById(taskId)
    Service->>Provider: getCurrentUserId() returns Bob UUID
    Service->>Repo: findByIdAndGoalOwnerId(taskId, Bob UUID)
    Repo->>DB: SELECT id, title FROM tasks WHERE id = ? AND goal.owner_id = ?
    DB-->>Repo: Empty Result (Record does not belong to Bob)
    Repo-->>Service: Optional.empty()
    Service-->>Controller: throw ResourceNotFoundException
    Controller-->>Bob: 404 Not Found (Zero Information Leakage)
```

### 5.1 Critical Security Invariants

1. **Zero Client Trust**: User ID supplied via URL parameters, query strings, or JSON request bodies is **never** accepted as proof of ownership. Authenticated identity is derived strictly from the cryptographically verified JWT via `CurrentUserProvider.getCurrentUserId()`.
2. **Ownership-Constrained Queries**: Repository queries enforce ownership directly in SQL/JPQL:
   - `GoalRepository.findByIdAndOwnerId(id, ownerId)`
   - `TaskRepository.findByIdAndGoalOwnerId(id, ownerId)`
   - `DailyProgressRepository.findByIdAndUserId(id, userId)`
3. **No Information Leakage (404 Policy)**: Attempting to retrieve, update, or delete another user's private resource by guessing a UUID yields **HTTP 404 Not Found** instead of 403 Forbidden, preventing attackers from confirming whether a foreign UUID exists.
4. **Identity Spoofing Rejection (403 Policy)**: Attempting to create a resource with another user's identity (e.g. Alice sending Bob's `userId` or `ownerId` in the request body) results in **HTTP 403 Forbidden** (`AccessDeniedException`).
5. **Parent Relationship Integrity**: Tasks can only be attached to goals owned by the authenticated caller. Assigning a task to another user's goal is rejected with HTTP 404.

### 5.2 Collaborative Team RBAC & Workspace Authorization (Phase A3.4)

DevSync enforces strict separation between personal resources and collaborative squad resources with role-based access control (`OWNER`, `ADMIN`, `MEMBER`):

```mermaid
sequenceDiagram
    autonumber
    actor Owner as Team Owner / Admin
    actor Member as Team Member
    actor Outsider as Non-Member (Attacker)
    participant Filter as JwtAuthenticationFilter
    participant Controller as TeamResourceController
    participant AuthServ as TeamAuthorizationService
    participant TaskServ as TaskServiceImpl
    participant DB as PostgreSQL Database

    Note over Owner,DB: Flow A: Owner or Admin Creates Collaborative Team Task
    Owner->>Filter: POST /api/v1/tasks (teamTask: true, assigneeId: Member UUID)
    Filter->>Controller: Authenticated as Owner UUID
    Controller->>TaskServ: createTask(request)
    TaskServ->>AuthServ: requireCanManageTeam(teamId)
    AuthServ->>DB: Query team_members for role IN (OWNER, ADMIN)
    DB-->>AuthServ: Verified active OWNER role
    TaskServ->>DB: INSERT INTO tasks (team_task = true, assignee_id = Member)
    DB-->>TaskServ: Saved Team Task record
    TaskServ-->>Owner: 201 Created (Team Task DTO)

    Note over Member,DB: Flow B: Assigned Member Updates Progress (Allowed)
    Member->>Filter: PUT /api/v1/tasks/:id (status: COMPLETED, actualMinutes: 45)
    Filter->>Controller: Authenticated as Member UUID
    Controller->>TaskServ: updateTask(taskId, request)
    TaskServ->>AuthServ: requireActiveMember(teamId)
    AuthServ-->>TaskServ: Verified active team membership
    TaskServ->>TaskServ: Allow progress fields update (status, minutes)
    TaskServ->>DB: UPDATE tasks SET status = COMPLETED, actual_minutes = 45
    DB-->>TaskServ: Updated record
    TaskServ-->>Member: 200 OK (Progress Saved)

    Note over Member,Controller: Flow C: Member Attempts Metadata Tampering / Deletion (Rejected)
    Member->>Filter: DELETE /api/v1/tasks/:id
    Filter->>Controller: Authenticated as Member UUID
    Controller->>TaskServ: deleteTask(taskId)
    TaskServ->>AuthServ: requireCanManageTeam(teamId)
    AuthServ-->>Controller: throw AccessDeniedException
    Controller-->>Member: 403 Forbidden (Insufficient Role)

    Note over Outsider,Controller: Flow D: Non-Member Access Attempt (Zero Leakage)
    Outsider->>Filter: GET /api/v1/tasks/:id
    Filter->>Controller: Authenticated as Outsider UUID
    Controller->>TaskServ: getTaskById(taskId)
    TaskServ->>AuthServ: requireActiveMember(teamId)
    AuthServ-->>Controller: throw ResourceNotFoundException
    Controller-->>Outsider: 404 Not Found (Zero Information Leakage)
```

---

## 6. Database Entity Relationships & Relational Data Architecture

Entity relationships and foreign key cascades across personal, collaborative team, and platform integration schemas:

```mermaid
sequenceDiagram
    autonumber
    participant Users as users (UUID PK)
    participant Teams as teams (UUID PK)
    participant Members as team_members (user_id, team_id)
    participant Goals as goals (owner_id, team_id)
    participant Tasks as tasks (goal_id, team_id, assignee_id)
    participant Progress as daily_progress (user_id, team_id)
    participant Profiles as coding_profiles / accounts (user_id)
    participant Badges as achievements & reminders (user_id)

    Note over Users,Teams: 1. Core Identity & Multi-Tenant Scoping
    Users->>Members: FK user_id (1:N user team participation)
    Teams->>Members: FK team_id (1:N team roster composition)

    Note over Users,Goals: 2. Ownership & Goal Planning Schema
    Users->>Goals: FK owner_id (1:N user goal ownership)
    Teams-->>Goals: FK team_id nullable (1:N collaborative team scoping)

    Note over Goals,Tasks: 3. Hierarchical Task Execution Schema
    Goals->>Tasks: FK goal_id (1:N goal task decomposition)
    Teams-->>Tasks: FK team_id nullable (1:N squad task scoping)
    Users-->>Tasks: FK assignee_id nullable (1:N delegated task assignment)

    Note over Users,Progress: 4. Daily Accountability & Growth Schema
    Users->>Progress: FK user_id (1:N daily standup progress logs)
    Teams-->>Progress: FK team_id nullable (1:N team-scoped daily accountability)

    Note over Users,Badges: 5. Profiles, Gamification & Notification Schema
    Users->>Profiles: FK user_id (1:N competitive coding platform accounts)
    Users->>Badges: FK user_id (1:N earned achievements, notifications & reminders)
```

### 6.1 Core Relational Schema

| Entity | Primary Key | Key Foreign Keys | Key Attributes & Constraints |
|---|---|---|---|
| **`users`** | `id` (UUID) | — | `email` (Unique), `password_hash`, `timezone`, `active` |
| **`teams`** | `id` (UUID) | — | `name`, `description`, `active` |
| **`team_members`** | `id` (UUID) | `user_id`, `team_id` | `role` (`OWNER`, `ADMIN`, `MEMBER`), `active` |
| **`goals`** | `id` (UUID) | `owner_id`, `team_id` | `title`, `status`, `priority`, `progress_percentage` |
| **`tasks`** | `id` (UUID) | `goal_id`, `assignee_id`, `team_id` | `title`, `status`, `priority`, `due_date`, `actual_minutes` |
| **`daily_progress`** | `id` (UUID) | `user_id`, `team_id` | `progress_date`, `what_studied`, `study_minutes`, `status` |
| **`coding_profiles`** | `id` (UUID) | `user_id` | `platform` (LeetCode, GitHub, etc.), `username` |
| **`achievements`** | `id` (UUID) | `user_id` | `badge_name`, `xp_awarded`, `earned_at` |
| **`reminders`** | `id` (UUID) | `user_id` | `title`, `remind_at`, `is_sent` |
| **`notifications`** | `id` (UUID) | `user_id` | `title`, `message`, `is_read` |

---

## 7. Implementation Progress & Testing Verification

### 7.1 Milestones Completed

| Milestone | Scope | Status | Verification |
|---|---|---|---|
| **Phase 1: Foundation** | Spring Boot 3.4.3, PostgreSQL, JPA, Flyway (V1–V9), Maven | ✅ Complete | Build & Flyway migrations pass |
| **Phase 2: Domain Layer** | Users, Teams, Goals, Tasks, Daily Progress, Leaderboard | ✅ Complete | 151 core domain unit/slice tests |
| **Phase 3: Frontend** | React 18, Vite 6, Tailwind CSS, TanStack Query v5, Kanban | ✅ Complete | Vite production build pass |
| **Phase A1: Spring Security** | SecurityConfig, PBKDF2 PasswordEncoder, Error Handlers | ✅ Complete | Stateless session, CORS, CSRF disabled |
| **Phase A2: JWT Auth** | JJWT 0.12.6, JwtTokenProvider, JwtAuthenticationFilter | ✅ Complete | 202 backend tests passing |
| **Phase A3.1: User Context** | CurrentUserProvider, SecurityService, @EnableMethodSecurity | ✅ Complete | 228 backend tests passing |
| **Phase A3.2: Ownership Security** | IDOR protection on Tasks, Goals, Daily Progress | ✅ Complete | 251 backend tests passing |
| **Phase A3.3: Team Authorization** | Squad roles (OWNER, ADMIN, MEMBER), workspace isolation | ✅ Complete | 295 backend tests passing |
| **Phase A3.4: Collaborative Resources** | Team Goals & Tasks, assignee progress, metadata protection | ✅ Complete | 325 backend tests passing |
| **Phase A3.5: Security Regression Audit** | Comprehensive JWT, IDOR, RBAC, partial update hardening | ✅ Complete | 332 backend tests passing |
| **Step 5A: Coding Intelligence Foundation** | Multi-platform schema (V12), snapshot architecture, /coding UI | ✅ Complete | **348 backend tests passing (0 failures)** |

### 7.2 Backend Test Suite Breakdown (348 Tests)

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Tests run: 348, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **Authentication & Security Infrastructure** (42 tests):
  - `JwtSecurityTest`: 29 tests (Token generation, validation, expiration, signature tamper checks, endpoint security boundaries)
  - `CurrentUserProviderTest`: 13 tests (Context extraction, anonymous rejection, principal type checks)
- **Method Authorization & Utilities** (13 tests):
  - `SecurityServiceTest`: 8 tests (Ownership verification, delegation)
  - `SecurityMethodAuthorizationTest`: 5 tests (`@EnableMethodSecurity`, bean resolution, public/protected route regressions)
- **Object-Level Authorization & IDOR Integration Tests** (23 tests):
  - `GoalOwnershipSecurityTest`: 8 tests (Cross-user read, update, delete, progress tamper, spoofing rejection)
  - `TaskOwnershipSecurityTest`: 9 tests (Foreign task lookups, goal tampering, assignee spoofing, status protection)
  - `ProgressOwnershipSecurityTest`: 6 tests (Cross-user access, identity spoofing, scoped list queries)
- **Domain Services & Web Controllers** (173 tests):
  - Goal, Task, Progress, Team, User, Coding Profile, Leaderboard, Notification, and Reminder test suites.

### 7.3 Frontend Production Build Verification

```bash
$ npm --prefix frontend run build
> devsync-frontend@0.1.0 build
> tsc && vite build

vite v6.4.3 building for production...
✓ 1758 modules transformed.
dist/index.html                   0.96 kB │ gzip:   0.54 kB
dist/assets/index-DqCnLOFQ.css   57.44 kB │ gzip:   9.52 kB
dist/assets/index-BEQ-ovPK.js   528.05 kB │ gzip: 151.76 kB
✓ built in 13.58s
exit code: 0
```

---

## 8. Getting Started & Local Development

### 8.1 Prerequisites
- **Java 21 JDK** installed ([Eclipse Temurin](https://adoptium.net/) or OpenJDK).
- **Node.js** (v18.x or v20.x LTS) & **npm**.
- **PostgreSQL 17** running locally or in Docker.

### 8.2 Database Setup
Create the local PostgreSQL database:
```sql
CREATE DATABASE devsync;
```

### 8.3 Backend Configuration & Startup
```powershell
# 1. Clone repository
git clone https://github.com/RANAPRINCE06/DevSync.git
cd DevSync

# 2. Run the full test suite (251 tests using H2 in-memory test profile)
.\mvnw.cmd clean test

# 3. Start Spring Boot backend (Flyway runs migrations automatically on localhost:8080)
.\mvnw.cmd spring-boot:run
```

Backend endpoints:
- **API Base**: `http://localhost:8080/api/v1`
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 8.4 Frontend Configuration & Startup
```powershell
# 1. Navigate to frontend directory
cd frontend

# 2. Install dependencies
npm install

# 3. Start Vite dev server on http://localhost:5173
npm run dev

# 4. Production build check
npm run build
```

---

## 9. API Reference Overview

All business endpoints under `/api/v1/**` require an `Authorization: Bearer <JWT>` header, except for public authentication routes.

| Method | Endpoint | Description | Security Policy |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new user | Public |
| `POST` | `/api/v1/auth/login` | Authenticate & obtain JWT | Public |
| `GET` | `/api/v1/goals` | List caller's goals | Authenticated (scoped to caller) |
| `POST` | `/api/v1/goals` | Create goal under a team | Authenticated (caller is owner) |
| `GET` | `/api/v1/goals/{id}` | Get goal by UUID | Owner only (404 if foreign) |
| `PUT` | `/api/v1/goals/{id}` | Update goal & progress | Owner only (404 if foreign) |
| `DELETE` | `/api/v1/goals/{id}` | Soft delete goal | Owner only (404 if foreign) |
| `GET` | `/api/v1/tasks` | List caller's tasks | Authenticated (scoped to caller's goals) |
| `POST` | `/api/v1/tasks` | Create task under caller's goal | Authenticated (owner of parent goal) |
| `GET` | `/api/v1/tasks/{id}` | Get task by UUID | Owner only (404 if foreign) |
| `PUT` | `/api/v1/tasks/{id}` | Update task details & status | Owner only (404 if foreign) |
| `DELETE` | `/api/v1/tasks/{id}` | Soft delete task | Owner only (404 if foreign) |
| `GET` | `/api/v1/progress` | List caller's daily progress logs | Authenticated (scoped to caller) |
| `POST` | `/api/v1/progress` | Record daily study/work log | Authenticated (caller's identity) |
| `GET` | `/api/v1/progress/{id}` | Get progress entry by UUID | Owner only (404 if foreign) |
| `PUT` | `/api/v1/progress/{id}` | Update progress entry | Owner only (404 if foreign) |
| `GET` | `/api/v1/teams` | List teams | Authenticated |
| `GET` | `/api/v1/leaderboard` | View team / global leaderboard | Authenticated |

---

## 10. Future Development Roadmap

- [x] **Phase A1**: Spring Security foundation & password encoding.
- [x] **Phase A2**: Real HS256 JWT authentication & token validation filter.
- [x] **Phase A3.1**: Centralized security foundation (`CurrentUserProvider`, `SecurityService`).
- [x] **Phase A3.2**: Resource ownership enforcement & IDOR protection (Goals, Tasks, Daily Progress).
- [ ] **Phase A3.3**: Team membership verification, Team Lead role authorization & workspace boundaries.
- [ ] **Phase A3.4**: Coding profile and notification resource scoping.
- [ ] **Phase A4**: Automated reminder background scheduler & real-time WebSocket notifications.
- [ ] **Phase A5**: Production containerization (Docker, Docker Compose) & rate limiting.

---

## 11. Contributing & License

Contributions, issues, and feature requests are welcome!

Distributed under the **MIT License**. See `LICENSE` for more information.
