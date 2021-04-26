package ru.otus.vcs.newversion.gitrepo;

import ru.otus.vcs.exception.GitException;

public class GitRepositoryException extends GitException {
    public GitRepositoryException(final String message) {
        super(message);
    }

    public GitRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
