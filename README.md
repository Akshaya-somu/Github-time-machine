````markdown
# ⏳ GitHub Time Machine

> **Explore how a GitHub repository evolved over time — commits, contributors, files, collaboration, and code history in one interactive dashboard.**

GitHub Time Machine is a full-stack repository intelligence platform that analyzes the complete Git history of public GitHub repositories and transforms it into an interactive visual timeline.

It uses **JGit** to analyze repository history, **Spring Boot + PostgreSQL** to persist and expose historical data through REST APIs, and **React + TypeScript** to provide an interactive dashboard for exploring repository evolution.

---

## 🚀 Why GitHub Time Machine?

GitHub repositories contain years of development history, but understanding that history usually requires manually navigating commits, contributors, branches, and file changes.

GitHub Time Machine turns that raw Git history into a structured, visual experience.

### It answers questions like:

- 📈 How has this repository evolved over time?
- 👨‍💻 Who contributed the most?
- 🤝 Which contributors worked on the same files?
- 📁 Which files changed throughout the project's history?
- 🔀 What happened in a particular commit?
- ⏳ What did the repository look like at a specific point in time?
- 📊 How did additions and deletions change over the years?

---

# ✨ Features

### 📦 Repository Analysis

- Analyze any public GitHub repository.
- Clone and inspect complete Git history.
- Extract commits, authors, file changes, and statistics.
- Preserve the complete repository history without truncation.

### ⏱️ Repository Timeline

- Chronological repository evolution.
- Commit timestamps and authors.
- Commit messages and file-change statistics.
- Cumulative repository statistics.

### 👨‍💻 Contributor Intelligence

- Contributor summaries.
- Commit counts.
- Additions and deletions.
- Files modified.
- Contributor collaboration graph.
- Contributor search and focused graph exploration.

### 📁 File Change Explorer

- Explore files changed throughout repository history.
- View additions, deletions, and change types.
- Inspect individual file history.

### 🔍 Commit Details

- Full commit hash.
- Author and email.
- Timestamp.
- Commit message.
- Parent commit information.
- File-level changes.

### ⏳ Time Travel

Select a historical commit and explore the repository's state at that point in its evolution.

View:

- Commits up to that point
- Contributors up to that point
- Files changed up to that point
- Cumulative additions
- Cumulative deletions

### 📊 Visual Analytics

- Additions vs. deletions charts.
- Repository evolution timeline.
- Contributor collaboration graph.
- Aggregated activity for large repositories.

### ⚡ Large Repository Support

The application is designed to handle repositories containing tens of thousands of commits and hundreds of thousands of file changes.

Frontend safeguards include:

- Bounded timeline rendering.
- Load More behavior.
- Contributor search.
- Limited React Flow graph rendering.
- Deferred loading of very large file datasets.
- Month/year chart aggregation.
- Progressive loading.
- Request loading and error states.

---

# 🏗️ Architecture

```text
                    GitHub Repository
                           │
                           ▼
                    ┌──────────────┐
                    │    JGit      │
                    │ Git Analyzer │
                    └──────┬───────┘
                           │
                           ▼
                 ┌───────────────────┐
                 │   Spring Boot     │
                 │ REST API Layer    │
                 └─────────┬─────────┘
                           │
                           ▼
                 ┌───────────────────┐
                 │   Service Layer   │
                 │                   │
                 │ Git Analyzer      │
                 │ Contributors      │
                 │ Timeline          │
                 └─────────┬─────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ PostgreSQL  │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ React/Vite  │
                    │ Dashboard   │
                    └─────────────┘
```
````

---

# 🛠️ Tech Stack

## Backend

| Technology        | Purpose                       |
| ----------------- | ----------------------------- |
| Java 21           | Core language                 |
| Spring Boot 3.3.3 | Backend framework             |
| Spring Web MVC    | REST APIs                     |
| Spring Data JPA   | Data access                   |
| Hibernate ORM     | Persistence                   |
| PostgreSQL        | Production database           |
| H2                | Testing database              |
| JGit 6.10         | Git repository analysis       |
| Lombok            | Boilerplate reduction         |
| Springdoc OpenAPI | API documentation             |
| Maven             | Build & dependency management |

## Frontend

| Technology   | Purpose                         |
| ------------ | ------------------------------- |
| React 19     | UI                              |
| TypeScript   | Type safety                     |
| Vite         | Frontend tooling                |
| Recharts     | Data visualization              |
| React Flow   | Contributor collaboration graph |
| Lucide React | Icons                           |
| ESLint       | Code quality                    |

---

# 📂 Project Structure

```text
github-time-machine/
│
├── pom.xml
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/com/githubtimemachine/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│
└── frontend/
    ├── package.json
    ├── vite.config.ts
    └── src/
        ├── components/
        ├── hooks/
        ├── services/
        ├── types/
        ├── App.tsx
        └── App.css
```

