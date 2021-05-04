package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.vcs.newversion.commands.RemoveOption;

@Command(name = "rm", description = "removes file from index and fs.")
public class Remove extends GitCommand {

    @Arguments(required = true)
    private String stringPath;
    @Option(name = {"-f", "--force"})
    private boolean force;
    @Option(name = {"-c", "--cached"})
    private boolean cached;


    @Override
    public void execute() {
        final RemoveOption removeOption;
        if (force) {
            removeOption = RemoveOption.Force;
        } else if (cached) {
            removeOption = RemoveOption.Cached;
        } else {
            removeOption = RemoveOption.Normal;
        }
        commandProcessor.remove(stringPath, removeOption);
    }
}
