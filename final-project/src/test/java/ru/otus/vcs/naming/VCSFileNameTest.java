package ru.otus.vcs.naming;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ru.otus.vcs.naming.VCSFileName.isValidVCSFileName;

public class VCSFileNameTest {

    @Test
    void testEmpty() {
        Assertions.assertThat(isValidVCSFileName("")).isFalse();
    }

    @Test
    void testCurrentDir() {
        Assertions.assertThat(isValidVCSFileName(".")).isFalse();
    }

    @Test
    void testParentDir() {
        Assertions.assertThat(isValidVCSFileName("..")).isFalse();
    }

    @Test
    void testNormalName() {
        Assertions.assertThat(isValidVCSFileName("a.jar")).isTrue();
    }

    @Test
    void testBadName() {
        Assertions.assertThat(isValidVCSFileName("выа")).isFalse();
    }
}
