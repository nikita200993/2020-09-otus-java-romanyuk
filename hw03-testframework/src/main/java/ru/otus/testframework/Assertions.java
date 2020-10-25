package ru.otus.testframework;

import java.util.Objects;

public class Assertions {

    public static void assertEquals(
            final Object expected,
            final Object actual)
    {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionFailure(
                    String.format(
                            "Objects are not equals:%nExpected = %s%nActual = %s",
                            expected,
                            actual
                    )
            );
        }
    }
}
