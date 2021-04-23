package ru.otus.vcs.repository;

import ru.otus.utils.Contracts;
import ru.otus.vcs.config.GitConfig;
import ru.otus.vcs.exception.InnerException;
import ru.otus.vcs.index.Index;
import ru.otus.vcs.objects.GitObject;
import ru.otus.vcs.ref.Sha1;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static ru.otus.vcs.utils.Utils.compress;
import static ru.otus.vcs.utils.Utils.decompress;

public class GitRepository {

    public static final String GITDIR = ".simplegit";
    public static final String CONFIG = "config";

    private static final String BRANCHES = "branches";
    private static final String OBJECTS = "objects";
    private static final String REFS = "refs";
    private static final String TAGS = "tags";
    private static final String HEADS = "heads";
    private static final String DESCRIPTION = "description";
    private static final String HEAD = "HEAD";
    private static final String INDEX = "index";
    private static final String DEFAULT_HEAD_CONTENT = "ref: refs/heads/master" + System.lineSeparator();
    private static final String DEFAULT_DESCRIPTION_CONTENT = "Unnamed repository; edit this file 'description'"
            + "to name the repository." + System.lineSeparator();

    private final Path workdir;
    private final Path gitDir;
    private final GitConfig config;

    private GitRepository(final Path workdir, final Path gitDir, final GitConfig config) {
        this.workdir = workdir;
        this.gitDir = gitDir;
        this.config = config;
    }

    public static GitRepository createNew(final Path pathToEmptyDir) {
        Contracts.requireNonNullArgument(pathToEmptyDir);
        checkContractsOnTheNewRepoPath(pathToEmptyDir);

        try {
            final var realPathToEmptyDir = pathToEmptyDir.toRealPath(LinkOption.NOFOLLOW_LINKS);
            final var defaultConfig = new GitConfig();
            final var gitdir = realPathToEmptyDir.resolve(GITDIR);
            createRepoLayout(gitdir, defaultConfig);
            return new GitRepository(realPathToEmptyDir, gitdir, defaultConfig);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to create repo for path = '" + pathToEmptyDir + "'.", ex);
        }
    }

