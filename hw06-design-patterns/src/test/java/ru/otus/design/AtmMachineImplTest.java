package ru.otus.design;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class AtmMachineImplTest {

    private final BanknoteStorage storage = new BanknoteStorageImpl(
            Map.of(
                    Nominal.ONE_HUNDRED, 5,
                    Nominal.FIVE_HUNDRED, 2,
                    Nominal.FIVE_THOUSAND, 2
            ),
            BanknoteImpl::new
    );
    private final AtmMachine atmMachine = new AtmMachineImpl(storage);
    private final int initialBalance = 11_500;

    @Test
    public void testBalance() {
        Assertions.assertEquals(initialBalance, atmMachine.balance());
    }

    @Test
    public void testReplenish() {
        atmMachine.replenish(List.of(new BanknoteImpl(Nominal.TWO_HUNDRED)));
        Assertions.assertEquals(1, storage.banknoteAmount(Nominal.TWO_HUNDRED));
        Assertions.assertEquals(initialBalance + 200, atmMachine.balance());
    }

    @Test
    public void testWithdraw_InvalidAmountRequested() {
        Assertions.assertThrows(InvalidAmountRequested.class, () -> atmMachine.withdraw(11));
    }

    @Test
    public void testWithdraw_OutOfBanknotes() {
        Assertions.assertThrows(OutOfBanknotes.class, () -> atmMachine.withdraw(15_000));
        Assertions.assertEquals(initialBalance, atmMachine.balance());
    }

    @Test
    public void testWithdraw_NormalCase() {
        final int requestedAmount = 5_600;
        final List<Banknote> banknotes = atmMachine.withdraw(requestedAmount);
        final int receivedAmount = banknotes.stream()
                .map(Banknote::nominal)
                .mapToInt(Nominal::toInt)
                .sum();
        Assertions.assertEquals(initialBalance - requestedAmount, atmMachine.balance());
        Assertions.assertEquals(requestedAmount, receivedAmount);
        Assertions.assertEquals(3, banknotes.size());
    }
}
