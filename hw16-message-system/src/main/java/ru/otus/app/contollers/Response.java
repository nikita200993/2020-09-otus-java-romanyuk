package ru.otus.app.contollers;

import ru.otus.utils.Contracts;

import javax.annotation.Nullable;

public class Response {

    private final String status;
    @Nullable
    private final Object result;

    public Response(final String status, final @Nullable Object result) {
        this.status = Contracts.ensureNonNullArgument(status);
        this.result = result;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <V> V getResult() {
        return (V) result;
    }
}
