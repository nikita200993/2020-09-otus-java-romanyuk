package ru.otus.core.service;

import ru.otus.core.model.Account;

import java.util.Optional;

public interface DBServiceAccount {

    void saveAccount(Account account);

    Optional<Account> findById(String no);
}
