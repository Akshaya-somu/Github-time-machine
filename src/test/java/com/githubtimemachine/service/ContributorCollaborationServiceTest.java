package com.githubtimemachine.service;

import com.githubtimemachine.dto.ContributorDto;
import com.githubtimemachine.dto.ContributorGraphDto;
import com.githubtimemachine.entity.RepositoryEntity;
import com.githubtimemachine.repository.RepositoryEntityRepository;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({GitAnalyzerService.class, ContributorAnalysisService.class})
class ContributorCollaborationServiceTest {

    @Autowired
    private GitAnalyzerService gitAnalyzerService;

    @Autowired
    private ContributorAnalysisService contributorAnalysisService;

    @Autowired
    private RepositoryEntityRepository repositoryEntityRepository;

    @Test
    void contributesAggregation_andCollaborationGraph_areCalculatedFromPersistedHistory() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-graph-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "hello\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("initial commit").call();

            Files.writeString(repoDir.resolve("README.md"), "hello\nworld\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("update readme").call();

            Files.writeString(repoDir.resolve("notes.txt"), "shared\n");
            git.add().addFilepattern("notes.txt").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("add notes").call();

            Files.writeString(repoDir.resolve("README.md"), "hello\nworld\nextra\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("update notes").call();
        }

        RepositoryEntity saved = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/project.git");

        List<ContributorDto> contributors = contributorAnalysisService.getContributors(saved.getId());
        assertThat(contributors).hasSize(2);
        assertThat(contributors.get(0).getName()).isEqualTo("Alice Example");
        assertThat(contributors.get(0).getCommitCount()).isEqualTo(2L);
        assertThat(contributors.get(0).getTotalAdditions()).isGreaterThan(0L);
        assertThat(contributors.get(0).getFilesModified()).isGreaterThan(0L);

        ContributorGraphDto graph = contributorAnalysisService.getContributorGraph(saved.getId());
        assertThat(graph.getNodes()).hasSize(2);
        assertThat(graph.getEdges()).hasSize(1);
        assertThat(graph.getEdges().get(0).getSharedFiles()).isEqualTo(1);
        assertThat(graph.getEdges().get(0).getWeight()).isEqualTo(1);
    }

    @Test
    void duplicateEdges_arePreventedForSameSharedFiles() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-duplicates-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "one\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("alice first").call();

            Files.writeString(repoDir.resolve("README.md"), "one\nsecond\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("alice second").call();

            Files.writeString(repoDir.resolve("README.md"), "one\nsecond\nthird\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("bob first").call();

            Files.writeString(repoDir.resolve("README.md"), "one\nsecond\nthird\nfourth\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("bob second").call();
        }

        RepositoryEntity saved = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/project.git");

        ContributorGraphDto graph = contributorAnalysisService.getContributorGraph(saved.getId());
        assertThat(graph.getEdges()).hasSize(1);
        assertThat(graph.getEdges().get(0).getSource()).isIn("alice@example.com", "bob@example.com");
    }

    @Test
    void emptyRepository_hasNoContributorNodesOrEdges() {
        RepositoryEntity repository = repositoryEntityRepository.save(RepositoryEntity.builder()
                .fullName("example/empty")
                .remoteUrl("https://github.com/example/empty.git")
                .defaultBranch("main")
                .localPath("/tmp/empty")
                .build());

        ContributorGraphDto graph = contributorAnalysisService.getContributorGraph(repository.getId());
        assertThat(graph.getNodes()).isEmpty();
        assertThat(graph.getEdges()).isEmpty();
        assertThat(contributorAnalysisService.getContributors(repository.getId())).isEmpty();
    }

    @Test
    void singleContributor_hasNoCollaborationEdges() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-single-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "hello\nworld\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("only commit").call();
        }

        RepositoryEntity saved = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/solo.git");

        ContributorGraphDto graph = contributorAnalysisService.getContributorGraph(saved.getId());
        assertThat(graph.getNodes()).hasSize(1);
        assertThat(graph.getNodes().get(0).getName()).isEqualTo("Alice Example");
        assertThat(graph.getEdges()).isEmpty();
    }
}
