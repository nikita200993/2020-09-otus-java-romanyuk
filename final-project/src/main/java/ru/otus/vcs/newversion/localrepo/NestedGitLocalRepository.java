package ru.otus.vcs.newversion.localrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.index.Index;
import ru.otus.vcs.newversion.index.diff.Addition;
import ru.otus.vcs.newversion.index.diff.Deletion;
import ru.otus.vcs.newversion.index.diff.Modification;
import ru.otus.vcs.newversion.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.gitrepo.CommitMessage;
import ru.otus.vcs.newversion.gitrepo.GitRepository;
import ru.otus.vcs.newversion.gitrepo.GitRepositoryException;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.Ref;
import ru.otus.vcs.newversion.utils.Utils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class NestedGitLocalRepository implements LocalRepository {

    private final Path repoPath;
    private final GitRepository gitRepo;

    public NestedGitLocalRepository(final GitRepository gitRepository) {
        Contracts.requireNonNullArgument(gitRepository);
        Contracts.requireNonNull(gitRepository.repoRealPath().getParent());

        this.repoPath = gitRepository.repoRealPath().getParent();
        this.gitRepo = gitRepository;
    }

    @Override
    public Path realRepoDir() {
        return repoPath;
    }

    @Override
    public void checkThatIsRepositoryPath(final Path absolutePath) throws LocalRepositoryException {
        Contracts.requireNonNullArgument(absolutePath);
        Contracts.requireThat(absolutePath.isAbsolute());

        final Path moreAccuratePath;
        if (Files.exists(absolutePath, LinkOption.NOFOLLOW_LINKS)) {
            moreAccuratePath = Utils.toReal(absolutePath);
        } else {
            moreAccuratePath = absolutePath;
        }
        if (!moreAccuratePath.startsWith(repoPath) || moreAccuratePath.equals(repoPath)) {
            throw new LocalRepositoryException("Provided path " + absolutePath + " is not under repo path " + repoPath);
        }
    }

    @Override
    public void add(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var osPath = repoPath.resolve(path.toOsPath());
        if (!Files.isRegularFile(repoPath.resolve(path.toOsPath()), LinkOption.NOFOLLOW_LINKS)) {
            throw new LocalRepositoryException("Can't add file at path " + osPath + ". File must exist and be regular.");
        }
        final var content = Utils.readBytes(osPath);
        gitRepo.add(content, path);
    }

    @Override
    public void remove(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var index = gitRepo.getIndex();
        if (!index.contains(path)) {
            throw new LocalRepositoryException("Can't remove " + path + ". It is not in the index.");
        }
        final var osPath = repoPath.resolve(path.toOsPath());
        if (!Files.isRegularFile(osPath)) {
            gitRepo.remove(path);
            return;
        }
        if (index.inConflict(path)) {
            Utils.delete(osPath);
            gitRepo.remove(path);
            return;
        }
        final var currentContent = Utils.readBytes(osPath);
        final var currentHash = gitRepo.hash(currentContent);
        final var stagedHash = index.hashOfPath(path);
        if (currentHash.equals(stagedHash)) {
            Utils.delete(osPath);
            gitRepo.remove(path);
        } else {
            throw new LocalRepositoryException("Can't remove file at path " + osPath
                    + " because it will overwrite local changes."
            );
        }
    }

    @Override
    public void removeFromIndex(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        if (!gitRepo.remove(path)) {
            throw new LocalRepositoryException("Can't delete path from index" +
                    path.toOsPath() + ". It doesn't exist in index."
            );
        }
    }

    @Override
    public void removeForcibly(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        removeFromIndex(path);

        final var osPath = repoPath.resolve(path.toOsPath());

        if (Files.isRegularFile(osPath, LinkOption.NOFOLLOW_LINKS)) {
            Utils.delete(osPath);
        }
    }

    @Override
    public void commit(final CommitMessage message) {
        Contracts.requireNonNullArgument(message);

        gitRepo.commit(message);
    }

    @Override
    public void checkout(final Ref ref) {
        Contracts.requireNonNullArgument(ref);

        try {
            final List<VCSFileChange> changes = gitRepo.checkoutChanges(ref);
            final List<LocalConflict> localConflicts = localConflicts(changes);
            if (localConflicts.isEmpty()) {
                writeChanges(changes);
                gitRepo.checkout(ref);
            } else {
                throw new LocalRepositoryException(
                        "Local conflicts prevent checkout. Conflicts:" + System.lineSeparator()
                                + getMessageForUser(localConflicts)
                );
            }
        } catch (final GitRepositoryException ex) {
            throw new LocalRepositoryException("Local repository checkout failed.", ex);
        }
    }

    @Override
    public void checkoutFile(final Ref ref, final VCSPath vcsPath) {
        Contracts.requireNonNullArgument(ref);
        Contracts.requireNonNullArgument(vcsPath);
        Contracts.forbidThat(vcsPath.isRoot());

        final var osPath = resolveVCSPath(vcsPath);
        final var data = gitRepo.readFile(ref, vcsPath);
        if (!Files.exists(resolveVCSPath(vcsPath), LinkOption.NOFOLLOW_LINKS)) {
            if (!createDirs(vcsPath)) {
                throw new LocalRepositoryException(LocalConflict.cantCreateDirs(vcsPath).toUserMessage());
            }
            Utils.writeBytes(osPath, data);
        }
        if (!Files.isRegularFile(osPath)) {
            throw new LocalRepositoryException(LocalConflict.alreadyExists(vcsPath).toUserMessage());
        }
        Utils.writeBytes(osPath, data);
    }

    @Override
    public LocalRepoStatus status() {
        final var stagedIndex = gitRepo.getIndex();
        final var localIndex = Index.create(repoPath, gitRepo::hash);
        final var localChanges = localIndex.getDiff(stagedIndex);
        return new LocalRepoStatus(gitRepo.status(), localChanges);
    }

    private static String getMessageForUser(final List<LocalConflict> localConflicts) {
        return localConflicts.stream()
                .map(LocalConflict::toUserMessage)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private List<LocalConflict> localConflicts(final List<VCSFileChange> changes) {
        return changes.stream()
                .map(this::localConflict)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Nullable
    private LocalConflict localConflict(final VCSFileChange fileChange) {
        final var changedVCSPath = fileChange.getChangePath();
        final var changedOsPath = resolveVCSPath(changedVCSPath);
        if (fileChange instanceof Addition) {
            if (Files.exists(changedOsPath, LinkOption.NOFOLLOW_LINKS)) {
                return LocalConflict.alreadyExists(changedVCSPath);
            } else if (!createDirs(changedVCSPath)) {
                return LocalConflict.cantCreateDirs(changedVCSPath);
            }
        } else if (fileChange instanceof Modification) {
            final var exists = Files.exists(changedOsPath, LinkOption.NOFOLLOW_LINKS);
            if (!exists) {
                if (!createDirs(changedVCSPath)) {
                    return LocalConflict.cantCreateDirs(changedVCSPath);
                }
            } else {
                if (!Files.isRegularFile(changedOsPath, LinkOption.NOFOLLOW_LINKS)) {
                    return LocalConflict.alreadyExists(changedVCSPath);
                }
                final var newHash = gitRepo.hash(Utils.readBytes(changedOsPath));
                final var modification = (Modification) fileChange;
                final var originalSha = modification.getOriginalSha();
                if (!newHash.equals(originalSha)) {
                    return LocalConflict.localFileChanged(changedVCSPath);
                }
            }
        }
        return null;
    }

    private boolean createDirs(final VCSPath vcsPath) {
        if (vcsPath.getParent().isRoot()) {
            return true;
        }
        final var osPath = resolveVCSPath(vcsPath.getParent());
        try {
            Files.createDirectories(osPath);
            return true;
        } catch (FileAlreadyExistsException ex) {
            return false;
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't create dirs for path " + osPath, ex);
        }
    }

    private Path resolveVCSPath(final VCSPath path) {
        return repoPath.resolve(path.toOsPath());
    }

    private void writeChanges(final List<VCSFileChange> changes) {
        for (final var fileChange : changes) {
            final var osPath = resolveVCSPath(fileChange.getChangePath());
            if (fileChange instanceof Addition) {
                final var sha = ((Addition) fileChange).getAddedFileDesc().getSha();
                Utils.writeBytes(osPath, gitRepo.readFile(sha));
            } else if (fileChange instanceof Modification) {
                final var sha = ((Modification) fileChange).getModifiedFileDesc().getSha();
                Utils.writeBytes(osPath, gitRepo.readFile(sha));
            } else if (fileChange instanceof Deletion) {
                final var deletedFileSha = ((Deletion) fileChange).getDeletedFileDesc().getSha();
                if (Files.isRegularFile(osPath, LinkOption.NOFOLLOW_LINKS)) {
                    final var newSha = gitRepo.hash(Utils.readBytes(osPath));
                    if (newSha.equals(deletedFileSha)) {
                        // safe to delete
                        Utils.delete(osPath);
                    }
                }
            } else {
                throw Contracts.unreachable();
            }
        }
    }
}
