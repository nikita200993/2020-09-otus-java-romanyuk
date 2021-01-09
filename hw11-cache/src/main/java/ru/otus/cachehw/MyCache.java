package ru.otus.cachehw;

import ru.otus.utils.Contracts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MyCache<K, V> implements HwCache<K, V> {

    private final Map<K, V> backingMap;

    private final List<HwListener<K, V>> listeners;

    public MyCache() {
        this.backingMap = new WeakHashMap<>();
        this.listeners = new ArrayList<>(1);
    }

    public MyCache(final Map<K, V> backingMap) {
        this.backingMap = new WeakHashMap<>(backingMap);
        this.listeners = new ArrayList<>(1);
    }

    @Override
    public void put(final K key, final V value) {
        Contracts.requireNonNullArgument(key);
        Contracts.requireNonNullArgument(value);

        final var old = backingMap.put(key, value);
        final String event = old == null ? "add" : "update";
        listeners.forEach(listener -> listener.notify(key, value, event));
    }

    @Override
    public void remove(final K key) {
        Contracts.requireNonNullArgument(key);

        if (backingMap.remove(key) != null) {
            listeners.forEach(listener -> listener.notify(key, null, "remove"));
        }
    }

    @Override
    public V get(final K key) {
        Contracts.requireNonNullArgument(key);

        final var result = backingMap.get(key);
        listeners.forEach(listener -> listener.notify(key, result, "access"));
        return result;
    }

    @Override
    public void addListener(final HwListener<K, V> listener) {
        Contracts.requireNonNullArgument(listener);

        listeners.add(listener);
    }

    @Override
    public void removeListener(final HwListener<K, V> listener) {
        Contracts.requireNonNullArgument(listener);

        listeners.remove(listener);
    }

    @Override
    public int size() {
        return backingMap.size();
    }
}
