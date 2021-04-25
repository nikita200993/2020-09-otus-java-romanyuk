package ru.otus.vcs.index.diff;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSFileDesc;
import ru.otus.vcs.path.VCSPath;

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
}
