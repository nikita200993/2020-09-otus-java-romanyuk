package ru.otus.processor.homework;

import java.util.concurrent.TimeUnit;

public interface Clock {

    long currentTimeInMillis();

    default long currentTimeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(currentTimeInMillis());
    }
}
