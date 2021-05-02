package ru.otus.vcs.newversion.localrepo;

import ru.otus.vcs.newversion.gitrepo.CommitMessage;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Ref;

import java.nio.file.Path;

public interface LocalRepository {

    Path realRepoDir();

    void checkThatIsRepositoryPath(Path absolutePath) throws LocalRepositoryException;

    void add(VCSPath path);

    void remove(VCSPath path);

    void removeFromIndex(VCSPath path);

    void removeForcibly(VCSPath path);

    void commit(CommitMessage message);

    void checkout(Ref ref);

    void checkoutFile(Ref ref, VCSPath path);

    LocalRepoStatus status();
}
