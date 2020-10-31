package ru.otus.testframework;

import ru.otus.testframework.annotations.After;
import ru.otus.testframework.annotations.Before;
import ru.otus.testframework.annotations.Test;
import ru.otus.utils.Contracts;
import ru.otus.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassTestRunner {

    public void runTestsInClass(final String className)
            throws ClassNotFoundException, NoSuchMethodException
    {
        final Class<?> testedClazz = Class.forName(className);
        Contracts.requireThat(
                !Modifier.isAbstract(testedClazz.getModifiers()),
                "Abstract class can't be a source for tests");
        final Constructor<?> testClassConstructor = testedClazz.getDeclaredConstructor();
        checkCreationOfTestInstance(testClassConstructor);
        final List<Method> testMethods = extractInstanceMethods(testedClazz, Test.class);
        if (testMethods.isEmpty()) {
            System.out.println("There are no test methods in class = " + className);
            return;
        }
        testMethods.forEach(method -> method.setAccessible(true));
        final List<TestResult> testResults = runTests(
                testMethods,
                new StatementFactory(
                        testClassConstructor,
                        AssertionFailure.class,
                        extractInstanceMethods(testedClazz, Before.class).stream()
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList()),
                        extractInstanceMethods(testedClazz, After.class).stream()
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList())
                )
        );
        logTestResults(testResults);
    }

    private static List<TestResult> runTests(
            final List<Method> testedMethods,
            final StatementFactory statementFactory)
    {
        final List<TestResult> testResults = new ArrayList<>();
        for (final Method testMethod : testedMethods) {
            final var testResult = new TestResult(
                    testMethod.getDeclaringClass().getName() + testMethod.getName());
            statementFactory.createForTestMethod(testMethod, testResult)
                    .execute();
            testResults.add(testResult);
        }
        return testResults;
    }

    private static void logTestResults(final List<TestResult> testResults)
    {
        System.out.println("***Test summary***");
        System.out.println("Total: " + testResults.size());
        final long fails = testResults.stream()
                .filter(TestResult::isFailure)
                .count();
        final long errors = testResults.stream()
                .filter(TestResult::isError)
                .count();
        System.out.println("Fails: " + fails);
        System.out.println("Errors: " + errors);
        System.out.println("******");
        testResults.forEach(ClassTestRunner::logTestResult);
    }

    private static void logTestResult(final TestResult testResult)
    {
        if (testResult.isSuccess()) {
            System.out.println("SUCCESS " + testResult.getTestName());
        } else if (testResult.isFailure()) {
            System.out.println("FAILURE " + testResult.getTestName());
            testResult.getException().printStackTrace(System.out);
        } else if (testResult.isError()) {
            System.out.println("ERROR " + testResult.getTestName());
            testResult.getException().printStackTrace(System.out);
        } else {
            throw Contracts.unreachable();
        }
    }

    private static List<Method> extractInstanceMethods(
            final Class<?> clazz,
            final Class<? extends Annotation> annotationClass)
    {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
    }

    private static void checkCreationOfTestInstance(final Constructor<?> testClassConstructor)
    {
        try {
            ReflectionUtils.invokeConstructor(testClassConstructor);
        } catch (final RuntimeException exception) {
            throw new RuntimeException(
                    "Can't instantiate instance of test class "
                            + testClassConstructor.getDeclaringClass(),
                    exception);
        }
    }
}
