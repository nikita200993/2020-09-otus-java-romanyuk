package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.index.diff.VCSFileChange;

import javax.annotation.Nullable;
import java.util.List;


public class GitRepoStatus {
    private final List<VCSFileChange> uncommittedStagedChanges;
    @Nullable
    private final MergeConflicts mergeConflicts;

    public GitRepoStatus(
            final List<VCSFileChange> uncommittedStagedChanges,
            @Nullable final MergeConflicts mergeConflicts) {
        Contracts.requireNonNullArgument(uncommittedStagedChanges);

        this.uncommittedStagedChanges = List.copyOf(uncommittedStagedChanges);
        this.mergeConflicts = mergeConflicts;
    }

    public List<VCSFileChange> getUncommittedStagedChanges() {
        return uncommittedStagedChanges;
    }

    @Nullable
    public MergeConflicts getMergeConflicts() {
        return mergeConflicts;
    }
}
