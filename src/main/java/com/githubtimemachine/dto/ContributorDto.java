package com.githubtimemachine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributorDto {

    @Schema(description = "Contributor unique identifier")
    private String id;

    @Schema(description = "Contributor name")
    private String name;

    @Schema(description = "Contributor email")
    private String email;

    @Schema(description = "Number of commits by this contributor")
    private Long commitCount;

    @Schema(description = "Total added lines across all this contributor's commits")
    private Long totalAdditions;

    @Schema(description = "Total deleted lines across all this contributor's commits")
    private Long totalDeletions;

    @Schema(description = "Distinct files modified by this contributor")
    private Long filesModified;
}
