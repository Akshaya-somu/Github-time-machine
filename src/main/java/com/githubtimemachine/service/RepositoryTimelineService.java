package com.githubtimemachine.service;

import com.githubtimemachine.dto.RepositoryTimelineDto;
import com.githubtimemachine.dto.RepositoryTimelineEntryDto;
import com.githubtimemachine.dto.RepositoryTimelineSnapshotDto;
import com.githubtimemachine.dto.RepositoryTimelineStatsDto;
import com.githubtimemachine.entity.CommitEntity;
import com.githubtimemachine.entity.FileChangeEntity;
import com.githubtimemachine.entity.RepositoryEntity;
import com.githubtimemachine.exception.ResourceNotFoundException;
import com.githubtimemachine.repository.CommitRepository;
import com.githubtimemachine.repository.FileChangeRepository;
import com.githubtimemachine.repository.RepositoryEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepositoryTimelineService {

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final CommitRepository commitRepository;
    private final FileChangeRepository fileChangeRepository;

    @Transactional(readOnly = true)
    public RepositoryTimelineDto getTimeline(Long repositoryId) {
        getRepositoryOrThrow(repositoryId);
        List<CommitEntity> commits = new ArrayList<>(commitRepository.findByRepositoryIdOrderByCommitTimestampDesc(repositoryId));
        commits.sort(Comparator.comparing(CommitEntity::getCommitTimestamp)
                .thenComparingInt(CommitEntity::getParentCount)
                .thenComparing(CommitEntity::getId));

        List<FileChangeEntity> fileChanges = fileChangeRepository.findByRepositoryId(repositoryId);
        Map<String, Long> filesChangedByCommit = fileChanges.stream()
                .collect(Collectors.groupingBy(fileChange -> fileChange.getCommit().getHash(), Collectors.mapping(FileChangeEntity::getFilePath, Collectors.toSet())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (long) entry.getValue().size()));

        Map<String, Long> additionsByCommit = fileChanges.stream()
                .collect(Collectors.groupingBy(fileChange -> fileChange.getCommit().getHash(), Collectors.summingLong(FileChangeEntity::getLinesAdded)));

        Map<String, Long> deletionsByCommit = fileChanges.stream()
                .collect(Collectors.groupingBy(fileChange -> fileChange.getCommit().getHash(), Collectors.summingLong(FileChangeEntity::getLinesDeleted)));

        List<RepositoryTimelineEntryDto> entries = new ArrayList<>();
        long totalFilesChanged = 0L;
        long totalAdditions = 0L;
        long totalDeletions = 0L;
        Set<String> contributors = new HashSet<>();

        for (CommitEntity commit : commits) {
            long filesChanged = filesChangedByCommit.getOrDefault(commit.getHash(), 0L);
            long additions = additionsByCommit.getOrDefault(commit.getHash(), 0L);
            long deletions = deletionsByCommit.getOrDefault(commit.getHash(), 0L);

            totalFilesChanged += filesChanged;
            totalAdditions += additions;
            totalDeletions += deletions;
            contributors.add(commit.getAuthorEmail());

            entries.add(RepositoryTimelineEntryDto.builder()
                    .commitHash(commit.getHash())
                    .timestamp(commit.getCommitTimestamp())
                    .authorName(commit.getAuthorName())
                    .message(commit.getMessage())
                    .filesChanged(filesChanged)
                    .additions(additions)
                    .deletions(deletions)
                    .build());
        }

        RepositoryTimelineStatsDto stats = RepositoryTimelineStatsDto.builder()
                .totalCommits((long) commits.size())
                .totalContributors((long) contributors.size())
                .totalFilesChanged(totalFilesChanged)
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .firstCommitTimestamp(commits.isEmpty() ? null : commits.getFirst().getCommitTimestamp())
                .latestCommitTimestamp(commits.isEmpty() ? null : commits.get(commits.size() - 1).getCommitTimestamp())
                .build();

        return RepositoryTimelineDto.builder().entries(entries).statistics(stats).build();
    }

    @Transactional(readOnly = true)
    public RepositoryTimelineSnapshotDto getTimelineAtCommit(Long repositoryId, String commitHash) {
        getRepositoryOrThrow(repositoryId);
        CommitEntity commit = commitRepository.findByRepositoryIdAndHash(repositoryId, commitHash)
                .orElseThrow(() -> new ResourceNotFoundException("Commit not found for repository id: " + repositoryId + " and hash: " + commitHash));

        List<CommitEntity> commits = new ArrayList<>(commitRepository.findByRepositoryIdOrderByCommitTimestampDesc(repositoryId));
        commits.sort(Comparator.comparing(CommitEntity::getCommitTimestamp)
                .thenComparingInt(CommitEntity::getParentCount)
                .thenComparing(CommitEntity::getId));

        int selectedIndex = commits.indexOf(commit);
        if (selectedIndex < 0) {
            throw new ResourceNotFoundException("Commit not found for repository id: " + repositoryId + " and hash: " + commitHash);
        }

        List<FileChangeEntity> fileChanges = fileChangeRepository.findByRepositoryId(repositoryId);
        Map<String, Long> filesChangedByCommit = fileChanges.stream()
                .collect(Collectors.groupingBy(fileChange -> fileChange.getCommit().getHash(), Collectors.mapping(FileChangeEntity::getFilePath, Collectors.toSet())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (long) entry.getValue().size()));

        Map<String, Long> additionsByCommit = fileChanges.stream()
                .collect(Collectors.groupingBy(fileChange -> fileChange.getCommit().getHash(), Collectors.summingLong(FileChangeEntity::getLinesAdded)));

        Map<String, Long> deletionsByCommit = fileChanges.stream()
                .collect(Collectors.groupingBy(fileChange -> fileChange.getCommit().getHash(), Collectors.summingLong(FileChangeEntity::getLinesDeleted)));

        List<CommitEntity> upToSelected = commits.subList(0, selectedIndex + 1);
        Set<String> contributors = new HashSet<>();
        long filesChangedUpToPoint = 0L;
        long cumulativeAdditions = 0L;
        long cumulativeDeletions = 0L;

        for (CommitEntity candidate : upToSelected) {
            contributors.add(candidate.getAuthorEmail());
            filesChangedUpToPoint += filesChangedByCommit.getOrDefault(candidate.getHash(), 0L);
            cumulativeAdditions += additionsByCommit.getOrDefault(candidate.getHash(), 0L);
            cumulativeDeletions += deletionsByCommit.getOrDefault(candidate.getHash(), 0L);
        }

        return RepositoryTimelineSnapshotDto.builder()
                .commitHash(commit.getHash())
                .timestamp(commit.getCommitTimestamp())
                .commitsUpToPoint((long) upToSelected.size())
                .contributorsUpToPoint((long) contributors.size())
                .filesChangedUpToPoint(filesChangedUpToPoint)
                .cumulativeAdditions(cumulativeAdditions)
                .cumulativeDeletions(cumulativeDeletions)
                .build();
    }

    private RepositoryEntity getRepositoryOrThrow(Long repositoryId) {
        return repositoryEntityRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repositoryId));
    }
}
