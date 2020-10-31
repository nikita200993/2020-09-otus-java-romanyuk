package ru.otus.testframework.example;

import ru.otus.testframework.Assertions;
import ru.otus.testframework.annotations.After;
import ru.otus.testframework.annotations.Before;
import ru.otus.testframework.annotations.Test;

public class TestExample {

    private static int testNum = 0;

    @Before
    void increment() {
        testNum++;
        if (testNum == 3) {
            throw new RuntimeException();
        }
    }

    @Test
    void successfulTest() {
        Assertions.assertEquals(1, 1);
    }

    @Test
    void failTest() {
        Assertions.assertEquals(0, 1);
    }

    @Test
    void failTestDueToError() {
    }

    @After
    void sayGoodBye() {
        System.out.println("good bye");
    }
}
