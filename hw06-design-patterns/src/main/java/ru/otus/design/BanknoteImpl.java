package ru.otus.design;

import ru.otus.utils.Contracts;

class BanknoteImpl implements Banknote {

    private final Nominal nominal;

    public BanknoteImpl(final Nominal nominal) {
        Contracts.requireNonNullArgument(nominal);

        this.nominal = nominal;
    }

    @Override
    public Nominal nominal() {
        return nominal;
    }
}
