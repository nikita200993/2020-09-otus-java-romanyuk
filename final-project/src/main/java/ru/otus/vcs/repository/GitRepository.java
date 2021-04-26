package ru.otus.vcs.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.utils.Contracts;
import ru.otus.vcs.config.GitConfig;
import ru.otus.vcs.index.Index;
import ru.otus.vcs.objects.Blob;
import ru.otus.vcs.objects.Commit;
import ru.otus.vcs.objects.GitObject;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.BranchName;
import ru.otus.vcs.ref.Ref;
import ru.otus.vcs.ref.ReservedRef;
import ru.otus.vcs.ref.Sha1;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.otus.vcs.utils.Utils.compress;
import static ru.otus.vcs.utils.Utils.decompress;

public class GitRepository {

    public static final String GITDIR = ".simplegit";
    public static final String CONFIG = "config";

    private static final Logger logger = LoggerFactory.getLogger(GitRepository.class);

    private static final String BRANCHES = "branches";
    private static final String OBJECTS = "objects";
    private static final String REFS = "refs";
    private static final String TAGS = "tags";
    private static final String HEADS = "heads";
    private static final String DESCRIPTION = "description";
    private static final String HEAD = "HEAD";
    private static final String MERGE_HEAD = "HEAD";
    private static final String INDEX = "index";
    private static final String DEFAULT_HEAD_CONTENT = "master\n";
    private static final String DEFAULT_DESCRIPTION_CONTENT = "Unnamed repository; edit this file 'description'"
            + "to name the repository." + System.lineSeparator();

    private final Path workdir;
    private final Path gitDir;
    private final GitConfig config;

    private GitRepository(final Path workdir, final Path gitDir, final GitConfig config) {
        this.workdir = workdir;
        this.gitDir = gitDir;
        this.config = config;
    }

    public static GitRepository createNew(final Path pathToEmptyDir) {
        Contracts.requireNonNullArgument(pathToEmptyDir);
        checkContractsOnTheNewRepoPath(pathToEmptyDir);

        try {
            final var realPathToEmptyDir = pathToEmptyDir.toRealPath(LinkOption.NOFOLLOW_LINKS);
            final var defaultConfig = new GitConfig();
            final var gitdir = realPathToEmptyDir.resolve(GITDIR);
            createRepoLayout(gitdir, defaultConfig);
            return new GitRepository(realPathToEmptyDir, gitdir, defaultConfig);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to create repo for path = '" + pathToEmptyDir + "'.", ex);
        }
    }

    @Nullable
    public static GitRepository find(final Path currentPathToSearch) {
        Contracts.requireNonNullArgument(currentPathToSearch);
        Contracts.requireThat(Files.exists(currentPathToSearch, LinkOption.NOFOLLOW_LINKS));

        try {
            final var realPath = currentPathToSearch.toRealPath(LinkOption.NOFOLLOW_LINKS);
            final Path repoPath = realPath.resolve(GITDIR);
            if (Files.isDirectory(repoPath) && isRepositoryLayout(repoPath)) {
                final var config = GitConfig.create(repoPath.resolve(CONFIG));
                return new GitRepository(
                        realPath,
                        repoPath,
                        config
                );
            } else if (realPath.getParent() == null) {
                return null;
            } else {
                return find(realPath.getParent());
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error while searching for git repository.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Commit readCommit(final Ref ref) {
        Contracts.requireNonNullArgument(ref);

        final var object = readGitObjectOrNullIfAbsent(ref);
        Contracts.requireThat(object instanceof Commit, "Object for ref " + ref + " is not a commit");
        return (Commit) object;
    }

    public boolean writeRegularFileIfNotExists(final VCSPath path, final Sha1 sha1) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());
        Contracts.forbidThat(path.toString().startsWith(GITDIR));

        final var osPath = workdir.resolve(path.toOsPath());
        if (Files.exists(osPath)) {
            return false;
        }
        try {
            Files.createDirectories(osPath.getParent());
            Files.write(osPath, readBlobThrow(sha1).getContent());
            return true;
        } catch (final FileAlreadyExistsException ignore) {
            logger.info("Can't create directories = " + osPath.getParent()
                    + " because file with the same name already exists."
            );
            return false;
        } catch (final IOException ex) {
            throw new UncheckedIOException("Error creating directories " + osPath.getParent(), ex);
        }
    }

    public boolean hasLocalChanges(final VCSPath path, final Sha1 sha) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());
        Contracts.forbidThat(path.toString().startsWith(GITDIR));

        final var osPath = workdir.resolve(path.toOsPath());
        try {
            if (!Files.isRegularFile(osPath, LinkOption.NOFOLLOW_LINKS)) {
                return true;
            } else {
                final var content = Files.readAllBytes(osPath);
                return !sha.equals(new Blob(content).sha1());
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException("Error reading " + osPath, ex);
        }
    }

