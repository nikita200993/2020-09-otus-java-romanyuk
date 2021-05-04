package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "add", description = "adds regular file to index")
public class Add extends GitCommand {

    @Arguments(required = true)
    private String path;

    @Override
    public void execute() {
        commandProcessor.add(path);
    }
}
