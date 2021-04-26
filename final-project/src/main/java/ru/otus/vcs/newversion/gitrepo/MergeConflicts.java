package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.BranchName;
import ru.otus.vcs.ref.Ref;
import ru.otus.vcs.ref.Sha1;

import java.util.List;

public class MergeConflicts {

    private final Ref receiver;
    private final Ref giver;
    private final List<VCSPath> conflictingPaths;

    public MergeConflicts(final Ref receiver, final Ref giver, final List<VCSPath> conflictingPaths) {
        Contracts.requireNonNullArgument(receiver);
        Contracts.requireNonNullArgument(giver);
        Contracts.requireNonNullArgument(conflictingPaths);
        Contracts.requireThat(receiver instanceof Sha1 || receiver instanceof BranchName);
        Contracts.requireThat(giver instanceof Sha1 || giver instanceof BranchName);
        Contracts.forbidThat(conflictingPaths.isEmpty());

        this.receiver = receiver;
        this.giver = giver;
        this.conflictingPaths = List.copyOf(conflictingPaths);
    }

    public Ref getReceiver() {
        return receiver;
    }

    public Ref getGiver() {
        return giver;
    }

    public List<VCSPath> getConflictingChanges() {
        return conflictingPaths;
    }
}
