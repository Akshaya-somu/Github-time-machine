<h1>⏳ GitHub Time Machine</h1>

<p>
  <strong>Explore how a GitHub repository evolved over time — commits, contributors, files, collaboration, and historical snapshots in one interactive dashboard.</strong>
</p>

<p>
  GitHub Time Machine is a full-stack <strong>Git repository intelligence platform</strong> that analyzes the complete Git history of public GitHub repositories and transforms it into an interactive visual experience.
</p>

<p>
  Built with <strong>JGit, Spring Boot, PostgreSQL, React, TypeScript, Recharts, and React Flow</strong>.
</p>

<hr>

<h2>🚀 Overview</h2>

<p>
  GitHub repositories contain years of development history, but understanding that history often requires manually navigating commits, contributors, files, and changes.
</p>

<p>
  GitHub Time Machine brings this information together into a single dashboard to help answer:
</p>

<ul>
  <li>📈 How has a repository evolved over time?</li>
  <li>👨‍💻 Who contributed to the project?</li>
  <li>🤝 Which contributors worked on the same files?</li>
  <li>📁 Which files changed throughout the project's history?</li>
  <li>🔀 What happened in a particular commit?</li>
  <li>⏳ What did the repository look like at a specific point in time?</li>
  <li>📊 How did additions and deletions change over the years?</li>
</ul>

<hr>

<h2>✨ Features</h2>

<h3>📦 Repository Analysis</h3>

<ul>
  <li>Analyze any public GitHub repository</li>
  <li>Clone and inspect complete Git history</li>
  <li>Extract commits, authors, file changes, and statistics</li>
  <li>Preserve the complete repository history</li>
</ul>

<h3>⏱️ Repository Timeline</h3>

<ul>
  <li>Chronological commit timeline</li>
  <li>Commit timestamps and authors</li>
  <li>Commit messages</li>
  <li>Files changed per commit</li>
  <li>Cumulative repository statistics</li>
</ul>

<h3>👨‍💻 Contributor Intelligence</h3>

<ul>
  <li>Contributor summaries</li>
  <li>Commit statistics</li>
  <li>Additions and deletions</li>
  <li>Files modified</li>
  <li>Contributor search</li>
  <li>Contributor collaboration graph</li>
</ul>

<h3>📁 File Change Explorer</h3>

<ul>
  <li>Explore repository file changes</li>
  <li>View additions, deletions, and change types</li>
  <li>Inspect individual file history</li>
</ul>

<h3>🔍 Commit Explorer</h3>

<ul>
  <li>Full commit hash</li>
  <li>Author and email</li>
  <li>Timestamp</li>
  <li>Commit message</li>
  <li>Parent commits</li>
  <li>File-level changes</li>
</ul>

<h3>⏳ Time Travel</h3>

<p>
  Select a historical commit and explore the repository's state at that point in its evolution.
</p>

<p><strong>View:</strong></p>

<ul>
  <li>Commits up to that point</li>
  <li>Contributors up to that point</li>
  <li>Files changed</li>
  <li>Cumulative additions</li>
  <li>Cumulative deletions</li>
</ul>

<h3>📊 Visual Analytics</h3>

<ul>
  <li>Repository evolution timeline</li>
  <li>Additions vs. deletions charts</li>
  <li>Contributor collaboration graph</li>
  <li>Historical activity trends</li>
  <li>Time-based repository statistics</li>
</ul>

<h3>⚡ Large Repository Support</h3>

<p>
  The application is designed to handle repositories containing tens of thousands of commits and hundreds of thousands of file changes.
</p>

<p><strong>Large-repository safeguards include:</strong></p>

<ul>
  <li>Bounded timeline rendering</li>
  <li>Progressive loading</li>
  <li>Contributor search</li>
  <li>Limited collaboration graph rendering</li>
  <li>Deferred loading of large file datasets</li>
  <li>Month/year chart aggregation</li>
  <li>On-demand commit details</li>
  <li>On-demand time-travel data</li>
  <li>Loading and error states</li>
</ul>

<hr>

<h2>🏗️ Architecture</h2>

<pre>
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
</pre>

<hr>

<h2>🛠️ Technology Stack</h2>

<h3>Backend</h3>

<ul>
  <li><strong>Java 21</strong></li>
  <li><strong>Spring Boot 3.3.3</strong></li>
  <li>Spring Web MVC</li>
  <li>Spring Data JPA</li>
  <li>Hibernate ORM</li>
  <li>PostgreSQL</li>
  <li>H2</li>
  <li>JGit 6.10</li>
  <li>Lombok</li>
  <li>Springdoc OpenAPI</li>
  <li>Maven</li>
</ul>

<h3>Frontend</h3>

<ul>
  <li><strong>React 19</strong></li>
  <li><strong>TypeScript</strong></li>
  <li><strong>Vite</strong></li>
  <li><strong>Recharts</strong></li>
  <li><strong>React Flow</strong></li>
  <li><strong>Lucide React</strong></li>
  <li>ESLint</li>
</ul>

<hr>

<h2>📂 Project Structure</h2>

<pre>
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
</pre>

<hr>

<h2>⚡ Large Repository Engineering</h2>

<p>
  GitHub Time Machine is designed to preserve and analyze complete repository history without truncating large repositories.
</p>

<p><strong>Backend optimizations</strong></p>

<ul>
  <li>Streamed Git history processing</li>
  <li>JGit <code>setNoCheckout(true)</code></li>
  <li>Git diff processing outside database transactions</li>
  <li>Short per-commit database transactions</li>
  <li>Valid repository clone reuse</li>
  <li>Idempotent persistence</li>
  <li>PostgreSQL <code>TEXT</code> for large commit messages and file paths</li>
  <li>Efficient contributor aggregation</li>
  <li>Open-EntityManager-in-View disabled</li>
