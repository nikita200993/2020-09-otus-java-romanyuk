package ru.otus.vcs.newversion.gitrepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.index.diff.Addition;
import ru.otus.vcs.index.diff.Deletion;
import ru.otus.vcs.index.diff.Modification;
import ru.otus.vcs.index.diff.VCSFileChange;
import ru.otus.vcs.path.VCSFileDesc;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.BranchName;
import ru.otus.vcs.ref.Ref;
import ru.otus.vcs.ref.ReservedRef;
import ru.otus.vcs.ref.Sha1;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GitRepositoryTest {

    @TempDir
    Path path;
    private GitRepoImpl gitRepository;

    @BeforeEach
    void createRepo() {
        gitRepository = new GitRepositoryFactoryImpl().createNew(path, false);
    }

    @Test
    void testAdd() {
        final var vcsPath = VCSPath.create("a");
        gitRepository.add("ad".getBytes(), vcsPath);
        assertThat(gitRepository.getIndex())
                .returns(true, index -> index.contains(vcsPath));
    }

    @Test
    void testRemoveSuccess() {
        final var vcsPath = VCSPath.create("a");
        gitRepository.add("ad".getBytes(), vcsPath);
        assertThat(gitRepository.remove(vcsPath))
                .isTrue();
    }

    @Test
    void testRemoveAbsent() {
        assertThat(gitRepository.remove(VCSPath.create("a")))
                .isFalse();
    }

    @Test
    void testEmptyCommitThrows() {
        assertThatThrownBy(
                () -> gitRepository.commit(CommitMessage.create("abc"))
        ).isInstanceOf(GitRepositoryException.class);
    }

    @Test
    void testCommitSuccess() {
        final var vcsPath = VCSPath.create("a");
        gitRepository.add("a".getBytes(), vcsPath);
        final var sha = gitRepository.commit(CommitMessage.create("abc"));
        assertThat(new String(gitRepository.readFile(ReservedRef.head, vcsPath)))
                .isEqualTo("a");
    }

    @Test
    void testCommitNoChanges() {
        add("a", "a");
        commit("abc");
        assertThatThrownBy(
                () -> gitRepository.commit(CommitMessage.create("b"))
        ).isInstanceOf(GitRepositoryException.class);
    }

    @Test
    void testCommitEmptyIndex() {
        add("a", "a");
        commit("abc");
        remove("a");
        assertThatThrownBy(
                () -> gitRepository.commit(CommitMessage.create("b"))
        ).isInstanceOf(GitRepositoryException.class);
    }

    @Test
    void testBranchNoCommitCase() {
        assertThatThrownBy(() -> gitRepository.branch(BranchName.create("abc")))
                .isInstanceOf(GitRepositoryException.class);
    }

    @Test
    void testBranchNormalCase() {
        add("a", "a");
        commit("abc");
        gitRepository.branch(BranchName.create("abc"));
        assertThat(gitRepository.repoRealPath().resolve(RepositoryLayout.HEADS))
                .isDirectoryContaining(path -> path.endsWith("abc"));
    }

    @Test
    void testBranchAlreadyExists() {
        add("a", "a");
        commit("abc");
        gitRepository.branch(BranchName.create("abc"));
        assertThatThrownBy(
                () -> gitRepository.branch(BranchName.create("abc"))
        ).isInstanceOf(GitRepositoryException.class);
    }

    @Test
    void testCheckoutChangesUncommittedCase() {
        add("a", "b");
        commit("a");
        branch("new");
        remove("a");
        assertThatThrownBy(() -> checkoutChanges("new"))
                .isInstanceOf(GitRepositoryException.class);
    }

    @Test
    void testCheckoutChangesNormalCase() {
        add("a", "a");
        add("b", "b");
        commit("master first");
        branch("new");
        remove("a");
        add("b", "bb");
        add("c", "c");
        commit("new first");
        assertThat(checkoutChanges("new"))
                .isEqualTo(
                        Set.of(
                                new Deletion(fileDesc("c", "c")),
                                new Addition(fileDesc("a", "a")),
                                new Modification(fileDesc("b", "b"), hash("bb"))
                        )
                );
    }

    @Test
    void testCheckoutChangesReservedCase() {
        add("a", "a");
        add("b", "b");
        commit("master first");
        assertThatThrownBy(
                () -> checkoutChanges("HEAD")
        ).isInstanceOf(GitRepositoryException.class);
    }

    @Test
    void testCheckoutChangesSameCommitCase1() {
        add("a", "a");
        add("b", "b");
        commit("master first");
        assertThatThrownBy(
                () -> checkoutChanges(gitRepository.readCommitOrNull(ReservedRef.head).sha1().getHexString())
        ).isInstanceOf(GitRepositoryException.class)
                .hasMessageContaining("same");
    }

    @Test
    void testCheckoutChangesSameCommitCase2() {
        add("a", "a");
        add("b", "b");
        commit("master first");
        assertThatThrownBy(
                () -> checkoutChanges("master")
        ).isInstanceOf(GitRepositoryException.class)
                .hasMessageContaining("same");
    }

    @Test
    void testCheckout1() {
        add("a", "a");
        add("b", "b");
        final var commitSha = commit("1");
        branch("new");
        remove("a");
        add("c", "c");
        commit("2");
        checkout("new");
        assertThat(gitRepository.readCommitOrNull(ReservedRef.head).sha1())
                .isEqualTo(commitSha);
        assertThat(gitRepository.getIndex().getFileDescriptors())
                .isEqualTo(
                        Set.of(
                                fileDesc("a", "a"),
                                fileDesc("b", "b")
                        )
                );
    }

    @Test
    void testCheckout2() {
        add("a", "a");
        add("b", "b");
        commit("1");
        branch("new");
        remove("a");
        add("c", "c");
        add("b", "bb");
        final var lastCommitSha = commit("2");
        checkout("new");
        checkout("master");
        assertThat(gitRepository.readCommitOrNull(ReservedRef.head).sha1())
                .isEqualTo(lastCommitSha);
        assertThat(gitRepository.getIndex().getFileDescriptors())
                .isEqualTo(
                        Set.of(
                                fileDesc("c", "c"),
                                fileDesc("b", "bb")
                        )
                );
    }

    @Test
    void testReadFile() {
        add("a", "a");
        add("b", "b");
        commit("1");
        branch("new");
        add("b", "bb");
        assertThat(readFile("new", "b"))
                .isEqualTo("b");
        commit("2");
        assertThat(readFile("HEAD", "b"))
                .isEqualTo("bb");
    }

    @Test
    void testStatusNoCommitAndEmptyStage() {
        assertThat(gitRepository.status())
                .returns(emptyList(), GitRepoStatus::getUncommittedStagedChanges)
                .returns(null, GitRepoStatus::getMergeConflicts);
        ;
    }

    @Test
    void testStatusNoCommitAndNonEmptyStage() {
        add("a", "a");
        assertThat(gitRepository.status())
                .returns(
                        List.of(new Addition(fileDesc("a", "a"))),
                        GitRepoStatus::getUncommittedStagedChanges
                ).returns(null, GitRepoStatus::getMergeConflicts);
    }

    @Test
    void testStatusCommitCase() {
        add("a", "a");
        add("b", "b");
        commit("1");
        add("c", "c");
        remove("a");
        add("b", "bb");
        assertThat(gitRepository.status())
                .returns(null, GitRepoStatus::getMergeConflicts)
                .extracting(repo -> Set.copyOf(repo.getUncommittedStagedChanges()))
                .isEqualTo(
                        Set.of(
                                new Addition(fileDesc("c", "c")),
                                new Deletion(fileDesc("a", "a")),
                                new Modification(fileDesc("b", "bb"), hash("b"))
                        )
                );
    }

    private void add(final String path, final String content) {
        gitRepository.add(content.getBytes(), VCSPath.create(path));
    }

    private Sha1 commit(final String message) {
        return gitRepository.commit(CommitMessage.create(message));
    }

    private void remove(final String removePath) {
        gitRepository.remove(VCSPath.create(removePath));
    }

    private void branch(final String branch) {
        gitRepository.branch(BranchName.create(branch));
    }

    private Set<VCSFileChange> checkoutChanges(final String ref) {
        return new HashSet<>(gitRepository.checkoutChanges(Ref.create(ref)));
    }

    private void checkout(final String ref) {
        gitRepository.checkout(Ref.create(ref));
    }

    private VCSFileDesc fileDesc(final String path, final String content) {
        return new VCSFileDesc(VCSPath.create(path), hash(content));
    }

    private Sha1 hash(final String content) {
        return gitRepository.hash(content.getBytes());
    }

    private String readFile(final String refString, final String path) {
        return new String(gitRepository.readFile(Ref.create(refString), VCSPath.create(path)));
    }
}