    @Nullable
    public static GitRepository find(final Path currentPathToSearch) {
        Contracts.requireNonNullArgument(currentPathToSearch);
        Contracts.requireThat(Files.exists(currentPathToSearch, LinkOption.NOFOLLOW_LINKS));

        try {
            final var realPath = currentPathToSearch.toRealPath(LinkOption.NOFOLLOW_LINKS);
            final Path repoPath = realPath.resolve(GITDIR);
            if (Files.isDirectory(repoPath) && isRepositoryLayout(repoPath)) {
                final var config = GitConfig.create(repoPath.resolve(CONFIG));
                return new GitRepository(
                        realPath,
                        repoPath,
                        config
                );
            } else if (realPath.getParent() == null) {
                return null;
            } else {
                return find(realPath.getParent());
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error while searching for git repository.", ex);
        }
    }

    public Index readIndex() {
        try {
            return Index.deserialize(Files.readAllBytes(gitDir.resolve(INDEX)));
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error reading index file.", ex);
        }
    }

    public void saveIndex(final Index index) {
        Contracts.requireNonNullArgument(index);
        try {
            Files.write(gitDir.resolve(INDEX), index.serialize());
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error while writing index to file.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public <V extends GitObject> V readGitObjectOrThrowIfAbsent(final Sha1 sha) {
        Contracts.requireNonNullArgument(sha);

        try {
            final String dirName = sha.getHexString().substring(0, 2);
            final String fileName = sha.getHexString().substring(2);
            final Path pathToFile = repoPath(Path.of(OBJECTS, dirName, fileName));

            Contracts.requireThat(Files.exists(pathToFile, LinkOption.NOFOLLOW_LINKS));

            final byte[] raw = decompress(Files.readAllBytes(pathToFile));
            return (V) GitObject.deserialize(raw);
        } catch (final IOException ex) {
            throw new InnerException("Can't read object for sha = " + sha + ".", ex);
        }
    }

    /**
     * Saves git object and returns its sha1 hex representation.
     *
     * @param gitObject object to save.
     * @return sha1 hex.
     */
    public Sha1 saveGitObjectIfAbsentAndReturnSha(final GitObject gitObject) {
        Contracts.requireNonNullArgument(gitObject);

        final var bytes = gitObject.serialize();
        final var result = Sha1.hash(bytes);
        final String shaHex = result.getHexString();
        final Path savePath = repoPath(Path.of(OBJECTS, shaHex.substring(0, 2), shaHex.substring(2)));
        try {
            if (!Files.exists(savePath)) {
                Files.createDirectories(savePath.getParent());
                Files.write(savePath, compress(bytes), StandardOpenOption.CREATE_NEW);
                savePath.toFile().setReadOnly();
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't save git object with sha '" + shaHex + "'.", ex);
        }
        return result;
    }

    public Path repoPath(final Path relative) {
        return gitDir.resolve(relative);
    }

    public Path getWorkdir() {
        return workdir;
    }

    public GitConfig getConfig() {
        return config;
    }

    public Path getGitDir() {
        return gitDir;
    }

    private static void createRepoLayout(final Path gitdir, final GitConfig config) {
        try {
            Files.createDirectory(gitdir);
            Files.writeString(gitdir.resolve(CONFIG), config.toString());
            Files.createDirectory(gitdir.resolve(BRANCHES));
            Files.createDirectory(gitdir.resolve(OBJECTS));
            Files.createDirectories(gitdir.resolve(REFS).resolve(TAGS));
            Files.createDirectories(gitdir.resolve(REFS).resolve(HEADS));
            Files.writeString(gitdir.resolve(DESCRIPTION), DEFAULT_DESCRIPTION_CONTENT);
            Files.writeString(gitdir.resolve(HEAD), DEFAULT_HEAD_CONTENT);
            Files.createFile(gitdir.resolve(INDEX));
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't create layout for repository at dir = " + gitdir + ".", ex);
        }
    }


    private static boolean isRepositoryLayout(final Path gitdir) {
        return isLayoutFileIsPresent(gitdir, Path.of(CONFIG))
                && isLayoutDirIsPresent(gitdir, Path.of(BRANCHES))
                && isLayoutDirIsPresent(gitdir, Path.of(OBJECTS))
                && isLayoutDirIsPresent(gitdir, Path.of(REFS))
                && isLayoutDirIsPresent(gitdir, Path.of(REFS).resolve(TAGS))
                && isLayoutDirIsPresent(gitdir, Path.of(REFS).resolve(HEADS))
                && isLayoutFileIsPresent(gitdir, Path.of(DESCRIPTION))
                && isLayoutFileIsPresent(gitdir, Path.of(HEAD))
                && isLayoutFileIsPresent(gitdir, Path.of(INDEX));
    }

    private static boolean isLayoutDirIsPresent(final Path gitdir, final Path relativePath) {
        return Files.isDirectory(gitdir.resolve(relativePath));
    }

    private static boolean isLayoutFileIsPresent(final Path gitdir, final Path relativePath) {
        return Files.isRegularFile(gitdir.resolve(relativePath));
    }

    private static void checkContractsOnTheNewRepoPath(final Path realPathToEmptyDir) {
        try {
            Contracts.requireThat(
                    Files.isDirectory(realPathToEmptyDir),
                    realPathToEmptyDir + " is not an empty dir."
            );
            Contracts.requireThat(Files.list(realPathToEmptyDir).count() == 0, "Dir is not empty.");
        } catch (final IOException ex) {
            throw new UncheckedIOException(
                    "IO error while checking contracts for path = '" + realPathToEmptyDir + "'.",
                    ex);
        }
    }
}
