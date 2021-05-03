package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import javax.annotation.Nullable;

@Command(name = "init", description = "initializes new repo from empty dir")
public class Init extends GitCommand {

    @Nullable
    @Arguments
    private String stringPath;

    @Override
    public void execute() {
        commandProcessor.init(stringPath == null ? "" : stringPath);
    }
}
