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
public class RepositoryAnalysisResponse {

    @Schema(description = "Persisted repository identifier")
    private Long repositoryId;

    @Schema(description = "Full repository name")
    private String fullName;

    @Schema(description = "Remote Git repository URL")
    private String remoteUrl;

    @Schema(description = "Total number of commits analyzed")
    private int commitCount;

    @Schema(description = "Total number of contributors extracted")
    private int contributorCount;

    @Schema(description = "Total number of file changes extracted")
    private int fileChangeCount;
}
