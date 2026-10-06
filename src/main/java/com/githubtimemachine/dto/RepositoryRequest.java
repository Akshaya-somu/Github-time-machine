package com.githubtimemachine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class RepositoryRequest {

    @Schema(description = "Full GitHub repository name, for example owner/repository")
    @NotBlank(message = "Repository full name is required")
    private String fullName;

    @Schema(description = "Remote Git repository URL")
    @NotBlank(message = "Remote URL is required")
    private String remoteUrl;

    @Schema(description = "Preferred default branch name", example = "main")
    private String defaultBranch;
}
