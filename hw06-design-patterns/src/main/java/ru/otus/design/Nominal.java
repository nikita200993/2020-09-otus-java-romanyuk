package ru.otus.design;

import javax.annotation.Nullable;

public enum Nominal {

    ONE_HUNDRED(100),
    TWO_HUNDRED(200),
    FIVE_HUNDRED(500),
    ONE_THOUSAND(1000),
    FIVE_THOUSAND(5000);

    private final int value;

    Nominal(final int value) {
        this.value = value;
    }

    @Nullable
    public Nominal next() {
        return switch (this) {
            case ONE_HUNDRED -> TWO_HUNDRED;
            case TWO_HUNDRED -> FIVE_HUNDRED;
            case FIVE_HUNDRED -> ONE_THOUSAND;
            case ONE_THOUSAND -> FIVE_THOUSAND;
            case FIVE_THOUSAND -> null;
        };
    }

    @Nullable
    public Nominal prev() {
        return switch (this) {
            case ONE_HUNDRED -> null;
            case TWO_HUNDRED -> ONE_HUNDRED;
            case FIVE_HUNDRED -> TWO_HUNDRED;
            case ONE_THOUSAND -> FIVE_HUNDRED;
            case FIVE_THOUSAND -> ONE_THOUSAND;
        };
    }

    public static Nominal minimum() {
        return ONE_HUNDRED;
    }

    public static Nominal maximum() {
        return FIVE_THOUSAND;
    }

    public int toInt() {
        return value;
    }
}
