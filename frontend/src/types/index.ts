export interface RepositoryAnalysisResponse {
  repositoryId: number;
  fullName: string;
  remoteUrl: string;
  commitCount: number;
  contributorCount: number;
  fileChangeCount: number;
}

export interface CommitDto {
  hash: string;
  authorName: string;
  authorEmail: string;
  commitTimestamp: string;
  message: string;
  parentHashes: string[];
}

export interface FileChangeDto {
  filePath: string;
  changeType: string;
  linesAdded: number;
  linesDeleted: number;
  commitHash: string;
}

export interface ContributorDto {
  id: string;
  name: string;
  email: string;
  commitCount: number;
  totalAdditions: number;
  totalDeletions: number;
  filesModified: number;
}

export interface ContributorDetailDto {
  id: string;
  name: string;
  email: string;
  commitCount: number;
  totalAdditions: number;
  totalDeletions: number;
  filesModified: number;
  commits: CommitDto[];
  mostFrequentlyModifiedFiles: string[];
}

export interface ContributorNodeDto {
  id: string;
  name: string;
  email: string;
  commits: number;
  totalAdditions: number;
  totalDeletions: number;
  filesModified: number;
}

export interface ContributorEdgeDto {
  source: string;
  target: string;
  sharedFiles: number;
  weight: number;
}

export interface ContributorGraphDto {
  nodes: ContributorNodeDto[];
  edges: ContributorEdgeDto[];
}

export interface RepositoryTimelineEntryDto {
  commitHash: string;
  timestamp: string;
  authorName: string;
  message: string;
  filesChanged: number;
  additions: number;
  deletions: number;
}

export interface RepositoryTimelineStatsDto {
  totalCommits: number;
  totalContributors: number;
  totalFilesChanged: number;
  totalAdditions: number;
  totalDeletions: number;
  firstCommitTimestamp: string | null;
  latestCommitTimestamp: string | null;
}

export interface RepositoryTimelineDto {
  entries: RepositoryTimelineEntryDto[];
  statistics: RepositoryTimelineStatsDto;
}

export interface RepositoryTimelineSnapshotDto {
  commitHash: string;
  timestamp: string;
  commitsUpToPoint: number;
  contributorsUpToPoint: number;
  filesChangedUpToPoint: number;
  cumulativeAdditions: number;
  cumulativeDeletions: number;
}