---

# ⚙️ Getting Started

## Prerequisites

Install:

- JDK 21
- Maven 3.9+
- Node.js 20+
- npm
- PostgreSQL 15+
- Git

Verify:

```powershell
java -version
mvn -version
node --version
npm --version
psql --version
git --version
```

---

# 🗄️ Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE github_time_machine;
```

The default development configuration uses:

```text
Host: localhost
Port: 5432
Database: github_time_machine
Username: postgres
```

> Configure your local password through your Spring configuration/environment. Do not commit production credentials.

The project currently uses Hibernate schema updates:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

There is currently no Flyway or Liquibase migration setup.

### Large Repository Database Requirements

Commit messages and Git file paths use PostgreSQL `TEXT` columns:

```sql
ALTER TABLE public.commits
ALTER COLUMN message TYPE TEXT;

ALTER TABLE public.file_changes
ALTER COLUMN file_path TYPE TEXT;
```

These are widening-only changes and preserve existing data.

---

# ▶️ Running the Backend

From the project root:

```powershell
cd C:\Users\aksha\github-time-machine
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Verify:

```powershell
curl.exe http://localhost:8080/api/v1/repositories
```

---

# 💻 Running the Frontend

Open another terminal:

```powershell
cd C:\Users\aksha\github-time-machine\frontend
npm install
npm run dev -- --host 0.0.0.0
```

Open:

```text
http://localhost:5173
```

The Vite development proxy forwards `/api` requests to:

```text
http://localhost:8080
```

---

# 🔎 Analyze a Repository

Enter a public GitHub repository URL in the dashboard.

Example:

```text
https://github.com/octocat/Hello-World.git
```

or:

```text
https://github.com/spring-projects/spring-boot.git
```

Then click:

**Analyze Repository**

The application clones/reuses the repository, analyzes its Git history, persists the results, and exposes them through the dashboard.

---

# 🔌 REST API

## Repository Management

### List repositories

```http
GET /api/v1/repositories
```

### Get repository

```http
GET /api/v1/repositories/{id}
```

### Create repository

```http
POST /api/v1/repositories
Content-Type: application/json
```

```json
{
  "repositoryUrl": "https://github.com/octocat/Hello-World.git"
}
```

### Delete repository

```http
DELETE /api/v1/repositories/{id}
```

---

## Repository Analysis

```http
POST /api/repositories/analyze
```

Request:

```json
{
  "repositoryUrl": "https://github.com/spring-projects/spring-boot.git"
}
```

Example verified response:

```json
{
  "repositoryId": 14,
  "fullName": "spring-projects/spring-boot",
  "remoteUrl": "https://github.com/spring-projects/spring-boot.git",
  "commitCount": 63514,
  "contributorCount": 1675,
  "fileChangeCount": 440599
}
```

---

## Commits

Paginated commits:

```http
GET /api/repositories/{id}/commits?page=0&size=20
```

Individual commit:

```http
GET /api/repositories/{id}/commits/{hash}
```

---

## Files

```http
GET /api/repositories/{id}/files
```

File history:

```http
GET /api/repositories/{id}/files/{path}/history
```

Large repositories defer the full file dataset in the frontend to avoid unnecessary browser memory usage.

---

## Contributors

List contributors:

```http
GET /api/repositories/{id}/contributors
```

Contributor details:

```http
GET /api/repositories/{id}/contributors/{contributorId}
```

Contributor collaboration graph:

```http
GET /api/repositories/{id}/contributors/graph
```

---

## Timeline

Repository timeline:

```http
GET /api/repositories/{id}/timeline
```

Historical time-travel snapshot:

```http
GET /api/repositories/{id}/timeline/{commitHash}
```

---

# 🧠 Backend Architecture

The backend follows a controller → service → repository architecture.

```text
HTTP Request
     │
     ▼
Controller
     │
     ▼
Service
     │
     ▼
Spring Data Repository
     │
     ▼
JPA Entity
     │
     ▼
PostgreSQL
```

### Core Services

#### `GitAnalyzerService`

Responsible for:

- GitHub URL validation
- Repository cloning/reuse
- Complete Git history traversal
- Commit analysis
- File-change analysis
- Contributor persistence
- Idempotent analysis

#### `ContributorAnalysisService`

Responsible for:

- Contributor summaries
- Contributor statistics
- Collaboration graph
- Efficient contributor aggregation

#### `RepositoryTimelineService`

Responsible for:

- Repository timeline
- Timeline statistics
- Cumulative historical snapshots
- Time-travel functionality

#### `GlobalExceptionHandler`

Provides consistent API error handling for:

- Validation errors
- Git errors
- Resource errors
- Unexpected server errors

---

# ⚡ Large Repository Engineering

