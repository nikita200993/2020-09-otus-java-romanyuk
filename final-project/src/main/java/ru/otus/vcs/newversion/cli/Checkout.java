package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import javax.annotation.Nullable;

@Command(name = "checkout", description = "checkout branch or concrete file from branch")
public class Checkout extends GitCommand {

    @Arguments(required = true)
    private String refString;
    @Nullable
    @Option(name = "-f", arity = 1)
    private String filePath;

    @Override
    public void execute() {
        if (filePath == null) {
            commandProcessor.checkout(refString);
        } else {
            commandProcessor.checkoutFile(refString, filePath);
        }
    }
}
