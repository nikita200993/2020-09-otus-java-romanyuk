package ru.otus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DIYArrayList<V> implements List<V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    private static final int GROWTH_SHIFT = 1;

    private static final Object[] EMPTY = new Object[0];

    private int size;

    private V[] backingArray;

    @SuppressWarnings("unchecked")
    public DIYArrayList() {
        size = 0;
        backingArray = (V[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<V> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        if (size == 0) {
            return EMPTY;
        }
        return Arrays.copyOf(backingArray, size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(final V v) {
        if (size >= capacity()) {
            backingArray = grow(size + 1);
        }
        backingArray[size++] = v;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(final int index) {
        checkIndex(index);
        return backingArray[index];
    }

    @Override
    public V set(final int index, final V element) {
        checkIndex(index);
        final V oldValue = backingArray[index];
        backingArray[index] = element;
        return oldValue;
    }

    @Override
    public void add(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<V> listIterator() {
        return new DIYArrayListIterator();
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private int capacity() {
        return backingArray.length;
    }

    private V[] grow(final int necessaryCapacity) {
        if (necessaryCapacity < 0) {
            throw new OutOfMemoryError(
                    "Required capacity is negative due to integer overflow."
                            + " Can't hold more than 2^31 - 1 elements"
            );
        }
        final int defaultGrowthCapacity = capacity() + capacity() >> GROWTH_SHIFT;
        final int desiredCapacity = defaultGrowthCapacity < 0
                ? Integer.MAX_VALUE
                : defaultGrowthCapacity;
        return Arrays.copyOf(backingArray, Math.max(necessaryCapacity, desiredCapacity));
    }

    private void checkIndex(final int index) {
        if (index >= size || size < 0) {
            throw new IndexOutOfBoundsException(
                    String.format("Size = %s, index = %s", size, index));
        }
    }

    private boolean listEquals(final List<?> otherList) {
        int processedElements = 0;
        for (final Object otherElement : otherList) {
            if (processedElements == size) {
                return false;
            } else if (!Objects.equals(backingArray[processedElements], otherElement)) {
                return false;
            }
            processedElements++;
        }
        return processedElements == size;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof List)) {
            return false;
        }
        return listEquals((List<?>) obj);
    }

    @Override
    public int hashCode() {
        // initialize to remove collisions of null lists with different sizes;
        int hash = 1;
        for (int i = 0; i < size; i++) {
            final Object objectToHash = backingArray[i];
            hash = 31 * hash + (objectToHash == null ? 0 : objectToHash.hashCode());
        }
        return hash;
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "{}";
        }
        final var stringBuilder = new StringBuilder("{");
        for (int i = 0; i < size - 1; i++) {
            stringBuilder.append(backingArray[i]);
            stringBuilder.append(", ");
        }
        stringBuilder.append(backingArray[size - 1]);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private class DIYArrayListIterator implements ListIterator<V> {

        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < DIYArrayList.this.size;
        }

        @Override
        public V next() {
            if (cursor >= DIYArrayList.this.size) {
                throw new NoSuchElementException();
            }
            return DIYArrayList.this.backingArray[cursor++];
        }

        @Override
        public boolean hasPrevious() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V previous() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(V v) {
            final int indexToSet = cursor - 1;
            if (indexToSet < 0) {
                throw new IllegalStateException();
            }
            DIYArrayList.this.set(indexToSet, v);
        }

        @Override
        public void add(V v) {
            throw new UnsupportedOperationException();
        }
    }
}
