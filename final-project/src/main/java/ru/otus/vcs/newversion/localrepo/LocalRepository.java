package ru.otus.vcs.newversion.localrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.gitrepo.CommitMessage;
import ru.otus.vcs.newversion.gitrepo.MergeConflicts;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Ref;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

public interface LocalRepository {

    Path realRepoDir();

    void checkThatIsRepositoryPath(Path absolutePath) throws LocalRepositoryException;

    boolean add(VCSPath path);

    void remove(VCSPath path);

    void commit(CommitMessage message);

    void checkout(Ref ref);

    void checkoutFile(Ref ref, VCSPath path);

    StatusResult status();

    class StatusResult {

        private final List<VCSPath> untrackedFiles;
        private final List<VCSFileChange> localChanges;
        private final List<VCSFileChange> uncommittedStagedChanges;
        @Nullable
        private final MergeConflicts mergeConflicts;

        StatusResult(
                final List<VCSPath> untrackedFiles,
                final List<VCSFileChange> localChanges,
                final List<VCSFileChange> uncommittedStagedChanges,
                @Nullable final MergeConflicts mergeConflicts) {
            Contracts.requireNonNullArgument(untrackedFiles);
            Contracts.requireNonNullArgument(localChanges);
            Contracts.requireNonNullArgument(uncommittedStagedChanges);

            this.untrackedFiles = List.copyOf(untrackedFiles);
            this.localChanges = List.copyOf(localChanges);
            this.uncommittedStagedChanges = List.copyOf(uncommittedStagedChanges);
            this.mergeConflicts = mergeConflicts;
        }

        public List<VCSPath> getUntrackedFiles() {
            return untrackedFiles;
        }

        public List<VCSFileChange> getLocalChanges() {
            return localChanges;
        }

        public List<VCSFileChange> getUncommittedStagedChanges() {
            return uncommittedStagedChanges;
        }

        @Nullable
        public MergeConflicts getMergeConflicts() {
            return mergeConflicts;
        }
    }
}
