package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Command;

@Command(name = "status", description = "show status of repository state")
public class Status extends GitCommand {

    @Override
    public void execute() {
        final String message = commandProcessor.status();
        if (!message.isBlank()) {
            System.out.println(message);
        }
    }
}
