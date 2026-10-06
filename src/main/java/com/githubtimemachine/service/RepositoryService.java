package com.githubtimemachine.service;

import com.githubtimemachine.config.GitProperties;
import com.githubtimemachine.dto.RepositoryRequest;
import com.githubtimemachine.dto.RepositoryResponse;
import com.githubtimemachine.entity.RepositoryEntity;
import com.githubtimemachine.exception.ResourceNotFoundException;
import com.githubtimemachine.repository.RepositoryEntityRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final GitProperties gitProperties;

    public List<RepositoryResponse> getAllRepositories() {
        return repositoryEntityRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RepositoryResponse getRepositoryById(Long id) {
        RepositoryEntity entity = repositoryEntityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + id));
        return toResponse(entity);
    }

    @Transactional
    public RepositoryResponse createRepository(RepositoryRequest request) {
        String fullName = request.getFullName().trim();
        String remoteUrl = request.getRemoteUrl().trim();
        String defaultBranch = request.getDefaultBranch() == null || request.getDefaultBranch().isBlank()
                ? gitProperties.getDefaultBranch()
                : request.getDefaultBranch();

        if (repositoryEntityRepository.findByFullName(fullName).isPresent()) {
            throw new IllegalArgumentException("Repository already exists: " + fullName);
        }

        Path repoDirectory = Path.of(gitProperties.getBaseDirectory(), sanitizeName(fullName));
        try {
            Files.createDirectories(repoDirectory.getParent());
            if (!Files.exists(repoDirectory)) {
                Git.cloneRepository()
                        .setURI(remoteUrl)
                        .setDirectory(repoDirectory.toFile())
                        .setBranch(defaultBranch)
                        .call();
            }
        } catch (GitAPIException | IOException e) {
            throw new IllegalStateException("Unable to clone repository: " + remoteUrl, e);
        }

        RepositoryEntity entity = RepositoryEntity.builder()
                .fullName(fullName)
                .remoteUrl(remoteUrl)
                .defaultBranch(defaultBranch)
                .localPath(repoDirectory.toString())
                .build();

        RepositoryEntity saved = repositoryEntityRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public void deleteRepository(Long id) {
        RepositoryEntity repository = repositoryEntityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + id));

        repositoryEntityRepository.delete(repository);
        try {
            Files.deleteIfExists(Path.of(repository.getLocalPath()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to delete repository directory for id: " + id, e);
        }
    }

    private RepositoryResponse toResponse(RepositoryEntity entity) {
        return RepositoryResponse.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .remoteUrl(entity.getRemoteUrl())
                .defaultBranch(entity.getDefaultBranch())
                .localPath(entity.getLocalPath())
                .build();
    }

    private String sanitizeName(String fullName) {
        return fullName.replace("/", "__").replace("\\", "__");
    }
}
