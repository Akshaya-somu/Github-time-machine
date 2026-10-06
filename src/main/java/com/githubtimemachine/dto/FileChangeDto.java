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
public class FileChangeDto {

    @Schema(description = "File path")
    private String filePath;

    @Schema(description = "Change type")
    private String changeType;

    @Schema(description = "Number of added lines")
    private Integer linesAdded;

    @Schema(description = "Number of deleted lines")
    private Integer linesDeleted;

    @Schema(description = "Commit hash associated with this change")
    private String commitHash;
}
