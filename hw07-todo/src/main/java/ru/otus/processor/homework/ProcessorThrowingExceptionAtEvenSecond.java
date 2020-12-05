package ru.otus.processor.homework;

import ru.otus.Message;
import ru.otus.processor.Processor;
import ru.otus.utils.Contracts;

public class ProcessorThrowingExceptionAtEvenSecond implements Processor {

    private final Clock clock;


    public ProcessorThrowingExceptionAtEvenSecond(final Clock clock) {
        Contracts.requireNonNullArgument(clock);

        this.clock = clock;
    }

    @Override
    public Message process(final Message message) {
        Contracts.requireNonNullArgument(message);
        if (clock.currentTimeInSeconds() % 2 == 0) {
            throw new RuntimeException("Current time in seconds is even");
        }
        return message;
    }
}
