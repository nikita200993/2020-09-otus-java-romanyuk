package ru.otus.design;

import ru.otus.utils.Contracts;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AtmMachineImpl implements AtmMachine {

    private final BanknoteStorage storage;

    public AtmMachineImpl(final BanknoteStorage storage) {
        Contracts.requireNonNullArgument(storage);

        this.storage = storage;
    }

    @Override
    public int balance() {
        int balance = 0;
        for (final Nominal nominal : Nominal.values()) {
            balance += storage.banknoteAmount(nominal) * nominal.toInt();
        }
        return balance;
    }

    @Override
    public List<Banknote> withdraw(final int requestedAmount)
            throws InvalidAmountRequested, OutOfBanknotes {
        checkAmountRequested(requestedAmount);
        final var result = new ArrayList<Banknote>();
        Nominal currentNominal = Nominal.maximum();
        int remainedAmount = requestedAmount;
        while (currentNominal != null && remainedAmount != 0) {
            final Banknote banknote = getNextBanknoteIfPossible(currentNominal, remainedAmount);
            if (banknote == null) {
                currentNominal = currentNominal.prev();
            } else {
                result.add(banknote);
                remainedAmount -= currentNominal.toInt();
            }
        }
        if (remainedAmount > 0) {
            result.forEach(storage::put);
            throw new OutOfBanknotes(requestedAmount);
        }
        return result;
    }

    @Override
    public void replenish(final List<Banknote> banknotes) {
        Contracts.requireNonNullArgument(banknotes);

        banknotes.forEach(storage::put);
    }

    @Nullable
    private Banknote getNextBanknoteIfPossible(final Nominal nominal, final int remainedAmount) {
        if (nominal.toInt() > remainedAmount) {
            return null;
        } else if (storage.banknoteAmount(nominal) == 0) {
            return null;
        } else {
            return storage.take(nominal);
        }
    }

    private void checkAmountRequested(final int amount) {
        final Nominal minNominal = Nominal.minimum();
        if (amount < 0 || amount % minNominal.toInt() != 0) {
            throw new InvalidAmountRequested("Amount requested " + amount
                    + " is not multiple of " + minNominal.toInt());
        }
    }
}
