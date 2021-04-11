package ru.otus.vcs.exception;

public class InnerException extends GitException {

    public InnerException(final String message) {
        super(message);
    }

    public InnerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
