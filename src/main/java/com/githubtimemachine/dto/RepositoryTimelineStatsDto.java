package com.githubtimemachine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryTimelineStatsDto {

    @Schema(description = "Total commit count")
    private Long totalCommits;

    @Schema(description = "Total unique contributors encountered")
    private Long totalContributors;

    @Schema(description = "Total files changed across the repository history")
    private Long totalFilesChanged;

    @Schema(description = "Total cumulative additions")
    private Long totalAdditions;

    @Schema(description = "Total cumulative deletions")
    private Long totalDeletions;

    @Schema(description = "First commit timestamp")
    private Instant firstCommitTimestamp;

    @Schema(description = "Latest commit timestamp")
    private Instant latestCommitTimestamp;
}
