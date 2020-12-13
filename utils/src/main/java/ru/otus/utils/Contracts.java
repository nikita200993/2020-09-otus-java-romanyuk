package ru.otus.utils;

public class Contracts {

    private Contracts() {
        throw new IllegalAccessError();
    }

    public static void requireNonNullArgument(final Object argument) {
        requireNonNull(argument, "Null arguments are forbidden");
    }

    public static void requireNonNull(final Object object) {
        requireNonNull(object, "Null values are forbidden");
    }

    public static <T> T ensureNonNull(final T object) {
        requireNonNull(object);
        return object;
    }

    public static void requireNonNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalStateException(message);
        }
    }

    public static void requireThat(final boolean predicate, final String message) {
        Contracts.requireNonNullArgument(message);

        if (!predicate) {
            throw new IllegalStateException(message);
        }
    }

    public static void requireThat(final boolean predicate) {
        requireThat(predicate, "Predicate doesn't hold");
    }

    public static void forbidThat(final boolean predicate) {
        requireThat(!predicate, "Prohibition doesn't hold");
    }

    public static IllegalStateException unreachable() {
        return new IllegalStateException("Unreachable code is reached o_o");
    }

    public static IllegalStateException unreachable(final String message) {
        return new IllegalStateException(message);
    }
}
