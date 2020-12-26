package ru.otus.jdbc;

import ru.otus.utils.Contracts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

public class DbExecutorImpl implements DbExecutor {

    @Override
    public void executeInsert(
            final Connection connection,
            final String sql,
            final List<Object> params) throws SQLException {
        Savepoint savePoint = connection.setSavepoint("savePointName");
        try (var pst = connection.prepareStatement(sql)) {
            for (int idx = 0; idx < params.size(); idx++) {
                pst.setObject(idx + 1, params.get(idx));
            }
            pst.executeUpdate();
        } catch (SQLException ex) {
            connection.rollback(savePoint);
            throw ex;
        }
    }

    @Override
    public List<List<Object>> executeSelect(
            final Connection connection,
            final String sqlTemplate,
            final List<Object> params,
            final int numColumns) throws SQLException {
        Contracts.requireNonNullArgument(connection);
        Contracts.requireNonNullArgument(sqlTemplate);
        Contracts.requireNonNullArgument(params);
        Contracts.requireThat(numColumns > 0);

        final List<List<Object>> rows = new ArrayList<>();
        try (var pst = connection.prepareStatement(sqlTemplate)) {
            for (int i = 1; i <= params.size(); i++) {
                pst.setObject(i, params.get(i - 1));
            }
            try (var rs = pst.executeQuery()) {
                while (rs.next()) {
                    rows.add(getRow(rs, numColumns));
                }
            }
        }
        return rows;
    }

    @Override
    public void executeUpdate(
            final Connection connection,
            final String sqlTemplate,
            final List<Object> params) throws SQLException {
        Savepoint savePoint = connection.setSavepoint("savePointName");
        try (var preparedStatement = connection.prepareStatement(sqlTemplate)) {
            for (int idx = 0; idx < params.size(); idx++) {
                preparedStatement.setObject(idx + 1, params.get(idx));
            }
            preparedStatement.executeUpdate();
        } catch (final SQLException sqlException) {
            connection.rollback(savePoint);
            throw sqlException;
        }
    }

    private static List<Object> getRow(
            final ResultSet resultSet,
            final int numColumns) throws SQLException {
        final List<Object> row = new ArrayList<>();
        for (int i = 1; i <= numColumns; i++) {
            row.add(resultSet.getObject(i));
        }
        return row;
    }
}
