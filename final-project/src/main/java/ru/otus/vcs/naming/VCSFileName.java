package ru.otus.vcs.naming;

import ru.otus.utils.Contracts;

import java.util.regex.Pattern;

public final class VCSFileName {

    private final static Pattern pattern = Pattern.compile("^[a-zA-Z0-9-.]{1,1000}$");

    private final String name;

    private VCSFileName(final String name) {
        this.name = name;
    }

    public static String getPatternString() {
        return pattern.pattern();
    }

    public static VCSFileName create(final String name) {
        Contracts.requireNonNullArgument(name);
        Contracts.requireThat(isValidVCSFileName(name));

        return new VCSFileName(name);
    }

    public static boolean isValidVCSFileName(final String name) {
        Contracts.requireNonNullArgument(name);

        return !".".equals(name) && !"..".equals(name) && pattern.asMatchPredicate().test(name);
    }

    public static String hint() {
        return "name shouldn't equal to '.' or '..' and should follow " + pattern.pattern();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VCSFileName that = (VCSFileName) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "VCSFileName{" +
                "name='" + name + '\'' +
                '}';
    }
}
