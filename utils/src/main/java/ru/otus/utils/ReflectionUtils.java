package ru.otus.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

public class ReflectionUtils {

    private ReflectionUtils() {
        throw new IllegalAccessError();
    }

    public static Object invokeMethod(
            final Method method,
            final Object receiver,
            final Object... args) throws RuntimeException {
        Contracts.requireNonNullArgument(method);

        try {
            return method.invoke(receiver, args);
        } catch (final IllegalAccessException ex) {
            throw new RuntimeException("Couldn't invoke method " + method, ex);
        } catch (final InvocationTargetException ex) {
            throw new RuntimeException(
                    "Invoked method " + method + " thrown exception",
                    ex.getCause()
            );
        }
    }

    public static <T> T invokeConstructor(
            final Constructor<T> constructor,
            final Object... args) throws RuntimeException {
        Contracts.requireNonNullArgument(constructor);

        try {
            return constructor.newInstance(args);
        } catch (final IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException("Couldn't invoke constructor " + constructor, ex);
        } catch (final InvocationTargetException ex) {
            throw new RuntimeException(
                    "Invoked constructor " + constructor + " thrown exception",
                    ex.getCause()
            );
        }
    }

    public static boolean isBoxedPrimitive(final Object object) {
        Contracts.requireNonNullArgument(object);

        final Class<?> clazz = object.getClass();

        return clazz.equals(Integer.class)
                || clazz.equals(Long.class)
                || clazz.equals(Short.class)
                || clazz.equals(Float.class)
                || clazz.equals(Double.class)
                || clazz.equals(Boolean.class)
                || clazz.equals(Byte.class)
                || clazz.equals(Character.class);
    }

    public static Stream<Field> getInstanceFieldsStream(final Class<?> clazz) {
        Contracts.requireNonNullArgument(clazz);

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()));
    }

    public static <T extends AccessibleObject> T asAccessible(final T accessibleObject) {
        Contracts.requireNonNullArgument(accessibleObject);

        accessibleObject.setAccessible(true);
        return accessibleObject;
    }

    public static Object getFieldValue(final Field field, final Object object) {
        Contracts.requireNonNullArgument(field);
        Contracts.requireNonNullArgument(object);

        try {
            return field.get(object);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to extract field "
                    + field
                    + " from object of class "
                    + object.getClass());
        }
    }

    public static <V> V newInstance(final Constructor<V> constructor, Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to create new instance of class " + constructor.getDeclaringClass());
        }
    }
}
