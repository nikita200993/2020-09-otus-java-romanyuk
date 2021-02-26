package ru.otus.threads;

public class LockSolution {

    private int i = 1;

    public static void main(final String[] args) throws InterruptedException {
        final var lockSolution = new LockSolution();
        final var oddThread = new Thread(() -> lockSolution.threadAction(true));
        final var evenThread = new Thread(() -> lockSolution.threadAction(false));
        oddThread.start();
        evenThread.start();
        oddThread.join();
        evenThread.join();
        System.out.println("Finished printing numbers.");
    }

    private synchronized void threadAction(final boolean odd) {
        try {
            final int remainder = odd ? 1 : 0;
            final String messageTemplate = "Thread " + (odd ? "1 " : "2 ") + "value = %d.%n";
            while (i < 20) {
                while (i % 2 != remainder) {
                    wait();
                }
                if (i == 20) {
                    return;
                }
                final int intToPrint = i <= 10 ? i : 20 - i;
                System.out.printf(messageTemplate, intToPrint);
                i++;
                notify();
            }
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(interruptedException);
        }
    }
}
