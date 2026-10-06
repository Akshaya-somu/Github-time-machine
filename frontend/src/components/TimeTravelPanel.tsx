import type { RepositoryTimelineSnapshotDto } from "../types";

interface TimeTravelPanelProps {
  snapshot: RepositoryTimelineSnapshotDto | null;
  selectedDate?: string | null;
}

export function TimeTravelPanel({
  snapshot,
  selectedDate,
}: TimeTravelPanelProps) {
  if (!snapshot) {
    return (
      <div className="empty-state">
        Select a commit in the timeline to travel through history.
      </div>
    );
  }

  return (
    <div className="panel time-travel-panel">
      <div className="panel-header">
        <h3>Repository at this point in time</h3>
      </div>
      <div className="time-travel__meta">
        <span className="label">Selected checkpoint</span>
        <strong>{new Date(snapshot.timestamp).toLocaleString()}</strong>
        {selectedDate ? (
          <span className="time-travel__selected">{selectedDate}</span>
        ) : null}
      </div>
      <div className="stat-grid compact-grid">
        <div className="mini-stat">
          <span>Commits up to point</span>
          <strong>{snapshot.commitsUpToPoint}</strong>
        </div>
        <div className="mini-stat">
          <span>Contributors up to point</span>
          <strong>{snapshot.contributorsUpToPoint}</strong>
        </div>
        <div className="mini-stat">
          <span>Files changed</span>
          <strong>{snapshot.filesChangedUpToPoint}</strong>
        </div>
        <div className="mini-stat">
          <span>Cumulative additions</span>
          <strong>{snapshot.cumulativeAdditions}</strong>
        </div>
        <div className="mini-stat">
          <span>Cumulative deletions</span>
          <strong>{snapshot.cumulativeDeletions}</strong>
        </div>
      </div>
    </div>
  );
}
