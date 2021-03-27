package ru.otus.app.dao;

import ru.otus.app.model.User;
import ru.otus.app.sessionmanager.SessionManager;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    List<User> findAll();

    Optional<User> findById(long id) throws DaoException;

    Optional<User> findByLogin(String login) throws DaoException;

    long insert(User user) throws DaoException;

    SessionManager getSessionManager() throws DaoException;
}
