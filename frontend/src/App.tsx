import { useEffect, useMemo, useState } from "react";
import {
  Activity,
  GitBranch,
  GitCommitVertical,
  Layers3,
  Search,
  Sparkles,
  Users,
} from "lucide-react";

import { TrendChart } from "./components/Charts";
import { CommitDetails } from "./components/CommitDetails";
import { ContributorGraph } from "./components/ContributorGraph";
import { FileTable } from "./components/FileTable";
import { StatCard } from "./components/StatCard";
import { TimeTravelPanel } from "./components/TimeTravelPanel";
import { Timeline } from "./components/Timeline";
import { api } from "./services/api";
import "./App.css";
import type {
  CommitDto,
  ContributorDetailDto,
  ContributorDto,
  ContributorGraphDto,
  FileChangeDto,
  RepositoryAnalysisResponse,
  RepositoryTimelineDto,
  RepositoryTimelineSnapshotDto,
} from "./types";

const NAV_ITEMS = [
  { id: "overview", label: "Overview", icon: Layers3 },
  { id: "timeline", label: "Timeline", icon: GitCommitVertical },
  { id: "contributors", label: "Contributors", icon: Users },
  { id: "files", label: "Files", icon: Search },
  { id: "time-travel", label: "Time Travel", icon: Activity },
];

