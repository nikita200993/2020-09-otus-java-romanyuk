package ru.otus.aop;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        System.out.println("main");
        new Main().funcToLogOne(2L, "hi");
        funcToLogTwo(1, new Main(), 2.0, Map.of("key", "value"));
        new Main().funcNotToLog(2);
    }

    @Log
    public void funcToLogOne(final long aLong, final String string) {
        System.out.println("above should be logged arguments for function number 1");
    }

    @Log
    private static <E> List<E> funcToLogTwo(
            final int anInt,
            final Main main,
            final double aDouble,
            final Map<?, ?> map) {
        System.out.println("above should be logged arguments for function number 2");
        return Collections.emptyList();
    }

    private void funcNotToLog(final int z) {
        System.out.println("above shouldn't be logged arguments");
    }
}
