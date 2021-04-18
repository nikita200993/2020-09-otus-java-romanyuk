package ru.otus.vcs.utils;

import ru.otus.utils.Contracts;

public class Tuple2<A, B> {
    private final A a;
    private final B b;

    public Tuple2(final A a, final B b) {
        this.a = Contracts.ensureNonNullArgument(a);
        this.b = Contracts.ensureNonNullArgument(b);
    }

    public A first() {
        return a;
    }

    public B second() {
        return b;
    }
}
