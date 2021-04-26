package ru.otus.vcs.exception;

import ru.otus.utils.Contracts;

public class GitException extends RuntimeException {

    public GitException(final String message) {
        super(Contracts.ensureNonNullArgument(message));
    }

    public GitException(final String message, final Throwable cause) {
        super(Contracts.ensureNonNullArgument(message), Contracts.ensureNonNullArgument(cause));
    }

    public String toUserMessage() {
        final var builder = new StringBuilder();
        builder.append("Error: ")
                .append(getMessage())
                .append(System.lineSeparator());
        Throwable currentEx = this.getCause();
        while (currentEx instanceof GitException) {
            builder.append("Reason: ")
                    .append(currentEx.getMessage())
                    .append(System.lineSeparator());
            currentEx = currentEx.getCause();
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
