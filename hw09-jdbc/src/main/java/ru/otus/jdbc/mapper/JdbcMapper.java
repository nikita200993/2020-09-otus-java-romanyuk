package ru.otus.jdbc.mapper;

import java.sql.Connection;
import java.util.Optional;

public interface JdbcMapper<T> {

    void insert(Connection connection, T entity);

    void update(Connection connection, T entity);

    void insertOrUpdate(Connection connection, T entity);

    Optional<T> findById(Connection connection, Object id);
}
