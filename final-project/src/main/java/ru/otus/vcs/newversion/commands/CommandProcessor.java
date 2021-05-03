package ru.otus.vcs.newversion.commands;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.exception.UserException;
import ru.otus.vcs.newversion.gitrepo.CommitMessage;
import ru.otus.vcs.newversion.gitrepo.GitRepositoryFactory;
import ru.otus.vcs.newversion.gitrepo.UpwardsRepoSearcher;
import ru.otus.vcs.newversion.localrepo.LocalRepoStatus;
import ru.otus.vcs.newversion.localrepo.LocalRepository;
import ru.otus.vcs.newversion.localrepo.LocalRepositoryException;
import ru.otus.vcs.newversion.localrepo.NestedGitLocalRepository;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.BranchName;
import ru.otus.vcs.newversion.ref.Ref;
import ru.otus.vcs.newversion.ref.ReservedRef;
import ru.otus.vcs.newversion.ref.Sha1;
import ru.otus.vcs.newversion.utils.Utils;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class CommandProcessor {

    private final GitRepositoryFactory gitRepositoryFactory;
    private final Path currentWorkingDir;

    public CommandProcessor(final GitRepositoryFactory gitRepositoryFactory) {
        this(gitRepositoryFactory, Path.of(""));
    }

    public CommandProcessor(final GitRepositoryFactory gitRepositoryFactory, final Path path) {
        Contracts.requireNonNullArgument(gitRepositoryFactory);
        Contracts.requireNonNullArgument(path);
        Contracts.requireThat(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS));

        this.gitRepositoryFactory = gitRepositoryFactory;
        this.currentWorkingDir = Utils.toReal(path);
    }

    public void init(final String stringPath) {
        Contracts.requireNonNullArgument(stringPath);

        final var path = toPathOrThrowUserEx(stringPath);
        if (!Utils.isEmptyDir(path)) {
            throw new UserException("Init error. Provided path " + stringPath + " is not an empty dir.");
        }

        gitRepositoryFactory.createNew(path, false);
    }

    public void add(final String stringPath) {
        Contracts.requireNonNullArgument(stringPath);

        final var path = toPathOrThrowUserEx(stringPath);
        final Path absPath = toAbsolute(path);
        if (!Utils.isRegularFileNoFollow(absPath)) {
            throw new UserException("Regular file at path " + path + " doesn't exist.");
        }
        try {
            final Path realPath = Utils.toReal(absPath);
            final var localRepo = findRepoOrThrow();
            localRepo.checkThatIsRepositoryPath(realPath);
            final var relativePath = localRepo.realRepoDir().relativize(realPath);
            checkThatValidVCSPath(relativePath, realPath);
            localRepo.add(VCSPath.create(relativePath));
        } catch (final LocalRepositoryException ex) {
            throw new UserException("Can't add file to index.", ex);
        }
    }

    public void branch(final String branchName) {
        Contracts.requireNonNullArgument(branchName);

        if (!BranchName.isValidBranchName(branchName)) {
            throw new UserException("Bad branch name = " + branchName);
        }
        findRepoOrThrow().branch(BranchName.create(branchName));
    }

    public void remove(final String stringPath, final RemoveOption removeOption) {
        Contracts.requireNonNullArgument(stringPath);

        final var path = toPathOrThrowUserEx(stringPath);
        final var localRepo = findRepoOrThrow();
        final Path absolutePath = toAbsolute(path);
        localRepo.checkThatIsRepositoryPath(absolutePath);
        final Path relativePath = localRepo.realRepoDir().relativize(absolutePath);
        checkThatValidVCSPath(relativePath, absolutePath);
        final VCSPath vcsPath = VCSPath.create(relativePath);
        switch (removeOption) {
            case Normal:
                localRepo.remove(vcsPath);
                return;
            case Force:
                localRepo.removeForcibly(vcsPath);
                return;
            case Cached:
                localRepo.removeFromIndex(vcsPath);
                return;
            default:
                throw Contracts.unreachable();
        }
    }

    public void commit(final String message) {
        Contracts.requireNonNullArgument(message);

        if (!CommitMessage.isValidMessage(message)) {
            throw new UserException("Bad format of message.");
        }
        findRepoOrThrow().commit(CommitMessage.create(message));
    }

    public String status() {
        final LocalRepoStatus statusResult;
        statusResult = findRepoOrThrow().status();
        return statusResultToUserMessage(statusResult);
    }

    public void checkout(final String refString) {
        Contracts.requireNonNullArgument(refString);

        final var ref = toRefOfThrowUserForCheckout(refString);
        final var repo = findRepoOrThrow();
        repo.checkout(ref);
    }

    public void checkoutFile(final String refString, final String vcsPathString) {
        Contracts.requireNonNullArgument(refString);
        Contracts.requireNonNullArgument(vcsPathString);

        final var ref = toRefOfThrowUserForCheckout(refString);
        final var path = toPathOrThrowUserEx(vcsPathString);
        if (!VCSPath.isValidVCSPath(path)) {
            throw new UserException("Bad repo path " + vcsPathString + ".");
        }
        final var repo = findRepoOrThrow();
        final var vcsPath = VCSPath.create(path);
        repo.checkoutFile(ref, vcsPath);
    }

    private LocalRepository findRepoOrThrow() {
        final var gitRep = new UpwardsRepoSearcher(currentWorkingDir, gitRepositoryFactory).find(false);
        if (gitRep == null) {
            throw new UserException("Can't find repository searching upwards from " + currentWorkingDir);
        }
        return new NestedGitLocalRepository(gitRep);
    }

    private static void checkThatValidVCSPath(final Path relativePath, final Path original) {
        if (!VCSPath.isValidVCSPath(relativePath)) {
            throw new UserException("Bad path format " + original + ". It's relative repository path = " + relativePath
                    + " is not valid."
            );
        }
    }

    private static Path toPathOrThrowUserEx(final String stringPath) {
        try {
            return Path.of(stringPath);
        } catch (final InvalidPathException ex) {
            throw new UserException("Bad path syntax of provided string = " + stringPath);
        }
    }

    private static String statusResultToUserMessage(final LocalRepoStatus status) {
        final var strBuilder = new StringBuilder();
        @Nullable final var mergeConflicts = status.getMergeConflicts();
        if (mergeConflicts != null) {
            strBuilder.append("There merge conflicts on receiver(HEAD) ")
                    .append(getUserMessageForRef(mergeConflicts.getReceiver()))
                    .append(" and giver ")
                    .append(getUserMessageForRef(mergeConflicts.getGiver()))
                    .append(". Below conflicting paths:")
                    .append(System.lineSeparator());
            for (final var modification : mergeConflicts.getConflictingChanges()) {
                strBuilder.append(modification.getChangePath())
                        .append(System.lineSeparator());
            }
        }
        final var localFileChanges = status.getLocalFileChanges();
        if (!localFileChanges.isEmpty()) {
            strBuilder.append("Local changes:")
                    .append(System.lineSeparator());
            for (final var change : localFileChanges) {
                strBuilder.append(change)
                        .append(System.lineSeparator());
            }
        }
        final var uncommittedChanges = status.getUncommittedChanges();
        if (!uncommittedChanges.isEmpty()) {
            strBuilder.append("Staged for commit:")
                    .append(System.lineSeparator());
            for (final var change : uncommittedChanges) {
                strBuilder.append(change)
                        .append(System.lineSeparator());
            }
        }
        if (strBuilder.length() > 0) {
            strBuilder.deleteCharAt(strBuilder.length() - 1);
        }
        return strBuilder.toString();
    }

    private static String getUserMessageForRef(final Ref ref) {
        if (ref instanceof BranchName) {
            return ((BranchName) ref).getBranchName();
        } else if (ref instanceof Sha1) {
            return ((Sha1) ref).getHexString();
        } else {
            throw Contracts.unreachable();
        }
    }

    private static Ref toRefOfThrowUserForCheckout(final String refString) {
        if (!Ref.isValidRefString(refString)) {
            throw new UserException("Bad reference to commit " + refString + ".");
        } else if (ReservedRef.mergeHead.getRefString().equals(refString)) {
            throw new UserException("Can't checkout internal ref " + refString + ".");
        }
        return Ref.create(refString);
    }

    private Path toAbsolute(final Path path) {
        if (path.isAbsolute()) {
            return path;
        } else {
            return currentWorkingDir.resolve(path);
        }
    }
}
