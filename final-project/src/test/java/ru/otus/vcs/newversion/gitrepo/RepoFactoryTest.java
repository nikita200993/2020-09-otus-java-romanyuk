package ru.otus.vcs.newversion.gitrepo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.newversion.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RepoFactoryTest {

    private final GitRepositoryFactory factory = new GitRepositoryFactoryImpl();

    @TempDir
    Path tempDir;

    @Test
    void testCreationBareCase() {
        factory.createNew(tempDir, true);
        Assertions.assertThat(tempDir)
                .matches(RepositoryLayout::isRepoLayout);
    }

    @Test
    void testCreationNonBareCase() {
        factory.createNew(tempDir, false);
        Assertions.assertThat(tempDir)
                .extracting(path -> path.resolve(GitRepository.DIR_NAME))
                .matches(RepositoryLayout::isRepoLayout);
    }

    @Test
    void testCreationNonEmptyDir() throws IOException {
        Files.createFile(tempDir.resolve("aa"));
        Assertions.assertThatThrownBy(() -> factory.createNew(tempDir, false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testRestoreBareCase() {
        RepositoryLayout.createLayout(tempDir);

        Assertions.assertThat(factory.restore(tempDir, true))
                .isNotNull()
                .returns(tempDir, GitRepository::repoRealPath);
    }

    @Test
    void testRestoreNonBareCase() {
        final var repoPath = tempDir.resolve(GitRepository.DIR_NAME);
        Utils.createDir(repoPath);
        RepositoryLayout.createLayout(repoPath);
        Assertions.assertThat(factory.restore(tempDir, false))
                .isNotNull()
                .returns(repoPath, GitRepository::repoRealPath);
    }

    @Test
    void testRestoreNonBareCaseBadLayout() {
        final var repoPath = tempDir.resolve(GitRepository.DIR_NAME);
        Utils.createDir(repoPath);
        RepositoryLayout.createLayout(repoPath);
        Utils.delete(repoPath.resolve(RepositoryLayout.OBJECTS));
        Assertions.assertThat(factory.restore(tempDir, false))
                .isNull();
    }
}
