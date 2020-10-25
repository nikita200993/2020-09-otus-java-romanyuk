package ru.otus.testframework;

import ru.otus.utils.Contracts;
import ru.otus.utils.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

class StatementFactory {

    private final Constructor<?> noArgTestClassConstructor;
    private final Class<? extends RuntimeException> assertionExceptionClass;
    private final List<Method> beforeMethods;
    private final List<Method> afterMethods;

    StatementFactory(
            final Constructor<?> noArgTestClassConstructor,
            final Class<? extends RuntimeException> assertionExceptionClass,
            final List<Method> beforeMethods,
            final List<Method> afterMethods)
    {
        Contracts.requireNonNullArgument(noArgTestClassConstructor);
        Contracts.requireNonNullArgument(assertionExceptionClass);
        Contracts.requireNonNullArgument(beforeMethods);
        Contracts.requireNonNullArgument(afterMethods);

        this.noArgTestClassConstructor = noArgTestClassConstructor;
        this.assertionExceptionClass = assertionExceptionClass;
        this.beforeMethods = List.copyOf(beforeMethods);
        this.afterMethods = List.copyOf(afterMethods);
    }

    Statement createForTestMethod(
            final Method testMethod,
            final TestResult testResult)
    {
        Contracts.requireNonNullArgument(testMethod);
        Contracts.requireNonNullArgument(testResult);

        final var receiver = ReflectionUtils.invokeConstructor(noArgTestClassConstructor);
        final Statement testStatement = () -> runTestMethod(testMethod, receiver, testResult);
        final Statement wrappedWithBefore = new BeforeStatement(
                receiver,
                testResult,
                testStatement);
        return new AfterStatement(
                receiver,
                testResult,
                wrappedWithBefore);
    }

    private void runTestMethod(
            final Method testMethod,
            final Object receiver,
            final TestResult testResult)
    {
        try {
            // check that before methods finished successfully
            // junit doesn't run test if before methods thrown exception
            Contracts.forbidThat(testResult.isError());
            ReflectionUtils.invokeMethod(testMethod, receiver);
        } catch (final Exception exception) {
            // if cause is not and exception, then stop testing(it fatal error like oom
            final Exception causeException = extractCauseOrRethrowIfNotAnException(exception);
            if (assertionExceptionClass.isInstance(causeException)) {
                testResult.assertionFailed(causeException);
            } else {
                testResult.addException(causeException);
            }
        }
    }

    private static Exception extractCauseOrRethrowIfNotAnException(final Exception exception)
    {
        final Throwable cause = exception.getCause();
        if (cause instanceof Exception) {
            return (Exception) cause;
        } else {
            throw new RuntimeException(cause);
        }
    }

    private class BeforeStatement implements Statement {

        private final Object receiver;
        private final TestResult testResult;
        private final Statement nextStatementToExecute;

        BeforeStatement(
                final Object receiver,
                final TestResult testResult,
                final Statement nextStatementToExecute)
        {
            this.receiver = receiver;
            this.testResult = testResult;
            this.nextStatementToExecute = nextStatementToExecute;
        }

        @Override
        public void execute()
        {
            try {
                for (final Method beforeMethod : StatementFactory.this.beforeMethods) {
                    ReflectionUtils.invokeMethod(beforeMethod, receiver);
                }
            } catch (final Exception exception) {
                testResult.addException(extractCauseOrRethrowIfNotAnException(exception));
                return;
            }
            nextStatementToExecute.execute();
        }
    }

    private class AfterStatement implements Statement {

        private final Object receiver;
        private final TestResult testResult;
        private final Statement statementToExecuteBefore;

        AfterStatement(
                final Object receiver,
                final TestResult testResult,
                final Statement statementToExecuteBefore)
        {
            this.receiver = receiver;
            this.testResult = testResult;
            this.statementToExecuteBefore = statementToExecuteBefore;
        }

        @Override
        public void execute()
        {
            statementToExecuteBefore.execute();
            // after statements executed even if there where preceding exceptions (junit behaviour)
            for (final Method afterMethod : StatementFactory.this.afterMethods) {
                try {
                    ReflectionUtils.invokeMethod(afterMethod, receiver);
                } catch (final Exception exception) {
                    testResult.addException(extractCauseOrRethrowIfNotAnException(exception));
                }
            }
        }
    }
}
