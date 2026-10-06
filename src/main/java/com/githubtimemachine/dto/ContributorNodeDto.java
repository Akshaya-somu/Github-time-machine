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
public class ContributorNodeDto {

    @Schema(description = "Unique contributor identifier")
    private String id;

    @Schema(description = "Contributor name")
    private String name;

    @Schema(description = "Contributor email")
    private String email;

    @Schema(description = "Total commits by this contributor")
    private Long commits;

    @Schema(description = "Total added lines across the repository history")
    private Long totalAdditions;

    @Schema(description = "Total deleted lines across the repository history")
    private Long totalDeletions;

    @Schema(description = "Unique files modified by this contributor")
    private Long filesModified;
}
