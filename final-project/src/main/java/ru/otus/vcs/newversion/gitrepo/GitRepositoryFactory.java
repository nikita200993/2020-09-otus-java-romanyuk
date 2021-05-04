package ru.otus.vcs.newversion.gitrepo;

import javax.annotation.Nullable;
import java.nio.file.Path;

public interface GitRepositoryFactory {

    /**
     * Null if can't find repo.
     *
     * @param path possible location of repo.
     * @return Repo if it located at provided path.
     */
    @Nullable
    GitRepository restore(Path path, boolean bare);

    /**
     * Creates new repo in empty dir.
     *
     * @param path empty dir.
     * @param bare
     * @return new repo.
     */
    GitRepository createNew(Path path, boolean bare);
}
