package ru.otus.jdbc.mapper;

import ru.otus.jdbc.DbExecutor;
import ru.otus.utils.Contracts;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcMapperImpl<T> implements JdbcMapper<T> {

    private final SQLEntity<T> sqlEntity;
    private final DbExecutor dbExecutor;

    public JdbcMapperImpl(
            final SQLEntity<T> sqlEntity,
            final DbExecutor dbExecutor) {
        Contracts.requireNonNullArgument(sqlEntity);
        Contracts.requireNonNullArgument(dbExecutor);

        this.sqlEntity = sqlEntity;
        this.dbExecutor = dbExecutor;
    }

    @Override
    public void insert(final Connection connection, final T objectData) {
        Contracts.requireNonNullArgument(connection);
        Contracts.requireNonNullArgument(objectData);

        try {
            dbExecutor.executeInsert(
                    connection,
                    sqlEntity.getInsertTemplate(),
                    sqlEntity.getRow(objectData));
        } catch (final SQLException exception) {
            throw new RuntimeException("Couldn't insert " + objectData, exception);
        }
    }

    @Override
    public void update(final Connection connection, final T entity) {
        Contracts.requireNonNullArgument(connection);
        Contracts.requireNonNullArgument(entity);

        try {
            dbExecutor.executeUpdate(
                    connection,
                    sqlEntity.getUpdateByIdTemplate(),
                    sqlEntity.getArgumentsForUpdate(entity));
        } catch (final SQLException sqlException) {
            throw new RuntimeException("Couldn't update entity " + entity, sqlException);
        }
    }

    @Override
    public void insertOrUpdate(final Connection connection, final T entity) {
        Contracts.requireNonNullArgument(connection);
        Contracts.requireNonNullArgument(entity);

        try {
            dbExecutor.executeInsert(
                    connection,
                    sqlEntity.getInsertOrUpdate(),
                    sqlEntity.getRow(entity)
            );
        } catch (final SQLException sqlException) {
            throw new RuntimeException("Couldn't insert or update entity " + entity, sqlException);
        }
    }

    @Override
    public Optional<T> findById(final Connection connection, final Object id) {
        Contracts.requireNonNullArgument(connection);
        Contracts.requireNonNullArgument(id);

        try {
            final List<List<Object>> rows = dbExecutor.executeSelect(
                    connection,
                    sqlEntity.getSelectByIdTemplate(),
                    List.of(id),
                    sqlEntity.numColumns());
            Contracts.forbidThat(rows.size() > 1);
            return rows.size() == 0
                    ? Optional.empty()
                    : Optional.of(sqlEntity.createFromRow(rows.get(0)));
        } catch (final SQLException sqlException) {
            throw new RuntimeException("Select failed for id " + id);
        }
    }
}
