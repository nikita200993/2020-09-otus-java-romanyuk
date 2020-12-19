package ru.otus.jdbc.dao;

import ru.otus.core.dao.AccountDao;
import ru.otus.core.model.Account;
import ru.otus.core.sessionmanager.SessionManager;
import ru.otus.jdbc.mapper.JdbcMapper;
import ru.otus.jdbc.sessionmanager.SessionManagerJdbc;
import ru.otus.utils.Contracts;

import java.util.Optional;

public class JdbcMapperAccountDao implements AccountDao {

    private final JdbcMapper<Account> accountJdbcMapper;
    private final SessionManagerJdbc sessionManagerJdbc;

    public JdbcMapperAccountDao(
            final JdbcMapper<Account> accountJdbcMapper,
            final SessionManagerJdbc sessionManagerJdbc) {
        this.accountJdbcMapper = Contracts.ensureNonNullArgument(accountJdbcMapper);
        this.sessionManagerJdbc = Contracts.ensureNonNullArgument(sessionManagerJdbc);
    }

    @Override
    public void insert(final Account account) {
        Contracts.requireNonNullArgument(account);

        accountJdbcMapper.insert(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                account
        );
    }

    @Override
    public void update(final Account account) {
        Contracts.requireNonNullArgument(account);

        accountJdbcMapper.update(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                account
        );
    }

    @Override
    public void insertOrUpdate(final Account account) {
        Contracts.requireNonNullArgument(account);

        accountJdbcMapper.insertOrUpdate(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                account
        );
    }

    @Override
    public Optional<Account> findById(final String id) {
        Contracts.requireNonNullArgument(id);

        return accountJdbcMapper.findById(
                sessionManagerJdbc.getCurrentSession().getConnection(),
                id
        );
    }

    @Override
    public SessionManager geSessionManager() {
        return sessionManagerJdbc;
    }
}
