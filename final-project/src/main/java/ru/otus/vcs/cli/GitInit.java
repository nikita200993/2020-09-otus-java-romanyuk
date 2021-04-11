package ru.otus.vcs.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import javax.annotation.Nullable;

@Command(name = "init", description = "Init git repository")
public class GitInit extends GitCommand {

    @Arguments(description = "path to repository")
    @Nullable
    private String path;

    @Override
    public void run() {
        System.out.println("init command");
    }
}
