package ru.otus.vcs.newversion.localrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.gitrepo.CommitMessage;
import ru.otus.vcs.newversion.gitrepo.GitRepository;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Ref;
import ru.otus.vcs.utils.Utils;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

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
        if (!moreAccuratePath.startsWith(repoPath)) {
            throw new LocalRepositoryException("Provided path " + absolutePath + " is not under repo path " + repoPath);
        }
    }

    @Override
    public boolean add(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var osPath = repoPath.resolve(path.toOsPath());
        if (!Files.isRegularFile(repoPath.resolve(path.toOsPath()), LinkOption.NOFOLLOW_LINKS)) {
            throw new LocalRepositoryException("Can't add file at path " + osPath + ". File must exist and be regular.");
        }
        final var content = Utils.readBytes(osPath);
        return gitRepo.add(content, path);
    }

    @Override
    public void remove(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var removedSha = gitRepo.remove(path);
        if (removedSha == null) {
            throw new LocalRepositoryException("Can't remove " + path + ". It is not in the index.");
        }
        if (!Files.isRegularFile(repoPath.resolve(path.toOsPath()))) {
            return;
        }

    }

    @Override
    public void commit(CommitMessage message) {

    }

    @Override
    public void checkout(Ref ref) {

    }

    @Override
    public void checkoutFile(Ref ref, VCSPath path) {

    }

    @Override
    public StatusResult status() {
        return null;
    }
}
