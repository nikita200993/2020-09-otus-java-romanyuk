package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.InnerException;

public abstract class GitObject {

    public static GitObject deserialize(final byte[] bytes) throws DeserializationException {
        Contracts.requireNonNullArgument(bytes);

        final String content = new String(bytes);
        final int firstWs = content.indexOf(' ');
        if (firstWs == -1) {
            throw badFormat("Can read type of object.");
        }
        final String type = content.substring(0, firstWs);
        final int endOfSize = content.indexOf(0);
        if (endOfSize == -1 || firstWs + 1 >= endOfSize) {
            throw badFormat("Can't find size part of content.");
        }
        final int size = parseSize(content.substring(firstWs + 1, endOfSize));
        if (size != bytes.length - endOfSize - 1) {
            throw badFormat("Size read from byte array is not equal to actual size");
        }
        final String objectContent = content.substring(endOfSize + 1);
        switch (type) {
            case Blob.type:
                return new Blob(objectContent);
            default:
                throw badFormat("Not a git object type " + type);
        }
    }

    public abstract byte[] serialize();

    private static int parseSize(final String size) {
        try {
            return Integer.parseInt(size);
        } catch (final NumberFormatException ex) {
            throw badFormat("Can't parse size.", ex);
        }
    }

    private static DeserializationException badFormat(final String additionalInfo) {
        return new DeserializationException("Bad format of content. " + additionalInfo);
    }

    private static DeserializationException badFormat(final String additionalInfo, final Throwable cause) {
        return new DeserializationException("Bad format of content. " + additionalInfo, cause);
    }

    public static class DeserializationException extends InnerException {

        public DeserializationException(final String message) {
            super(message);
        }

        public DeserializationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
