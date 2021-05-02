package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.config.GitConfig;
import ru.otus.vcs.utils.Utils;

import javax.annotation.Nullable;
import java.nio.file.Path;

public class GitRepositoryFactoryImpl implements GitRepositoryFactory {

    @Nullable
    @Override
    public GitRepoImpl restore(final Path path, boolean bare) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireThat(Utils.isDirectoryNoFollow(path));

        final var realPath = Utils.toReal(path);
        final Path repoPath;
        if (bare) {
            repoPath = realPath;
        } else {
            repoPath = realPath.resolve(GitRepository.DIR_NAME);
        }
        if (Utils.isDirectoryNoFollow(repoPath) && RepositoryLayout.isRepoLayout(repoPath)) {
            return new GitRepoImpl(
                    repoPath,
                    GitConfig.create(repoPath.resolve(RepositoryLayout.CONFIG))
            );
        } else {
            return null;
        }
    }

    @Override
    public GitRepoImpl createNew(final Path path, boolean bare) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireThat(Utils.isEmptyDir(path));

        final var realPath = Utils.toReal(path);

        final Path pathForRepo;
        if (bare) {
            pathForRepo = realPath;
        } else {
            pathForRepo = realPath.resolve(GitRepository.DIR_NAME);
            Utils.createDir(pathForRepo);
        }
        RepositoryLayout.createLayout(pathForRepo);
        return new GitRepoImpl(
                pathForRepo,
                GitConfig.create(pathForRepo.resolve(RepositoryLayout.CONFIG))
        );
    }
}
