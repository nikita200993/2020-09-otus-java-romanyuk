package ru.otus.vcs.newversion.exception;

public class UserException extends GitException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
