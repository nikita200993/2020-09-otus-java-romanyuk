package ru.otus.jdbc.mapper;

import ru.otus.utils.Contracts;
import ru.otus.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlEntityFactory {

    private final EntityClassMetaDataFactory factory = new EntityClassMetaDataFactory();

    public <V> SQLEntity<V> from(final Class<V> clazz) {
        Contracts.requireNonNullArgument(clazz);

        return from(factory.fromClass(clazz));
    }

    private <V> SQLEntity<V> from(final EntityClassMetaData<V> entityClassMetaData) {
        final List<Field> fields = entityClassMetaData.getFields();
        Contracts.forbidThat(fields.size() <= 1);
        final List<String> columnNames = fields.stream()
                .map(Field::getName)
                .collect(Collectors.toList());
        final String idColumnName = entityClassMetaData.getIdField().getName();
        final String tableName = entityClassMetaData.getName().toLowerCase();
        return new SqlEntityImpl<>(
                getColumnExtractor(fields),
                list -> ReflectionUtils.newInstance(entityClassMetaData.getConstructor(), list.toArray()),
                columnNames,
                idColumnName,
                getInsertTemplate(columnNames, tableName),
                getSelectTemplate(columnNames, tableName, idColumnName),
                getUpdateByIdTemplate(columnNames, tableName, idColumnName),
                getInsertOrUpdate(columnNames, tableName, idColumnName)
        );
    }

    private static <V> BiFunction<V, String, Object> getColumnExtractor(final List<Field> fields) {
        final Map<String, Field> fieldNameToReflectedField = fields.stream()
                .collect(Collectors.toMap(Field::getName, Function.identity()));
        return (entity, columnName) -> ReflectionUtils.getFieldValue(fieldNameToReflectedField.get(columnName), entity);
    }

    private static String getInsertTemplate(final List<String> columnNames, final String className) {
        return "insert into " +
                className +
                " (" +
                String.join(", ", columnNames) +
                ") " +
                "values (" +
                String.join(", ", Collections.nCopies(columnNames.size(), "?")) +
                ')';
    }

    private static String getSelectTemplate(
            final List<String> columnNames,
            final String tableName,
            final String idFieldName) {
        return "select " +
                String.join(", ", columnNames) +
                " from " +
                tableName +
                " where " +
                idFieldName +
                " = ?";
    }

    private static String getUpdateByIdTemplate(
            final List<String> columnNames,
            final String tableName,
            final String idFieldName) {
        final String setPartOfQuery = columnNames.stream()
                .filter(columnName -> !columnName.equals(idFieldName))
                .map(columnName -> columnName + " = ?")
                .collect(Collectors.joining(", "));
        return "update " +
                tableName +
                " set " +
                setPartOfQuery +
                " where " +
                idFieldName +
                " = ?";
    }

    private static String getInsertOrUpdate(
            final List<String> columnNames,
            final String tableName,
            final String idColumnName) {
        final String insertTemplate = getInsertTemplate(columnNames, tableName);
        final List<String> assignmentList = columnNames.stream()
                .filter(columnName -> !idColumnName.equals(columnName))
                .map(columnName -> columnName + "=excluded." + columnName)
                .collect(Collectors.toList());
        return insertTemplate +
                " on conflict (" + idColumnName + ") do update" +
                " set "
                + String.join(", ", assignmentList);
    }
}
