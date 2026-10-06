import { useCallback, useState } from "react";

import { api } from "../services/api";
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

export function useRepositoryData() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const analyzeRepository = useCallback(async (repositoryUrl: string) => {
    setLoading(true);
    setError(null);

    try {
      const response = await api.analyzeRepository(repositoryUrl);
      return response;
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to analyze repository",
      );
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const loadResource = useCallback(async <T>(loader: () => Promise<T>) => {
    setLoading(true);
    setError(null);

    try {
      return await loader();
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Failed to load resource";
      setError(message);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loading,
    error,
    setError,
    analyzeRepository,
    loadResource,
  };
}

export async function getRepositoryOverview(
  repositoryId: number,
): Promise<RepositoryAnalysisResponse | null> {
  try {
    const [timeline, contributors, , files] = await Promise.all([
      api.getTimeline(repositoryId),
      api.getContributors(repositoryId),
      api.getContributorGraph(repositoryId),
      api.getFiles(repositoryId),
    ]);

    const overview: RepositoryAnalysisResponse = {
      repositoryId,
      fullName: "",
      remoteUrl: "",
      commitCount: timeline.statistics.totalCommits,
      contributorCount: contributors.length,
      fileChangeCount: files.length,
    };

    return overview;
  } catch {
    return null;
  }
}

export async function fetchRepositoryTimeline(
  repositoryId: number,
): Promise<RepositoryTimelineDto | null> {
  return api.getTimeline(repositoryId).catch(() => null);
}

export async function fetchContributors(
  repositoryId: number,
): Promise<ContributorDto[] | null> {
  return api.getContributors(repositoryId).catch(() => null);
}

export async function fetchContributorGraph(
  repositoryId: number,
): Promise<ContributorGraphDto | null> {
  return api.getContributorGraph(repositoryId).catch(() => null);
}

export async function fetchFiles(
  repositoryId: number,
): Promise<FileChangeDto[] | null> {
  return api.getFiles(repositoryId).catch(() => null);
}

export async function fetchContributorDetail(
  repositoryId: number,
  contributorId: string,
): Promise<ContributorDetailDto | null> {
  return api
    .getContributorDetail(repositoryId, contributorId)
    .catch(() => null);
}

export async function fetchCommit(
  repositoryId: number,
  hash: string,
): Promise<CommitDto | null> {
  return api.getCommit(repositoryId, hash).catch(() => null);
}

export async function fetchTimelineAtCommit(
  repositoryId: number,
  commitHash: string,
): Promise<RepositoryTimelineSnapshotDto | null> {
  return api.getTimelineAtCommit(repositoryId, commitHash).catch(() => null);
}
