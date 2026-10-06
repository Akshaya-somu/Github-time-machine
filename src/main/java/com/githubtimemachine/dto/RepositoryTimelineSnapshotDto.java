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
public class RepositoryTimelineSnapshotDto {

    @Schema(description = "Commit hash at the selected point in history")
    private String commitHash;

    @Schema(description = "Selected commit timestamp")
    private Instant timestamp;

    @Schema(description = "Commits considered up to and including the selected commit")
    private Long commitsUpToPoint;

    @Schema(description = "Unique contributors encountered up to and including the selected commit")
    private Long contributorsUpToPoint;

    @Schema(description = "Files changed up to and including the selected commit")
    private Long filesChangedUpToPoint;

    @Schema(description = "Cumulative additions up to and including the selected commit")
    private Long cumulativeAdditions;

    @Schema(description = "Cumulative deletions up to and including the selected commit")
    private Long cumulativeDeletions;
}
