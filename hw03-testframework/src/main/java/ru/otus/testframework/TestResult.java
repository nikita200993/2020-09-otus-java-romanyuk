package ru.otus.testframework;

import ru.otus.utils.Contracts;

class TestResult {

    private final String testName;
    private boolean failure = false;
    private boolean error = false;
    private Exception exception;

    TestResult(final String testName)
    {
        Contracts.requireNonNullArgument(testName);

        this.testName = testName;
    }

    boolean isSuccess()
    {
        return !failure && !error;
    }

    boolean isFailure()
    {
        return failure;
    }

    boolean isError()
    {
        return error;
    }

    String getTestName()
    {
        return testName;
    }

    void assertionFailed(final Exception exception)
    {
        forbidAnyFlagIsRaised();
        forbidExceptionIsSet();
        this.failure = true;
        this.exception = exception;
    }

    void addException(final Exception exception)
    {
        if (!error && !failure) {
            forbidExceptionIsSet();
            this.error = true;
            this.exception = exception;
        } else {
            Contracts.requireNonNullArgument(exception);
            this.exception.addSuppressed(exception);
        }
    }

    Exception getException()
    {
        return exception;
    }

    private void forbidAnyFlagIsRaised()
    {
        Contracts.forbidThat(error);
        Contracts.forbidThat(failure);
    }

    private void forbidExceptionIsSet()
    {
        Contracts.requireThat(exception == null);
    }
}
