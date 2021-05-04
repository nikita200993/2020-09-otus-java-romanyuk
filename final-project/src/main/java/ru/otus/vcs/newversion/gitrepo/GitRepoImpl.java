package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.config.GitConfig;
import ru.otus.vcs.newversion.index.Index;
import ru.otus.vcs.newversion.index.diff.Addition;
import ru.otus.vcs.newversion.index.diff.Modification;
import ru.otus.vcs.newversion.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.objects.Blob;
import ru.otus.vcs.newversion.objects.Commit;
import ru.otus.vcs.newversion.objects.GitObject;
import ru.otus.vcs.newversion.objects.Tree;
import ru.otus.vcs.newversion.path.VCSFileDesc;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.BranchName;
import ru.otus.vcs.newversion.ref.Ref;
import ru.otus.vcs.newversion.ref.ReservedRef;
import ru.otus.vcs.newversion.ref.Sha1;
import ru.otus.vcs.newversion.utils.Tuple2;
import ru.otus.vcs.newversion.utils.Utils;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class GitRepoImpl implements GitRepository {

    private final Path repoRoot;
    private final GitConfig config;

    GitRepoImpl(final Path repoRoot, final GitConfig config) {
        Contracts.requireNonNullArgument(repoRoot);
        Contracts.requireNonNullArgument(config);

        this.repoRoot = Utils.toReal(repoRoot);
        this.config = config;
    }

    @Override
    public void branch(final BranchName branchName) {
        Contracts.requireNonNullArgument(branchName);

        final var headCommit = readCommitOrNull(ReservedRef.head);
        if (headCommit == null) {
            throw new GitRepositoryException("Can't create branch. No commit on current head.");
        }
        final Path pathToBranchFile = repoRoot.resolve(RepositoryLayout.HEADS)
                .resolve(branchName.getBranchName());
        if (Utils.isRegularFileNoFollow(pathToBranchFile)) {
            throw new GitRepositoryException("Branch with name " + branchName.getBranchName() + " already exists.");
        }
        Utils.writeUtf8(pathToBranchFile, headCommit.sha1().getHexString() + "\n");
    }

    @Override
    public Sha1 commit(final CommitMessage commitMessage) {
        Contracts.requireNonNullArgument(commitMessage);

        final var index = getIndex();
        if (index.hasMergeConflict()) {
            throw new GitRepositoryException(
                    String.format(
                            "Resolve merge conflicts. Conflicts:%n%s",
                            index.getConflictPaths()
                                    .stream()
                                    .map(VCSPath::toOsPath)
                                    .map(Path::toString)
                                    .collect(
                                            joining(System.lineSeparator())
                                    )
                    )
            );
        } else if (index.isEmpty()) {
            throw new GitRepositoryException("Nothing to commit. No files were staged.");
        }
        final var headCommit = readCommitOrNull(ReservedRef.head);
        if (headCommit == null) {
            return commitEmptyRepo(commitMessage, index);
        }
        final var mergeHeadCommit = readCommitOrNull(ReservedRef.mergeHead);
        if (mergeHeadCommit != null) {
            return commitMerge(commitMessage, headCommit, mergeHeadCommit, index);
        } else {
            return commitSimple(commitMessage, headCommit, index);
        }
    }

    @Override
    public Path repoRealPath() {
        return repoRoot;
    }

    @Override
    public Tuple2<List<VCSFileChange>, MergeConflicts> startMerge(final Ref ref) {
        Contracts.requireNonNullArgument(ref);

        if (!(ref instanceof BranchName)) {
            throw new UnsupportedOperationException();
        }
        checkMergeNotInProgress(() -> new GitRepositoryException("Finish previous merge."));
        final var headCommit = readCommitOrNull(ReservedRef.head);
        if (headCommit == null) {
            throw new GitRepositoryException("No commits on current HEAD.");
        }
        checkNoChangesWithHead(getIndex(), () -> new GitRepositoryException("There are uncommitted changes."));
        final var branchName = (BranchName) ref;
        final var targetCommit = readCommitOrNull(branchName);
        if (targetCommit == null) {
            throw new GitRepositoryException("No commit for target branch " + branchName.getBranchName() + ".");
        }
        if (targetCommit.sha1().equals(headCommit.sha1())) {
            throw new GitRepositoryException("Target branch commit is the same as head");
        }
        final var parentsOfHead = getAllParents(headCommit);
        final var parentsOfTarget = getAllParents(targetCommit);
        if (parentsOfHead.contains(targetCommit.sha1())) {
            throw new GitRepositoryException("Current head is ahead of target.");
        }
        if (parentsOfTarget.contains(headCommit.sha1())) {
            return new Tuple2<>(startFastForwardMerge(headCommit, targetCommit), null);
        }
        Sha1 firstCommonParent = null;
        for (final var parentSha : parentsOfHead) {
            if (parentsOfTarget.contains(parentSha)) {
                firstCommonParent = parentSha;
                break;
            }
        }
        Contracts.requireNonNullArgument(firstCommonParent);
        final var baseCommit = Contracts.ensureNonNull(readCommitOrNull(firstCommonParent));
        return startLinearMerge(baseCommit, headCommit, targetCommit);
    }

    @Override
    public void finishMerge() {
        final var headCommit = Contracts.ensureNonNull(readCommitOrNull(ReservedRef.head));
        final var mergeHeadCommit = Contracts.ensureNonNull(readCommitOrNull(ReservedRef.mergeHead));
        final var index = getIndex();
        Contracts.forbidThat(index.hasMergeConflict());
        final var trees = Tree.createFromIndex(index);
        trees.forEach(this::saveGitObjectIfAbsent);
        final var newCommit = new Commit(
                trees.get(0).sha1(),
                headCommit.sha1(),
                mergeHeadCommit.sha1(),
                config.get(GitConfig.USER),
                CommitMessage.create("Merge " + headCommit.sha1().getHexString() + " and " + mergeHeadCommit.sha1().getHexString() + ".")
        );
        saveGitObjectIfAbsent(newCommit);
        Utils.delete(repoRoot.resolve(RepositoryLayout.MERGE_HEAD));
        updateHeadAfterCommit(newCommit.sha1());
    }

    @Override
    public void abortMerge() {
        final var commit = Contracts.ensureNonNull(readCommitOrNull(ReservedRef.head));
        final var tree = Contracts.ensureNonNull(readTreeOrNull(commit.getTreeSha()));
        final var index = tree.index(sha -> Contracts.ensureNonNull(readTreeOrNull(sha)));
        saveIndex(index);
        Utils.delete(repoRoot.resolve(RepositoryLayout.MERGE_HEAD));
    }

    @Override
    public void add(final byte[] data, final VCSPath path) {
        Contracts.requireNonNullArgument(data);
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var index = getIndex();
        final var blob = new Blob(data);
        saveGitObjectIfAbsent(blob);
        final var newIndex = index.withNewIndexEntry(path, blob.sha1());
        saveIndex(newIndex);
    }

    @Override
    public boolean remove(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var index = getIndex();
        final var newIndex = index.withRemovedIndexEntry(path);
        if (newIndex == null) {
            return false;
        }
        saveIndex(newIndex);
        return true;
    }

    @Override
    public Sha1 hash(byte[] data) {
        return new Blob(data).sha1();
    }

    @Override
    public byte[] readFile(final Ref ref, final VCSPath vcsPath) {
        Contracts.requireNonNullArgument(ref);
        Contracts.requireNonNullArgument(vcsPath);
        Contracts.forbidThat(vcsPath.isRoot());

        final Commit referredCommit;
        if (ref instanceof Sha1) {
            final var sha = (Sha1) ref;
            final var gitObject = readGitObjectOrNull(sha);
            if (!(gitObject instanceof Commit)) {
                throw new GitRepositoryException("Provided sha " + sha.getHexString() + " refers not to commit.");
            }
            referredCommit = (Commit) gitObject;
        } else {
            referredCommit = readCommitOrNull(ref);
        }
        if (referredCommit == null) {
            throw new GitRepositoryException("Ref should refer to commit.");
        }
        final var index = indexFromTreeRef(referredCommit.getTreeSha());
        if (!index.contains(vcsPath)) {
            throw new GitRepositoryException("File for provided ref and path doesn't exist");
        }
        final var sha = index.hashOfPath(vcsPath);
        final var blob = Contracts.ensureNonNull(readBlobOrNull(sha));
        return blob.getContent();
    }

    @Override
    public byte[] readFile(final Sha1 sha) {
        Contracts.requireNonNullArgument(sha);

        return Contracts.ensureNonNull(readBlobOrNull(sha)).getContent();
    }

    @Override
    public List<VCSFileChange> checkoutChanges(final Ref ref) {
        Contracts.requireNonNullArgument(ref);

        checkIsValidCheckoutRef(ref, () -> new GitRepositoryException("Bad checkout ref " + ref + "."));
        checkMergeNotInProgress(() -> new GitRepositoryException("Finish merge."));
        final var staged = getIndex();
        checkNoChangesWithHead(staged, () -> new GitRepositoryException("There are uncommitted changes"));
        final Commit checkoutCommit = readCommitForUserProvidedRef(ref);
        final var checkoutIndex = indexFromTreeRef(checkoutCommit.getTreeSha());
        return checkoutIndex.getDiff(staged);
    }

    @Override
    public void checkout(final Ref ref) {
        Contracts.requireNonNullArgument(ref);

        checkIsValidCheckoutRef(ref, () -> new IllegalStateException("Bad checkout ref " + ref + "."));
        checkMergeNotInProgress(() -> new IllegalStateException("Can't call checkout while merge in progress"));
        checkNoChangesWithHead(
                getIndex(),
                () -> new IllegalStateException("Can't call checkout with uncommitted changes")
        );
        final Commit checkoutCommit = Contracts.ensureNonNull(readCommitOrNull(ref));
        saveIndex(indexFromTreeRef(checkoutCommit.getTreeSha()));
        updateHeadForCheckout(ref, checkoutCommit.sha1());
    }

    @Override
    public GitRepoStatus status() {
        final var staged = getIndex();
        final var headCommit = readCommitOrNull(ReservedRef.head);
        if (headCommit == null) {
            Contracts.forbidThat(mergeInProgress());
            return new GitRepoStatus(
                    staged.getDiff(Index.create(emptyList())),
                    null
            );
        }

        final Index headIndex = indexFromTreeRef(headCommit.getTreeSha());
        final List<VCSFileChange> nonConflictUncommittedChanges = staged.withDroppedConflicts()
                .getDiff(headIndex);
        final var mergeConflicts = staged.getMergeConflicts();
        if (!mergeConflicts.isEmpty()) {
            final var mergeHeadCommit = Contracts.ensureNonNull(readCommitOrNull(ReservedRef.mergeHead));
            return new GitRepoStatus(
                    nonConflictUncommittedChanges,
                    new MergeConflicts(
                            headCommit.sha1(),
                            mergeHeadCommit.sha1(),
                            mergeConflicts
                    )
            );
        } else {
            return new GitRepoStatus(
                    nonConflictUncommittedChanges,
                    null
            );
        }
    }

    @Override
    public Index getIndex() {
        return Index.deserialize(Utils.readBytes(repoRoot.resolve(RepositoryLayout.INDEX)));
    }

    @Nullable
    Commit readCommitOrNull(final Ref ref) {
        if (ref instanceof ReservedRef) {
            return readCommitOrNull((ReservedRef) ref);
        } else if (ref instanceof BranchName) {
            return readCommitOrNull((BranchName) ref);
        } else if (ref instanceof Sha1) {
            return readCommitOrNull((Sha1) ref);
        } else {
            throw Contracts.unreachable();
        }
    }

    private void saveIndex(final Index index) {
        Utils.writeBytes(repoRoot.resolve(RepositoryLayout.INDEX), index.serialize());
    }

    private void saveGitObjectIfAbsent(final GitObject gitObject) {
        final var sha = gitObject.sha1();
        final var pathToFile = pathToFile(sha);
        if (Files.exists(pathToFile)) {
            return;
        }
        Utils.createDir(pathToFile.getParent());
        Utils.writeBytes(pathToFile, gitObject.serialize());
    }

    @Nullable
    private GitObject readGitObjectOrNull(final Sha1 sha1) {
        final var rawContent = readRawObjectOrNull(pathToFile(sha1));
        return rawContent == null ? null : GitObject.deserialize(rawContent);
    }

    @Nullable
    private Tree readTreeOrNull(final Sha1 sha1) {

        final var gitObject = readGitObjectOrNull(sha1);
        if (gitObject == null) {
            return null;
        }
        Contracts.requireThat(gitObject instanceof Tree);
        return (Tree) gitObject;
    }

    @Nullable
    private Commit readCommitOrNull(final Sha1 sha1) {
        final var gitObject = readGitObjectOrNull(sha1);
        if (gitObject == null) {
            return null;
        }
        Contracts.requireThat(gitObject instanceof Commit);
        return (Commit) gitObject;
    }

    @Nullable
    private Commit readCommitOrNull(final ReservedRef reservedRef) {
        @Nullable final String content;
        if (reservedRef.equals(ReservedRef.head)) {
            content = readHeadContent();
        } else if (reservedRef.equals(ReservedRef.mergeHead)) {
            content = readMergeHeadContentOrNull();
        } else {
            throw Contracts.unreachable();
        }
        if (content == null) {
            return null;
        }
        if (Sha1.isValidSha1HexString(content)) {
            return readCommitOrNull(Sha1.create(content));
        } else if (BranchName.isValidBranchName(content)) {
            return readCommitOrNull(BranchName.create(content));
        } else {
            throw Contracts.unreachable();
        }
    }

    @Nullable
    private Commit readCommitOrNull(final BranchName branchName) {
        final var content = readBranchContentOrNull(branchName.getBranchName());
        if (content == null) {
            return null;
        }
        Contracts.requireThat(Sha1.isValidSha1HexString(content));
        return readCommitOrNull(Sha1.create(content));
    }

    @Nullable
    private Blob readBlobOrNull(final Sha1 sha1) {
        final var gitObject = readGitObjectOrNull(sha1);
        if (gitObject == null) {
            return null;
        }
        Contracts.requireThat(gitObject instanceof Blob);
        return (Blob) gitObject;
    }

    private Path pathToFile(final Sha1 sha1) {
        final var dirName = sha1.getHexString().substring(0, 2);
        final var fileName = sha1.getHexString().substring(2);
        return repoRoot.resolve(RepositoryLayout.OBJECTS)
                .resolve(dirName)
                .resolve(fileName);
    }

    @Nullable
    private byte[] readRawObjectOrNull(final Path path) {
        if (Files.exists(path)) {
            return Utils.readBytes(path);
        } else {
            return null;
        }
    }

    @Nullable
    private String readBranchContentOrNull(final String branchName) {
        final Path path = repoRoot.resolve(RepositoryLayout.HEADS)
                .resolve(branchName);
        if (Files.exists(path)) {
            return Utils.readUtf8(path).stripTrailing();
        } else {
            return null;
        }
    }

    private String readHeadContent() {
        return Utils.readUtf8(repoRoot.resolve(RepositoryLayout.HEAD)).stripTrailing();
    }

    @Nullable
    private String readMergeHeadContentOrNull() {
        final var path = repoRoot.resolve(RepositoryLayout.MERGE_HEAD);
        if (Files.exists(path)) {
            return Utils.readUtf8(path).stripTrailing();
        } else {
            return null;
        }
    }

    private Sha1 commitEmptyRepo(final CommitMessage message, final Index index) {
        final var trees = Tree.createFromIndex(index);
        trees.forEach(this::saveGitObjectIfAbsent);
        final var commit = new Commit(
                trees.get(0).sha1(),
                null,
                null,
                config.get(GitConfig.USER),
                message
        );
        saveGitObjectIfAbsent(commit);
        updateHeadAfterCommit(commit.sha1());
        return commit.sha1();
    }

    private Sha1 commitSimple(final CommitMessage commitMessage, final Commit headCommit, final Index staged) {
        final var tree = Contracts.ensureNonNull(readTreeOrNull(headCommit.getTreeSha()));
        final var headIndex = tree.index((sha) -> Contracts.ensureNonNull(readTreeOrNull(sha)));
        if (headIndex.getFileDescriptors().equals(staged.getFileDescriptors())) {
            throw new GitRepositoryException("Nothing to commit. No changes with head.");
        }
        final var trees = Tree.createFromIndex(staged);
        trees.forEach(this::saveGitObjectIfAbsent);
        final var commit = new Commit(
                trees.get(0).sha1(),
                headCommit.sha1(),
                null,
                config.get(GitConfig.USER),
                commitMessage
        );
        saveGitObjectIfAbsent(commit);
        updateHeadAfterCommit(commit.sha1());
        return commit.sha1();
    }

    private Sha1 commitMerge(
            final CommitMessage message,
            final Commit headCommit,
            final Commit mergeCommit,
            final Index index) {
        final var trees = Tree.createFromIndex(index);
        trees.forEach(this::saveGitObjectIfAbsent);
        final var commit = new Commit(
                trees.get(0).sha1(),
                headCommit.sha1(),
                mergeCommit.sha1(),
                config.get(GitConfig.USER),
                message
        );
        saveGitObjectIfAbsent(commit);
        updateHeadAfterCommit(commit.sha1());
        Utils.delete(repoRoot.resolve(RepositoryLayout.MERGE_HEAD));
        return commit.sha1();
    }

    private void updateHeadAfterCommit(final Sha1 sha) {
        final var headContent = readHeadContent();
        if (Sha1.isValidSha1HexString(headContent)) {
            Utils.writeUtf8(repoRoot.resolve(RepositoryLayout.HEAD), sha.getHexString() + "\n");
        } else {
            Contracts.requireThat(BranchName.isValidBranchName(headContent));
            Utils.writeUtf8(repoRoot.resolve(RepositoryLayout.HEADS).resolve(headContent), sha.getHexString() + "\n");
        }
    }

    private Index indexFromTreeRef(final Sha1 sha1) {
        return Contracts.ensureNonNull(readTreeOrNull(sha1))
                .index((sha) -> Contracts.ensureNonNull(readTreeOrNull(sha)));
    }

    private boolean mergeInProgress() {
        return Utils.isRegularFileNoFollow(repoRoot.resolve(RepositoryLayout.MERGE_HEAD));
    }

    private Commit readCommitForUserProvidedRef(final Ref ref) {
        if (ReservedRef.mergeHead.equals(ref)) {
            throw new GitRepositoryException("Can't refer to merge head. It is inner structure.");
        }
        final Commit commit;
        if (ref instanceof Sha1) {
            final var sha = (Sha1) ref;
            final var gitObject = readGitObjectOrNull(sha);
            if (!(gitObject instanceof Commit)) {
                throw new GitRepositoryException("Sha " + sha + " refers not to commit");
            }
            commit = (Commit) gitObject;
        } else {
            commit = readCommitOrNull(ref);
        }
        if (commit == null) {
            throw new GitRepositoryException("No commit for ref.");
        }
        return commit;
    }

    private void checkNoChangesWithHead(final Index indexToCheck, final Supplier<? extends RuntimeException> exc) {
        final var headCommit = readCommitOrNull(ReservedRef.head);
        final Index headIndex;
        if (headCommit == null) {
            headIndex = Index.create(emptyList());
        } else {
            headIndex = indexFromTreeRef(headCommit.getTreeSha());
        }
        if (!headIndex.getFileDescriptors().equals(indexToCheck.getFileDescriptors())) {
            throw exc.get();
        }
    }

    private void checkMergeNotInProgress(final Supplier<? extends RuntimeException> exc) {
        if (mergeInProgress()) {
            throw exc.get();
        }
    }

    private void checkIsValidCheckoutRef(final Ref ref, final Supplier<? extends RuntimeException> exc) {
        if (ref instanceof ReservedRef) {
            exc.get();
        }
    }

    private void updateHeadForCheckout(final Ref ref, final Sha1 sha1) {
        final var headPath = repoRoot.resolve(RepositoryLayout.HEAD);
        if (ref instanceof Sha1) {
            final var sha = (Sha1) ref;
            Utils.writeUtf8(headPath, sha.getHexString() + "\n");
        } else if (ref instanceof BranchName) {
            final var branchName = ((BranchName) ref).getBranchName();
            Utils.writeUtf8(headPath, branchName + "\n");
        } else {
            throw Contracts.unreachable();
        }
    }

    private LinkedHashSet<Sha1> getAllParents(final Commit commit) {
        final var result = new LinkedHashSet<Sha1>();
        final var queue = new ArrayDeque<Commit>();
        queue.add(commit);
        while (!queue.isEmpty()) {
            final var curCommit = queue.removeFirst();
            final var sha = curCommit.sha1();
            if (result.contains(sha)) {
                continue;
            }
            result.add(sha);
            if (curCommit.getFirstParentSha() != null) {
                final var parent = Contracts.ensureNonNull(readCommitOrNull(curCommit.getFirstParentSha()));
                queue.addLast(parent);
            }
            if (curCommit.getSecondParentSha() != null) {
                final var parent = Contracts.ensureNonNull(readCommitOrNull(curCommit.getSecondParentSha()));
                queue.addLast(parent);
            }
        }
        return result;
    }

    private List<VCSFileChange> startFastForwardMerge(final Commit headCommit, final Commit targetHead) {
        final var targetIndex = indexFromTreeRef(targetHead.getTreeSha());
        final var headIndex = indexFromTreeRef(headCommit.getTreeSha());
        final var diff = targetIndex.getDiff(headIndex);
        final var mergeHead = repoRoot.resolve(RepositoryLayout.MERGE_HEAD);
        saveIndex(targetIndex);
        Utils.writeUtf8(mergeHead, targetHead.sha1().getHexString() + "\n");
        return diff;
    }

    private Tuple2<List<VCSFileChange>, MergeConflicts> startLinearMerge(
            final Commit base,
            final Commit receiver,
            final Commit giver) {
        final var baseIndex = indexFromTreeRef(base.getTreeSha());
        final var receiverIndex = indexFromTreeRef(receiver.getTreeSha());
        final var giverIndex = indexFromTreeRef(giver.getTreeSha());
        final var changes = giverIndex.getDiff(receiverIndex);
        final var additions = changes.stream()
                .filter(Addition.class::isInstance)
                .collect(toList());
        final List<Modification> conflictingModifications = new ArrayList<>();
        final List<Modification> nonConflictingModifications = new ArrayList<>();
        for (final var fileDesc : receiverIndex.getFileDescriptors()) {
            final var path = fileDesc.getPath();
            if (!giverIndex.contains(path)) {
                continue;
            }
            final var giverSha = giverIndex.getSha(fileDesc.getPath());
            final var receiverSha = fileDesc.getSha();
            if (receiverSha.equals(giverSha)) {
                continue;
            }
            if (!baseIndex.contains(fileDesc.getPath())) {
                conflictingModifications.add(new Modification(new VCSFileDesc(path, giverSha), receiverSha));
            }
            final var baseSha = baseIndex.getSha(path);
            if (baseSha.equals(receiverSha)) {
                nonConflictingModifications.add(new Modification(new VCSFileDesc(path, giverSha), receiverSha));
            } else if (!baseSha.equals(giverSha)) {
                conflictingModifications.add(new Modification(new VCSFileDesc(path, giverSha), receiverSha));
            }
        }
        final var nonConflictingChanges = new ArrayList<>(additions);
        nonConflictingChanges.addAll(nonConflictingModifications);
        var indexToWrite = receiverIndex;
        for (final var nonConflictChange : nonConflictingChanges) {
            if (nonConflictChange instanceof Addition) {
                final var addition = (Addition) nonConflictChange;
                indexToWrite = indexToWrite.withNewIndexEntry(addition.getChangePath(), addition.getAddedFileDesc().getSha());
            } else {
                Contracts.requireThat(nonConflictChange instanceof Modification);
                final var modification = (Modification) nonConflictChange;
                indexToWrite = indexToWrite.withNewIndexEntry(
                        modification.getChangePath(),
                        modification.getModifiedFileDesc().getSha()
                );
            }
        }
        for (final var conflictModification : conflictingModifications) {
            indexToWrite = indexToWrite.withNewConflict(
                    conflictModification.getChangePath(),
                    conflictModification.getOriginalSha(),
                    conflictModification.getModifiedFileDesc().getSha()
            );
        }
        saveIndex(indexToWrite);
        Utils.writeUtf8(repoRoot.resolve(RepositoryLayout.MERGE_HEAD), giver.sha1().getHexString() + "\n");
        if (conflictingModifications.isEmpty()) {
            return new Tuple2<>(nonConflictingChanges, null);
        } else {
            return new Tuple2<>(
                    nonConflictingChanges,
                    new MergeConflicts(receiver.sha1(), giver.sha1(), conflictingModifications)
            );
        }
    }
}
