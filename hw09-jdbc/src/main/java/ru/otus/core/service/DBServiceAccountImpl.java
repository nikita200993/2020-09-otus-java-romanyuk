package ru.otus.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.dao.AccountDao;
import ru.otus.core.model.Account;
import ru.otus.utils.Contracts;

import java.util.Optional;

public class DBServiceAccountImpl implements DBServiceAccount {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBServiceAccountImpl.class);

    private final AccountDao accountDao;

    public DBServiceAccountImpl(final AccountDao accountDao) {
        this.accountDao = Contracts.ensureNonNullArgument(accountDao);
    }

    @Override
    public void saveAccount(final Account account) {
        Contracts.requireNonNullArgument(account);

        try (var sessionManager = accountDao.geSessionManager()) {
            sessionManager.beginSession();
            try {
                accountDao.insertOrUpdate(account);
                sessionManager.commitSession();
            } catch (Exception e) {
                sessionManager.rollbackSession();
                throw new DbServiceException(e);
            }
        }
    }

    @Override
    public Optional<Account> findById(final String no) {
        Contracts.requireNonNullArgument(no);

        try (var sessionManager = accountDao.geSessionManager()) {
            sessionManager.beginSession();
            try {
                return accountDao.findById(no);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                sessionManager.rollbackSession();
            }
            return Optional.empty();
        }
    }
}
