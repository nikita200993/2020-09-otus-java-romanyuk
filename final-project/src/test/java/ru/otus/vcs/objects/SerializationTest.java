package ru.otus.vcs.objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializationTest {

    @Test
    void testBlobSerialization() {
        final var blob = new Blob("Привет!");
        Assertions.assertThat(GitObject.deserialize(blob.serialize()))
                .isEqualTo(blob);
    }
}
