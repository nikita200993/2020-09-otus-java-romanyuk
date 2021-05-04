package ru.otus.vcs.newversion.index.diff;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.path.VCSFileDesc;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.Sha1;

import java.util.Objects;

public class Modification extends VCSFileChange {
    private final VCSFileDesc modifiedFileDesc;

    private final Sha1 originalSha;

    public Modification(final VCSFileDesc modifiedFileDesc, final Sha1 originalSha) {
        this.modifiedFileDesc = Contracts.ensureNonNullArgument(modifiedFileDesc);
        this.originalSha = Contracts.ensureNonNullArgument(originalSha);
    }

    @Override
    public VCSPath getChangePath() {
        return modifiedFileDesc.getPath();
    }

    public VCSFileDesc getModifiedFileDesc() {
        return modifiedFileDesc;
    }

    public Sha1 getOriginalSha() {
        return originalSha;
    }

    @Override
    public String toString() {
        return "modified file " + modifiedFileDesc.getPath().toOsPath()
                + " new hash " + modifiedFileDesc.getSha().getHexString()
                + " previous hash " + originalSha.getHexString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Modification that = (Modification) o;
        return modifiedFileDesc.equals(that.modifiedFileDesc) && originalSha.equals(that.originalSha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifiedFileDesc, originalSha);
    }
}
