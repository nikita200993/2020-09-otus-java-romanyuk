package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.index.diff.Modification;
import ru.otus.vcs.newversion.ref.BranchName;
import ru.otus.vcs.newversion.ref.Ref;
import ru.otus.vcs.newversion.ref.Sha1;

import java.util.List;

public class MergeConflicts {

    private final Ref receiver;
    private final Ref giver;
    private final List<Modification> conflictingModifications;

    public MergeConflicts(final Ref receiver, final Ref giver, final List<Modification> conflictingModifications) {
        Contracts.requireNonNullArgument(receiver);
        Contracts.requireNonNullArgument(giver);
        Contracts.requireNonNullArgument(conflictingModifications);
        Contracts.requireThat(receiver instanceof Sha1 || receiver instanceof BranchName);
        Contracts.requireThat(giver instanceof Sha1 || giver instanceof BranchName);
        Contracts.forbidThat(conflictingModifications.isEmpty());

        this.receiver = receiver;
        this.giver = giver;
        this.conflictingModifications = List.copyOf(conflictingModifications);
    }

    public Ref getReceiver() {
        return receiver;
    }

    public Ref getGiver() {
        return giver;
    }

    public List<Modification> getConflictingChanges() {
        return conflictingModifications;
    }
}
