package ru.otus.design;

public class OutOfBanknotes extends RuntimeException {

    public OutOfBanknotes(final int requestedAmount) {
        super("Not enough banknotes for requested amount: " + requestedAmount);
    }
}
