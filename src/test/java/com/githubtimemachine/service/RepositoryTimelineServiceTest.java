package com.githubtimemachine.service;

import com.githubtimemachine.dto.RepositoryTimelineDto;
import com.githubtimemachine.dto.RepositoryTimelineSnapshotDto;
import com.githubtimemachine.entity.RepositoryEntity;
import com.githubtimemachine.exception.ResourceNotFoundException;
import com.githubtimemachine.repository.RepositoryEntityRepository;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({GitAnalyzerService.class, ContributorAnalysisService.class, RepositoryTimelineService.class})
class RepositoryTimelineServiceTest {

    @Autowired
    private RepositoryTimelineService repositoryTimelineService;

    @Autowired
    private GitAnalyzerService gitAnalyzerService;

    @Autowired
    private RepositoryEntityRepository repositoryEntityRepository;

    @Test
    void getTimeline_generatesChronologicalEntriesAndRepositoryStats() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-timeline-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "one\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("first commit").call();

            Files.writeString(repoDir.resolve("README.md"), "one\ntwo\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("second commit").call();
        }

        RepositoryEntity repository = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/project.git");

        RepositoryTimelineDto timeline = repositoryTimelineService.getTimeline(repository.getId());

        assertThat(timeline.getEntries()).hasSize(2);
        assertThat(timeline.getEntries().get(0).getAuthorName()).isEqualTo("Alice Example");
        assertThat(timeline.getEntries().get(1).getAuthorName()).isEqualTo("Bob Example");
        assertThat(timeline.getStatistics().getTotalCommits()).isEqualTo(2L);
        assertThat(timeline.getStatistics().getTotalContributors()).isEqualTo(2L);
        assertThat(timeline.getStatistics().getTotalAdditions()).isGreaterThan(0L);
        assertThat(timeline.getStatistics().getTotalDeletions()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void getTimelineAtCommit_returnsCumulativeStatisticsUpToSelectedCommit() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-snapshot-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "line 1\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("first commit").call();

            Files.writeString(repoDir.resolve("README.md"), "line 1\nline 2\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("second commit").call();
        }

        RepositoryEntity repository = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/snapshot.git");
        String hash = commitRepositoryHash(repository.getId(), 0);

        RepositoryTimelineSnapshotDto snapshot = repositoryTimelineService.getTimelineAtCommit(repository.getId(), hash);

        assertThat(snapshot.getCommitsUpToPoint()).isEqualTo(1L);
        assertThat(snapshot.getContributorsUpToPoint()).isEqualTo(1L);
        assertThat(snapshot.getCumulativeAdditions()).isGreaterThan(0L);
    }

    @Test
    void getTimeline_throwsForInvalidRepository() {
        assertThatThrownBy(() -> repositoryTimelineService.getTimeline(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    void getTimelineAtCommit_throwsForInvalidCommitHash() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-bad-hash-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "one\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("single commit").call();
        }

        RepositoryEntity repository = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/badhash.git");

        assertThatThrownBy(() -> repositoryTimelineService.getTimelineAtCommit(repository.getId(), "not-a-real-hash"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Commit not found");
    }

    @Test
    void getTimeline_handlesMergeCommit() throws Exception {
        Path repoDir = Files.createTempDirectory("github-time-machine-merge-");

        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve("README.md"), "base\n");
            git.add().addFilepattern("README.md").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("base").call();

            git.checkout().setCreateBranch(true).setName("feature").call();
            Files.writeString(repoDir.resolve("feature.txt"), "feature\n");
            git.add().addFilepattern("feature.txt").call();
            git.commit().setAuthor("Bob Example", "bob@example.com").setMessage("feature work").call();

            git.checkout().setName("master").call();
            Files.writeString(repoDir.resolve("main.txt"), "main\n");
            git.add().addFilepattern("main.txt").call();
            git.commit().setAuthor("Alice Example", "alice@example.com").setMessage("main work").call();

            git.merge().include(git.getRepository().resolve("feature"))
                    .setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.NO_FF)
                    .setMessage("merge feature")
                    .call();
        }

        RepositoryEntity repository = gitAnalyzerService.analyzeRepository(repoDir, "https://github.com/example/merge.git");
        RepositoryTimelineDto timeline = repositoryTimelineService.getTimeline(repository.getId());

        assertThat(timeline.getEntries()).hasSizeGreaterThanOrEqualTo(4);
        assertThat(timeline.getStatistics().getTotalCommits()).isGreaterThanOrEqualTo(4L);
    }

    private String commitRepositoryHash(Long repositoryId, int indexFromStart) {
        return repositoryEntityRepository.findById(repositoryId)
                .map(repository -> repositoryTimelineService.getTimeline(repositoryId).getEntries().get(indexFromStart).getCommitHash())
                .orElseThrow();
    }
}
