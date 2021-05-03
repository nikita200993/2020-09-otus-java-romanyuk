package ru.otus.vcs.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.otus.vcs.newversion.exception.UserException;

import javax.annotation.Nullable;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@Command(name = "init", description = "Init git repository")
public class GitInit extends GitCommand {

    @Arguments(description = "path to repository", title = "path")
    @Nullable
    private String stringPath;

    @Override
    public void run() {
        try {
            final var path = Path.of(stringPath  == null ? "" : stringPath);
            throw new UnsupportedOperationException();
        } catch (final InvalidPathException ex) {
            throw new UserException("Bad path '" + stringPath + "'.");
        }
    }
}
