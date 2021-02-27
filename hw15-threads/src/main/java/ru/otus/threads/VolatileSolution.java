package ru.otus.threads;

public class VolatileSolution {

    private volatile int i = 1;

    public static void main(final String[] args) throws InterruptedException {
        final var volatileSolution = new VolatileSolution();
        final var oddThread = new Thread(() -> volatileSolution.threadAction(true));
        final var evenThread = new Thread(() -> volatileSolution.threadAction(false));
        oddThread.start();
        evenThread.start();
        oddThread.join();
        evenThread.join();
        System.out.println("Finished printing numbers.");
    }

    private void threadAction(final boolean odd) {
        final int remainder = odd ? 1 : 0;
        final String threadNumber = odd ? "1" : "2";
        while (true) {
            final int iValue = i;
            if (iValue == 20) {
                break;
            }
            // our turn if condition is true
            if (iValue % 2 == remainder) {
                final int valueToPrint = iValue > 10 ? 20 - iValue : iValue;
                System.out.println("thread " + threadNumber + " " + valueToPrint);
                i = iValue + 1;
            }
        }
    }
}
