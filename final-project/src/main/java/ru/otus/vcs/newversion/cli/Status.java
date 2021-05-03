package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Command;

@Command(name = "status", description = "show status of repository state")
public class Status extends GitCommand {

    @Override
    public void execute() {
        System.out.println(commandProcessor.status());
    }
}
