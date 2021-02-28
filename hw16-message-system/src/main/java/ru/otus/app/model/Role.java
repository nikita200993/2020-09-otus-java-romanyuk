package ru.otus.app.model;

import ru.otus.utils.Contracts;

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

    public String getRoleName() {
        return roleName;
    }
}