    @Nullable
    public GitObject readGitObjectOrNullIfAbsent(final Ref ref) {
        Contracts.requireNonNullArgument(ref);

        if (ref instanceof Sha1) {
            return readGitObjectOrNullIfAbsent((Sha1) ref);
        } else if (ref instanceof BranchName) {
            final var branchName = (BranchName) ref;
            if (!Files.isRegularFile(resolveRepoPath(REFS, HEADS, branchName.getBranchName()), LinkOption.NOFOLLOW_LINKS)) {
                return null;
            }
            final var branchCommitHexSha = readLineFromOneLineFile(
                    resolveRepoPath(REFS, HEADS, branchName.getBranchName()),
                    "Bad format of branch file '" + branchName + "'."
            );
            Contracts.requireThat(
                    Sha1.isValidSha1HexString(branchCommitHexSha),
                    "Bad content of branch file '" + branchName + "'."
            );
            final var object = readGitObjectOrNullIfAbsent(Sha1.create(branchCommitHexSha));
            Contracts.requireThat(object instanceof Commit);
            return object;
        } else if (ref instanceof ReservedRef) {
            final var reservedRef = (ReservedRef) ref;
            if (!Files.isRegularFile(resolveRepoPath(reservedRef.getRefString()), LinkOption.NOFOLLOW_LINKS)) {
                return null;
            }
            final var lineContent = readLineFromOneLineFile(
                    resolveRepoPath(reservedRef.getRefString()),
                    "Bad format of HEAD file."
            );
            Contracts.requireThat(
                    Sha1.isValidSha1HexString(lineContent) || BranchName.isValidBranchName(lineContent),
                    "Bad content of HE"
            );
            if (Sha1.isValidSha1HexString(lineContent)) {
                final var object = readGitObjectOrNullIfAbsent(Sha1.create(lineContent));
                Contracts.requireThat(
                        object instanceof Commit,
                        "Object pointed by ref " + ref + " is not a commit"
                );
                return object;
            } else {
                return readGitObjectOrNullIfAbsent(BranchName.create(lineContent));
            }
        } else {
            throw Contracts.unreachable("Illegal type " + ref.getClass());
        }
    }

    public GitObject readGitObjectOrNullIfAbsent(final Sha1 sha) {
        Contracts.requireNonNullArgument(sha);

        try {
            final String dirName = sha.getHexString().substring(0, 2);
            final String fileName = sha.getHexString().substring(2);
            final Path pathToFile = resolveRepoPath(OBJECTS, dirName, fileName);
            if (!Files.isRegularFile(pathToFile, LinkOption.NOFOLLOW_LINKS)) {
                return null;
            }
            final byte[] raw = decompress(Files.readAllBytes(pathToFile));
            return GitObject.deserialize(raw);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't read object for sha = " + sha + ".", ex);
        }
    }

    public boolean hasBranch(final BranchName branchName) {
        Contracts.requireNonNullArgument(branchName);

        if (branchName.isMaster()) {
            return true;
        } else {
            return Files.isRegularFile(resolveRepoPath(REFS, HEADS, branchName.getBranchName()), LinkOption.NOFOLLOW_LINKS);
        }
    }

    public boolean isMergeInProgress() {
        return Files.exists(resolveRepoPath(MERGE_HEAD));
    }

    public void removeMergeHead() {
        try {
            Files.delete(resolveRepoPath(MERGE_HEAD));
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't delete MERGE_HEAD", ex);
        }
    }

    public void updateHead(final Commit commit) {
        Contracts.requireNonNullArgument(commit);

        final var headContent = readLineFromOneLineFile(resolveRepoPath(HEAD), "Bad format of HEAD file.");
        final var lineToWrite = commit.sha1().getHexString() + "\n";
        final Path fileToWrite;
        if (Sha1.isValidSha1HexString(headContent)) {
            fileToWrite = resolveRepoPath(HEAD);
        } else if (BranchName.isValidBranchName(headContent)) {
            fileToWrite = resolveRepoPath(REFS, HEADS, headContent);
        } else {
            throw Contracts.unreachable();
        }
        writeUtf8(fileToWrite, lineToWrite);
    }

