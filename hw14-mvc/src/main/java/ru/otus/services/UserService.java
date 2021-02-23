package ru.otus.services;

import ru.otus.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(long id);

    Optional<User> findByLogin(final String login);

    long insert(User user);

}
