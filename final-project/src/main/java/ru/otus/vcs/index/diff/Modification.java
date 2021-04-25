package ru.otus.vcs.index.diff;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSFileDesc;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

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
}
