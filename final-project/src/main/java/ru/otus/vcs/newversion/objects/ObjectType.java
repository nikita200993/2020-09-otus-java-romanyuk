package ru.otus.vcs.newversion.objects;

import ru.otus.utils.Contracts;

import java.util.Set;

public enum ObjectType {

    Blob("blob"),
    Tree("tree"),
    Commit("commit");

    private static final Set<String> validNames = Set.of(Blob.name, Tree.name, Commit.name);

    private final String name;

    ObjectType(final String name) {
        this.name = name;
    }

    public static boolean isValidName(final String name) {
        Contracts.requireNonNull(name);

        return validNames.contains(name);
    }

    public static ObjectType forName(final String name) {
        Contracts.requireNonNullArgument(name);
        Contracts.requireThat(isValidName(name));

        if (Blob.name.equals(name)) {
            return Blob;
        } else if (Tree.name.equals(name)) {
            return Tree;
        } else if (Commit.name.equals(name)) {
            return Commit;
        } else {
            throw Contracts.unreachable();
        }
    }

    public String getName() {
        return name;
    }
}
