package ru.otus.jdbc.mapper;

import ru.otus.utils.Contracts;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {

    private final String simpleClassName;
    private final Constructor<T> constructor;
    private final Field idField;
    private final List<Field> fields;

    EntityClassMetaDataImpl(
            final String simpleClassName,
            final Constructor<T> constructor,
            final Field idField,
            final List<Field> fields) {
        Contracts.requireNonNullArgument(simpleClassName);
        Contracts.requireNonNullArgument(constructor);
        Contracts.requireNonNullArgument(idField);
        Contracts.requireNonNullArgument(fields);
        Contracts.requireThat(constructor.getParameterCount() == fields.size());

        this.simpleClassName = simpleClassName;
        this.constructor = constructor;
        this.idField = idField;
        this.fields = fields;
    }

    @Override
    public String getName() {
        return simpleClassName;
    }

    @Override
    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    public Field getIdField() {
        return idField;
    }

    @Override
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }
}