A major design goal of GitHub Time Machine is handling repositories much larger than typical demo repositories.

### Backend optimizations

- Git history is processed as a stream.
- JGit clones use `setNoCheckout(true)`.
- Git diff computation happens outside database transactions.
- Commit persistence uses short transactions.
- Open-EntityManager-in-View is disabled.
- Valid repository clones are reused.
- Already-persisted commits are skipped.
- Commit messages and file paths use PostgreSQL `TEXT`.
- Contributor statistics are aggregated efficiently.

### Frontend optimizations

- Timeline rendering is bounded.
- Timeline supports progressive loading.
- Contributors are searchable and bounded.
- Contributor graphs are limited to manageable subsets.
- Large file datasets are loaded only when required.
- Charts aggregate large histories by time periods.
- Commit details are loaded for selected commits.
- Time-travel data is loaded on demand.
- Loading and error states prevent confusing UI behavior.

---

# 🔁 Idempotent Analysis

Repeated analysis of the same repository does **not** create duplicate data.

The application identifies records using:

```text
Repository
    → fullName

Commit
    → repository + commit hash

File Change
    → repository + commit + file path

Contributor
    → repository + email
```

### Verified Spring Boot repeat analysis

```text
Before repeat
────────────────────────
Commits:        63,514
File changes:  440,599
Contributors:   1,675


After repeat
────────────────────────
Commits:        63,514
File changes:  440,599
Contributors:   1,675
```

✅ No duplicate commits  
✅ No duplicate file changes  
✅ No duplicate contributors  
✅ Contributor statistics remain stable

---

# 🧪 Testing

Run the complete backend test suite:

```powershell
cd C:\Users\aksha\github-time-machine
mvn test -q
```

### Current verified result

```text
Tests:      15
Failures:    0
Errors:      0
```

The test suite covers:

- Spring application startup
- Repository analysis
- Persistence
- Repeat-analysis idempotency
- Contributor collaboration
- Timeline statistics
- Time travel
- Controller behavior
- Validation

### Frontend build

```powershell
cd C:\Users\aksha\github-time-machine\frontend
npm run build
```

---

# 📊 Verified Large Repository

The application has been successfully tested against:

### `spring-projects/spring-boot`

```text
Commits:              63,514
Contributors:          1,675
File changes:         440,599

Maximum commit message: 7,816 characters
Maximum file path:        271 characters
```

The complete history is preserved while the frontend limits how much data is rendered at one time.

---

# 📚 API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

---

# 🖥️ Dashboard

The dashboard provides dedicated views for:

```text
┌──────────────────────────────────────────┐
│             GitHub Time Machine          │
├───────────────┬──────────────────────────┤
│ Overview      │ Repository Statistics    │
│ Timeline      │ Commit Evolution         │
│ Contributors  │ Collaboration Graph      │
│ Files         │ File Changes             │
│ Time Travel   │ Historical Snapshots     │
└───────────────┴──────────────────────────┘
```

---

# 🔐 Data & Security Notes

GitHub Time Machine is intended for **public GitHub repositories**.

Do not commit:

- Database passwords
- API tokens
- GitHub personal access tokens
- Production credentials
- Environment-specific secrets

Use environment variables or external Spring configuration for production deployments.

---

# 🚧 Current Limitations

- Analysis is currently intended for public GitHub repositories.
- Very large repositories can take significant time to clone and analyze.
- Full file-change datasets can be extremely large and are therefore loaded progressively.
- Contributor graphs are intentionally bounded for browser performance.
- The project currently relies on Hibernate `ddl-auto: update` rather than a migration framework.

---

# 🔮 Future Improvements

Potential future enhancements include:

- [ ] GitHub OAuth integration
- [ ] Private repository support
- [ ] Branch comparison
- [ ] Pull-request history
- [ ] Issue activity analysis
- [ ] Release timeline
- [ ] Advanced contributor analytics
- [ ] Repository comparison
- [ ] Commit heatmaps
- [ ] Code ownership visualization
- [ ] Background/asynchronous repository analysis
- [ ] Job progress tracking
- [ ] Redis caching
- [ ] Production deployment
- [ ] Database migration management with Flyway

---

# 🎯 Project Highlights

GitHub Time Machine demonstrates:

- Full-stack application development
- Git internals and JGit
- REST API design
- Spring Boot architecture
- PostgreSQL data modeling
- JPA/Hibernate
- Large-scale data processing
- Transaction management
- Idempotent persistence
- Data visualization
- Graph visualization
- React performance optimization
- API-driven frontend architecture
- Historical/time-series analysis

---

# 👩‍💻 Author

**Akshaya Somu**

B.Tech — Computer Science & Engineering  
Shri Vishnu Engineering College for Women

---

## ⭐ GitHub Time Machine

**Understand not just what a repository looks like today — but how it got there.**

```

```
