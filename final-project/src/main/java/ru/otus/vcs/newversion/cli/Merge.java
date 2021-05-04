package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "merge", description = "merge current branch with target")
public class Merge extends GitCommand {

    @Arguments(required = true)
    private String refString;

    @Override
    public void execute() {
        if (commandProcessor.merge(refString)) {
            System.out.println("Successfully merged head with " + refString);
        } else {
            System.out.println("Please, resolve merge conflicts and commit them to finish merge");
        }
    }
}
