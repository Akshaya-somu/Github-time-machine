import type {
  CommitDto,
  ContributorDetailDto,
  ContributorDto,
  ContributorGraphDto,
  FileChangeDto,
  RepositoryAnalysisResponse,
  RepositoryTimelineDto,
  RepositoryTimelineSnapshotDto,
} from "../types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api";

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...((options.headers as Record<string, string>) ?? {}),
    },
  });

  const text = await response.text();
  let payload: unknown = null;

  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = text;
    }
  }

  if (!response.ok) {
    const message =
      typeof payload === "object" && payload !== null && "message" in payload
        ? String((payload as { message?: string }).message)
        : "Request failed";
    throw new Error(message || "Request failed");
  }

  return (payload ?? undefined) as T;
}

export const api = {
  analyzeRepository: (repositoryUrl: string) =>
    request<RepositoryAnalysisResponse>("/repositories/analyze", {
      method: "POST",
      body: JSON.stringify({ repositoryUrl }),
    }),

  getTimeline: (repositoryId: number) =>
    request<RepositoryTimelineDto>(`/repositories/${repositoryId}/timeline`),

  getContributors: (repositoryId: number) =>
    request<ContributorDto[]>(`/repositories/${repositoryId}/contributors`),

  getContributorGraph: (repositoryId: number) =>
    request<ContributorGraphDto>(
      `/repositories/${repositoryId}/contributors/graph`,
    ),

  getFiles: (repositoryId: number) =>
    request<FileChangeDto[]>(`/repositories/${repositoryId}/files`),

  getContributorDetail: (repositoryId: number, contributorId: string) =>
    request<ContributorDetailDto>(
      `/repositories/${repositoryId}/contributors/${encodeURIComponent(contributorId)}`,
    ),

  getCommit: (repositoryId: number, hash: string) =>
    request<CommitDto>(
      `/repositories/${repositoryId}/commits/${encodeURIComponent(hash)}`,
    ),

  getTimelineAtCommit: (repositoryId: number, commitHash: string) =>
    request<RepositoryTimelineSnapshotDto>(
      `/repositories/${repositoryId}/timeline/${encodeURIComponent(commitHash)}`,
    ),
};
