package ru.otus.app.dto;

import ru.otus.app.model.Role;
import ru.otus.app.model.User;
import ru.otus.utils.Contracts;

public class UserDto {

    private final String login;
    private final String password;
    private final String roleName;

    public UserDto(final String login, final String password, final String roleName) {
        this.login = Contracts.ensureNonNullArgument(login);
        this.password = Contracts.ensureNonNullArgument(password);
        this.roleName = Contracts.ensureNonNullArgument(roleName);
        Role.isValidRole(roleName);
    }

    public User toUser() {
        return new User(login, password, Role.forRoleName(roleName));
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getRoleName() {
        return roleName;
    }
}
