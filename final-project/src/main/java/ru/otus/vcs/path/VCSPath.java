package ru.otus.vcs.path;

import ru.otus.utils.Contracts;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class VCSPath {

    public static final VCSPath root = new VCSPath(Collections.emptyList());

    private static final String separator = "/";

    private final List<VCSFileName> path;

    private VCSPath(final List<VCSFileName> path) {
        this.path = path;
    }

    public static String getSeparator() {
        return separator;
    }

    public static VCSPath create(final String path) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireThat(isValidVCSPathString(path));

        return new VCSPath(
                Arrays.stream(path.split(separator))
                        .map(VCSFileName::create)
                        .collect(toList())
        );
    }

    public static VCSPath create(final Path path) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireThat(isValidVCSPath(path));

        return create(path.toString().replace(File.separator, separator));
    }

    public static boolean isValidVCSPathString(final String path) {
        Contracts.requireNonNull(path);
        final var split = path.split(separator);
        if (split.length == 0) {
            return false;
        }
        return Arrays.stream(split)
                .allMatch(VCSFileName::isValidVCSFileName);
    }

//    TODO: test "/afd"
    public static boolean isValidVCSPath(final Path path) {
        Contracts.requireNonNull(path);

        return isValidVCSPathString(path.toString().replace(File.separator, separator));
    }

    public VCSPath resolve(final VCSFileName fileName) {
        final var copy = new ArrayList<>(path);
        copy.add(fileName);
        return new VCSPath(copy);
    }

    public Path toOsPath() {
        return Path.of(
                path.stream()
                        .map(VCSFileName::getName)
                        .collect(joining(File.separator))
        );
    }

    public String toUnixPathString() {
        return path.stream()
                .map(VCSFileName::getName)
                .collect(joining(separator));
    }

    public VCSFileName getFileName() {
        return path.get(path.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VCSPath vcsPath = (VCSPath) o;
        return path.equals(vcsPath.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path.stream()
                .map(VCSFileName::getName)
                .collect(joining(separator));
    }
}
