package ru.otus.vcs.index.diff;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSFileDesc;
import ru.otus.vcs.path.VCSPath;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deletion deletion = (Deletion) o;
        return deletedFileDesc.equals(deletion.deletedFileDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deletedFileDesc);
    }
}
