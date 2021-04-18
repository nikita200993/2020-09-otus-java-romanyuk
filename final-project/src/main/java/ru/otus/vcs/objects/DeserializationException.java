package ru.otus.vcs.objects;

import ru.otus.vcs.exception.InnerException;

public class DeserializationException extends InnerException {

    public DeserializationException(final String message) {
        super(message);
    }

    public DeserializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
