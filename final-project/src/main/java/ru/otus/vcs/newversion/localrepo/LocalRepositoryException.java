package ru.otus.vcs.newversion.localrepo;

import ru.otus.vcs.exception.GitException;

public class LocalRepositoryException extends GitException {
    public LocalRepositoryException(final String message) {
        super(message);
    }

    public LocalRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
