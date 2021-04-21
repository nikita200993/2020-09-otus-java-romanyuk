package ru.otus.vcs.utils;

import ru.otus.utils.Contracts;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return a.equals(tuple2.a) && b.equals(tuple2.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
