package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;

public class CommitMessage {

    private final String message;

    private CommitMessage(final String message) {
        this.message = message;
    }

    public static boolean isValidMessage(final String message) {
        Contracts.requireNonNullArgument(message);

        return !message.isBlank();
    }

    public static CommitMessage create(final String message) {
        Contracts.requireNonNullArgument(message);
        Contracts.requireThat(isValidMessage(message));

        return new CommitMessage(message);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitMessage that = (CommitMessage) o;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }
}
