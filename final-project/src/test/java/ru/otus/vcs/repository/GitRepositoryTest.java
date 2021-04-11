package ru.otus.vcs.repository;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.config.GitConfig;
import ru.otus.vcs.exception.UserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitRepositoryTest {

    @Test
    void createNewRepoSuccessfulCase(@TempDir final Path temp) {
        final var repo = GitRepository.createNew(temp.toString());
        Assertions.assertThat(repo.getGitDir())
                .exists()
                .isNotEmptyDirectory()
                .isEqualTo(temp.resolve(GitRepository.GITDIR));
    }

    @Test
    void createNewRepoWithNonExistentDir(@TempDir final Path temp) {
        Assertions.assertThatThrownBy(() -> GitRepository.createNew(temp.resolve("abc").toString()))
                .isExactlyInstanceOf(UserException.class);
    }

    @Test
    void createNewRepoWithGitDirAlreadyExists(@TempDir final Path temp) throws IOException {
        Files.createDirectory(temp.resolve(GitRepository.GITDIR));
        Assertions.assertThatThrownBy(() -> GitRepository.createNew(temp.toString()))
                .isExactlyInstanceOf(UserException.class);
    }

    @Test
    void testFindRepo(@TempDir final Path temp) throws IOException {
        GitRepository.createNew(temp.toString());
        final Path dirToSearch = temp.resolve("abc");
        Files.createDirectory(dirToSearch);
        final var repo = GitRepository.find(dirToSearch.toString());
        Assertions.assertThat(repo.getGitDir())
                .isEqualTo(temp.resolve(GitRepository.GITDIR));
    }

    @Test
    void testFindRepoFailingCase(@TempDir final Path temp) {
        Assertions.assertThatThrownBy(
                () -> GitRepository.find(temp.toString())
        ).isExactlyInstanceOf(UserException.class);
    }

    @Test
    void testFindRepoBadVersion(@TempDir final Path temp) throws IOException {
        final var repo = GitRepository.createNew(temp.toString());
        final var config = repo.getConfig();
        config.put(GitConfig.REPO_VER_KEY, 1);
        Files.writeString(repo.repoPath(Path.of(GitRepository.CONFIG)), config.toString());
        Assertions.assertThatThrownBy(() -> GitRepository.find(temp.toString()))
                .isExactlyInstanceOf(GitRepository.RepoCreationException.class)
                .hasMessageContaining("should be '0'");
    }
}