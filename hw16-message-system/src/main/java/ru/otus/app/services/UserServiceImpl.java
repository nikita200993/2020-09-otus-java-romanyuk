package ru.otus.app.services;

import ru.otus.app.dao.UserDao;
import ru.otus.app.model.User;
import ru.otus.app.sessionmanager.SessionManager;
import ru.otus.utils.Contracts;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(final UserDao userDao) {
        this.userDao = Contracts.ensureNonNullArgument(userDao);
    }

    @Override
    public List<User> findAll() {
        return doInTransaction(userDao::findAll);
    }

    @Override
    public Optional<User> findById(long id) {
        return doInTransaction(() -> userDao.findById(id));
    }

    @Override
    public Optional<User> findByLogin(final String login) {
        Contracts.requireNonNullArgument(login);

        return doInTransaction(() -> userDao.findByLogin(login));
    }

    @Override
    public long insert(User user) {
        Contracts.requireNonNullArgument(user);

        return doInTransaction(() -> userDao.insert(user));
    }

    private <V> V doInTransaction(final Supplier<V> supplier) {
        try (SessionManager sessionManager = userDao.getSessionManager()) {
            try {
                sessionManager.beginSession();
                final V result = supplier.get();
                sessionManager.commitSession();
                return result;
            } catch (final Exception exception) {
                sessionManager.rollbackSession();
                throw exception;
            }
        }
    }
}
