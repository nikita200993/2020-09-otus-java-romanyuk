package ru.otus.vcs.objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.vcs.ref.Sha1;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class SerializationTest {

    @Test
    void testBlobSerialization() {
        final var blob = new Blob("Привет!".getBytes(StandardCharsets.UTF_8));
        Assertions.assertThat(GitObject.deserialize(blob.serialize()))
                .isEqualTo(blob);
    }

    @Test
    void testTreeLeafSerialization() {
        final var leaf = new TreeLeaf(
                FileMode.REGULAR,
                Path.of("файл"),
                DigestUtils.sha1Hex("привет")
        );
        Assertions.assertThat(TreeLeaf.deserialize(leaf.serialize()))
                .isEqualTo(leaf);
    }

    @Test
    void testTreeSerialization() {
        final var first = new TreeLeaf(
                FileMode.REGULAR,
                Path.of("файл"),
                DigestUtils.sha1Hex("привет")
        );
        final var second = new TreeLeaf(
                FileMode.SYMLINK,
                Path.of("симвссылка"),
                DigestUtils.sha1Hex("файл")
        );
        final var tree = new Tree(List.of(first, second));
        Assertions.assertThat(GitObject.deserialize(tree.serialize()))
                .isEqualTo(tree);
    }

    @Test
    void testCommitSerialization() {
        final var commit = new Commit(Sha1.hash("a"), Sha1.hash("b"), null, "adsa", "fasdf");

        Assertions.assertThat(GitObject.deserialize(commit.serialize()))
                .isEqualTo(commit);
    }

    @Test
    void testCommitSerializationNoBlankLine() {
        final var dataWithNoBlankLine = "tree " + Sha1.hash("a") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "author vasya\n"
                + "message\n";

        Assertions.assertThatThrownBy(
                () -> Commit.deserialize(dataWithNoBlankLine.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testCommitSerializationMoreThanTwoParents() {
        final var manyParents = "tree " + Sha1.hash("a") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "author vasya\n"
                + "\n"
                + "message\n";

        Assertions.assertThatThrownBy(
                () -> Commit.deserialize(manyParents.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testCommitSerializationNoTree() {
        final var noTree = "parent " + Sha1.hash("b") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "author vasya\n"
                + "\n"
                + "message\n";

        Assertions.assertThatThrownBy(
                () -> Commit.deserialize(noTree.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testCommitSerializationNoAuthor() {
        final var noTree = "tree " + Sha1.hash("a") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "parent " + Sha1.hash("b") + "\n"
                + "\n"
                + "message\n";
        Assertions.assertThatThrownBy(
                () -> Commit.deserialize(noTree.getBytes(StandardCharsets.UTF_8))
        ).isInstanceOf(IllegalStateException.class);
    }

}
