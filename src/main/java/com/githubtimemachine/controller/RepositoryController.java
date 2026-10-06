package com.githubtimemachine.controller;

import com.githubtimemachine.dto.RepositoryRequest;
import com.githubtimemachine.dto.RepositoryResponse;
import com.githubtimemachine.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
@Tag(name = "Repositories", description = "Operations for tracking Git repositories and snapshots")
public class RepositoryController {

    private final RepositoryService repositoryService;

    @GetMapping
    @Operation(summary = "List all repositories")
    public ResponseEntity<List<RepositoryResponse>> getAllRepositories() {
        return ResponseEntity.ok(repositoryService.getAllRepositories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repository by id")
    public ResponseEntity<RepositoryResponse> getRepositoryById(@PathVariable Long id) {
        return ResponseEntity.ok(repositoryService.getRepositoryById(id));
    }

    @PostMapping
    @Operation(summary = "Create and clone a repository")
    public ResponseEntity<RepositoryResponse> createRepository(@Valid @RequestBody RepositoryRequest request) {
        RepositoryResponse response = repositoryService.createRepository(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a repository")
    public ResponseEntity<Void> deleteRepository(@PathVariable Long id) {
        repositoryService.deleteRepository(id);
        return ResponseEntity.noContent().build();
    }
}
