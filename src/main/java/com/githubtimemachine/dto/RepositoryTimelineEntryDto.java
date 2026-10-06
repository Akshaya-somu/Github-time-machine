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
public class RepositoryTimelineEntryDto {

    @Schema(description = "Commit hash")
    private String commitHash;

    @Schema(description = "Commit timestamp")
    private Instant timestamp;

    @Schema(description = "Author name")
    private String authorName;

    @Schema(description = "Commit message")
    private String message;

    @Schema(description = "Number of files changed in this commit")
    private Long filesChanged;

    @Schema(description = "Additions in this commit")
    private Long additions;

    @Schema(description = "Deletions in this commit")
    private Long deletions;
}
