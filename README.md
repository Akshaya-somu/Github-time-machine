````markdown
# ⏳ GitHub Time Machine

> **Explore how a GitHub repository evolved over time — commits, contributors, files, collaboration, and historical snapshots in one interactive dashboard.**

GitHub Time Machine is a full-stack **Git repository intelligence platform** that analyzes the complete Git history of public GitHub repositories and transforms it into an interactive visual experience.

Built with **JGit, Spring Boot, PostgreSQL, React, TypeScript, Recharts, and React Flow**.

---

## 🚀 Overview

GitHub repositories contain years of development history, but understanding that history often requires manually navigating commits, contributors, files, and changes.

GitHub Time Machine brings this information together into a single dashboard to help answer:

- 📈 How has a repository evolved over time?
- 👨‍💻 Who contributed to the project?
- 🤝 Which contributors worked on the same files?
- 📁 Which files changed throughout the project's history?
- 🔀 What happened in a particular commit?
- ⏳ What did the repository look like at a specific point in time?
- 📊 How did additions and deletions change over the years?

---

## ✨ Features

### 📦 Repository Analysis

- Analyze any public GitHub repository
- Clone and inspect complete Git history
- Extract commits, authors, file changes, and statistics
- Preserve the complete repository history

### ⏱️ Repository Timeline

- Chronological commit timeline
- Commit timestamps and authors
- Commit messages
- Files changed per commit
- Cumulative repository statistics

### 👨‍💻 Contributor Intelligence

- Contributor summaries
- Commit statistics
- Additions and deletions
- Files modified
- Contributor search
- Contributor collaboration graph

### 📁 File Change Explorer

- Explore repository file changes
- View additions, deletions, and change types
- Inspect individual file history

### 🔍 Commit Explorer

- Full commit hash
- Author and email
- Timestamp
- Commit message
- Parent commits
- File-level changes

### ⏳ Time Travel

Select a historical commit and explore the repository's state at that point in its evolution.

View:

- Commits up to that point
- Contributors up to that point
- Files changed
- Cumulative additions
- Cumulative deletions

### 📊 Visual Analytics

- Repository evolution timeline
- Additions vs. deletions charts
- Contributor collaboration graph
- Historical activity trends
- Time-based repository statistics

### ⚡ Large Repository Support

The application is designed to handle repositories containing tens of thousands of commits and hundreds of thousands of file changes.

Large-repository safeguards include:

- Bounded timeline rendering
- Progressive loading
- Contributor search
- Limited collaboration graph rendering
- Deferred loading of large file datasets
- Month/year chart aggregation
- On-demand commit details
- On-demand time-travel data
- Loading and error states

---

## 🏗️ Architecture

```text
                         ┌──────────────────────────┐
                         │     GitHub Repository    │
                         │  Commits • Files • Git   │
                         └────────────┬─────────────┘
                                      │
                                      ▼
                         ┌──────────────────────────┐
                         │          JGit            │
                         │     Git History Engine   │
                         └────────────┬─────────────┘
                                      │
                                      ▼
                  ┌────────────────────────────────────┐
                  │           Spring Boot API           │
                  │                                    │
                  │ Repository │ Commits │ Contributors│
                  │ Timeline   │ Files   │ Time Travel│
                  └──────────────────┬─────────────────┘
                                     │
                                     ▼
                         ┌──────────────────────┐
                         │      PostgreSQL      │
                         │ Repository History   │
                         │ Commits • Files      │
                         │ Contributors         │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     React + Vite     │
                         │ Interactive Dashboard│
                         └──────────────────────┘
```
````

---

## 🛠️ Technology Stack

### Backend

- **Java 21**
- **Spring Boot 3.3.3**
- Spring Web MVC
- Spring Data JPA
- Hibernate ORM
- PostgreSQL
- H2
- JGit 6.10
- Lombok
- Springdoc OpenAPI
- Maven

### Frontend

- **React 19**
- **TypeScript**
- **Vite**
- **Recharts**
- **React Flow**
- **Lucide React**
- ESLint

---

## 📂 Project Structure

```text
github-time-machine/
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
├── frontend/
│   └── src/
│       ├── components/
│       ├── hooks/
│       ├── services/
│       ├── types/
│       ├── App.tsx
│       └── App.css
│
├── pom.xml
└── README.md
```

