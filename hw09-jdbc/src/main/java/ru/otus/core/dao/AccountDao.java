package ru.otus.core.dao;

import ru.otus.core.model.Account;
import ru.otus.core.sessionmanager.SessionManager;

import java.util.Optional;

public interface AccountDao {

    void insert(Account account);

    void update(Account account);

    void insertOrUpdate(Account account);

    Optional<Account> findById(String id);

    SessionManager geSessionManager();
}
