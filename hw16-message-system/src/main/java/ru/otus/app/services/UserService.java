package ru.otus.app.services;

import ru.otus.app.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDto> findAll();

    Optional<UserDto> findById(long id);

    Optional<UserDto> findByLogin(final String login);

    long insert(UserDto user);

}
