package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "branch", description = "create new branch")
public class Branch extends GitCommand {

    @Arguments(required = true)
    private String branchName;

    @Override
    public void execute() {
        commandProcessor.branch(branchName);
    }
}
