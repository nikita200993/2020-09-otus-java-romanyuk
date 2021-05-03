package ru.otus.vcs.newversion.ref;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSha1 {

    @Test
    void testIsValidSha1StringEmptyCase() {
        Assertions.assertThat(Sha1.isValidSha1HexString(""))
                .isFalse();
    }

    @Test
    void testIsValidSha1StringInvalidLength() {
        Assertions.assertThat(Sha1.isValidSha1HexString("ad333"))
                .isFalse();
    }

    @Test
    void testIsValidSha1StringInvalidChar() {
        final var sha = DigestUtils.sha1Hex("a");
        final var chars = new char[40];
        sha.getChars(0, sha.length(), chars, 0);
        chars[3] = ';';
        Assertions.assertThat(Sha1.isValidSha1HexString(new String(chars)))
                .isFalse();
    }

    @Test
    void testIsValidSha1StringPositiveCase() {
        Assertions.assertThat(Sha1.isValidSha1HexString(DigestUtils.sha1Hex("a")))
                .isTrue();
    }

    @Test
    void testShaComputation() {
        Assertions.assertThat(Sha1.hash("abc").getHexString())
                .isEqualTo(DigestUtils.sha1Hex("abc"));
    }
}



