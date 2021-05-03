package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Command;

@Command(name = "status")
public class Status extends GitCommand {

    @Override
    public void execute() {
        System.out.println(commandProcessor.status());
    }
}