function App() {
  const [repoUrl, setRepoUrl] = useState(
    "https://github.com/spring-projects/spring-boot.git",
  );
  const [repositoryId, setRepositoryId] = useState<number | null>(null);
  const [repository, setRepository] =
    useState<RepositoryAnalysisResponse | null>(null);
  const [timeline, setTimeline] = useState<RepositoryTimelineDto | null>(null);
  const [contributors, setContributors] = useState<ContributorDto[]>([]);
  const [graph, setGraph] = useState<ContributorGraphDto | null>(null);
  const [files, setFiles] = useState<FileChangeDto[]>([]);
  const [selectedCommit, setSelectedCommit] = useState<CommitDto | null>(null);
  const [selectedContributor, setSelectedContributor] =
    useState<ContributorDetailDto | null>(null);
  const [snapshot, setSnapshot] =
    useState<RepositoryTimelineSnapshotDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [activeSection, setActiveSection] = useState("overview");

  const isLargeRepository = Boolean(
    repository &&
    (repository.commitCount > 10_000 || repository.fileChangeCount > 10_000),
  );

  const handleNavClick = (sectionId: string) => {
    setActiveSection(sectionId);
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  useEffect(() => {
    if (!repositoryId) return;

    const loadRepositoryData = async () => {
      setLoading(true);
      setError(null);
      try {
        const [timelineData, contributorsData] = await Promise.all([
          api.getTimeline(repositoryId),
          api.getContributors(repositoryId),
        ]);

        const [graphData, filesData] = isLargeRepository
          ? [null, []]
          : await Promise.all([
              api.getContributorGraph(repositoryId),
              api.getFiles(repositoryId),
            ]);

        setTimeline(timelineData);
        setContributors(contributorsData);
        setGraph(graphData);
        setFiles(filesData);

        if (timelineData?.entries.length) {
          const firstCommit = timelineData.entries[0];
          const [commitData, snapshotData] = await Promise.all([
            api.getCommit(repositoryId, firstCommit.commitHash),
            api.getTimelineAtCommit(repositoryId, firstCommit.commitHash),
          ]);
          setSelectedCommit(commitData);
          setSnapshot(snapshotData);
        }
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to load repository data",
        );
      } finally {
        setLoading(false);
      }
    };

    void loadRepositoryData();
  }, [isLargeRepository, repositoryId]);

  const stats = useMemo(() => {
    if (!timeline?.statistics) {
      return [] as Array<{ label: string; value: string; hint?: string }>;
    }

    return [
      {
        label: "Total commits",
        value: String(timeline.statistics.totalCommits),
        hint: "Across current history",
      },
      {
        label: "Total contributors",
        value: String(timeline.statistics.totalContributors),
        hint: "Unique authors",
      },
      {
        label: "Total file changes",
        value: String(timeline.statistics.totalFilesChanged),
        hint: "Across all tracked files",
      },
      {
        label: "Additions",
        value: String(timeline.statistics.totalAdditions),
        hint: "Lines added",
      },
      {
        label: "Deletions",
        value: String(timeline.statistics.totalDeletions),
        hint: "Lines removed",
      },
      {
        label: "First commit",
        value: formatDate(timeline.statistics.firstCommitTimestamp),
        hint: "Earliest tracked event",
      },
      {
        label: "Latest commit",
        value: formatDate(timeline.statistics.latestCommitTimestamp),
        hint: "Most recent snapshot",
      },
    ];
  }, [timeline]);

  const handleAnalyze = async () => {
    if (!repoUrl.trim()) {
      setError("Please enter a GitHub repository URL.");
      return;
    }

    setLoading(true);
    setError(null);
    setSelectedCommit(null);
    setSelectedContributor(null);
    setSnapshot(null);

    try {
      const result = await api.analyzeRepository(repoUrl.trim());
      setRepository(result);
      setRepositoryId(result.repositoryId);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to analyze repository",
      );
      setRepository(null);
      setRepositoryId(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectCommit = async (commitHash: string) => {
    if (!repositoryId) return;

    try {
      const [commitData, snapshotData] = await Promise.all([
        api.getCommit(repositoryId, commitHash),
        api.getTimelineAtCommit(repositoryId, commitHash),
      ]);

      setSelectedCommit(commitData);
      setSnapshot(snapshotData);
      setSelectedContributor(null);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to fetch commit details",
      );
    }
  };

  const handleSelectContributor = async (email: string) => {
    if (!repositoryId) return;

    try {
      const detail = await api.getContributorDetail(repositoryId, email);
      setSelectedContributor(detail);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to fetch contributor details",
      );
    }
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">
            <GitBranch size={20} />
          </div>
          <div>
            <div className="brand-name">GitHub Time Machine</div>
            <div className="brand-subtitle">Repository intelligence</div>
          </div>
        </div>

        <nav className="nav">
          {NAV_ITEMS.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              type="button"
              className={`nav-item ${activeSection === id ? "is-active" : ""}`}
              onClick={() => handleNavClick(id)}
            >
              <Icon size={16} />
              {label}
            </button>
          ))}
        </nav>
      </aside>

      <main className="content">
        <section className="panel search-panel">
          <div className="panel-header panel-header--space">
            <div>
              <div className="eyebrow">Repository Input</div>
              <h2>Analyze a GitHub repository</h2>
            </div>
            <button
              type="button"
              className="primary-button"
              onClick={handleAnalyze}
              disabled={loading}
            >
              {loading ? "Analyzing…" : "Analyze Repository"}
            </button>
          </div>
          <div className="repo-input-row">
            <input
              value={repoUrl}
              onChange={(event) => setRepoUrl(event.target.value)}
              placeholder="https://github.com/owner/repository.git"
            />
          </div>
          {error ? <div className="error-banner">{error}</div> : null}
        </section>

        {loading ? (
          <section className="empty-state-shell loading-state">
            <Sparkles size={18} />
            <div>
              <strong>Analyzing repository...</strong>
              {repository?.fileChangeCount &&
              repository.fileChangeCount > 10_000 ? (
                <span>
                  Large repository detected — preparing optimized dashboard...
                </span>
              ) : null}
            </div>
          </section>
        ) : repository && timeline ? (
          <>
            <section id="overview" className="overview-header">
              <div>
                <div className="eyebrow">Repository Overview</div>
                <h1>{repository.fullName}</h1>
              </div>
              <div className="repository-link">{repository.remoteUrl}</div>
            </section>

            <section className="stat-grid">
              {stats.map((stat) => (
                <StatCard
                  key={stat.label}
                  label={stat.label}
                  value={stat.value}
                  hint={stat.hint}
                />
              ))}
            </section>

            <section id="timeline" className="two-column">
              <Timeline
                entries={timeline.entries}
                totalCount={timeline.statistics.totalCommits}
                selectedCommit={selectedCommit?.hash ?? null}
                onSelectCommit={handleSelectCommit}
              />
              <CommitDetails commit={selectedCommit} />
            </section>

            <section className="chart-grid">
              <TrendChart entries={timeline.entries} />
            </section>

            <section id="contributors" className="two-column">
              <ContributorGraph
                data={graph ?? { nodes: [], edges: [] }}
                contributors={contributors}
                isLargeRepository={isLargeRepository}
                onSelectContributor={handleSelectContributor}
              />
              {selectedContributor ? (
                <div className="panel contributor-panel">
                  <div className="panel-header">
                    <h3>Contributor Details</h3>
                  </div>
                  <div className="detail-list">
                    <div>
                      <span className="label">Name</span>
                      <strong>{selectedContributor.name}</strong>
                    </div>
                    <div>
                      <span className="label">Email</span>
                      <strong>{selectedContributor.email}</strong>
                    </div>
                    <div>
                      <span className="label">Commit count</span>
                      <strong>{selectedContributor.commitCount}</strong>
                    </div>
                    <div>
                      <span className="label">Total additions</span>
                      <strong>{selectedContributor.totalAdditions}</strong>
                    </div>
                    <div>
                      <span className="label">Total deletions</span>
                      <strong>{selectedContributor.totalDeletions}</strong>
                    </div>
                    <div>
                      <span className="label">Files modified</span>
                      <strong>{selectedContributor.filesModified}</strong>
                    </div>
                    {selectedContributor.mostFrequentlyModifiedFiles.length >
                    0 ? (
                      <div className="wide-field">
                        <span className="label">Most frequent files</span>
                        <ul>
                          {selectedContributor.mostFrequentlyModifiedFiles.map(
                            (file) => (
                              <li key={file}>{file}</li>
                            ),
                          )}
                        </ul>
                      </div>
                    ) : null}
                  </div>
                </div>
              ) : (
                <div className="panel empty-state">
                  Select a contributor to load details.
                </div>
              )}
            </section>

            <section id="files" className="one-column">
              <FileTable
                files={files}
                totalCount={repository.fileChangeCount}
                isLargeRepository={isLargeRepository}
                search={search}
                onSearchChange={setSearch}
              />
            </section>

            <section id="time-travel" className="two-column">
              <TimeTravelPanel
                snapshot={snapshot}
                selectedDate={
                  selectedCommit
                    ? new Date(selectedCommit.commitTimestamp).toLocaleString()
                    : null
                }
              />
              <div className="panel panel--soft">
                <div className="panel-header">
                  <h3>Repository Summary</h3>
                </div>
                <div className="detail-list compact-list">
                  <div>
                    <span className="label">Repository</span>
                    <strong>{repository.fullName}</strong>
                  </div>
                  <div>
                    <span className="label">URL</span>
                    <strong>{repository.remoteUrl}</strong>
                  </div>
                  <div>
                    <span className="label">Commits</span>
                    <strong>{repository.commitCount}</strong>
                  </div>
                  <div>
                    <span className="label">Contributors</span>
                    <strong>{repository.contributorCount}</strong>
                  </div>
                  <div>
                    <span className="label">File changes</span>
                    <strong>{repository.fileChangeCount}</strong>
                  </div>
                </div>
              </div>
            </section>
          </>
        ) : (
          <section className="empty-state-shell">
            <Sparkles size={18} />
            Enter a GitHub repository URL and analyze it to populate the
            dashboard.
          </section>
        )}
      </main>
    </div>
  );
}

function formatDate(value: string | null | undefined) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export default App;
