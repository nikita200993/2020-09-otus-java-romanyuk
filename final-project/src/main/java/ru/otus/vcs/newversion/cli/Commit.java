package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "commit")
public class Commit extends GitCommand {

    @Arguments(required = true)
    private String message;

    @Override
    public void execute() {
        commandProcessor.commit(message);
    }
}
