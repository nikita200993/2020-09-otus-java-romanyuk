package ru.otus.jdbc.dao;

import ru.otus.core.dao.ClientDao;
import ru.otus.core.model.Client;
import ru.otus.jdbc.mapper.JdbcMapper;
import ru.otus.jdbc.sessionmanager.SessionManagerJdbc;
import ru.otus.utils.Contracts;

import java.util.Optional;

public class JdbcMapperClientDao implements ClientDao {

    private final SessionManagerJdbc sessionManagerJdbc;
    private final JdbcMapper<Client> clientJdbcMapper;

    public JdbcMapperClientDao(
            final SessionManagerJdbc sessionManagerJdbc,
            final JdbcMapper<Client> clientJdbcMapper) {
        Contracts.requireNonNullArgument(sessionManagerJdbc);
        Contracts.requireNonNullArgument(clientJdbcMapper);

        this.sessionManagerJdbc = sessionManagerJdbc;
        this.clientJdbcMapper = clientJdbcMapper;
    }

    @Override
    public Optional<Client> findById(final long id) {
        return clientJdbcMapper.findById(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                id);
    }

    @Override
    public void insert(final Client client) {
        Contracts.requireNonNullArgument(client);

        clientJdbcMapper.insertOrUpdate(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                client);
    }

    @Override
    public void update(final Client client) {
        Contracts.requireNonNullArgument(client);

        clientJdbcMapper.update(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                client);
    }

    @Override
    public void insertOrUpdate(final Client client) {
        Contracts.requireNonNullArgument(client);

        clientJdbcMapper.insertOrUpdate(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                client);
    }

    @Override
    public SessionManagerJdbc getSessionManager() {
        return sessionManagerJdbc;
    }
}
