package ru.otus.design;

import ru.otus.utils.Contracts;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class BanknoteStorageImpl implements BanknoteStorage {

    private final Map<Nominal, Integer> banknoteCells;
    private final Function<Nominal, Banknote> banknoteFactory;

    public BanknoteStorageImpl(
            final Map<Nominal, Integer> banknoteCells,
            final Function<Nominal, Banknote> banknoteFactory) {
        Contracts.requireNonNullArgument(banknoteCells);
        Contracts.requireNonNullArgument(banknoteFactory);

        this.banknoteCells = initMap(
                new EnumMap<>(
                        ensureAmountsAreValid(banknoteCells)
                )
        );
        this.banknoteFactory = banknoteFactory;
    }


    @Override
    public int banknoteAmount(final Nominal nominal) {
        return banknoteCells.get(nominal);
    }

    @Override
    public void put(final Banknote banknote) {
        Contracts.requireNonNullArgument(banknote);

        increment(banknote.nominal());
    }

    @Override
    public Banknote take(final Nominal nominal) throws NoSuchElementException {
        Contracts.requireNonNullArgument(nominal);

        if (banknoteAmount(nominal) == 0) {
            throw new NoSuchElementException("No banknotes for nominal " + nominal.toInt());
        }
        decrement(nominal);
        return banknoteFactory.apply(nominal);
    }

    private Map<Nominal, Integer> ensureAmountsAreValid(
            final Map<Nominal, Integer> bunchOfBanknotes) {
        final boolean invalidAmountMet = bunchOfBanknotes.values().stream()
                .anyMatch(amount -> amount == null || amount < 0);
        Contracts.forbidThat(invalidAmountMet);
        return bunchOfBanknotes;
    }

    private Map<Nominal, Integer> initMap(final Map<Nominal, Integer> bunchOfBanknotes) {
        Arrays.stream(Nominal.values())
                .forEach(nominal -> bunchOfBanknotes.putIfAbsent(nominal, 0));
        return bunchOfBanknotes;
    }

    private void increment(final Nominal nominal) {
        banknoteCells.compute(nominal, (unused, amount) -> amount + 1);
    }

    private void decrement(final Nominal nominal) {
        banknoteCells.compute(nominal, (unused, amount) -> amount - 1);
    }
}
