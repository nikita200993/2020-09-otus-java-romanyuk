package ru.otus.vcs.objects;

import ru.otus.vcs.exception.InnerException;

import java.nio.charset.StandardCharsets;

public abstract class GitObject {

    public static GitObject deserialize(final byte[] bytes) throws DeserializationException {
        final String asciiString = new String(bytes, StandardCharsets.US_ASCII);
        throw new UnsupportedOperationException();
    }

    public abstract byte[] serialize();

    public static class DeserializationException extends InnerException {

        public DeserializationException(final String message) {
            super(message);
        }
    }
}
