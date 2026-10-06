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
public class RepositoryTimelineDto {

    @Schema(description = "Repository-wide evolution timeline")
    private List<RepositoryTimelineEntryDto> entries;

    @Schema(description = "Aggregate statistics for the repository history")
    private RepositoryTimelineStatsDto statistics;
}
