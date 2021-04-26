package ru.otus.vcs.path;

import ru.otus.utils.Contracts;
import ru.otus.vcs.ref.Sha1;

import java.util.Objects;

public class VCSFileDesc {

    private final VCSPath path;
    private final Sha1 sha;

    public VCSFileDesc(final VCSPath path, final Sha1 sha) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(sha);
        Contracts.forbidThat(path.isRoot());

        this.path = path;
        this.sha = sha;
    }

    public VCSPath getPath() {
        return path;
    }

    public Sha1 getSha() {
        return sha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VCSFileDesc that = (VCSFileDesc) o;
        return path.equals(that.path) && sha.equals(that.sha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, sha);
    }

    @Override
    public String toString() {
        return "VCSFileDesc{" +
                "path=" + path +
                ", sha=" + sha +
                '}';
    }
}
