package ru.otus.vcs.newversion.localrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.gitrepo.GitRepoStatus;
import ru.otus.vcs.newversion.gitrepo.MergeConflicts;

import javax.annotation.Nullable;
import java.util.List;

public class LocalRepoStatus {
    private final GitRepoStatus gitRepoStatus;
    private final List<VCSFileChange> localFileChanges;

    public LocalRepoStatus(final GitRepoStatus gitRepoStatus, final List<VCSFileChange> localFileChanges) {
        Contracts.requireNonNullArgument(gitRepoStatus);
        Contracts.requireNonNullArgument(localFileChanges);

        this.gitRepoStatus = gitRepoStatus;
        this.localFileChanges = List.copyOf(localFileChanges);
    }

    public List<VCSFileChange> getLocalFileChanges() {
        return localFileChanges;
    }

    public List<VCSFileChange> getUncommittedChanges() {
        return gitRepoStatus.getUncommittedStagedChanges();
    }

    @Nullable
    public MergeConflicts getMergeConflicts() {
        return gitRepoStatus.getMergeConflicts();
    }
}
