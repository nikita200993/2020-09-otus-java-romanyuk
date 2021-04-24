package ru.otus.vcs.repository;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.objects.Blob;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.BranchName;
import ru.otus.vcs.ref.Sha1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitRepositoryTest {

    @TempDir
    Path temp;

    @Test
    void createNewRepoSuccessfulCase() {
        final var repo = GitRepository.createNew(temp);
        Assertions.assertThat(repo.getGitDir())
                .exists()
                .isNotEmptyDirectory()
                .isEqualTo(temp.resolve(GitRepository.GITDIR));
    }

    @Test
    void createNewRepoWithNonExistentDir() {
        Assertions.assertThatThrownBy(() -> GitRepository.createNew(temp.resolve("abc")))
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void createNewRepoWithGitDirAlreadyExists() throws IOException {
        Files.createDirectory(temp.resolve(GitRepository.GITDIR));
        Assertions.assertThatThrownBy(() -> GitRepository.createNew(temp))
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void testFindRepo() throws IOException {
        GitRepository.createNew(temp);
        final Path dirToSearch = temp.resolve("abc");
        Files.createDirectory(dirToSearch);
        final var repo = GitRepository.find(temp);
        Assertions.assertThat(repo)
                .isNotNull()
                .returns(temp.resolve(GitRepository.GITDIR), GitRepository::getGitDir);
    }

    @Test
    void testFindRepoFailingCase() {
        Assertions.assertThat(GitRepository.find(temp))
                .isNull();
    }

    @Test
    void testSaveObject() {
        final var repo = GitRepository.createNew(temp);
        final var blob = new Blob("Kek".getBytes(StandardCharsets.UTF_8));
        repo.saveGitObjectIfAbsent(blob);
        Assertions.assertThat(repo.readGitObjectOrThrowIfAbsent(blob.sha1()))
                .isEqualTo(blob);
    }

    @Test
    void testReadingAndSavingIndex() {
        final var repo = GitRepository.createNew(temp);
        final var index = repo.readIndex();
        Assertions.assertThat(repo.readIndex().isEmpty())
                .isTrue();
        final var newIndex = index.withNewIndexEntry(VCSPath.create("a"), Sha1.hash("a"));
        repo.saveIndex(newIndex);
        Assertions.assertThat(repo.readIndex())
                .isEqualTo(newIndex);
    }

    @Test
    void testHasBranchMasterCase() {
        final var repo = GitRepository.createNew(temp);
        Assertions.assertThat(repo.hasBranch(BranchName.create("master")))
                .isTrue();
    }

    @Test
    void testHasBranchNonMasterAbsentCase() {
        final var repo = GitRepository.createNew(temp);
        Assertions.assertThat(repo.hasBranch(BranchName.create("lol")))
                .isFalse();
    }
}