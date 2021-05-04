package ru.otus.vcs.newversion;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import io.airlift.airline.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.vcs.newversion.cli.Add;
import ru.otus.vcs.newversion.cli.Branch;
import ru.otus.vcs.newversion.cli.Checkout;
import ru.otus.vcs.newversion.cli.Commit;
import ru.otus.vcs.newversion.cli.Init;
import ru.otus.vcs.newversion.cli.Merge;
import ru.otus.vcs.newversion.cli.Remove;
import ru.otus.vcs.newversion.cli.Status;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        final var gitCli = Cli.<Runnable>builder("simplegit")
                .withDescription("version control system")
                .withDefaultCommand(Help.class)
                .withCommands(
                        Init.class,
                        Add.class,
                        Remove.class,
                        Branch.class,
                        Checkout.class,
                        Merge.class,
                        Commit.class,
                        Status.class)
                .build();
        try {
            gitCli.parse(args).run();
        } catch (final ParseException ex) {
            System.out.println("Bad command line arguments. " + ex.getMessage());
        } catch (final Throwable throwable) {
            logger.error("Critical error.", throwable);
        }
    }
}
