package ru.otus.vcs.newversion.gitrepo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.newversion.utils.Utils;

import java.nio.file.Path;

public class RepoSearcherTest {

    @TempDir
    Path temp;

    @Test
    void testFindAbsentCase() {
        Assertions.assertThat(new UpwardsRepoSearcher(temp, new GitRepositoryFactoryImpl()).find(false))
                .isNull();
    }

    @Test
    void testFindBareCase() {
        RepositoryLayout.createLayout(temp);
        Assertions.assertThat(new UpwardsRepoSearcher(temp, new GitRepositoryFactoryImpl()).find(true))
                .returns(temp, GitRepository::repoRealPath);
    }

    @Test
    void testFindNonBareCase() {
        final var repoPath = temp.resolve(GitRepository.DIR_NAME);
        Utils.createDir(repoPath);
        RepositoryLayout.createLayout(repoPath);
        Assertions.assertThat(new UpwardsRepoSearcher(temp, new GitRepositoryFactoryImpl()).find(false))
                .returns(repoPath, GitRepository::repoRealPath);
    }

    @Test
    void testFindNonBareCase2() {
        RepositoryLayout.createLayout(temp);
        Assertions.assertThat(new UpwardsRepoSearcher(temp, new GitRepositoryFactoryImpl()).find(false))
                .isNull();
    }

    @Test
    void testFindNonBareCase3() {
        final var repoPath = temp.resolve(GitRepository.DIR_NAME);
        Utils.createDir(repoPath);
        RepositoryLayout.createLayout(repoPath);
        Utils.delete(repoPath.resolve(RepositoryLayout.OBJECTS));
        Assertions.assertThat(new UpwardsRepoSearcher(temp, new GitRepositoryFactoryImpl()).find(false))
                .isNull();
    }
}
