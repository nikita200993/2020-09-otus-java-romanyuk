package ru.otus.design;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;

public class BanknoteStorageImplTest {

    private final BanknoteStorage storage = new BanknoteStorageImpl(
            Map.of(
                    Nominal.ONE_HUNDRED, 10,
                    Nominal.FIVE_HUNDRED, 5,
                    Nominal.FIVE_THOUSAND, 2
            ),
            BanknoteImpl::new
    );

    @Test
    public void testBanknotesAmount() {
        Assertions.assertEquals(0, storage.banknoteAmount(Nominal.TWO_HUNDRED));
        Assertions.assertEquals(5, storage.banknoteAmount(Nominal.FIVE_HUNDRED));
    }

    @Test
    public void testPut() {
        storage.put(new BanknoteImpl(Nominal.TWO_HUNDRED));
        Assertions.assertEquals(1, storage.banknoteAmount(Nominal.TWO_HUNDRED));
    }

    @Test
    public void testTake_NoSuchElementException() {
        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> storage.take(Nominal.TWO_HUNDRED));
    }

    @Test
    public void testTake_NormalCase() {
        final Nominal nominal = Nominal.ONE_HUNDRED;
        final int banknotesAmountBefore = storage.banknoteAmount(nominal);
        final Banknote banknote = storage.take(nominal);
        Assertions.assertEquals(nominal, banknote.nominal());
        Assertions.assertEquals(banknotesAmountBefore - 1, storage.banknoteAmount(nominal));
    }
}
