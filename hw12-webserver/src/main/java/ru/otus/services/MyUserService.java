package ru.otus.services;

import ru.otus.model.MyUser;

import java.util.List;
import java.util.Optional;

public interface MyUserService {

    List<MyUser> findAll();

    Optional<MyUser> findById(long id);

    Optional<MyUser> findByLogin(final String login);

    long insert(MyUser user);

}
