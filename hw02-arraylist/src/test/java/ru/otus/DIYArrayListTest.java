package ru.otus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class DIYArrayListTest {

    private static final int MAX_RANDOM_LIST_SIZE = 100000;

    @Test
    public void testCollectionsAdd() {
        final var actual = new DIYArrayList<Integer>();
        final List<Integer> expected = getRandomIntegerList(
                new Random().nextInt(MAX_RANDOM_LIST_SIZE));
        Collections.addAll(actual, expected.toArray(new Integer[0]));
        // java.util.ArrayList.equals will be invoked
        // (don't want to test here equals if DIYArrayList)
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testEqualsTrueCase() {
        final var list = new ArrayList<>(List.of(1, 2));
        final var testedList = new DIYArrayList<Integer>();
        Collections.addAll(testedList, list.toArray(new Integer[0]));
        Assertions.assertEquals(testedList, list);
    }

    @Test
    public void testEqualsFalseCaseDifferentSize() {
        final var list = new ArrayList<>(List.of(1, 2));
        final var testedList = new DIYArrayList<Integer>();
        Collections.addAll(testedList, list.toArray(new Integer[0]));
        testedList.add(3);
        Assertions.assertNotEquals(testedList, list);
    }

    @Test
    public void testEqualsFalseCaseDifferentValue() {
        final var list = new ArrayList<>(List.of(1, 2));
        final var testedList = new DIYArrayList<Integer>();
        Collections.addAll(testedList, list.toArray(new Integer[0]));
        testedList.set(1, 3);
        Assertions.assertNotEquals(testedList, list);
    }

    @Test
    public void testEqualsEmptyLists() {
        Assertions.assertEquals(new DIYArrayList<Integer>(), new ArrayList<>());
    }

    @Test
    public void testCollectionsCopy() {
        final int sizeOfList = new Random().nextInt(MAX_RANDOM_LIST_SIZE);
        final List<Integer> expected = getRandomIntegerList(sizeOfList);
        final var actual = new DIYArrayList<Integer>();
        // make it the same size but with different elements (in most cases)
        Collections.addAll(
                actual,
                getRandomIntegerList(sizeOfList).toArray(new Integer[0]));
        Collections.copy(actual, expected);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testCollectionsSort() {
        final List<Integer> expected = getRandomIntegerList(
                new Random().nextInt(MAX_RANDOM_LIST_SIZE));
        final var actual = new DIYArrayList<Integer>();
        Collections.addAll(actual, expected.toArray(new Integer[0]));
        Collections.sort(actual);
        Collections.sort(expected);
        Assertions.assertEquals(expected, actual);
    }

    private static List<Integer> getRandomIntegerList(final int size) {
        final Random random = new Random();
        return IntStream.generate(random::nextInt)
                .limit(size)
                .collect(ArrayList::new, List::add, List::addAll);
    }
}
