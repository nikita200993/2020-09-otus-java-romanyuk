package ru.otus.vcs.processor;

import ru.otus.utils.Contracts;
import ru.otus.vcs.config.GitConfig;
import ru.otus.vcs.exception.UserException;
import ru.otus.vcs.index.Index;
import ru.otus.vcs.index.diff.Addition;
import ru.otus.vcs.index.diff.Deletion;
import ru.otus.vcs.index.diff.Modification;
import ru.otus.vcs.objects.Blob;
import ru.otus.vcs.objects.Commit;
import ru.otus.vcs.objects.Tree;
import ru.otus.vcs.path.VCSFileName;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.BranchName;
import ru.otus.vcs.ref.Ref;
import ru.otus.vcs.ref.ReservedRef;
import ru.otus.vcs.ref.Sha1;
import ru.otus.vcs.repository.GitRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Takes verified
 */
public class CommandProcessor {

    private final Path currentDir = Path.of("").toAbsolutePath();

    public void init(final Path path) {
        Contracts.requireNonNullArgument(path);

        try {
            if (!Files.isDirectory(path) || Files.list(path).count() != 0) {
                throw new UserException("Path '" + path + "' is not valid for init command. Should be empty directory.");
            }
            GitRepository.createNew(path);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't init repository.", ex);
        }
    }

    public void addToIndex(final Path path) {
        Contracts.requireNonNullArgument(path);

        if (!Files.isRegularFile(path)) {
            throw new UserException("Can't add to index. Path '" + path + "' should locate regular file.");
        }

        try {
            final var gitRepo = findRepo(currentDir);
            final var realPath = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
            checkIsRelativeToRepo(gitRepo, realPath);
            final var relativePath = gitRepo.getWorkdir().relativize(realPath);
            checkIsNotUnderGitDir(relativePath);
            checkIsValidVcsPath(relativePath);
            final var vcsPath = VCSPath.create(relativePath);
            final var blob = new Blob(Files.readAllBytes(realPath));
            gitRepo.saveGitObjectIfAbsent(blob);
            final var index = gitRepo.readIndex()
                    .withNewIndexEntry(vcsPath, blob.sha1());
            gitRepo.saveIndex(index);
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error while performing add operation.", ex);
        }
    }

    void removeFromIndex(final Path path) throws IOException {
        Contracts.requireNonNullArgument(path);

        final var gitRepo = findRepo(currentDir);
        final Path absolutePath = path.toAbsolutePath();
        final Path relativeToRepoPath;
        if (Files.exists(absolutePath)) {
            final var realPath = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
            checkIsRelativeToRepo(gitRepo, realPath);
            relativeToRepoPath = gitRepo.getWorkdir().relativize(realPath);
        } else {
            checkIsRelativeToRepo(gitRepo, absolutePath);
            relativeToRepoPath = gitRepo.getWorkdir().relativize(absolutePath);
        }
        checkIsNotUnderGitDir(relativeToRepoPath);
        checkIsValidVcsPath(relativeToRepoPath);
        final var index = gitRepo.readIndex();
        final var newIndex = index.withRemovedIndexEntry(VCSPath.create(relativeToRepoPath));
        if (newIndex == null) {
            throw new UserException("Path '" + path + "' is not in the index.");
        }
        gitRepo.saveIndex(newIndex);
        if (Files.isRegularFile(path)) {
            Files.delete(path);
        }
    }

    void branch(final String branch) {
        Contracts.requireNonNullArgument(branch);

        if (!BranchName.isValidBranchName(branch)) {
            throw new UserException("Bad branch name. Should follow this pattern " + BranchName.getNamePattern() + ".");
        }
        final BranchName branchName = BranchName.create(branch);
        final GitRepository gitRepository = findRepo(currentDir);
        if (gitRepository.hasBranch(branchName)) {
            throw new UserException("Branch '" + branch + "' already exists.");
        }
        final var headCommit = gitRepository.readCommit(ReservedRef.head);
        if (headCommit == null) {
            throw new UserException("No commits on current HEAD. Please commit something on HEAD and then try.");
        }
        gitRepository.createNonexistentBranch(branchName, headCommit.sha1());
    }

