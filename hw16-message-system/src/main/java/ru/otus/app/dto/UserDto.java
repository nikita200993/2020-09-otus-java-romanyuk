package ru.otus.app.dto;

import ru.otus.app.model.Role;
import ru.otus.app.model.User;
import ru.otus.utils.Contracts;

public class UserDto {

    private final long id;
    private final String login;
    private final Role role;

    public UserDto(final long id, final String login, final Role role) {
        this.id = id;
        this.login = Contracts.ensureNonNullArgument(login);
        this.role = Contracts.ensureNonNullArgument(role);
    }

    public static UserDto from(final User user) {
        Contracts.requireNonNullArgument(user);

        return new UserDto(
                Contracts.ensureNonNull(user.getId()),
                user.getLogin(),
                user.getRole());
    }
}
