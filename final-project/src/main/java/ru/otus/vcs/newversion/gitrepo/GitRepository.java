package ru.otus.vcs.newversion.gitrepo;

import ru.otus.vcs.index.Index;
import ru.otus.vcs.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.gitrepo.errors.CheckoutBranchError;
import ru.otus.vcs.newversion.gitrepo.errors.CommitError;
import ru.otus.vcs.newversion.gitrepo.errors.FileReadError;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Ref;
import ru.otus.vcs.ref.Sha1;
import ru.otus.vcs.utils.Tuple2;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

public interface GitRepository {

    String DIR_NAME = ".simplegit";

    Tuple2<Sha1, CommitError> commit(final CommitMessage commitMessage);

    Path repoRealPath();

    void abortMerge();

    boolean add(byte[] data, VCSPath path);

    @Nullable
    Sha1 remove(VCSPath path);

    Sha1 hash(byte[] data);

    Tuple2<byte[], FileReadError> readFile(Ref ref, VCSPath vcsPath);

    Tuple2<List<VCSFileChange>, CheckoutBranchError> checkout(Ref ref);

    List<VCSFileChange> status();

    Index getIndex();

    class Status {
        private final List<VCSFileChange> uncommitedStagedChanges;
        @Nullable
        private final MergeConflicts mergeConflicts;

        private Status(final List<VCSFileChange> uncommittedStagedChanges, @Nullable final MergeConflicts mergeConflicts) {
            this.uncommitedStagedChanges = uncommittedStagedChanges;
            this.mergeConflicts = mergeConflicts;
        };
    }
}