    public void createNonexistentBranch(final BranchName branchName, final Sha1 sha1) {
        Contracts.requireNonNullArgument(branchName);
        Contracts.requireNonNullArgument(sha1);
        Contracts.forbidThat(hasBranch(branchName));

        try {
            Files.writeString(
                    resolveRepoPath(OBJECTS, REFS, branchName.getBranchName()),
                    sha1.getHexString() + "\n",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't create branch " + branchName, ex);
        }
    }

    public Index readIndex() {
        try {
            return Index.deserialize(Files.readAllBytes(resolveRepoPath(INDEX)));
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error reading index file.", ex);
        }
    }

    public void saveIndex(final Index index) {
        Contracts.requireNonNullArgument(index);
        try {
            Files.write(resolveRepoPath(INDEX), index.serialize());
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error while writing index to file.", ex);
        }
    }

    public List<VCSPath> getAllVCSCompatibleFilesUnderRepo() {
        try (var paths = Files.walk(workdir)) {
            return paths.skip(1)
                    .map(workdir::relativize)
                    .filter(VCSPath::isValidVCSPath)
                    .map(VCSPath::create)
                    .collect(Collectors.toList());
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error while traversing workdir " + workdir, ex);
        }
    }

    public GitObject readGitObjectOrThrowIfAbsent(final Sha1 sha) {
        Contracts.requireNonNullArgument(sha);

        return Contracts.ensureNonNull(
                readGitObjectOrNullIfAbsent(sha),
                "Object with sha1 = " + sha.getHexString() + " is absent."
        );
    }

    /**
     * Saves git object and returns its sha1 hex representation.
     *
     * @param gitObject object to save.
     * @return sha1 hex.
     */
    public boolean saveGitObjectIfAbsent(final GitObject gitObject) {
        Contracts.requireNonNullArgument(gitObject);

        final Sha1 sha1 = gitObject.sha1();
        final Path savePath = resolveRepoPath(
                OBJECTS,
                sha1.getHexString().substring(0, 2),
                sha1.getHexString().substring(2)
        );
        try {
            if (!Files.exists(savePath)) {
                Files.createDirectories(savePath.getParent());
                Files.write(savePath, compress(gitObject.serialize()), StandardOpenOption.CREATE_NEW);
                savePath.toFile().setReadOnly();
                return true;
            } else {
                return false;
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't save git object " + gitObject + ".", ex);
        }
    }

    public Path resolveRepoPath(final String... paths) {
        Contracts.requireNonNullArgument(paths);
        if (paths.length == 0) {
            return gitDir;
        } else {
            final var withoutFirst = Arrays.copyOfRange(paths, 1, paths.length);
            final var first = paths[0];
            return gitDir.resolve(Path.of(first, withoutFirst));
        }
    }

    public Path getWorkdir() {
        return workdir;
    }

    public Path getGitDir() {
        return gitDir;
    }

    public GitConfig getConfig() {
        return config;
    }

    private static void createRepoLayout(final Path gitdir, final GitConfig config) {
        try {
            Files.createDirectory(gitdir);
            Files.writeString(gitdir.resolve(CONFIG), config.toString());
            Files.createDirectory(gitdir.resolve(BRANCHES));
            Files.createDirectory(gitdir.resolve(OBJECTS));
            Files.createDirectories(gitdir.resolve(REFS).resolve(TAGS));
            Files.createDirectories(gitdir.resolve(REFS).resolve(HEADS));
            Files.writeString(gitdir.resolve(DESCRIPTION), DEFAULT_DESCRIPTION_CONTENT);
            Files.writeString(gitdir.resolve(HEAD), DEFAULT_HEAD_CONTENT);
            Files.createFile(gitdir.resolve(INDEX));
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't create layout for repository at dir = " + gitdir + ".", ex);
        }
    }


    private static boolean isRepositoryLayout(final Path gitdir) {
        return isLayoutFileIsPresent(gitdir, Path.of(CONFIG))
                && isLayoutDirIsPresent(gitdir, Path.of(BRANCHES))
                && isLayoutDirIsPresent(gitdir, Path.of(OBJECTS))
                && isLayoutDirIsPresent(gitdir, Path.of(REFS))
                && isLayoutDirIsPresent(gitdir, Path.of(REFS).resolve(TAGS))
                && isLayoutDirIsPresent(gitdir, Path.of(REFS).resolve(HEADS))
                && isLayoutFileIsPresent(gitdir, Path.of(DESCRIPTION))
                && isLayoutFileIsPresent(gitdir, Path.of(HEAD))
                && isLayoutFileIsPresent(gitdir, Path.of(INDEX));
    }

    private static boolean isLayoutDirIsPresent(final Path gitdir, final Path relativePath) {
        return Files.isDirectory(gitdir.resolve(relativePath));
    }

    private static boolean isLayoutFileIsPresent(final Path gitdir, final Path relativePath) {
        return Files.isRegularFile(gitdir.resolve(relativePath), LinkOption.NOFOLLOW_LINKS);
    }

    private static void checkContractsOnTheNewRepoPath(final Path realPathToEmptyDir) {
        try {
            Contracts.requireThat(
                    Files.isDirectory(realPathToEmptyDir),
                    realPathToEmptyDir + " is not an empty dir."
            );
            Contracts.requireThat(Files.list(realPathToEmptyDir).count() == 0, "Dir is not empty.");
        } catch (final IOException ex) {
            throw new UncheckedIOException(
                    "IO error while checking contracts for path = '" + realPathToEmptyDir + "'.",
                    ex);
        }
    }

    private static String readLineFromOneLineFile(final Path path, final String contractMessage) {
        try {
            final var content = Files.readString(path, StandardCharsets.UTF_8);
            final var lines = content.split("\n");
            Contracts.requireThat(lines.length == 1, contractMessage);
            return lines[0];
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't read " + path, ex);
        }
    }

    private void writeUtf8(final Path path, final String content) {
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't write to path = " + path, ex);
        }
    }

    private Blob readBlobThrow(final Sha1 sha1) {
        final var gitObject = readGitObjectOrThrowIfAbsent(sha1);
        Contracts.requireThat(gitObject instanceof Blob);
        return (Blob) gitObject;
    }
}
