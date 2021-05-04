package ru.otus.vcs.newversion.index.diff;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.path.VCSFileDesc;
import ru.otus.vcs.newversion.path.VCSPath;

import java.util.Objects;

public class Addition extends VCSFileChange {

    private final VCSFileDesc addedFileDesc;

    public Addition(final VCSFileDesc addedFileDesc) {
        this.addedFileDesc = Contracts.ensureNonNullArgument(addedFileDesc);
    }

    public VCSFileDesc getAddedFileDesc() {
        return addedFileDesc;
    }


    @Override
    public VCSPath getChangePath() {
        return addedFileDesc.getPath();
    }

    @Override
    public String toString() {
        return "add file " + addedFileDesc.getPath().toOsPath() + " with hash " + addedFileDesc.getSha().getHexString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Addition addition = (Addition) o;
        return addedFileDesc.equals(addition.addedFileDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addedFileDesc);
    }
}
