package ru.otus.vcs.objects;

import ru.otus.vcs.exception.InnerException;

public enum FileMode {

    REGULAR(100644),
    SYMLINK(120000),
    EXECUTABLE(100755);

    private final int flags;

    static FileMode fromFlags(final int flags) {
        switch (flags) {
            case 100644:
                return REGULAR;
            case 120000:
                return SYMLINK;
            case 100755:
                return EXECUTABLE;
            default:
                throw new InnerException("Unsupported flags = " + flags);
        }
    }

    FileMode(final int flags) {
        this.flags = flags;
    }

    public int asIntFlags() {
        return flags;
    }
}
