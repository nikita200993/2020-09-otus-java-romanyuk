package ru.otus.testframework;

class AssertionFailure extends RuntimeException {

    AssertionFailure(final String message) {
        super(message);
    }
}
