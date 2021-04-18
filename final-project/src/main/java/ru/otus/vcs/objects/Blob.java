package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;

import java.util.Arrays;

public class Blob extends GitObject {

    public static final String type = "blob";

    private final byte[] content;

    public Blob(final byte[] content) {
        Contracts.requireNonNullArgument(content);
        this.content = Arrays.copyOf(content, content.length);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public byte[] serializeContent() {
        return content.clone();
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
