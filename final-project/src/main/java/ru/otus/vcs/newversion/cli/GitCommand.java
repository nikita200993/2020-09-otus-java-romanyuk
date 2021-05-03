package ru.otus.vcs.newversion.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.vcs.newversion.exception.GitException;
import ru.otus.vcs.newversion.commands.CommandProcessor;
import ru.otus.vcs.newversion.gitrepo.GitRepositoryFactoryImpl;

abstract class GitCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    protected final CommandProcessor commandProcessor = new CommandProcessor(new GitRepositoryFactoryImpl());

    public abstract void execute();

    @Override
    public void run() {
        try {
            execute();
        } catch (final GitException ex) {
            final var message = ex.toUserMessage();
            System.out.println(message);
        } catch (final Exception ex) {
            logger.error("Error executing command.", ex);
        }
    }
}
