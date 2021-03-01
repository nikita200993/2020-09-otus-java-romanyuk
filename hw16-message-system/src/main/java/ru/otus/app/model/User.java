package ru.otus.app.model;

import ru.otus.app.dto.UserDto;
import ru.otus.utils.Contracts;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;

@Entity(name = "user")
@Table(name = "users")
public class User {

    @Nullable
    @Id
    @SequenceGenerator(name = "userGenerator", sequenceName = "user_generator")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userGenerator")
    private Long id;
    @Nullable
    @Column(name = "login", unique = true)
    private String login;
    @Nullable
    @Column(name = "password", nullable = false)
    private String password;
    @Nullable
    @Column(name = "role", nullable = false)
    private String roleName;

    public User() {
    }

    public User(final String login, final String password, final Role roleName) {
        Contracts.requireNonNullArgument(login);
        Contracts.requireNonNullArgument(password);
        Contracts.requireNonNullArgument(roleName);

        this.login = login;
        this.password = password;
        this.roleName = roleName.getRoleName();
    }

    public UserDto toUserDto() {

        return new UserDto(login, password, Role.forRoleName(roleName));
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = Contracts.ensureNonNullArgument(id);
    }

    @Nullable
    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = Contracts.ensureNonNullArgument(login);
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = Contracts.ensureNonNullArgument(password);
    }

    @Nullable
    public Role getRoleName() {
        return roleName != null ? Role.forRoleName(roleName) : null;
    }

    public void setRoleName(final Role roleName) {
        Contracts.requireNonNullArgument(roleName);

        this.roleName = roleName.getRoleName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final User user = (User) o;
        return Objects.equals(login, user.login) && Objects.equals(password, user.password) && Objects.equals(roleName, user.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, roleName);
    }

}
