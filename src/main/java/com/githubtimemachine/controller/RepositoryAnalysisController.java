package com.githubtimemachine.controller;

import com.githubtimemachine.dto.CommitDto;
import com.githubtimemachine.dto.ContributorDetailDto;
import com.githubtimemachine.dto.ContributorDto;
import com.githubtimemachine.dto.ContributorGraphDto;
import com.githubtimemachine.dto.FileChangeDto;
import com.githubtimemachine.dto.RepositoryAnalysisRequest;
import com.githubtimemachine.dto.RepositoryAnalysisResponse;
import com.githubtimemachine.dto.RepositoryTimelineDto;
import com.githubtimemachine.dto.RepositoryTimelineSnapshotDto;
import com.githubtimemachine.service.ContributorAnalysisService;
import com.githubtimemachine.service.GitAnalyzerService;
import com.githubtimemachine.service.RepositoryTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Repository Analysis", description = "Git history analysis endpoints")
public class RepositoryAnalysisController {

    private final GitAnalyzerService gitAnalyzerService;
    private final ContributorAnalysisService contributorAnalysisService;
    private final RepositoryTimelineService repositoryTimelineService;

    @PostMapping("/repositories/analyze")
    @Operation(summary = "Clone and analyze a public GitHub repository")
    public ResponseEntity<RepositoryAnalysisResponse> analyzeRepository(@Valid @RequestBody RepositoryAnalysisRequest request) {
        RepositoryAnalysisResponse response = gitAnalyzerService.analyzeRepository(request.getRepositoryUrl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/repositories/{id}/commits")
    @Operation(summary = "Get paginated commits for a repository")
    public ResponseEntity<Page<CommitDto>> getCommits(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "commitTimestamp").and(Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(gitAnalyzerService.getCommits(id, pageable));
    }

    @GetMapping("/repositories/{id}/commits/{hash}")
    @Operation(summary = "Get a specific commit by hash")
    public ResponseEntity<CommitDto> getCommit(@PathVariable Long id, @PathVariable String hash) {
        return ResponseEntity.ok(gitAnalyzerService.getCommit(id, hash));
    }

    @GetMapping("/repositories/{id}/files")
    @Operation(summary = "Get all file changes for a repository")
    public ResponseEntity<java.util.List<FileChangeDto>> getFiles(@PathVariable Long id) {
        return ResponseEntity.ok(gitAnalyzerService.getFiles(id));
    }

    @GetMapping("/repositories/{id}/files/{path}/history")
    @Operation(summary = "Get change history for a specific file path")
    public ResponseEntity<java.util.List<FileChangeDto>> getFileHistory(@PathVariable Long id, @PathVariable String path) {
        return ResponseEntity.ok(gitAnalyzerService.getFileHistory(id, path));
    }

    @GetMapping("/repositories/{id}/contributors")
    @Operation(summary = "Get contributor summaries for a repository")
    public ResponseEntity<java.util.List<ContributorDto>> getContributors(@PathVariable Long id) {
        return ResponseEntity.ok(contributorAnalysisService.getContributors(id));
    }

    @GetMapping("/repositories/{id}/contributors/graph")
    @Operation(summary = "Get contributor collaboration graph for a repository")
    public ResponseEntity<ContributorGraphDto> getContributorGraph(@PathVariable Long id) {
        return ResponseEntity.ok(contributorAnalysisService.getContributorGraph(id));
    }

    @GetMapping("/repositories/{id}/contributors/{contributorId}")
    @Operation(summary = "Get a contributor's detailed statistics and contribution history")
    public ResponseEntity<ContributorDetailDto> getContributorDetail(@PathVariable Long id, @PathVariable String contributorId) {
        return ResponseEntity.ok(contributorAnalysisService.getContributorDetail(id, contributorId));
    }

    @GetMapping("/repositories/{id}/timeline")
    @Operation(summary = "Get the repository evolution timeline and aggregate statistics")
    public ResponseEntity<RepositoryTimelineDto> getTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(repositoryTimelineService.getTimeline(id));
    }

    @GetMapping("/repositories/{id}/timeline/{commitHash}")
    @Operation(summary = "Get cumulative repository statistics at a selected commit")
    public ResponseEntity<RepositoryTimelineSnapshotDto> getTimelineAtCommit(@PathVariable Long id, @PathVariable String commitHash) {
        return ResponseEntity.ok(repositoryTimelineService.getTimelineAtCommit(id, commitHash));
    }
}
