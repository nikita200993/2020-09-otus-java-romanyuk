package ru.otus.vcs.ref;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestBranchName {

    @Test
    void testIsValidBranchNameConflictWithSha1Hex() {
        Assertions.assertThat(BranchName.isValidBranchName(DigestUtils.sha1Hex("a")))
                .isFalse();
    }

    @Test
    void testIsValidBranchNameBadChars() {
        Assertions.assertThat(BranchName.isValidBranchName("das\\df"))
                .isFalse();
    }

    @Test
    void testIsValidBranchNameReserved() {
        Assertions.assertThat(BranchName.isValidBranchName("HEAD"))
                .isFalse();
        Assertions.assertThat(BranchName.isValidBranchName("MERGE_HEAD"))
                .isFalse();
    }

    @Test
    void testIsValidBranchNameTrueCase() {
        Assertions.assertThat(BranchName.isValidBranchName("fdsf123_dasd"))
                .isTrue();
    }
}
