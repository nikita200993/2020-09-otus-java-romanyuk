package ru.otus.design;

import java.util.List;

public interface AtmMachine {

    int balance();

    List<Banknote> withdraw(int amount) throws InvalidAmountRequested, OutOfBanknotes;

    void replenish(List<Banknote> banknotes);
}
