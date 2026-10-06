package com.githubtimemachine.service;

import com.githubtimemachine.dto.CommitDto;
import com.githubtimemachine.dto.ContributorDto;
import com.githubtimemachine.dto.FileChangeDto;
import com.githubtimemachine.dto.RepositoryAnalysisResponse;
import com.githubtimemachine.entity.CommitEntity;
import com.githubtimemachine.entity.ContributorEntity;
import com.githubtimemachine.entity.FileChangeEntity;
import com.githubtimemachine.entity.RepositoryEntity;
import com.githubtimemachine.exception.GitAnalysisException;
import com.githubtimemachine.exception.InvalidGitHubUrlException;
import com.githubtimemachine.exception.ResourceNotFoundException;
import com.githubtimemachine.repository.CommitRepository;
import com.githubtimemachine.repository.ContributorRepository;
import com.githubtimemachine.repository.FileChangeRepository;
import com.githubtimemachine.repository.RepositoryEntityRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GitAnalyzerService {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("^https?://github\\.com/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+?)(?:\\.git)?/?$");

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final CommitRepository commitRepository;
    private final ContributorRepository contributorRepository;
    private final FileChangeRepository fileChangeRepository;
    private final TransactionTemplate transactionTemplate;

    public GitAnalyzerService(
            RepositoryEntityRepository repositoryEntityRepository,
            CommitRepository commitRepository,
            ContributorRepository contributorRepository,
            FileChangeRepository fileChangeRepository,
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.repositoryEntityRepository = repositoryEntityRepository;
        this.commitRepository = commitRepository;
        this.contributorRepository = contributorRepository;
        this.fileChangeRepository = fileChangeRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public RepositoryAnalysisResponse analyzeRepository(String repositoryUrl) {
        String normalizedUrl = validateGithubUrl(repositoryUrl);
        String fullName = extractRepositoryName(normalizedUrl);
        Optional<RepositoryEntity> existingRepository = repositoryEntityRepository.findByFullName(fullName);
        if (existingRepository.isPresent() && isGitRepository(existingRepository.get().getLocalPath())) {
            RepositoryEntity repository = existingRepository.get();
            try {
                analyzeHistory(repository, Path.of(repository.getLocalPath()));
                return buildAnalysisResponse(repository);
            } catch (IOException e) {
                throw new GitAnalysisException("Failed to analyze existing repository: " + normalizedUrl, e);
            }
        }

        Path tempDirectory = null;
        boolean analysisCompleted = false;

        try {
            tempDirectory = Files.createTempDirectory("github-time-machine-");
            Path cloneDirectory = tempDirectory.resolve("repo");
            try (Git git = Git.cloneRepository()
                    .setURI(normalizedUrl)
                    .setDirectory(cloneDirectory.toFile())
                    .setNoCheckout(true)
                    .call()) {
                RepositoryEntity repository = persistRepository(normalizedUrl, cloneDirectory);
                analyzeHistory(repository, cloneDirectory);
                RepositoryAnalysisResponse response = buildAnalysisResponse(repository);
                analysisCompleted = true;
                return response;
            }
        } catch (InvalidGitHubUrlException e) {
            throw e;
        } catch (GitAPIException | IOException e) {
            throw new GitAnalysisException("Failed to clone or analyze repository: " + normalizedUrl, e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GitAnalysisException("Failed to analyze repository: " + normalizedUrl, e);
        } finally {
            if (!analysisCompleted) {
                cleanupDirectory(tempDirectory);
            }
        }
    }

    public RepositoryEntity analyzeRepository(Path repoPath, String remoteUrl) {
        if (repoPath == null || !Files.exists(repoPath)) {
            throw new GitAnalysisException("Repository path is invalid");
        }

        RepositoryEntity repository = persistRepository(remoteUrl, repoPath);
        try {
            analyzeHistory(repository, repoPath);
            return repository;
        } catch (Exception e) {
            throw new GitAnalysisException("Failed to analyze repository from local path: " + repoPath, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<CommitDto> getCommits(Long repositoryId, Pageable pageable) {
        getRepositoryOrThrow(repositoryId);
        return commitRepository.findByRepositoryIdOrderByCommitTimestamp(repositoryId, pageable)
                .map(this::toCommitDto);
    }

    @Transactional(readOnly = true)
    public CommitDto getCommit(Long repositoryId, String hash) {
        getRepositoryOrThrow(repositoryId);
        CommitEntity commit = commitRepository.findByRepositoryIdAndHash(repositoryId, hash)
                .orElseThrow(() -> new ResourceNotFoundException("Commit not found for repository id: " + repositoryId + " and hash: " + hash));
        return toCommitDto(commit);
    }

    @Transactional(readOnly = true)
    public List<FileChangeDto> getFiles(Long repositoryId) {
        getRepositoryOrThrow(repositoryId);
        return fileChangeRepository.findByRepositoryId(repositoryId).stream()
                .map(this::toFileChangeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FileChangeDto> getFileHistory(Long repositoryId, String path) {
        getRepositoryOrThrow(repositoryId);
        return fileChangeRepository.findByRepositoryIdAndFilePathOrderByCommitTimestamp(repositoryId, decodePath(path)).stream()
                .map(this::toFileChangeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContributorDto> getContributors(Long repositoryId) {
        getRepositoryOrThrow(repositoryId);
        return contributorRepository.findByRepositoryIdOrderByCommitCountDesc(repositoryId).stream()
                .map(this::toContributorDto)
                .collect(Collectors.toList());
    }

    private String validateGithubUrl(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new InvalidGitHubUrlException("Repository URL is required");
        }

        String candidate = repositoryUrl.trim().replace("\\", "/");
        if (!GITHUB_URL_PATTERN.matcher(candidate).matches()) {
            throw new InvalidGitHubUrlException("Invalid GitHub repository URL");
        }

        return candidate.endsWith(".git") ? candidate : candidate + ".git";
    }

    private RepositoryEntity persistRepository(String remoteUrl, Path repoDirectory) {
        String fullName = extractRepositoryName(remoteUrl);
        RepositoryEntity entity = repositoryEntityRepository.findByFullName(fullName)
                .orElseGet(() -> RepositoryEntity.builder()
                        .fullName(fullName)
                        .remoteUrl(remoteUrl)
                        .defaultBranch("main")
                        .localPath(repoDirectory.toString())
                        .build());

        entity.setRemoteUrl(remoteUrl);
        entity.setLocalPath(repoDirectory.toString());
        entity.setDefaultBranch(readDefaultBranch(repoDirectory));
        return repositoryEntityRepository.save(entity);
    }

    private String extractRepositoryName(String remoteUrl) {
        String candidate = remoteUrl.trim().replace("\\", "/");
        String withoutProtocol = candidate.replaceFirst("^https?://github\\.com/", "");
        String withoutGitSuffix = withoutProtocol.replaceFirst("\\.git/?$", "");
        String[] segments = withoutGitSuffix.split("/");

        if (segments.length != 2 || segments[0].isBlank() || segments[1].isBlank()) {
            throw new InvalidGitHubUrlException("Invalid GitHub repository URL");
        }

        return segments[0] + "/" + segments[1];
    }

    private boolean isGitRepository(String localPath) {
        if (localPath == null) {
            return false;
        }
        try (Git ignored = Git.open(Path.of(localPath).toFile())) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String readDefaultBranch(Path repoDirectory) {
        try (Git git = Git.open(repoDirectory.toFile())) {
            return git.getRepository().getBranch();
        } catch (IOException e) {
            return "main";
        }
    }

    private void analyzeHistory(RepositoryEntity repository, Path repoDirectory) throws IOException {
        try (Git git = Git.open(repoDirectory.toFile())) {
            Iterable<RevCommit> commitIterable = git.log().all().call();
            Map<String, ContributorEntity> contributorsByEmail = new LinkedHashMap<>();
            Set<String> processedCommitHashes = new HashSet<>();
            int commitCount = 0;

            for (RevCommit commit : commitIterable) {
                commitCount++;
                boolean newCommit = commitRepository.findByRepositoryIdAndHash(repository.getId(), commit.getId().getName()).isEmpty();
                if (!newCommit) {
                    continue;
                }
                List<FileChangeData> fileChanges = newCommit ? collectFileChanges(git, commit) : List.of();
                transactionTemplate.executeWithoutResult(status -> {
                    RepositoryEntity managedRepository = repositoryEntityRepository.findById(repository.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repository.getId()));
                    CommitEntity commitEntity = saveCommit(managedRepository, commit, processedCommitHashes);
                    if (newCommit) {
                        persistFileChanges(managedRepository, commitEntity, fileChanges);
                        upsertContributor(managedRepository, contributorsByEmail, commit);
                    }
                });
            }

            if (commitCount == 0) {
                throw new GitAnalysisException("Repository is empty or has no commit history");
            }

            transactionTemplate.executeWithoutResult(status -> {
                RepositoryEntity managedRepository = repositoryEntityRepository.findById(repository.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repository.getId()));
                syncContributors(managedRepository, contributorsByEmail);
            });
        } catch (GitAPIException e) {
            throw new GitAnalysisException("Invalid commit history for repository: " + repository.getFullName(), e);
        }
    }

    private CommitEntity saveCommit(RepositoryEntity repository, RevCommit revCommit, Set<String> processedCommitHashes) {
        String parentHashes = revCommit.getParents() == null || revCommit.getParents().length == 0
                ? ""
                : Arrays.stream(revCommit.getParents())
                        .map(parent -> parent.getId().getName())
                        .collect(Collectors.joining(","));

        CommitEntity entity = commitRepository.findByRepositoryIdAndHash(repository.getId(), revCommit.getId().getName())
                .orElseGet(() -> CommitEntity.builder()
                        .repository(repository)
                        .hash(revCommit.getId().getName())
                        .build());

        boolean isNew = entity.getId() == null;
        entity.setHash(revCommit.getId().getName());
        entity.setMessage(revCommit.getFullMessage());
        entity.setCommitTimestamp(Instant.ofEpochMilli(revCommit.getCommitTime() * 1000L));
        entity.setAuthorName(revCommit.getAuthorIdent().getName());
        entity.setAuthorEmail(revCommit.getAuthorIdent().getEmailAddress());
        entity.setParentCount(revCommit.getParentCount());
        entity.setParentHashes(parentHashes);
        CommitEntity saved = commitRepository.save(entity);
        if (isNew) {
            processedCommitHashes.add(saved.getHash());
        }
        return saved;
    }

    private List<FileChangeData> collectFileChanges(Git git, RevCommit commit) {
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(git.getRepository());

            AbstractTreeIterator oldTreeIterator;
            AbstractTreeIterator newTreeIterator = prepareTreeIterator(git.getRepository(), commit.getTree());

            if (commit.getParentCount() == 0) {
                oldTreeIterator = new EmptyTreeIterator();
            } else {
                RevCommit parent = commit.getParent(0);
                oldTreeIterator = prepareTreeIterator(git.getRepository(), parent.getTree());
            }

            List<DiffEntry> entries = diffFormatter.scan(oldTreeIterator, newTreeIterator);
            List<FileChangeData> fileChanges = new ArrayList<>(entries.size());
            for (DiffEntry entry : entries) {
                FileHeader fileHeader = diffFormatter.toFileHeader(entry);
                fileChanges.add(new FileChangeData(
                        resolveFilePath(entry),
                        mapChangeType(entry),
                        calculateAddedLines(fileHeader),
                        calculateDeletedLines(fileHeader)));
            }
            return fileChanges;
        } catch (IOException e) {
            throw new GitAnalysisException("Unable to inspect file changes for commit: " + commit.getName(), e);
        }
    }

    private void persistFileChanges(RepositoryEntity repository, CommitEntity commitEntity, List<FileChangeData> fileChanges) {
        for (FileChangeData fileChange : fileChanges) {
            fileChangeRepository.save(FileChangeEntity.builder()
                    .commit(commitEntity)
                    .repository(repository)
                    .filePath(fileChange.filePath())
                    .changeType(fileChange.changeType())
                    .linesAdded(fileChange.linesAdded())
                    .linesDeleted(fileChange.linesDeleted())
                    .build());
        }
    }

    private AbstractTreeIterator prepareTreeIterator(Repository repository, org.eclipse.jgit.lib.ObjectId treeId) throws IOException {
        CanonicalTreeParser parser = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            parser.reset(reader, treeId);
        }
        return parser;
    }

    private String resolveFilePath(DiffEntry entry) {
        if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
            return entry.getOldPath();
        }
        if (entry.getChangeType() == DiffEntry.ChangeType.RENAME) {
            return entry.getNewPath() != null ? entry.getNewPath() : entry.getOldPath();
        }
        return entry.getNewPath() != null ? entry.getNewPath() : entry.getOldPath();
    }

    private FileChangeEntity.ChangeType mapChangeType(DiffEntry entry) {
        if (entry.getChangeType() == DiffEntry.ChangeType.ADD) {
            return FileChangeEntity.ChangeType.ADD;
        }
        if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
            return FileChangeEntity.ChangeType.DELETE;
        }
        if (entry.getChangeType() == DiffEntry.ChangeType.RENAME) {
            return FileChangeEntity.ChangeType.RENAME;
        }
        return FileChangeEntity.ChangeType.MODIFY;
    }

    private int calculateAddedLines(FileHeader fileHeader) {
        int added = 0;
        for (HunkHeader hunk : fileHeader.getHunks()) {
            for (Edit edit : hunk.toEditList()) {
                added += Math.max(0, edit.getEndB() - edit.getBeginB());
            }
        }
        return added;
    }

    private int calculateDeletedLines(FileHeader fileHeader) {
        int deleted = 0;
        for (HunkHeader hunk : fileHeader.getHunks()) {
            for (Edit edit : hunk.toEditList()) {
                deleted += Math.max(0, edit.getEndA() - edit.getBeginA());
            }
        }
        return deleted;
    }

    private record FileChangeData(
            String filePath,
            FileChangeEntity.ChangeType changeType,
            int linesAdded,
            int linesDeleted) {
    }

    private void upsertContributor(RepositoryEntity repository, Map<String, ContributorEntity> contributorsByEmail, RevCommit commit) {
        PersonIdent authorIdent = commit.getAuthorIdent();
        String email = authorIdent.getEmailAddress();
        String name = authorIdent.getName();

        ContributorEntity contributor = contributorsByEmail.computeIfAbsent(email, key -> {
            ContributorEntity existing = contributorRepository.findByRepositoryIdAndEmail(repository.getId(), email)
                    .orElse(null);
            if (existing != null) {
                existing.setName(name);
                return existing;
            }
            return ContributorEntity.builder()
                    .repository(repository)
                    .email(email)
                    .name(name)
                    .commitCount(0L)
                    .build();
        });

        contributor.setName(name);
        contributor.setEmail(email);
        contributor.setCommitCount(contributor.getCommitCount() + 1);
        contributorRepository.save(contributor);
    }

    private void syncContributors(RepositoryEntity repository, Map<String, ContributorEntity> contributorsByEmail) {
        for (ContributorEntity contributor : contributorsByEmail.values()) {
            ContributorEntity persisted = contributorRepository.findByRepositoryIdAndEmail(repository.getId(), contributor.getEmail())
                    .orElse(contributor);
            persisted.setName(contributor.getName());
            persisted.setEmail(contributor.getEmail());
            persisted.setCommitCount(contributor.getCommitCount());
            contributorRepository.save(persisted);
        }
    }

    private RepositoryEntity getRepositoryOrThrow(Long repositoryId) {
        return repositoryEntityRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repositoryId));
    }

    private void cleanupDirectory(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try {
            Files.walk(directory)
                    .sorted((first, second) -> second.compareTo(first))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private RepositoryAnalysisResponse buildAnalysisResponse(RepositoryEntity repository) {
        return RepositoryAnalysisResponse.builder()
                .repositoryId(repository.getId())
                .fullName(repository.getFullName())
                .remoteUrl(repository.getRemoteUrl())
                .commitCount(Math.toIntExact(commitRepository.countByRepositoryId(repository.getId())))
                .contributorCount(contributorRepository.findByRepositoryId(repository.getId()).size())
                .fileChangeCount(fileChangeRepository.findByRepositoryId(repository.getId()).size())
                .build();
    }

    private CommitDto toCommitDto(CommitEntity commit) {
        List<String> parentHashes = new ArrayList<>();
        if (commit.getParentHashes() != null && !commit.getParentHashes().isBlank()) {
            parentHashes.addAll(Arrays.stream(commit.getParentHashes().split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toList()));
        }

        return CommitDto.builder()
                .hash(commit.getHash())
                .authorName(commit.getAuthorName())
                .authorEmail(commit.getAuthorEmail())
                .commitTimestamp(commit.getCommitTimestamp())
                .message(commit.getMessage())
                .parentHashes(parentHashes)
                .build();
    }

    private FileChangeDto toFileChangeDto(FileChangeEntity entity) {
        return FileChangeDto.builder()
                .filePath(entity.getFilePath())
                .changeType(entity.getChangeType().name())
                .linesAdded(entity.getLinesAdded())
                .linesDeleted(entity.getLinesDeleted())
                .commitHash(entity.getCommit() != null ? entity.getCommit().getHash() : null)
                .build();
    }

    private ContributorDto toContributorDto(ContributorEntity contributor) {
        return ContributorDto.builder()
                .name(contributor.getName())
                .email(contributor.getEmail())
                .commitCount(contributor.getCommitCount())
                .build();
    }

    private String decodePath(String path) {
        return path == null ? "" : path.replace("%2F", "/").replace("%20", " ");
    }
}
