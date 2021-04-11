package ru.otus.vcs.exception;

import ru.otus.utils.Contracts;

public class GitException extends RuntimeException {

    public GitException(final String message) {
        super(Contracts.ensureNonNullArgument(message));
    }

    public GitException(final String message, final Throwable cause) {
        super(Contracts.ensureNonNullArgument(message), Contracts.ensureNonNullArgument(cause));
    }
}
