package ru.otus.dao;

import ru.otus.model.MyUser;
import ru.otus.sessionmanager.SessionManager;

import java.util.List;
import java.util.Optional;

public interface MyUserDao {

    List<MyUser> findAll();

    Optional<MyUser> findById(long id) throws DaoException;

    Optional<MyUser> findByLogin(String login) throws DaoException;

    long insert(MyUser user) throws DaoException;

    SessionManager getSessionManager() throws DaoException;
}
