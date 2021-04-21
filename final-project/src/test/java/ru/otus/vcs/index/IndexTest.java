package ru.otus.vcs.index;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.vcs.exception.DeserializationException;
import ru.otus.vcs.naming.VCSPath;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
        ).isInstanceOf(DeserializationException.class);
    }

    @Test
    void testIncorrectConflictSameHashes() {
        final VCSPath path = VCSPath.create("a");
        final var lines = createLineOfIndexEntryFormat(1, path)
                + createLineOfIndexEntryFormat(2, path)
                + createLineOfIndexEntryFormat(3, path, "b");
        Assertions.assertThatThrownBy(
                () -> Index.deserialize(lines.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(DeserializationException.class);
    }

    @Test
    void testIncorrectConflictBadCodes() {
        final VCSPath path = VCSPath.create("a");
        final var lines = createLineOfIndexEntryFormat(0, path)
                + createLineOfIndexEntryFormat(2, path, "a")
                + createLineOfIndexEntryFormat(3, path, "b");
        Assertions.assertThatThrownBy(
                () -> Index.deserialize(lines.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(DeserializationException.class);
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

    private static String createLineOfIndexEntryFormat(final int code, final VCSPath path, final String salt) {
        return code + " " + path + " " + sha1(path, salt) + "\n";
    }

    private static String createLineOfIndexEntryFormat(final int code, final VCSPath path) {
        return createLineOfIndexEntryFormat(code, path, "");
    }

    private static String sha1(final VCSPath vcsPath) {
        return DigestUtils.sha1Hex(vcsPath.toString());
    }

    private static String sha1(final VCSPath vcsPath, final String salt) {
        return DigestUtils.sha1Hex(vcsPath.toString() + salt);
    }
}
