package ru.otus.vcs.newversion;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import ru.otus.vcs.newversion.cli.Add;
import ru.otus.vcs.newversion.cli.Branch;
import ru.otus.vcs.newversion.cli.Checkout;
import ru.otus.vcs.newversion.cli.Commit;
import ru.otus.vcs.newversion.cli.Init;
import ru.otus.vcs.newversion.cli.Remove;
import ru.otus.vcs.newversion.cli.Status;

public class Main {

    public static void main(final String[] args) {
        final var gitCli = Cli.<Runnable>builder("simplegit")
                .withDescription("version control system")
                .withDefaultCommand(Help.class)
                .withCommands(Init.class, Add.class, Remove.class, Branch.class, Checkout.class, Commit.class, Status.class)
                .build();
        gitCli.parse(args).run();
    }
}
