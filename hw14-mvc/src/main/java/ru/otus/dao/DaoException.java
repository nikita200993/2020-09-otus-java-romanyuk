package ru.otus.dao;

public class DaoException extends RuntimeException {

    public DaoException() {
        super();
    }

    public DaoException(final Throwable cause) {
        super(cause);
    }
}
