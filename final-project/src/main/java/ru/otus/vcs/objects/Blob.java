package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static ru.otus.vcs.utils.Utils.concat;

public class Blob extends GitObject {

    public static final String type = "blob";

    private final byte[] content;

    public Blob(final byte[] content) {
        Contracts.requireNonNullArgument(content);
        this.content = Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] serialize() {
        final int size = content.length;
        final byte[] firstPart = (type +
                ' ' +
                size +
                (char) 0).getBytes(StandardCharsets.UTF_8);
        return concat(firstPart, content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blob blob = (Blob) o;
        return Arrays.equals(content, blob.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }
}
