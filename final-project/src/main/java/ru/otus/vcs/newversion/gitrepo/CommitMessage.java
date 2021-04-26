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

}
