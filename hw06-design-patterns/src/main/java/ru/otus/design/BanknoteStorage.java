package ru.otus.design;

import java.util.NoSuchElementException;

public interface BanknoteStorage {

    int banknoteAmount(Nominal nominal);

    void put(Banknote banknote);

    Banknote take(final Nominal nominal) throws NoSuchElementException;
}
