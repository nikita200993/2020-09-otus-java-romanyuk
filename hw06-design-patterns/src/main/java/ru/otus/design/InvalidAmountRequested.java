package ru.otus.design;

import ru.otus.utils.Contracts;

public class InvalidAmountRequested extends RuntimeException {

    public InvalidAmountRequested(final String message) {
        super(message);
        Contracts.requireNonNullArgument(message);
    }
}
