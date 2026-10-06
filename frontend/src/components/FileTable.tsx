import { useMemo, useState } from "react";

import type { FileChangeDto } from "../types";

interface FileTableProps {
  files: FileChangeDto[];
  totalCount?: number;
  isLargeRepository?: boolean;
  search: string;
  onSearchChange: (value: string) => void;
}

export function FileTable({
  files,
  totalCount = files.length,
  isLargeRepository = false,
  search,
  onSearchChange,
}: FileTableProps) {
  const [visibleCount, setVisibleCount] = useState(100);
  const filteredFiles = useMemo(
    () =>
      files.filter(
        (file) =>
          file.filePath.toLowerCase().includes(search.toLowerCase()) ||
          file.commitHash.toLowerCase().includes(search.toLowerCase()),
      ),
    [files, search],
  );
  const visibleFiles = filteredFiles.slice(0, visibleCount);

  return (
    <div className="panel">
      <div className="panel-header panel-header--space">
        <h3>File Change Explorer</h3>
        <span className="panel-kicker">
          {isLargeRepository && files.length === 0
            ? `File rows deferred for performance (${totalCount.toLocaleString()} total)`
            : `Showing ${Math.min(visibleFiles.length, totalCount).toLocaleString()} of ${totalCount.toLocaleString()} file changes`}
        </span>
        <input
          className="search-input"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Search files or commit hash"
        />
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>File path</th>
              <th>Type</th>
              <th>Added</th>
              <th>Deleted</th>
              <th>Commit</th>
            </tr>
          </thead>
          <tbody>
            {filteredFiles.length === 0 ? (
              <tr>
                <td colSpan={5} className="empty-cell">
                  No matching file changes
                </td>
              </tr>
            ) : (
              visibleFiles.map((file) => (
                <tr key={`${file.commitHash}-${file.filePath}`}>
                  <td>{file.filePath}</td>
                  <td>{file.changeType}</td>
                  <td>{file.linesAdded}</td>
                  <td>{file.linesDeleted}</td>
                  <td className="mono">{file.commitHash.slice(0, 8)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {visibleFiles.length < filteredFiles.length ? (
        <button
          type="button"
          className="secondary-button load-more-button"
          onClick={() => setVisibleCount((count) => count + 100)}
        >
          Load more file changes
        </button>
      ) : null}
    </div>
  );
}
