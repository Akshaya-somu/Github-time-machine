import { useMemo, useState } from "react";

import type { RepositoryTimelineEntryDto } from "../types";

interface TimelineProps {
  entries: RepositoryTimelineEntryDto[];
  totalCount?: number;
  selectedCommit?: string | null;
  onSelectCommit?: (commitHash: string) => void;
}

export function Timeline({
  entries,
  totalCount = entries.length,
  selectedCommit,
  onSelectCommit,
}: TimelineProps) {
  const [visibleCount, setVisibleCount] = useState(100);
  const visibleEntries = useMemo(
    () => entries.slice(0, visibleCount),
    [entries, visibleCount],
  );

  return (
    <div className="panel timeline-panel">
      <div className="panel-header">
        <h3>Repository Evolution Timeline</h3>
        <span className="panel-kicker">
          Showing {Math.min(visibleEntries.length, totalCount).toLocaleString()}{" "}
          of {totalCount.toLocaleString()} commits
        </span>
      </div>
      <div className="timeline-list">
        {entries.length === 0 ? (
          <div className="empty-state">No timeline data found.</div>
        ) : (
          visibleEntries.map((entry) => (
            <button
              key={entry.commitHash}
              type="button"
              className={`timeline-item ${selectedCommit === entry.commitHash ? "is-selected" : ""}`}
              onClick={() => onSelectCommit?.(entry.commitHash)}
            >
              <div className="timeline-node" />
              <div className="timeline-body">
                <div className="timeline-row">
                  <strong>{entry.authorName}</strong>
                  <span>{new Date(entry.timestamp).toLocaleString()}</span>
                </div>
                <div className="timeline-message">{entry.message}</div>
                <div className="timeline-meta">
                  <span>{entry.commitHash.slice(0, 8)}</span>
                  <span>{entry.filesChanged} files</span>
                  <span>+{entry.additions}</span>
                  <span>-{entry.deletions}</span>
                </div>
              </div>
            </button>
          ))
        )}
      </div>
      {visibleEntries.length < entries.length ? (
        <button
          type="button"
          className="secondary-button load-more-button"
          onClick={() => setVisibleCount((count) => count + 100)}
        >
          Load more commits
        </button>
      ) : null}
    </div>
  );
}
