package com.githubtimemachine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributorDetailDto {

    @Schema(description = "Contributor identifier")
    private String id;

    @Schema(description = "Contributor name")
    private String name;

    @Schema(description = "Contributor email")
    private String email;

    @Schema(description = "Number of commits contributed")
    private Long commitCount;

    @Schema(description = "Total additions by this contributor")
    private Long totalAdditions;

    @Schema(description = "Total deletions by this contributor")
    private Long totalDeletions;

    @Schema(description = "Unique files modified by this contributor")
    private Long filesModified;

    @Schema(description = "Contributor commit history")
    private List<CommitDto> commits;

    @Schema(description = "Most frequently modified files")
    private List<String> mostFrequentlyModifiedFiles;
}
