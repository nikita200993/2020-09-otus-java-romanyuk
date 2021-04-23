package ru.otus.vcs.repository;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.objects.Blob;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitRepositoryTest {

    @Test
    void createNewRepoSuccessfulCase(@TempDir final Path temp) {
        final var repo = GitRepository.createNew(temp);
        Assertions.assertThat(repo.getGitDir())
                .exists()
                .isNotEmptyDirectory()
                .isEqualTo(temp.resolve(GitRepository.GITDIR));
    }

    @Test
    void createNewRepoWithNonExistentDir(@TempDir final Path temp) {
        Assertions.assertThatThrownBy(() -> GitRepository.createNew(temp.resolve("abc")))
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void createNewRepoWithGitDirAlreadyExists(@TempDir final Path temp) throws IOException {
        Files.createDirectory(temp.resolve(GitRepository.GITDIR));
        Assertions.assertThatThrownBy(() -> GitRepository.createNew(temp))
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void testFindRepo(@TempDir final Path temp) throws IOException {
        GitRepository.createNew(temp);
        final Path dirToSearch = temp.resolve("abc");
        Files.createDirectory(dirToSearch);
        final var repo = GitRepository.find(temp);
        Assertions.assertThat(repo)
                .isNotNull()
                .returns(temp.resolve(GitRepository.GITDIR), GitRepository::getGitDir);
    }

    @Test
    void testFindRepoFailingCase(@TempDir final Path temp) {
        Assertions.assertThat(GitRepository.find(temp))
                .isNull();
    }

    @Test
    void testSaveObject(@TempDir final Path temp) throws IOException {
        final var repo = GitRepository.createNew(temp);
        final var blob = new Blob("Kek".getBytes(StandardCharsets.UTF_8));
        final var sha = repo.saveGitObject(blob);
        Assertions.assertThat(repo.<Blob>readGitObject(sha))
                .isEqualTo(blob);
    }

    @Test
    void testReadingAndSavingIndex(@TempDir final Path temp) {
        final var repo = GitRepository.createNew(temp);
        final var index = repo.readIndex();
        Assertions.assertThat(repo.readIndex().isEmpty())
                .isTrue();
        final var newIndex = index.withNewIndexEntry(VCSPath.create("a"), Sha1.hash("a"));
        repo.saveIndex(newIndex);
        Assertions.assertThat(repo.readIndex())
                .isEqualTo(newIndex);
    }
}