package ru.otus.app.services;

import ru.otus.app.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(long id);

    Optional<User> findByLogin(final String login);

    long insert(User user);

}
