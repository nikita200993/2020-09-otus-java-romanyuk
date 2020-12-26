package ru.otus.jdbc.mapper;

import ru.otus.core.annotation.Id;
import ru.otus.utils.Contracts;
import ru.otus.utils.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityClassMetaDataFactory {

    private final Map<Class<?>, EntityClassMetaData<?>> cache = new HashMap<>();

    // will return correct meta data if fields order obtained by reflection corresponds to constructor params.
    // Problem is that params obtained by reflection have name different to declared in source code.
    @SuppressWarnings("unchecked")
    public <T> EntityClassMetaData<T> fromClass(final Class<T> clazz) {
        Contracts.requireNonNullArgument(clazz);

        return (EntityClassMetaData<T>) cache.computeIfAbsent(clazz, (unused) -> createMetaData(clazz));
    }

    private static <T> EntityClassMetaDataImpl<T> createMetaData(final Class<T> clazz) {
        final List<Field> instanceFields = ReflectionUtils.getInstanceFieldsStream(clazz)
                .map(ReflectionUtils::asAccessible)
                .collect(Collectors.toList());
        final Field idField = instanceFields.stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> Contracts.unreachable("id field is absent for " + clazz));
        @SuppressWarnings("unchecked") final Constructor<T> constructor = (Constructor<T>) Arrays.stream(clazz.getDeclaredConstructors())
                .filter((constr) -> isRelevantConstructor(constr, instanceFields))
                .map(ReflectionUtils::asAccessible)
                .findFirst()
                .orElseThrow(() ->
                        Contracts.unreachable("No appropriate constructor was found for " + clazz));
        return new EntityClassMetaDataImpl<>(
                clazz.getSimpleName(),
                constructor,
                idField,
                instanceFields
        );
    }

    private static <T> boolean isRelevantConstructor(
            final Constructor<T> constructor,
            final Collection<? extends Field> fields) {
        final Parameter[] params = constructor.getParameters();
        if (params.length != fields.size()) {
            return false;
        }
        return true;
    }
}
