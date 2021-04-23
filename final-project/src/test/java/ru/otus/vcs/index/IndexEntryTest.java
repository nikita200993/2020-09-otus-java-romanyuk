package ru.otus.vcs.index;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

public class IndexEntryTest {

    private static final String sha1 = DigestUtils.sha1Hex("dasd");

    @Test
    void testEmptyLine() {
        Assertions.assertThatThrownBy(
                () -> IndexEntry.fromLineContent("")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testLineWithoutWhiteSpace() {
        Assertions.assertThatThrownBy(
                () -> IndexEntry.fromLineContent("1fdsf" + sha1)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testLineWithOneWhiteSpace() {
        Assertions.assertThatThrownBy(
                () -> IndexEntry.fromLineContent("1fdsf " + sha1)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testLineWithBadStageCode() {
        Assertions.assertThatThrownBy(
                () -> IndexEntry.fromLineContent("-1 fdsf " + sha1)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testCorrectLine() {
        final var path = "file.txt";
        final int code = 0;
        final var indexEntry = IndexEntry.fromLineContent(code + " " + path + " " + sha1);
        Assertions.assertThat(indexEntry.getPath())
                .isEqualTo(VCSPath.create(path));
        Assertions.assertThat(indexEntry.getStage().getCode())
                .isEqualTo(code);
        Assertions.assertThat(indexEntry.getSha())
                .isEqualTo(Sha1.create(sha1));
    }

    @Test
    void testDeserializationAndSerialization() {
        final var line = "1 abc/ddd/z..txt " + sha1;
        Assertions.assertThat(IndexEntry.fromLineContent(line).toLineContent())
                .isEqualTo(line);
    }
}
