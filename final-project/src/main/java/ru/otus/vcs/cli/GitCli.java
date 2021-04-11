package ru.otus.vcs.cli;

import io.airlift.airline.*;

public class GitCli {

    public static void main(final String[] args) {
        final var gitCLi = Cli.<Runnable>builder("git")
                .withDescription("version control system")
                .withCommand(Help.class)
                .withCommands(GitInit.class, GitAdd.class)
                .build();
        gitCLi.parse(args).run();
    }

}
