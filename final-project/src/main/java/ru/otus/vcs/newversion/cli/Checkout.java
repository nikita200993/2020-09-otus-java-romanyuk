package ru.otus.vcs.newversion.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import javax.annotation.Nullable;

@Command(name = "checkout")
public class Checkout extends GitCommand {

    @Arguments(required = true)
    private String refString;
    @Nullable
    @Arguments
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
