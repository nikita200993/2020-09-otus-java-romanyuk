package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.config.GitConfig;
import ru.otus.vcs.utils.Utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.writeString;
import static ru.otus.vcs.utils.Utils.isDirectoryNoFollow;
import static ru.otus.vcs.utils.Utils.isRegularFileNoFollow;

class RepositoryLayout {

    // files
    static final String CONFIG = "config";
    static final String INDEX = "index";
    static final String DESCRIPTION = "description";
    static final String HEAD = "HEAD";
    static final String MERGE_HEAD = "MERGE_HEAD";
    // dirs
    static final String OBJECTS = "objects";
    static final String HEADS = "heads";

    static final String DEFAULT_HEAD_CONTENT = "master\n";
    static final String DEFAULT_DESCRIPTION_CONTENT =
            "Unnamed repository; edit this file 'description' to name the repository.\n";

    static void createLayout(final Path path) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireThat(Utils.isEmptyDir(path));

        try {
            createFile(path.resolve(INDEX));
            writeString(path.resolve(CONFIG), new GitConfig().toString(), StandardCharsets.UTF_8);
            writeString(path.resolve(HEAD), DEFAULT_HEAD_CONTENT, StandardCharsets.UTF_8);
            writeString(path.resolve(DESCRIPTION), DEFAULT_DESCRIPTION_CONTENT, StandardCharsets.UTF_8);
            createDirectory(path.resolve(OBJECTS));
            createDirectories(path.resolve(HEADS));
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't create repo layout", ex);
        }
    }

    static boolean isRepoLayout(final Path path) {
        Contracts.requireNonNullArgument(path);

        if (!isDirectoryNoFollow(path)) {
            return false;
        }
        return isRegularFileNoFollow(path.resolve(INDEX))
                && isRegularFileNoFollow(path.resolve(CONFIG))
                && isRegularFileNoFollow(path.resolve(HEAD))
                && isRegularFileNoFollow(path.resolve(DESCRIPTION))
                && isDirectoryNoFollow(path.resolve(OBJECTS))
                && isDirectoryNoFollow(path.resolve(HEADS));

    }
}
