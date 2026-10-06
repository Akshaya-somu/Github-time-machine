package com.githubtimemachine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryAnalysisRequest {

    @Schema(description = "Public GitHub repository URL to clone and analyze", example = "https://github.com/spring-projects/spring-boot.git")
    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;
}
