package ru.otus.utils;

public class Contracts {

    private Contracts()
    {
        throw new IllegalAccessError();
    }

    public static void requireNonNullArgument(final Object argument)
    {
        if (argument == null) {
            throw new IllegalStateException();
        }
    }

    public static void requireThat(final boolean predicate, final String message)
    {
        Contracts.requireNonNullArgument(message);

        if (!predicate) {
            throw new IllegalStateException(message);
        }
    }

    public static void requireThat(final boolean predicate)
    {
        requireThat(predicate, "Predicate doesn't hold");
    }

    public static void forbidThat(final boolean predicate)
    {
        requireThat(!predicate, "Prohibition doesn't hold");
    }

    public static IllegalStateException unreachable() {
        return new IllegalStateException("Unreachable code is reached o_o");
    }
}
