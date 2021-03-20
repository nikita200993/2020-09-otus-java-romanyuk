package ru.otus.app.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.app.model.Role;
import ru.otus.app.model.User;
import ru.otus.messagesystem.client.ResultDataType;
import ru.otus.utils.Contracts;

import java.io.Serializable;

public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;
    private final String role;

    @JsonCreator
    public UserDto(
            @JsonProperty("login") final String login,
            @JsonProperty("password") final String password,
            @JsonProperty("role") final String role) {
        this.login = Contracts.ensureNonNullArgument(login);
        this.password = Contracts.ensureNonNullArgument(password);
        this.role = Contracts.ensureNonNullArgument(role);
        Role.isValidRole(this.role);
    }

    public User toUser() {
        return new User(login, password, Role.forRoleName(role));
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