---

## ⚡ Large Repository Engineering

GitHub Time Machine is designed to preserve and analyze complete repository history without truncating large repositories.

### Backend optimizations

- Streamed Git history processing
- JGit `setNoCheckout(true)`
- Git diff processing outside database transactions
- Short per-commit database transactions
- Valid repository clone reuse
- Idempotent persistence
- PostgreSQL `TEXT` for large commit messages and file paths
- Efficient contributor aggregation
- Open-EntityManager-in-View disabled

### Frontend optimizations

- Bounded timeline rendering
- Progressive loading
- Contributor search
- Limited collaboration graph
- Deferred large file datasets
- Month/year chart aggregation
- On-demand commit details
- On-demand time-travel data
- Loading and error states

---

## 📊 Verified Large Repository

The application has been successfully tested with:

**`spring-projects/spring-boot`**

```text
Commits:          63,514
Contributors:      1,675
File Changes:    440,599
```

Additional verified limits:

```text
Maximum commit message: 7,816 characters
Maximum file path:        271 characters
```

---

## 🔁 Idempotent Analysis

Repeated analysis of the same repository does **not create duplicate records**.

Identity rules:

```text
Repository  → fullName
Commit      → repository + commit hash
File Change → repository + commit + file path
Contributor → repository + email
```

### Verified repeat analysis

```text
                 Before       After
                 ──────       ─────

Commits          63,514       63,514
File Changes    440,599      440,599
Contributors      1,675        1,675
```

✅ No duplicate commits  
✅ No duplicate file changes  
✅ No duplicate contributors

---

## 🧪 Testing

Run the backend tests:

```powershell
cd C:\Users\aksha\github-time-machine
mvn test -q
```

Current verified result:

```text
15 Tests
0 Failures
0 Errors
```

Build the frontend:

```powershell
cd C:\Users\aksha\github-time-machine\frontend
npm run build
```

---

## ▶️ Getting Started

### Prerequisites

- JDK 21
- Maven 3.9+
- Node.js 20+
- npm
- PostgreSQL 15+
- Git

### 1. Create the database

```sql
CREATE DATABASE github_time_machine;
```

Default development configuration:

```text
Host:     localhost
Port:     5432
Database: github_time_machine
Username: postgres
```

> Configure database credentials locally. Do not commit production credentials.

### 2. Start the backend

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

### 3. Start the frontend

Open another terminal:

```powershell
cd C:\Users\aksha\github-time-machine\frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

---

## 🔌 REST API

| Method | Endpoint                                      | Description         |
| ------ | --------------------------------------------- | ------------------- |
| GET    | `/api/v1/repositories`                        | List repositories   |
| GET    | `/api/v1/repositories/{id}`                   | Repository details  |
| POST   | `/api/repositories/analyze`                   | Analyze repository  |
| DELETE | `/api/v1/repositories/{id}`                   | Delete repository   |
| GET    | `/api/repositories/{id}/commits`              | Paginated commits   |
| GET    | `/api/repositories/{id}/commits/{hash}`       | Commit details      |
| GET    | `/api/repositories/{id}/files`                | File changes        |
| GET    | `/api/repositories/{id}/files/{path}/history` | File history        |
| GET    | `/api/repositories/{id}/contributors`         | Contributors        |
| GET    | `/api/repositories/{id}/contributors/graph`   | Collaboration graph |
| GET    | `/api/repositories/{id}/timeline`             | Repository timeline |
| GET    | `/api/repositories/{id}/timeline/{hash}`      | Historical snapshot |

### Analyze Repository

```http
POST /api/repositories/analyze
Content-Type: application/json
```

```json
{
  "repositoryUrl": "https://github.com/spring-projects/spring-boot.git"
}
```

---

## 📖 API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

---

## 🔮 Future Enhancements

- GitHub OAuth and private repository support
- Branch and repository comparison
- Pull request and issue analytics
- Background analysis with real-time progress

---

## 👩‍💻 Author

**Akshaya Somu**

B.Tech — Computer Science & Engineering  
Shri Vishnu Engineering College for Women

---

<div align="center">

### ⏳ GitHub Time Machine

**Don't just see where a repository is today.  
See how it got there.**

</div>

