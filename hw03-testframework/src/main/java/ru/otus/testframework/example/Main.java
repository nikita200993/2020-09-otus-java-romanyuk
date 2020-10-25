package ru.otus.testframework.example;

import ru.otus.testframework.ClassTestRunner;

public class Main {

    public static void main(String[] args) throws NoSuchMethodException, ClassNotFoundException
    {
        new ClassTestRunner().runTestsInClass("ru.otus.testframework.example.TestExample");
    }
}
