package com.githubtimemachine.service;

import com.githubtimemachine.entity.RepositoryEntity;
import com.githubtimemachine.repository.CommitRepository;
import com.githubtimemachine.repository.ContributorRepository;
import com.githubtimemachine.repository.FileChangeRepository;
import com.githubtimemachine.repository.RepositoryEntityRepository;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(GitAnalyzerService.class)
class GitAnalyzerServiceTest {

    @Autowired
    private GitAnalyzerService gitAnalyzerService;

    @Autowired
    private RepositoryEntityRepository repositoryEntityRepository;

    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    private ContributorRepository contributorRepository;

    @Autowired
    private FileChangeRepository fileChangeRepository;

    @Test
    void analyzeRepositoryFromLocalPath_persistsCommitsContributorsAndFiles() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-test-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "hello\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("initial commit").call();

            Files.writeString(repoDir.resolve("README.md"), "hello\nworld\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("update readme").call();

            Files.writeString(repoDir.resolve("notes.txt"), "new file\n");
            git.add().addFilepattern("notes.txt").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("add notes").call();
        }

        RepositoryEntity saved = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/project.git");

        assertThat(saved.getId()).isNotNull();
        assertThat(repositoryEntityRepository.findById(saved.getId())).isPresent();
        assertThat(commitRepository.findByRepositoryIdOrderByCommitTimestampDesc(saved.getId())).hasSize(3);
        assertThat(contributorRepository.findByRepositoryIdOrderByCommitCountDesc(saved.getId())).hasSize(2);
        assertThat(fileChangeRepository.findByRepositoryId(saved.getId())).isNotEmpty();
    }

    @Test
    void analyzeRepository_isIdempotentAcrossRepeatedRuns() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-repeat-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "hello\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("initial commit").call();

            Files.writeString(repoDir.resolve("README.md"), "hello\nworld\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("update readme").call();
        }

        RepositoryEntity first = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/repeat.git");
        long commitCountBefore = commitRepository.countByRepositoryId(first.getId());
        long fileChangeCountBefore = fileChangeRepository.findByRepositoryId(first.getId()).size();

        RepositoryEntity second = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/repeat.git");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(commitRepository.countByRepositoryId(second.getId())).isEqualTo(commitCountBefore);
        assertThat(fileChangeRepository.findByRepositoryId(second.getId())).hasSize((int) fileChangeCountBefore);
    }

    @Test
    void analyzeRepository_handlesLargeHistoryWithoutLoadingEntireCommitList() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-large-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            for (int i = 0; i < 200; i++) {
                Path file = repoDir.resolve("commit-" + i + ".txt");
                Files.writeString(file, "value=" + i + "\n");
                git.add().addFilepattern(file.getFileName().toString()).call();
                git.commit().setAuthor("Large Repo Bot", "bot@example.com").setMessage("commit " + i).call();
            }
        }

        RepositoryEntity repository = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/large.git");

        assertThat(repository.getId()).isNotNull();
        assertThat(commitRepository.countByRepositoryId(repository.getId())).isEqualTo(200);
        assertThat(fileChangeRepository.findByRepositoryId(repository.getId())).hasSizeGreaterThan(0);
    }
}
