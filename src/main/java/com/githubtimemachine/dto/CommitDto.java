package com.githubtimemachine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitDto {

    @Schema(description = "Commit hash")
    private String hash;

    @Schema(description = "Author name")
    private String authorName;

    @Schema(description = "Author email")
    private String authorEmail;

    @Schema(description = "Commit timestamp")
    private Instant commitTimestamp;

    @Schema(description = "Commit message")
    private String message;

    @Schema(description = "Parent commit hashes")
    private List<String> parentHashes;
}