    void commit(final String message) {
        Contracts.requireNonNullArgument(message);

        if (!Commit.isValidMessage(message)) {
            throw new UserException("Message is blank.");
        }
        final var repo = findRepo(currentDir);
        final boolean isMergeInProgress = repo.isMergeInProgress();
        final var index = repo.readIndex();
        if (index.isEmpty()) {
            throw new UserException("Empty index. Add something.");
        }
        if (index.hasMergeConflict()) {
            throw new UserException(
                    String.format(
                            "Plz resolve conflicts with files:%n%s",
                            index.getConflictPaths().stream()
                                    .map(VCSPath::toOsPath)
                                    .map(relPath -> repo.getWorkdir().resolve(relPath))
                                    .map(Objects::toString)
                                    .collect(Collectors.joining(System.lineSeparator()))
                    )
            );
        }
        final var trees = Tree.createFromIndex(index);
        final var rootTree = trees.get(0);
        final var headCommit = repo.readCommit(ReservedRef.head);
        final var author = repo.getConfig().get(GitConfig.USER);
        final Commit newCommit;
        if (headCommit == null) {
            Contracts.forbidThat(isMergeInProgress);
            newCommit = new Commit(
                    rootTree.sha1(),
                    null,
                    null,
                    author,
                    message
            );
        } else if (isMergeInProgress) {
            final var giverCommit = repo.readCommit(ReservedRef.mergeHead);
            Contracts.requireNonNull(giverCommit);
            newCommit = new Commit(
                    rootTree.sha1(),
                    headCommit.sha1(),
                    giverCommit.sha1(),
                    author,
                    message
            );
        } else {
            newCommit = new Commit(
                    rootTree.sha1(),
                    headCommit.sha1(),
                    null,
                    author,
                    message
            );
        }
        if (headCommit == null || !headCommit.getTreeSha().equals(rootTree.sha1())) {
            trees.forEach(repo::saveGitObjectIfAbsent);
            Contracts.requireThat(repo.saveGitObjectIfAbsent(newCommit));
            repo.updateHead(newCommit);
            if (isMergeInProgress) {
                repo.removeMergeHead();
            }
        } else {
            throw new UserException("No changes are done comparing with current head commit.");
        }
    }

    public void checkout(final String refString) {
        Contracts.requireNonNull(refString);

        final var repo = findRepo(currentDir);
        final var ref = ensureValidRefString(refString);
        if (repo.isMergeInProgress()) {
            throw new UserException(
                    String.format(
                            "Merge in progress plz resolve conflicts:%n%s",
                            mergeConflicts(repo)
                    )
            );
        }
        final var gitObject = repo.readGitObjectOrNullIfAbsent(ref);
        if (gitObject == null) {
            throw new UserException("No commits for " + refString + ".");
        } else if (!(gitObject instanceof Commit)) {
            throw new UserException("Provided ref = " + refString + " doesn't refer to commit.");
        }
        final var checkoutCommit = (Commit) gitObject;
        final var headCommit = Contracts.ensureNonNull(repo.readCommit(ReservedRef.head));
        if (checkoutCommit.sha1().equals(headCommit.sha1())) {
            throw new UserException("Trying to checkout the same commit tree.");
        }
        final var checkoutTree = (Tree) repo.readGitObjectOrThrowIfAbsent(checkoutCommit.getTreeSha());
        final var headTree = (Tree) repo.readGitObjectOrThrowIfAbsent(headCommit.getTreeSha());
        final var checkoutIndex = checkoutTree.index(repoAsTreeReader(repo));
        final var headIndex = headTree.index(repoAsTreeReader(repo));
        final var stagedIndex = repo.readIndex();
        Contracts.forbidThat(stagedIndex.hasMergeConflict());
        if (!headIndex.equals(stagedIndex)) {
            throw new UserException("There staged uncommitted changes. Commit them before checkout.");
        }
        for (final var fileChange : checkoutIndex.getDiff(headIndex)) {
            if (fileChange instanceof Addition) {

            } else if (fileChange instanceof Deletion) {

            } else if (fileChange instanceof Modification) {

            } else {
                throw Contracts.unreachable();
            }
        }
    }

    public void status() {
        final var repo = findRepo(currentDir);
    }


    private static GitRepository findRepo(final Path path) {
        final var gitRepo = GitRepository.find(path);
        if (gitRepo == null) {
            throw new UserException("Working dir  = " + path + " should be under repo control.");
        }
        return gitRepo;
    }

    private static void checkIsValidVcsPath(final Path relativePath) {
        if (!VCSPath.isValidVCSPath(relativePath)) {
            throw new UserException("Path '" + relativePath + "' to repo file should follow convention - all file names must" +
                    " follow this pattern " + VCSFileName.getPatternString());
        }
    }

    private static void checkIsNotUnderGitDir(final Path relativePath) {
        if (relativePath.toString().startsWith(GitRepository.GITDIR)) {
            throw new UserException("Path '" + relativePath +
                    "' shouldn't be under repository dir '" + GitRepository.GITDIR + "'."
            );
        }
    }

    private static void checkIsRelativeToRepo(final GitRepository gitRepository, final Path path) {
        if (!path.startsWith(gitRepository.getWorkdir())) {
            throw new UserException("Path '" + path + "' is not under git repo path "
                    + gitRepository.getWorkdir()
            );
        }
    }

    private static Ref ensureValidRefString(final String refString) {
        if (Sha1.isValidSha1HexString(refString)) {
            return Sha1.create(refString);
        } else if (BranchName.isValidBranchName(refString)) {
            return BranchName.create(refString);
        } else if (ReservedRef.head.getRefString().equals(refString)){
            return ReservedRef.head;
        } else {
            throw new UserException("Provided ref = " + refString + " is invalid");
        }
    }

    private static Function<Sha1, Tree> repoAsTreeReader(final GitRepository repository) {
        return (sha1) -> {
            Contracts.requireNonNullArgument(sha1);
            final var gitObject = repository.readGitObjectOrThrowIfAbsent(sha1);
            Contracts.requireThat(gitObject instanceof Tree);
            return (Tree) gitObject;
        };
    }

    private static String mergeConflicts(final GitRepository repository) {
        return repository.readIndex().getConflictPaths().stream()
                .map(VCSPath::toOsPath)
                .map(Objects::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static void writeFile
}
