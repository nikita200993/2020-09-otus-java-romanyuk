package ru.otus.processor.homework;

import ru.otus.Message;
import ru.otus.processor.Processor;
import ru.otus.utils.Contracts;

public class FieldSwapper implements Processor {
    @Override
    public Message process(final Message message) {
        Contracts.requireNonNullArgument(message);

        return message.toBuilder()
                .field11(message.getField12())
                .field12(message.getField11())
                .build();
    }
}