</ul>

<p><strong>Frontend optimizations</strong></p>

<ul>
  <li>Bounded timeline rendering</li>
  <li>Progressive loading</li>
  <li>Contributor search</li>
  <li>Limited collaboration graph</li>
  <li>Deferred large file datasets</li>
  <li>Month/year chart aggregation</li>
  <li>On-demand commit details</li>
  <li>On-demand time-travel data</li>
  <li>Loading and error states</li>
</ul>

<hr>

<h2>📊 Verified Large Repository</h2>

<p>
  The application has been successfully tested with:
</p>

<p>
  <strong>spring-projects/spring-boot</strong>
</p>

<pre>
Commits:          63,514
Contributors:      1,675
File Changes:    440,599
</pre>

<p><strong>Additional verified limits:</strong></p>

<pre>
Maximum commit message: 7,816 characters
Maximum file path:        271 characters
</pre>

<hr>

<h2>🔁 Idempotent Analysis</h2>

<p>
  Repeated analysis of the same repository does <strong>not create duplicate records</strong>.
</p>

<p><strong>Identity rules:</strong></p>

<pre>
Repository  → fullName
Commit      → repository + commit hash
File Change → repository + commit + file path
Contributor → repository + email
</pre>

<p><strong>Verified repeat analysis</strong></p>

<pre>
                  Before       After
                  ──────       ─────

Commits          63,514       63,514
File Changes    440,599      440,599
Contributors      1,675        1,675
</pre>

<ul>
  <li>✅ No duplicate commits</li>
  <li>✅ No duplicate file changes</li>
  <li>✅ No duplicate contributors</li>
</ul>

<hr>

<h2>🧪 Testing</h2>

<p><strong>Run the backend tests:</strong></p>

<pre>
cd C:\Users\aksha\github-time-machine
mvn test -q
</pre>

<p><strong>Current verified result:</strong></p>

<pre>
15 Tests
0 Failures
0 Errors
</pre>

<p><strong>Build the frontend:</strong></p>

<pre>
cd C:\Users\aksha\github-time-machine\frontend
npm run build
</pre>

<hr>

<h2>▶️ Getting Started</h2>

<h3>Prerequisites</h3>

<ul>
  <li>JDK 21</li>
  <li>Maven 3.9+</li>
  <li>Node.js 20+</li>
  <li>npm</li>
  <li>PostgreSQL 15+</li>
  <li>Git</li>
</ul>

<h3>1. Create the database</h3>

<pre>
CREATE DATABASE github_time_machine;
</pre>

<p><strong>Default development configuration:</strong></p>

<pre>
Host:     localhost
Port:     5432
Database: github_time_machine
Username: postgres
</pre>

<p>
  Configure database credentials locally. Do not commit production credentials.
</p>

<h3>2. Start the backend</h3>

<pre>
cd C:\Users\aksha\github-time-machine
mvn spring-boot:run
</pre>

<p><strong>Backend:</strong></p>

<pre>
http://localhost:8080
</pre>

<p><strong>Verify:</strong></p>

<pre>
curl.exe http://localhost:8080/api/v1/repositories
</pre>

<h3>3. Start the frontend</h3>

<p>Open another terminal:</p>

<pre>
cd C:\Users\aksha\github-time-machine\frontend
npm install
npm run dev
</pre>

<p><strong>Open:</strong></p>

<pre>
http://localhost:5173
</pre>

<hr>

<h2>🔌 REST API</h2>

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>GET</td>
      <td><code>/api/v1/repositories</code></td>
      <td>List repositories</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/v1/repositories/{id}</code></td>
      <td>Repository details</td>
    </tr>
    <tr>
      <td>POST</td>
      <td><code>/api/repositories/analyze</code></td>
      <td>Analyze repository</td>
    </tr>
    <tr>
      <td>DELETE</td>
      <td><code>/api/v1/repositories/{id}</code></td>
      <td>Delete repository</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/commits</code></td>
      <td>Paginated commits</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/commits/{hash}</code></td>
      <td>Commit details</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/files</code></td>
      <td>File changes</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/files/{path}/history</code></td>
      <td>File history</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/contributors</code></td>
      <td>Contributors</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/contributors/graph</code></td>
      <td>Collaboration graph</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/timeline</code></td>
      <td>Repository timeline</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/api/repositories/{id}/timeline/{hash}</code></td>
      <td>Historical snapshot</td>
    </tr>
  </tbody>
</table>

<h3>Analyze Repository</h3>

<pre>
POST /api/repositories/analyze
Content-Type: application/json
</pre>

<pre>
{
  "repositoryUrl": "https://github.com/spring-projects/spring-boot.git"
}
</pre>

<hr>

<h2>📖 API Documentation</h2>

<p><strong>Swagger UI:</strong></p>

<pre>
http://localhost:8080/swagger-ui.html
</pre>

<p><strong>OpenAPI:</strong></p>

<pre>
http://localhost:8080/v3/api-docs
</pre>

<hr>

<h2>🔮 Future Enhancements</h2>

<ul>
  <li>GitHub OAuth and private repository support</li>
  <li>Branch and repository comparison</li>
  <li>Pull request and issue analytics</li>
  <li>Background analysis with real-time progress</li>
</ul>

<hr>

<h2>👩‍💻 Author</h2>

<p>
  <strong>Akshaya Somu</strong>
</p>

<p>
  B.Tech — Computer Science &amp; Engineering<br>
  Shri Vishnu Engineering College for Women
</p>

<hr>

<div align="center">

<h2>⏳ GitHub Time Machine</h2>

<p>
  <strong>Don't just see where a repository is today.<br>
  See how it got there.</strong>
</p>

</div>
