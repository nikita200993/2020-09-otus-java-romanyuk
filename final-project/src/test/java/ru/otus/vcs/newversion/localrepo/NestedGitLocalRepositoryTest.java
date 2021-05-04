package ru.otus.vcs.newversion.localrepo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.index.Index;
import ru.otus.vcs.newversion.index.IndexEntry;
import ru.otus.vcs.newversion.index.diff.Addition;
import ru.otus.vcs.newversion.index.diff.Deletion;
import ru.otus.vcs.newversion.index.diff.Modification;
import ru.otus.vcs.newversion.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.gitrepo.GitRepoStatus;
import ru.otus.vcs.newversion.gitrepo.GitRepository;
import ru.otus.vcs.newversion.path.VCSFileDesc;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.ReservedRef;
import ru.otus.vcs.newversion.ref.Sha1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NestedGitLocalRepositoryTest {

    @Mock
    private GitRepository mockGitRepo;
    @TempDir
    Path temp;
    private LocalRepository localRepository;

    @BeforeEach
    void commonSetup() throws IOException {
        final var gitRep = temp.resolve(GitRepository.DIR_NAME);
        Files.createDirectory(gitRep);
        when(mockGitRepo.repoRealPath())
                .thenReturn(gitRep);
        localRepository = new NestedGitLocalRepository(mockGitRepo);
    }

    @Test
    void checkThatIsRepositoryPathSuccessfulCheck1() {
        localRepository.checkThatIsRepositoryPath(temp.resolve(GitRepository.DIR_NAME));
    }

    @Test
    void checkThatIsRepositoryPathSuccessfulCheck2() {
        localRepository.checkThatIsRepositoryPath(temp.resolve("abz"));
    }

    @Test
    void checkThatIsRepositoryPathFailedCheck1() {
        Assertions.assertThatThrownBy(
                () -> localRepository.checkThatIsRepositoryPath(temp)
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void checkThatIsRepositoryPathFailedCheck2() {
        Assertions.assertThatThrownBy(
                () -> localRepository.checkThatIsRepositoryPath(temp.getParent())
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void checkThatIsRepositoryPathFailedCheck3() {
        Assertions.assertThatThrownBy(
                () -> localRepository.checkThatIsRepositoryPath(temp.getParent().resolve("adsadasd"))
        ).isInstanceOf(LocalRepositoryException.class);
    }


    @Test
    void addExistentRegularFile() throws IOException {
        Files.writeString(temp.resolve("a"), "hi");
        localRepository.add(VCSPath.create("a"));
    }

    @Test
    void addExistentSymlink() throws IOException {
        Files.createSymbolicLink(temp.resolve("a"), temp);
        Assertions.assertThatThrownBy(
                () -> localRepository.add(VCSPath.create("a"))
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void addExistentDir() throws IOException {
        Files.createDirectory(temp.resolve("a"));
        Assertions.assertThatThrownBy(
                () -> localRepository.add(VCSPath.create("a"))
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void removeExistentFileWithoutLocalChanges() throws IOException {
        final Path pathToRemove = temp.resolve("a");
        Files.writeString(pathToRemove, "abc");
        final var vcsPath = VCSPath.create("a");
        final var hash = Sha1.hash("a");
        final var index = Index.create(
                List.of(IndexEntry.newNormalEntry(vcsPath, hash))
        );
        when(mockGitRepo.getIndex()).thenReturn(index);
        when(mockGitRepo.hash(any())).thenReturn(hash);
        localRepository.remove(vcsPath);
        Assertions.assertThat(pathToRemove)
                .doesNotExist();
    }

    @Test
    void removeExistentFileWithLocalChanges() throws IOException {
        final Path pathToRemove = temp.resolve("a");
        Files.writeString(pathToRemove, "abc");
        final var vcsPath = VCSPath.create("a");
        final var hash = Sha1.hash("a");
        final var index = Index.create(
                List.of(IndexEntry.newNormalEntry(vcsPath, hash))
        );
        when(mockGitRepo.getIndex()).thenReturn(index);
        when(mockGitRepo.hash(any())).thenReturn(Sha1.hash("b"));
        Assertions.assertThatThrownBy(
                () -> localRepository.remove(vcsPath)
        ).isInstanceOf(LocalRepositoryException.class);
        Assertions.assertThat(pathToRemove)
                .exists();
    }

    @Test
    void removeAbsentInIndexFileButPresentInLocalFs() throws IOException {
        final Path pathToRemove = temp.resolve("a");
        Files.writeString(pathToRemove, "abc");
        final var vcsPath = VCSPath.create("a");
        final var index = Index.create(Collections.emptyList());
        when(mockGitRepo.getIndex()).thenReturn(index);
        Assertions.assertThatThrownBy(
                () -> localRepository.remove(vcsPath)
        ).isInstanceOf(LocalRepositoryException.class);
        Assertions.assertThat(pathToRemove)
                .exists();
    }

    @Test
    void removeAbsentInLocalFsButPresentInIndex() {
        final var vcsPath = VCSPath.create("a");
        final var index = Index.create(
                List.of(IndexEntry.newNormalEntry(vcsPath, Sha1.hash("a")))
        );
        when(mockGitRepo.getIndex()).thenReturn(index);
        localRepository.remove(vcsPath);
    }


    @Test
    void removeFromIndexDoesntDeletesLocal() throws IOException {
        final Path pathToRemove = temp.resolve("a");
        Files.writeString(pathToRemove, "abc");
        final var vcsPath = VCSPath.create("a");
        when(mockGitRepo.remove(vcsPath)).thenReturn(true);
        localRepository.removeFromIndex(vcsPath);
        Assertions.assertThat(pathToRemove)
                .exists();
    }

    @Test
    void removeFromIndexFailsIfNotInIndex() {
        final var vcsPath = VCSPath.create("a");
        when(mockGitRepo.remove(vcsPath)).thenReturn(false);
        Assertions.assertThatThrownBy(
                () -> localRepository.removeFromIndex(vcsPath)
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void removeForciblyAbsentInFsAndPresentInIndex() {
        final Path pathToRemove = temp.resolve("a");
        final var vcsPath = VCSPath.create("a");
        when(mockGitRepo.remove(vcsPath)).thenReturn(true);
        localRepository.removeForcibly(vcsPath);
    }

    @Test
    void removeForciblyLocalChangesIgnored() throws IOException {
        final Path pathToRemove = temp.resolve("a");
        Files.writeString(pathToRemove, "abc");
        final var vcsPath = VCSPath.create("a");
        when(mockGitRepo.remove(vcsPath)).thenReturn(true);
        localRepository.removeForcibly(vcsPath);
        Assertions.assertThat(pathToRemove)
                .doesNotExist();
    }

    @Test
    void checkoutSuccessCase() throws IOException {
        final var pathToBeRemoved = temp.resolve("removed");
        final var pathToBeAdded = temp.resolve("added");
        final var pathToBeModified = temp.resolve("modified");
        final var unchangedPath = temp.resolve("unchanged");

        Files.writeString(pathToBeRemoved, "removed");
        Files.writeString(pathToBeModified, "old");
        Files.writeString(unchangedPath, "unchanged");
        final VCSPath vcsPathToBeRemoved = VCSPath.create(temp.relativize(pathToBeRemoved));
        final VCSPath vcsPathToAdded = VCSPath.create(temp.relativize(pathToBeAdded));
        final VCSPath vcsPathToBeModified = VCSPath.create(temp.relativize(pathToBeModified));
        final var removedHash = Sha1.hash("removed");
        final var addedHash = Sha1.hash("added");
        final var modifiedOldHash = Sha1.hash("old");
        final var modifiedNewHash = Sha1.hash("new");
        final List<VCSFileChange> changes = List.of(
                new Deletion(new VCSFileDesc(vcsPathToBeRemoved, removedHash)),
                new Addition(new VCSFileDesc(vcsPathToAdded, addedHash)),
                new Modification(new VCSFileDesc(vcsPathToBeModified, modifiedNewHash), modifiedOldHash)
        );
        when(mockGitRepo.checkoutChanges(any()))
                .thenReturn(changes);
        when(mockGitRepo.hash(any())).then(
                invocation -> {
                    final var arg = (byte[]) invocation.getArgument(0);
                    if (Arrays.equals(arg, "old".getBytes())) {
                        return modifiedOldHash;
                    } else if (Arrays.equals(arg, "removed".getBytes())) {
                        return removedHash;
                    } else {
                        throw Contracts.unreachable();
                    }
                }
        );
        when(mockGitRepo.readFile(any())).then(
                invocation -> {
                    final var arg = (Sha1) invocation.getArgument(0);
                    if (arg.equals(modifiedNewHash)) {
                        return "new".getBytes();
                    } else if (arg.equals(addedHash)) {
                        return "added".getBytes();
                    } else {
                        throw Contracts.unreachable();
                    }
                }
        );
        localRepository.checkout(ReservedRef.head);
        verify(mockGitRepo).checkout(any());
        Assertions.assertThat(pathToBeRemoved).doesNotExist();
        Assertions.assertThat(unchangedPath).hasContent("unchanged");
        Assertions.assertThat(pathToBeAdded).hasContent("added");
        Assertions.assertThat(pathToBeModified).hasContent("new");
    }

    @Test
    void checkoutWithLocalChanges1() throws IOException {
        final var localChangeThatForbidsToWriteAddedFile = temp.resolve("a");
        Files.createSymbolicLink(localChangeThatForbidsToWriteAddedFile, temp);
        final var vcsPath = VCSPath.create("a");
        final var sha = Sha1.hash("a");
        final List<VCSFileChange> changes = List.of(
                new Addition(new VCSFileDesc(vcsPath, sha))
        );
        when(mockGitRepo.checkoutChanges(any()))
                .thenReturn(changes);
        Assertions.assertThatThrownBy(
                () -> localRepository.checkout(ReservedRef.head)
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void checkoutWithLocalChanges2() throws IOException {
        final var locallyChangedFile = temp.resolve("a");
        Files.writeString(locallyChangedFile, "new");
        final var vcsPath = VCSPath.create("a");
        final var sha = Sha1.hash("a");
        final List<VCSFileChange> changes = List.of(
                new Modification(new VCSFileDesc(vcsPath, sha), Sha1.hash("old"))
        );
        when(mockGitRepo.checkoutChanges(any()))
                .thenReturn(changes);
        when(mockGitRepo.hash("new".getBytes())).thenReturn(Sha1.hash("new"));
        Assertions.assertThatThrownBy(
                () -> localRepository.checkout(ReservedRef.head)
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void checkoutFileLocalChangePreventCheckout1() throws IOException {
        final var localChangeThatForbidsToWriteAddedFile = temp.resolve("a");
        Files.createSymbolicLink(localChangeThatForbidsToWriteAddedFile, temp);
        final var vcsPath = VCSPath.create(temp.relativize(localChangeThatForbidsToWriteAddedFile));
        when(mockGitRepo.readFile(ReservedRef.head, vcsPath)).thenReturn("data".getBytes());
        Assertions.assertThatThrownBy(
                () -> localRepository.checkoutFile(ReservedRef.head, vcsPath)
        ).isInstanceOf(LocalRepositoryException.class);
    }

    @Test
    void checkoutFileSucceeds() throws IOException {
        final var checkoutPath = temp.resolve("a");
        Files.writeString(checkoutPath, "old");
        final var vcsPath = VCSPath.create(temp.relativize(checkoutPath));
        when(mockGitRepo.readFile(ReservedRef.head, vcsPath)).thenReturn("new".getBytes());
        localRepository.checkoutFile(ReservedRef.head, vcsPath);
        Assertions.assertThat(checkoutPath).hasContent("new");
    }

    @Test
    void status() throws IOException {
        final var addedPath = VCSPath.create("added");
        final var removedPath = VCSPath.create("removed");
        final var modifiedPath = VCSPath.create("modified");
        Files.writeString(temp.resolve(addedPath.toOsPath()), "added");
        Files.writeString(temp.resolve(modifiedPath.toOsPath()), "new");
        final var addedSha = Sha1.hash("added");
        final var modifiedShaNewSha = Sha1.hash("new");
        final var modifiedShaOldSha = Sha1.hash("old");
        final var removedSha = Sha1.hash("removed");
        final var stubbedIndex = Index.create(
                List.of(
                        IndexEntry.newNormalEntry(modifiedPath, modifiedShaOldSha),
                        IndexEntry.newNormalEntry(removedPath, removedSha)
                )
        );
        final var expectedLocalChanges = List.of(
                new Addition(new VCSFileDesc(addedPath, addedSha)),
                new Deletion(new VCSFileDesc(removedPath, removedSha)),
                new Modification(new VCSFileDesc(modifiedPath, modifiedShaNewSha), modifiedShaOldSha)
        );
        final var stubbedGitStatus = new GitRepoStatus(Collections.emptyList(), null);
        when(mockGitRepo.status()).thenReturn(stubbedGitStatus);
        when(mockGitRepo.getIndex()).thenReturn(stubbedIndex);
        when(mockGitRepo.hash(any())).then(
                invocation -> Sha1.hash((byte[]) invocation.getArgument(0))
        );
        Assertions.assertThat(localRepository.status().getLocalFileChanges())
                .containsExactlyInAnyOrderElementsOf(expectedLocalChanges);
    }
}