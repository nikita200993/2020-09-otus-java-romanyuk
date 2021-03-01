package ru.otus.app.dto;

import ru.otus.app.model.Role;
import ru.otus.app.model.User;
import ru.otus.utils.Contracts;

public class UserDto {

    private final String login;
    private final String password;
    private final Role role;

    public UserDto(final String login, final String password, final Role role) {
        this.login = Contracts.ensureNonNullArgument(login);
        this.password = Contracts.ensureNonNullArgument(password);
        this.role = Contracts.ensureNonNullArgument(role);
    }

    public User toUser() {
        return new User(login, password, role);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
