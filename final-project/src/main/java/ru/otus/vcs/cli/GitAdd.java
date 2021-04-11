package ru.otus.vcs.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "add", description = "Add file contents to the index")
public class GitAdd extends GitCommand {

    @Arguments(description = "file or dir to be added to index", required = true)
    private String path;

    @Override
    public void run() {
        System.out.println("add command " + path);
    }
}
