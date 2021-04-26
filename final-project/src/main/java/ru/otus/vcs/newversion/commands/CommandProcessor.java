package ru.otus.vcs.newversion.commands;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.UserException;
import ru.otus.vcs.newversion.gitrepo.CommitMessage;
import ru.otus.vcs.newversion.gitrepo.GitRepoFactory;
import ru.otus.vcs.newversion.localrepo.LocalRepository;
import ru.otus.vcs.newversion.localrepo.LocalRepositoryException;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.BranchName;
import ru.otus.vcs.ref.Ref;
import ru.otus.vcs.ref.ReservedRef;
import ru.otus.vcs.ref.Sha1;
import ru.otus.vcs.utils.Utils;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class CommandProcessor {

    private final GitRepoFactory gitRepoFactory;
    private final Path currentWorkingDir;

    public CommandProcessor(final GitRepoFactory gitRepoFactory) {
        this(gitRepoFactory, Path.of(""));
    }

    public CommandProcessor(final GitRepoFactory gitRepoFactory, final Path path) {
        Contracts.requireNonNullArgument(gitRepoFactory);
        Contracts.requireNonNullArgument(path);
        Contracts.requireThat(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS));

        this.gitRepoFactory = gitRepoFactory;
        this.currentWorkingDir = Utils.toReal(path);
    }

    public void init(final String stringPath) {
        Contracts.requireNonNullArgument(stringPath);

        final var path = toPathOrThrowUserEx(stringPath);
        if (!Utils.isEmptyDir(path)) {
            throw new UserException("Init error. Provided path " + stringPath + " is not an empty dir.");
        }

        gitRepoFactory.createNew(path, false);
    }

    public void add(final String stringPath) {
        Contracts.requireNonNullArgument(stringPath);

        final var path = toPathOrThrowUserEx(stringPath);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new UserException("Regular file at path " + path + " doesn't exist.");
        }
        try {
            final Path realPath = Utils.toReal(path);
            final var localRepo = findRepoOrThrow();
            localRepo.checkThatIsRepositoryPath(realPath);
            final var relativePath = localRepo.realRepoDir().relativize(realPath);
            checkThatValidVCSPath(relativePath, realPath);
            localRepo.add(VCSPath.create(relativePath));
        } catch (final LocalRepositoryException ex) {
            throw new UserException("Can't add file to index.", ex);
        }
    }

    public void remove(final String stringPath) {
        Contracts.requireNonNullArgument(stringPath);

        final Path absolutePath;
        final var path = toPathOrThrowUserEx(stringPath);
        final var localRepo = findRepoOrThrow();
        if (!path.isAbsolute()) {
            absolutePath = currentWorkingDir.resolve(path);
        } else {
            absolutePath = path;
        }

        try {
            localRepo.checkThatIsRepositoryPath(absolutePath);
            final Path relativePath = localRepo.realRepoDir().relativize(absolutePath);
            checkThatValidVCSPath(relativePath, absolutePath);
            localRepo.remove(VCSPath.create(relativePath));
        } catch (final LocalRepositoryException ex) {
            throw new UserException("Error while removing file at path = " + absolutePath + ".", ex);
        }
    }

    public void commit(final String message) {
        Contracts.requireNonNullArgument(message);

        if (!CommitMessage.isValidMessage(message)) {
            throw new UserException("Bad format of message.");
        }
        try {
            findRepoOrThrow().commit(CommitMessage.create(message));
        } catch (final LocalRepositoryException ex) {
            throw new UserException("Commit error.", ex);
        }
    }

    public String status() {
        final LocalRepository.StatusResult statusResult;
        try {
            statusResult = findRepoOrThrow().status();
        } catch (final LocalRepositoryException ex) {
            throw new UserException("Can't retrieve status.", ex);
        }
        return statusResultToUserMessage(statusResult);
    }

    public void checkout(final String refString) {
        Contracts.requireNonNullArgument(refString);

        final var ref = toRefOfThrowUserForCheckout(refString);
        final var repo = findRepoOrThrow();
        try {
            repo.checkout(ref);
        } catch (final LocalRepositoryException ex) {
            throw new UserException("Checkout error for " + refString + ".", ex);
        }
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
        try {
            repo.checkoutFile(ref, vcsPath);
        } catch (final LocalRepositoryException ex) {
            throw new UserException("Checkout error for " + refString + ".", ex);
        }
    }

    public LocalRepository findRepoOrThrow() {
        throw new UnsupportedOperationException();
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

    private static String statusResultToUserMessage(final LocalRepository.StatusResult statusResult) {
        final var strBuilder = new StringBuilder();
        @Nullable final var mergeConflicts = statusResult.getMergeConflicts();
        if (mergeConflicts != null) {
            strBuilder.append("There merge conflicts on receiver(HEAD) ")
                    .append(getUserMessageForRef(mergeConflicts.getReceiver()))
                    .append(" and giver ")
                    .append(getUserMessageForRef(mergeConflicts.getGiver()))
                    .append(". Below conflicting paths:")
                    .append(System.lineSeparator());
            for (final var path : mergeConflicts.getConflictingChanges()) {
                strBuilder.append(path.toOsPath())
                        .append(System.lineSeparator());
            }
        }
        if (!statusResult.getUntrackedFiles().isEmpty()) {
            strBuilder.append("Untracked files:")
                    .append(System.lineSeparator());
            for (final var path : statusResult.getUntrackedFiles()) {
                strBuilder.append(path.toOsPath())
                        .append(System.lineSeparator());
            }
        }
        if (!statusResult.getLocalChanges().isEmpty()) {
            strBuilder.append("Local changes:")
                    .append(System.lineSeparator());
            for (final var change : statusResult.getLocalChanges()) {
                strBuilder.append(change)
                        .append(System.lineSeparator());
            }
        }
        if (!statusResult.getUncommittedStagedChanges().isEmpty()) {
            strBuilder.append("Staged for commit:")
                    .append(System.lineSeparator());
            for (final var change : statusResult.getUncommittedStagedChanges()) {
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
        final var ref = Ref.create(refString);
        return ref;
    }
}
