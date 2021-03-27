package ru.otus.app.services;

import ru.otus.app.dao.UserDao;
import ru.otus.app.dto.UserDto;
import ru.otus.app.model.User;
import ru.otus.app.sessionmanager.SessionManager;
import ru.otus.utils.Contracts;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(final UserDao userDao) {
        this.userDao = Contracts.ensureNonNullArgument(userDao);
    }

    @Override
    public List<UserDto> findAll() {
        return doInTransaction(userDao::findAll).stream()
                .map(User::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findById(long id) {
        return doInTransaction(() -> userDao.findById(id))
                .map(User::toUserDto);
    }

    @Override
    public Optional<UserDto> findByLogin(final String login) {
        Contracts.requireNonNullArgument(login);

        return doInTransaction(() -> userDao.findByLogin(login))
                .map(User::toUserDto);
    }

    @Override
    public long insert(final UserDto userDto) {
        Contracts.requireNonNullArgument(userDto);

        return doInTransaction(() -> userDao.insert(userDto.toUser()));
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
