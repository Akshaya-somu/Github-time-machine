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
public class RepositoryResponse {

    @Schema(description = "Repository identifier")
    private Long id;

    @Schema(description = "Full repository name")
    private String fullName;

    @Schema(description = "Remote Git repository URL")
    private String remoteUrl;

    @Schema(description = "Default branch")
    private String defaultBranch;

    @Schema(description = "Local clone path used by JGit")
    private String localPath;
}
