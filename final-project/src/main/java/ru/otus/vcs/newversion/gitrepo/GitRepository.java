package ru.otus.vcs.newversion.gitrepo;

import ru.otus.vcs.newversion.index.Index;
import ru.otus.vcs.newversion.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.BranchName;
import ru.otus.vcs.newversion.ref.Ref;
import ru.otus.vcs.newversion.ref.Sha1;

import java.nio.file.Path;
import java.util.List;

public interface GitRepository {

    String DIR_NAME = ".simplegit";

    Sha1 commit(final CommitMessage commitMessage);

    Path repoRealPath();

    void abortMerge();

    void branch(BranchName branchName);

    void add(byte[] data, VCSPath path);

    boolean remove(VCSPath path);

    Sha1 hash(byte[] data);

    byte[] readFile(Ref ref, VCSPath vcsPath);

    byte[] readFile(Sha1 sha);

    List<VCSFileChange> checkoutChanges(Ref ref);

    void checkout(Ref ref);

    GitRepoStatus status();

    Index getIndex();

}
