package ru.otus.vcs.repository;

import ru.otus.utils.Contracts;
import ru.otus.vcs.config.GitConfig;
import ru.otus.vcs.exception.InnerException;
import ru.otus.vcs.exception.UserException;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class GitRepository {

    public static final String GITDIR = ".git";
    public static final String CONFIG = "config";

    private static final String BRANCHES = "branches";
    private static final String OBJECTS = "objects";
    private static final String REFS = "refs";
    private static final String TAGS = "tags";
    private static final String HEADS = "heads";
    private static final String DESCRIPTION = "description";
    private static final String HEAD = "HEAD";
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

    public static GitRepository find(final String dirToSearchUpwards) {
        try {
            final var realPath = Path.of(dirToSearchUpwards).toRealPath();
            return find(realPath, realPath);
        } catch (IOException ioException) {
            throw wrapAsRepoCreationException(ioException);
        }
    }

    public static GitRepository createNew(final String workdir) {
        Contracts.requireNonNullArgument(workdir);
        try {
            final var workdirPath = Path.of(workdir)
                    .toAbsolutePath()
                    .normalize();
            if (!Files.exists(workdirPath)) {
                throw new UserException("Directory " + workdir + "doesn't exist.");
            }
            if (!Files.isDirectory(workdirPath)) {
                throw new UserException(workdir + " is not a directory.");
            }
            if (Files.list(workdirPath).count() != 0) {
                throw new UserException(workdir + " is not empty.");
            }
            final var defaultConfig = new GitConfig();
            createRepoLayout(workdirPath.resolve(GITDIR), defaultConfig);
            return new GitRepository(workdirPath, workdirPath.resolve(GITDIR), defaultConfig);
        } catch (final InvalidPathException ex) {
            throw new UserException("Bad repository path = " + workdir);
        } catch (final FileAlreadyExistsException ex) {
            throw new UserException(String.format("Directory %s already contains file .git.", workdir));
        } catch (final IOException ex) {
            throw wrapAsRepoCreationException(ex);
        }
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

    private static GitRepository find(final Path currentDirToSearch, final Path startDir) {
        try {
            final Path repoPath = currentDirToSearch.resolve(GITDIR);
            if (Files.exists(repoPath) && Files.isDirectory(repoPath)) {
                final var config = GitConfig.create(repoPath.resolve(CONFIG));
                if (config.get(GitConfig.REPO_VER_KEY) != 0) {
                    throw new RepoCreationException("Config value for key '" + GitConfig.REPO_VER_KEY.getName()
                            + "' should be '0'");
                }
                return new GitRepository(
                        currentDirToSearch,
                        repoPath,
                        GitConfig.create(repoPath.resolve(CONFIG))
                );
            } else if (currentDirToSearch.getParent() == null) {
                throw new UserException("Can't find .git dir. Searched upwards provided dir " + startDir);
            } else {
                return find(currentDirToSearch.getParent(), startDir);
            }
        } catch (GitConfig.ConfigReadException ex) {
            throw new RepoCreationException(
                    "Can't create repo from " + currentDirToSearch.resolve(GITDIR) + ". " + ex.getMessage(),
                    ex
            );
        }
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
        } catch (final IOException ioException) {
            throw wrapAsRepoCreationException(ioException);
        }
    }

    private static RepoCreationException wrapAsRepoCreationException(final Exception ex) {
        return new RepoCreationException("Error creating new git repository. " + ex.getMessage(), ex);
    }

    public static class RepoCreationException extends InnerException {

        private RepoCreationException(final String message) {
            super(message);
        }

        private RepoCreationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
