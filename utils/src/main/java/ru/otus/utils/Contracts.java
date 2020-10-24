package ru.otus.utils;

public class Contracts {

    private Contracts() {
        throw new IllegalAccessError();
    }

    public static void requireNonNullArgument(final Object argument) {
        if (argument == null) {
            throw new IllegalStateException();
        }
    }
}
