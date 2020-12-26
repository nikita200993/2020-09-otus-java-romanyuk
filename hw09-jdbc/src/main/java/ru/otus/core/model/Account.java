package ru.otus.core.model;

import ru.otus.core.annotation.Id;
import ru.otus.utils.Contracts;

public class Account {

    @Id
    private final String no;
    private final String type;
    private final float rest;

    public Account(final String no, final String type, final float rest) {
        this.no = Contracts.ensureNonNullArgument(no);
        this.type = Contracts.ensureNonNullArgument(type);
        this.rest = rest;
    }

    public String getNo() {
        return no;
    }

    public String getType() {
        return type;
    }

    public float getRest() {
        return rest;
    }

    @Override
    public String toString() {
        return "Account{" +
                "no='" + no + '\'' +
                ", type='" + type + '\'' +
                ", rest=" + rest +
                '}';
    }
}
