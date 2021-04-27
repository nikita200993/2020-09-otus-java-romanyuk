package ru.otus.vcs.index;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.index.diff.Addition;
import ru.otus.vcs.index.diff.Deletion;
import ru.otus.vcs.index.diff.Modification;
import ru.otus.vcs.path.VCSFileDesc;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class IndexTest {

    @Test
    void testEmpty() {
        final var index = Index.deserialize("".getBytes(StandardCharsets.UTF_8));
        Assertions.assertThat(index.getPathToIndexEntries())
                .isEmpty();
    }

    @Test
    void testIncorrectConflictRepeatedCodes() {
        final VCSPath path = VCSPath.create("a");
        final var lines = createLineOfIndexEntryFormat(1, path)
                + createLineOfIndexEntryFormat(2, path, "a")
                + createLineOfIndexEntryFormat(2, path, "b");
        Assertions.assertThatThrownBy(
                () -> Index.deserialize(lines.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testIncorrectConflictSameHashes() {
        final VCSPath path = VCSPath.create("a");
        final var lines = createLineOfIndexEntryFormat(1, path)
                + createLineOfIndexEntryFormat(2, path)
                + createLineOfIndexEntryFormat(3, path, "b");
        Assertions.assertThatThrownBy(
                () -> Index.deserialize(lines.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testIncorrectConflictBadCodes() {
        final VCSPath path = VCSPath.create("a");
        final var lines = createLineOfIndexEntryFormat(0, path)
                + createLineOfIndexEntryFormat(2, path, "a")
                + createLineOfIndexEntryFormat(3, path, "b");
        Assertions.assertThatThrownBy(
                () -> Index.deserialize(lines.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testConflictingContent() {
        final VCSPath path1 = VCSPath.create("a");
        final VCSPath path2 = VCSPath.create("b");
        final var lines = createLineOfIndexEntryFormat(1, path1)
                + createLineOfIndexEntryFormat(2, path1, "a")
                + createLineOfIndexEntryFormat(3, path1, "b")
                + createLineOfIndexEntryFormat(0, path2);
        final var index = Index.deserialize(lines.getBytes(StandardCharsets.UTF_8));
        Assertions.assertThat(index.hasMergeConflict()).isTrue();
        Assertions.assertThat(index.getConflictPaths())
                .hasSize(1)
                .contains(path1);
        Assertions.assertThatThrownBy(index::getPathToSha)
                .isInstanceOf(IllegalStateException.class);
        Assertions.assertThat(index.getPathToIndexEntries())
                .hasSize(2)
                .containsKeys(path1, path1);
    }

    @Test
    void testWithoutConflict() {
        final VCSPath path1 = VCSPath.create("a");
        final VCSPath path2 = VCSPath.create("b");
        final var lines = createLineOfIndexEntryFormat(0, path1) + createLineOfIndexEntryFormat(0, path2);
        final var index = Index.deserialize(lines.getBytes(StandardCharsets.UTF_8));
        Assertions.assertThat(index.hasMergeConflict()).isFalse();
        Assertions.assertThat(index.getConflictPaths()).isEmpty();
        Assertions.assertThat(index.getPathToSha())
                .hasSize(2)
                .containsEntry(path1, sha1(path1))
                .containsEntry(path2, sha1(path2));

    }

    @Test
    void testAddingIndexCaseOfSameSha() {
        final VCSPath path = VCSPath.create("a");
        final var indexEntry = new IndexEntry(Stage.normal, path, sha1(path));
        final Index index = Index.create(List.of(indexEntry));
        final Index newIndex = index.withNewIndexEntry(path, sha1(path));
        Assertions.assertThat(newIndex.getPathToIndexEntries())
                .isEqualTo(index.getPathToIndexEntries());
    }

    @Test
    void testAddingIndexCaseOfDifferentSha() {
        final VCSPath path = VCSPath.create("a");
        final var indexEntry = new IndexEntry(Stage.normal, path, sha1(path));
        final Index index = Index.create(List.of(indexEntry));
        final Index newIndex = index.withNewIndexEntry(path, sha1(path, "a"));
        Assertions.assertThat(newIndex.getPathToIndexEntries())
                .isNotEqualTo(index.getPathToIndexEntries())
                .hasSize(1)
                .containsEntry(
                        path,
                        List.of(
                                new IndexEntry(Stage.normal, path, sha1(path, "a"))
                        )
                );
    }

    @Test
    void testAddingIndexCaseOfDifferentEntry() {
        final VCSPath path1 = VCSPath.create("a");
        final VCSPath path2 = VCSPath.create("b");
        final var indexEntry = new IndexEntry(Stage.normal, path1, sha1(path1));
        final Index index = Index.create(List.of(indexEntry));
        final Index newIndex = index.withNewIndexEntry(path2, sha1(path2));
        Assertions.assertThat(newIndex.getPathToIndexEntries())
                .isNotEqualTo(index.getPathToIndexEntries())
                .hasSize(2)
                .containsEntry(
                        path2,
                        List.of(
                                new IndexEntry(Stage.normal, path2, sha1(path2))
                        )
                );
    }

    @Test
    void testRemoveCaseOfAbsentPath() {
        final VCSPath path1 = VCSPath.create("a");
        final VCSPath path2 = VCSPath.create("b");
        Assertions.assertThat(
                Index.create(
                        List.of(IndexEntry.newNormalEntry(path1, sha1(path1))
                        )
                ).withRemovedIndexEntry(path2)
        ).isNull();
    }

    @Test
    void testRemoveCaseOfExistentPath1() {
        final VCSPath path = VCSPath.create("a");
        Assertions.assertThat(
                Index.create(
                        List.of(IndexEntry.newNormalEntry(path, sha1(path)))
                ).withRemovedIndexEntry(path)
        ).returns(true, Index::isEmpty);
    }

    @Test
    void testRemoveCaseOfExistentPath2() {
        final VCSPath path1 = VCSPath.create("a");
        Assertions.assertThat(
                Index.create(
                        List.of(
                                new IndexEntry(Stage.receiver, path1, sha1(path1)),
                                new IndexEntry(Stage.giver, path1, sha1(path1, "a"))
                        )
                ).withRemovedIndexEntry(path1)
        ).returns(true, Index::isEmpty);
    }

    @Test
    void testCreateIndexFromPath(@TempDir final Path temp) throws IOException {
        Files.createSymbolicLink(temp.resolve("sym"), temp);
        Files.createDirectory(temp.resolve("dir1"));
        Files.createDirectory(temp.resolve("dir2"));
        Files.writeString(temp.resolve("file1"), "a");
        Files.writeString(temp.resolve("dir2").resolve("file2"), "b");
        final var index = Index.create(temp, Sha1::hash);
        final var expectedFileDescriptors = Set.of(
                new VCSFileDesc(VCSPath.create("file1"), Sha1.hash("a".getBytes())),
                new VCSFileDesc(VCSPath.create("dir2/file2"), Sha1.hash("b"))
        );
        Assertions.assertThat(index.getFileDescriptors())
                .isEqualTo(expectedFileDescriptors);
    }

    @Test
    void testDiff() {
        final VCSPath added = VCSPath.create("added");
        final Sha1 addedHash = Sha1.hash("added");
        final VCSPath modified = VCSPath.create("modified");
        final Sha1 modifiedNew = Sha1.hash("new");
        final Sha1 modifiedOld = Sha1.hash("old");
        final VCSPath removed = VCSPath.create("removed");
        final Sha1 removedSha = Sha1.hash("removed");
        final var indexOne = Index.create(
                List.of(
                        IndexEntry.newNormalEntry(added, addedHash),
                        IndexEntry.newNormalEntry(modified, modifiedNew)
                )
        );
        final var indexTwo = Index.create(
                List.of(
                        IndexEntry.newNormalEntry(removed, removedSha),
                        IndexEntry.newNormalEntry(modified, modifiedOld)
                )
        );
        Assertions.assertThat(indexOne.getDiff(indexTwo))
                .containsExactlyInAnyOrderElementsOf(
                        List.of(
                                new Addition(new VCSFileDesc(added, addedHash)),
                                new Deletion(new VCSFileDesc(removed, removedSha)),
                                new Modification(new VCSFileDesc(modified, modifiedNew), modifiedOld)
                        )
                );
    }

    private static String createLineOfIndexEntryFormat(final int code, final VCSPath path, final String salt) {
        return code + " " + path + " " + sha1(path, salt).getHexString() + "\n";
    }

    private static String createLineOfIndexEntryFormat(final int code, final VCSPath path) {
        return createLineOfIndexEntryFormat(code, path, "");
    }

    private static Sha1 sha1(final VCSPath vcsPath) {
        return Sha1.hash(vcsPath.toString());
    }

    private static Sha1 sha1(final VCSPath vcsPath, final String salt) {
        return Sha1.hash(vcsPath.toString() + salt);
    }
}
