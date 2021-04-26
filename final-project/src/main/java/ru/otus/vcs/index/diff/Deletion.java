package ru.otus.vcs.index.diff;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSFileDesc;
import ru.otus.vcs.path.VCSPath;

public class Deletion extends VCSFileChange {
    private final VCSFileDesc deletedFileDesc;

    public Deletion(final VCSFileDesc deletedFileDesc) {
        this.deletedFileDesc = Contracts.ensureNonNullArgument(deletedFileDesc);
    }

    public VCSFileDesc getDeletedFileDesc() {
        return deletedFileDesc;
    }

    @Override
    public VCSPath getChangePath() {
        return deletedFileDesc.getPath();
    }

    @Override
    public String toString() {
        return "removed file " + deletedFileDesc.getPath().toOsPath()
                + " with hash " + deletedFileDesc.getSha().getHexString();
    }
}
