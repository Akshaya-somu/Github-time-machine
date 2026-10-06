import type { CommitDto } from "../types";

interface CommitDetailsProps {
  commit: CommitDto | null;
}

export function CommitDetails({ commit }: CommitDetailsProps) {
  if (!commit) {
    return (
      <div className="empty-state">Select a commit to inspect its details.</div>
    );
  }

  return (
    <div className="panel detail-panel">
      <div className="panel-header">
        <h3>Commit Details</h3>
      </div>
      <div className="commit-detail__grid">
        <div>
          <span className="label">Hash</span>
          <strong>{commit.hash}</strong>
        </div>
        <div>
          <span className="label">Author</span>
          <strong>{commit.authorName}</strong>
        </div>
        <div>
          <span className="label">Email</span>
          <strong>{commit.authorEmail}</strong>
        </div>
        <div>
          <span className="label">Timestamp</span>
          <strong>{new Date(commit.commitTimestamp).toLocaleString()}</strong>
        </div>
      </div>
      <div className="commit-detail__message">
        <span className="label">Message</span>
        <p>{commit.message}</p>
      </div>
      {commit.parentHashes.length > 0 ? (
        <div className="commit-detail__parents">
          <span className="label">Parent commits</span>
          <ul>
            {commit.parentHashes.map((parent) => (
              <li key={parent}>{parent}</li>
            ))}
          </ul>
        </div>
      ) : null}
    </div>
  );
}
