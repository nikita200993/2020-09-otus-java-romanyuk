package ru.otus.vcs.ref;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestReservedRef {

    @Test
    void testIsValidReservedNameFalseCase1() {
        Assertions.assertThat(ReservedRef.isValidReservedRefString("adas"))
                .isFalse();
    }

    @Test
    void testIsValidReservedNameFalseCase2() {
        Assertions.assertThat(ReservedRef.isValidReservedRefString("HeAD"))
                .isFalse();
    }

    @Test
    void testIsValidReservedNameHeadCase() {
        Assertions.assertThat(ReservedRef.isValidReservedRefString("HEAD"))
                .isTrue();
    }

    @Test
    void testIsValidReservedNameHeadCase2() {
        Assertions.assertThat(ReservedRef.isValidReservedRefString("MERGE_HEAD"))
                .isTrue();
    }
}
