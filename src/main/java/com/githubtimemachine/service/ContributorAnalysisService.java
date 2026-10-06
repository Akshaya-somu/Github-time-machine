package com.githubtimemachine.service;

import com.githubtimemachine.dto.CommitDto;
import com.githubtimemachine.dto.ContributorDetailDto;
import com.githubtimemachine.dto.ContributorDto;
import com.githubtimemachine.dto.ContributorEdgeDto;
import com.githubtimemachine.dto.ContributorGraphDto;
import com.githubtimemachine.dto.ContributorNodeDto;
import com.githubtimemachine.entity.CommitEntity;
import com.githubtimemachine.entity.ContributorEntity;
import com.githubtimemachine.entity.FileChangeEntity;
import com.githubtimemachine.entity.RepositoryEntity;
import com.githubtimemachine.exception.ResourceNotFoundException;
import com.githubtimemachine.repository.CommitRepository;
import com.githubtimemachine.repository.ContributorRepository;
import com.githubtimemachine.repository.FileChangeRepository;
import com.githubtimemachine.repository.RepositoryEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContributorAnalysisService {

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final ContributorRepository contributorRepository;
    private final CommitRepository commitRepository;
    private final FileChangeRepository fileChangeRepository;

    @Transactional(readOnly = true)
    public List<ContributorDto> getContributors(Long repositoryId) {
        getRepositoryOrThrow(repositoryId);
        Map<String, ContributionStats> statsByEmail = buildContributionStats(repositoryId);
        return contributorRepository.findByRepositoryIdOrderByCommitCountDesc(repositoryId).stream()
                .map(contributor -> toContributorDto(contributor, statsByEmail.getOrDefault(contributor.getEmail(), ContributionStats.empty())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContributorGraphDto getContributorGraph(Long repositoryId) {
        getRepositoryOrThrow(repositoryId);
        List<ContributorEntity> contributors = contributorRepository.findByRepositoryIdOrderByCommitCountDesc(repositoryId);
        if (contributors.isEmpty()) {
            return ContributorGraphDto.builder().nodes(Collections.emptyList()).edges(Collections.emptyList()).build();
        }

        Map<String, ContributionStats> statsByEmail = buildContributionStats(repositoryId);
        List<ContributorNodeDto> nodes = contributors.stream()
                .map(contributor -> toContributorNode(contributor, statsByEmail
                        .getOrDefault(contributor.getEmail(), ContributionStats.empty())))
                .collect(Collectors.toList());

        Map<String, ContributorNodeDto> nodeById = nodes.stream()
                .collect(Collectors.toMap(ContributorNodeDto::getId, contributor -> contributor, (left, right) -> left));

        Set<String> seenEdges = new HashSet<>();
        List<ContributorEdgeDto> edges = new ArrayList<>();

        List<FileChangeEntity> fileChanges = fileChangeRepository.findByRepositoryId(repositoryId);
        Map<String, Set<String>> filesByContributor = new HashMap<>();
        for (FileChangeEntity fileChange : fileChanges) {
            if (fileChange.getCommit() == null || fileChange.getCommit().getAuthorEmail() == null) {
                continue;
            }
            String contributorId = fileChange.getCommit().getAuthorEmail();
            filesByContributor.computeIfAbsent(contributorId, ignored -> new HashSet<>()).add(fileChange.getFilePath());
        }

        List<String> contributorIds = new ArrayList<>(nodeById.keySet());
        for (int i = 0; i < contributorIds.size(); i++) {
            for (int j = i + 1; j < contributorIds.size(); j++) {
                String contributorA = contributorIds.get(i);
                String contributorB = contributorIds.get(j);
                Set<String> sharedFiles = new HashSet<>(filesByContributor.getOrDefault(contributorA, Collections.emptySet()));
                sharedFiles.retainAll(filesByContributor.getOrDefault(contributorB, Collections.emptySet()));
                if (sharedFiles.isEmpty()) {
                    continue;
                }

                String edgeKey = contributorA.compareTo(contributorB) < 0
                        ? contributorA + "::" + contributorB
                        : contributorB + "::" + contributorA;
                if (!seenEdges.add(edgeKey)) {
                    continue;
                }

                edges.add(ContributorEdgeDto.builder()
                        .source(contributorA)
                        .target(contributorB)
                        .sharedFiles(sharedFiles.size())
                        .weight(sharedFiles.size())
                        .build());
            }
        }

        return ContributorGraphDto.builder().nodes(nodes).edges(edges).build();
    }

    @Transactional(readOnly = true)
    public ContributorDetailDto getContributorDetail(Long repositoryId, String contributorId) {
        getRepositoryOrThrow(repositoryId);
        ContributorEntity contributor = contributorRepository.findByRepositoryIdAndEmail(repositoryId, contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Contributor not found for repository id: " + repositoryId + " and email: " + contributorId));

        List<CommitEntity> commits = commitRepository.findByRepositoryIdOrderByCommitTimestampDesc(repositoryId).stream()
                .filter(commit -> contributorId.equals(commit.getAuthorEmail()))
                .collect(Collectors.toList());

        List<FileChangeEntity> fileChanges = fileChangeRepository.findByRepositoryId(repositoryId).stream()
                .filter(fileChange -> fileChange.getCommit() != null && contributorId.equals(fileChange.getCommit().getAuthorEmail()))
                .collect(Collectors.toList());

        long totalAdditions = fileChanges.stream().mapToLong(FileChangeEntity::getLinesAdded).sum();
        long totalDeletions = fileChanges.stream().mapToLong(FileChangeEntity::getLinesDeleted).sum();
        long filesModified = fileChanges.stream().map(FileChangeEntity::getFilePath).distinct().count();

        Map<String, Long> fileCounts = new LinkedHashMap<>();
        for (FileChangeEntity fileChange : fileChanges) {
            fileCounts.merge(fileChange.getFilePath(), 1L, Long::sum);
        }

        List<String> mostFrequentlyModifiedFiles = fileCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return ContributorDetailDto.builder()
                .id(contributor.getEmail())
                .name(contributor.getName())
                .email(contributor.getEmail())
                .commitCount(contributor.getCommitCount())
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .filesModified(filesModified)
                .commits(commits.stream().map(this::toCommitDto).collect(Collectors.toList()))
                .mostFrequentlyModifiedFiles(mostFrequentlyModifiedFiles)
                .build();
    }

    private RepositoryEntity getRepositoryOrThrow(Long repositoryId) {
        return repositoryEntityRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repositoryId));
    }

        private ContributorDto toContributorDto(ContributorEntity contributor, ContributionStats stats) {
        return ContributorDto.builder()
                .id(contributor.getEmail())
                .name(contributor.getName())
                .email(contributor.getEmail())
                .commitCount(contributor.getCommitCount())
                .totalAdditions(stats.totalAdditions())
                .totalDeletions(stats.totalDeletions())
                .filesModified(stats.filesModified())
                .build();
    }

    private ContributorNodeDto toContributorNode(ContributorEntity contributor, ContributionStats stats) {
        return ContributorNodeDto.builder()
                .id(contributor.getEmail())
                .name(contributor.getName())
                .email(contributor.getEmail())
                .commits(contributor.getCommitCount())
                                .totalAdditions(stats.totalAdditions())
                                .totalDeletions(stats.totalDeletions())
                                .filesModified(stats.filesModified())
                .build();
    }

        private Map<String, ContributionStats> buildContributionStats(Long repositoryId) {
                Map<String, ContributionStatsAccumulator> accumulators = new HashMap<>();
                for (FileChangeEntity fileChange : fileChangeRepository.findByRepositoryId(repositoryId)) {
                        if (fileChange.getCommit() == null || fileChange.getCommit().getAuthorEmail() == null) {
                                continue;
                        }
                        String email = fileChange.getCommit().getAuthorEmail();
                        ContributionStatsAccumulator accumulator = accumulators.computeIfAbsent(email, ignored -> new ContributionStatsAccumulator());
                        accumulator.totalAdditions += fileChange.getLinesAdded();
                        accumulator.totalDeletions += fileChange.getLinesDeleted();
                        accumulator.files.add(fileChange.getFilePath());
                }
                return accumulators.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toStats()));
        }

        private record ContributionStats(long totalAdditions, long totalDeletions, long filesModified) {
                private static ContributionStats empty() {
                        return new ContributionStats(0, 0, 0);
                }
        }

        private static class ContributionStatsAccumulator {
                private long totalAdditions;
                private long totalDeletions;
                private final Set<String> files = new HashSet<>();

                private ContributionStats toStats() {
                        return new ContributionStats(totalAdditions, totalDeletions, files.size());
                }
        }

    private CommitDto toCommitDto(CommitEntity commit) {
        return CommitDto.builder()
                .hash(commit.getHash())
                .authorName(commit.getAuthorName())
                .authorEmail(commit.getAuthorEmail())
                .commitTimestamp(commit.getCommitTimestamp())
                .message(commit.getMessage())
                .parentHashes(commit.getParentHashes() == null || commit.getParentHashes().isBlank()
                        ? Collections.emptyList()
                        : java.util.Arrays.stream(commit.getParentHashes().split(","))
                                .map(String::trim)
                                .filter(value -> !value.isBlank())
                                .collect(Collectors.toList()))
                .build();
    }
}
