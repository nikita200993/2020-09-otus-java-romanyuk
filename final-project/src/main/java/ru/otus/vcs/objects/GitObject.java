package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.InnerException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static ru.otus.vcs.utils.Utils.indexOf;
import static ru.otus.vcs.utils.Utils.utf8;

public abstract class GitObject {

    public static GitObject deserialize(final byte[] bytes) throws DeserializationException {
        Contracts.requireNonNullArgument(bytes);

        final int firstWsPos = indexOf(bytes, (byte) ' ', 0, bytes.length);
        if (firstWsPos == -1) {
            throw badFormat("Can read type of object.");
        }
        final String type = utf8(Arrays.copyOfRange(bytes, 0, firstWsPos));
        final int nullBytePos = indexOf(bytes, (byte) 0, firstWsPos + 1, bytes.length);
        if (nullBytePos == -1 || firstWsPos + 1 >= nullBytePos) {
            throw badFormat("Can't find size part of content.");
        }
        final int size = parseSize(utf8(Arrays.copyOfRange(bytes, firstWsPos + 1, nullBytePos)));
        if (size != bytes.length - nullBytePos - 1) {
            throw badFormat("Size read from byte array is not equal to actual size");
        }
        final byte[] content = Arrays.copyOfRange(bytes, nullBytePos + 1, bytes.length);
        switch (type) {
            case Blob.type:
                return new Blob(content);
            case Tree.type:
                return Tree.deserialize(content);
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
