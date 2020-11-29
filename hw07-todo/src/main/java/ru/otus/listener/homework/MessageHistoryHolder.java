package ru.otus.listener.homework;

import ru.otus.Message;
import ru.otus.listener.Listener;
import ru.otus.utils.Contracts;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class MessageHistoryHolder implements Listener {

    private final Map<Long, Deque<Message>> idToHistory = new HashMap<>();

    @Override
    public void onUpdated(final Message oldMsg, final Message newMsg) {
        Contracts.requireNonNullArgument(oldMsg);
        Contracts.requireNonNullArgument(newMsg);
        Contracts.requireThat(oldMsg.getId() == newMsg.getId());

        final Deque<Message> correspondingHistory = idToHistory.computeIfAbsent(
                oldMsg.getId(),
                (unused) -> new ArrayDeque<>());
        // no need to copy, because Message is immutable (defensive copy of field13)
        if (correspondingHistory.isEmpty()) {
            correspondingHistory.addLast(oldMsg);
        }
        correspondingHistory.addLast(newMsg);
    }
}
