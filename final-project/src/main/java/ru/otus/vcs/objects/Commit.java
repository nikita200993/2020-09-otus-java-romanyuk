package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;

import java.util.Map;

public class Commit extends GitObject {

    private final Map<String, String> keyValues;
    private final String message;

    public Commit(final Map<String, String> keyValues, final String message) {
        Contracts.requireNonNullArgument(keyValues);
        Contracts.requireNonNullArgument(message);

        this.keyValues = Map.copyOf(keyValues);
        this.message = message;
    }

    @Override
    public byte[] serialize() {
        throw new UnsupportedOperationException();
    }

    public String getMessage() {
        return message;
    }

    public String getTreeSha() {
        return keyValues.get(Tree.type);
    }
}
