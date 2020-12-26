package ru.otus.jdbc.mapper;

import ru.otus.utils.Contracts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlEntityImpl<V> implements SQLEntity<V> {

    private final BiFunction<V, String, Object> columnsValuesExtractor;
    private final Function<List<Object>, V> fromRowFactory;
    private final List<String> columnNames;
    private final String idColumnName;
    private final String insertTemplate;
    private final String selectByIdTemplate;
    private final String updateByIdTemplate;
    private final String insertOrUpdateTemplate;

    public SqlEntityImpl(
            final BiFunction<V, String, Object> columnsValuesExtractor,
            final Function<List<Object>, V> fromRowFactory,
            final List<String> columnNames,
            final String idColumnName,
            final String insertTemplate,
            final String selectByIdTemplate,
            final String updateByIdTemplate,
            final String insertOrUpdateTemplate) {
        this.columnsValuesExtractor = Contracts.ensureNonNullArgument(columnsValuesExtractor);
        this.fromRowFactory = Contracts.ensureNonNullArgument(fromRowFactory);
        this.columnNames = Contracts.ensureNonNullArgument(columnNames);
        Contracts.forbidThat(columnNames.size() <= 1);
        this.idColumnName = Contracts.ensureNonNullArgument(idColumnName);
        this.insertTemplate = Contracts.ensureNonNullArgument(insertTemplate);
        this.selectByIdTemplate = Contracts.ensureNonNullArgument(selectByIdTemplate);
        this.updateByIdTemplate = Contracts.ensureNonNullArgument(updateByIdTemplate);
        this.insertOrUpdateTemplate = Contracts.ensureNonNullArgument(insertOrUpdateTemplate);
    }


    @Override
    public V createFromRow(final List<Object> row) {
        Contracts.requireNonNullArgument(row);

        return Contracts.ensureNonNull(fromRowFactory.apply(row));
    }

    @Override
    public List<Object> getRow(final V javaEntity) {
        return columnNames.stream()
                .map(columnName -> columnsValuesExtractor.apply(javaEntity, columnName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Object> getArgumentsForUpdate(V javaEntity) {
        final List<Object> result = new ArrayList<>();
        for (final String name : columnNames) {
            if (idColumnName.equals(name)) {
                continue;
            }
            result.add(columnsValuesExtractor.apply(javaEntity, name));
        }
        result.add(columnsValuesExtractor.apply(javaEntity, idColumnName));
        return result;
    }

    @Override
    public int numColumns() {
        return columnNames.size();
    }

    @Override
    public String getIdColumnName() {
        return idColumnName;
    }

    @Override
    public String getSelectByIdTemplate() {
        return selectByIdTemplate;
    }

    @Override
    public String getUpdateByIdTemplate() {
        return updateByIdTemplate;
    }

    @Override
    public String getInsertTemplate() {
        return insertTemplate;
    }

    @Override
    public String getInsertOrUpdate() {
        return insertOrUpdateTemplate;
    }
}
