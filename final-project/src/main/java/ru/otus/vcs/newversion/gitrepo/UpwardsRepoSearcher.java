package ru.otus.vcs.newversion.gitrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.utils.Utils;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;

public class UpwardsRepoSearcher {

    private final Path pathToStartSearchFrom;
    private final GitRepoFactory repoFactory;

    public UpwardsRepoSearcher(final Path pathToStartSearchFrom, final GitRepoFactory repoFactory) {
        Contracts.requireNonNullArgument(pathToStartSearchFrom);
        Contracts.requireNonNullArgument(repoFactory);
        Contracts.requireThat(Files.exists(pathToStartSearchFrom));

        this.pathToStartSearchFrom = Utils.toReal(pathToStartSearchFrom);
        this.repoFactory = repoFactory;
    }

    @Nullable
    public GitRepository find(final boolean bare) {
        Path whereToSearch = pathToStartSearchFrom;
        if (!Files.isDirectory(whereToSearch)) {
            whereToSearch = whereToSearch.getParent();
        }
        GitRepository repo = null;
        while (whereToSearch != null) {
            repo = repoFactory.restore(whereToSearch, bare);
            whereToSearch = whereToSearch.getParent();
        }
        return repo;
    }
}
