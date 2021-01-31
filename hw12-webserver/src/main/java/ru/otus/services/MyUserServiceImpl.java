package ru.otus.services;

import ru.otus.dao.MyUserDao;
import ru.otus.model.MyUser;
import ru.otus.sessionmanager.SessionManager;
import ru.otus.utils.Contracts;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MyUserServiceImpl implements MyUserService {

    private final MyUserDao userDao;

    public MyUserServiceImpl(final MyUserDao userDao) {
        this.userDao = Contracts.ensureNonNullArgument(userDao);
    }

    @Override
    public List<MyUser> findAll() {
        return doInTransaction(userDao::findAll);
    }

    @Override
    public Optional<MyUser> findById(long id) {
        return doInTransaction(() -> userDao.findById(id));
    }

    @Override
    public Optional<MyUser> findByLogin(final String login) {
        Contracts.requireNonNullArgument(login);

        return doInTransaction(() -> userDao.findByLogin(login));
    }

    @Override
    public long insert(MyUser user) {
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
