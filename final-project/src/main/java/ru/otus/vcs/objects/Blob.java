package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;

public class Blob extends GitObject {

    public static final String type = "blob";

    private final String content;

    public Blob(final String content) {
        this.content = Contracts.ensureNonNullArgument(content);
    }

    @Override
    public byte[] serialize() {
        final int size = content.getBytes().length;
        return (type +
                ' ' +
                size +
                (char) 0 +
                content)
                .getBytes();
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blob blob = (Blob) o;
        return content.equals(blob.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }
}
