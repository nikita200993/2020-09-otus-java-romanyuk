package ru.otus.vcs.newversion;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import ru.otus.vcs.newversion.cli.Add;
import ru.otus.vcs.newversion.cli.Init;
import ru.otus.vcs.newversion.cli.Remove;

public class Main {

    public static void main(final String[] args) {
        final var gitCLi = Cli.<Runnable>builder("git")
                .withDescription("version control system")
                .withCommand(Help.class)
                .withCommands(Init.class, Add.class, Remove.class)
                .build();
        gitCLi.parse(args).run();
    }
}
