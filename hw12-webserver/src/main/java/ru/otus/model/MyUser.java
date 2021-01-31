package ru.otus.model;

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
public class MyUser {

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
    private String role;

    public MyUser() {
    }

    public MyUser(final String login, final String password, final Role role) {
        Contracts.requireNonNullArgument(login);
        Contracts.requireNonNullArgument(password);
        Contracts.requireNonNullArgument(role);

        this.login = login;
        this.password = password;
        this.role = role.roleName;
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
    public Role getRole() {
        return role != null ? Role.forRoleName(role) : null;
    }

    public void setRole(final Role role) {
        Contracts.requireNonNullArgument(role);

        this.role = role.roleName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MyUser myUser = (MyUser) o;
        return Objects.equals(login, myUser.login) && Objects.equals(password, myUser.password) && Objects.equals(role, myUser.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, role);
    }

    public enum Role {

        USER("user"),
        ADMIN("admin");

        private final String roleName;

        Role(final String roleName) {
            this.roleName = roleName;
        }

        public static boolean isValidRole(final String roleName) {
            return USER.roleName.equals(roleName) || ADMIN.roleName.equals(roleName);
        }

        public static Role forRoleName(final String roleName) {
            Contracts.requireThat(isValidRole(roleName));

            if (ADMIN.roleName.equals(roleName)) {
                return ADMIN;
            } else {
                return USER;
            }
        }
    }
}
