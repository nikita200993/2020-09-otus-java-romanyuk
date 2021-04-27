package ru.otus.vcs.newversion.localrepo;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSPath;

class LocalConflict {

    private final Reason reason;
    private final VCSPath path;

    LocalConflict(final Reason reason, final VCSPath path) {
        Contracts.requireNonNullArgument(reason);
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        this.reason = reason;
        this.path = path;
    }

    static LocalConflict cantCreateDirs(final VCSPath vcsPath) {
        return new LocalConflict(Reason.CANT_CREATE_DIRS, vcsPath);
    }

    static LocalConflict alreadyExists(final VCSPath vcsPath) {
        return new LocalConflict(Reason.ALREADY_EXISTS, vcsPath);
    }

    static LocalConflict localFileChanged(final VCSPath vcsPath) {
        return new LocalConflict(Reason.LOCAL_FILE_CHANGED, vcsPath);
    }

    Reason getReason() {
        return reason;
    }

    VCSPath getPath() {
        return path;
    }

    String toUserMessage() {
        switch (reason) {
            case ALREADY_EXISTS:
                return "Can't checkout file " + path.toOsPath()
                        + " because there exists local file with the same name.";
            case CANT_CREATE_DIRS:
                return "Can't checkout file " + path.toOsPath()
                        + " because local files prevent from creating dirs to it.";
            case LOCAL_FILE_CHANGED:
                return "Can't checkout file " + path.toOsPath()
                        + " because local file has changed comparing with HEAD commit";
            default:
                throw Contracts.unreachable();
        }
    }

    enum Reason {
        LOCAL_FILE_CHANGED,
        CANT_CREATE_DIRS,
        ALREADY_EXISTS
    }
}
