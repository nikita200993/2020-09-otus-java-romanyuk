package ru.otus.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DbExecutor {

    void executeInsert(
            Connection connection,
            String sql,
            List<Object> params) throws SQLException;

    List<List<Object>> executeSelect(
            Connection connection,
            String sqlTemplate,
            List<Object> params,
            int columnSize) throws SQLException;

    void executeUpdate(
            Connection connection,
            String sqlTemplate,
            List<Object> params) throws SQLException;
}
