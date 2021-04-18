package ru.otus.vcs.objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
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
}
