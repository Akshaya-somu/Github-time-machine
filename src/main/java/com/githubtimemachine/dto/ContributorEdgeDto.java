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
public class ContributorEdgeDto {

    @Schema(description = "Contributor identifier for the source node")
    private String source;

    @Schema(description = "Contributor identifier for the target node")
    private String target;

    @Schema(description = "Number of shared files between the contributors")
    private Integer sharedFiles;

    @Schema(description = "Collaboration strength score for the edge")
    private Integer weight;
}
