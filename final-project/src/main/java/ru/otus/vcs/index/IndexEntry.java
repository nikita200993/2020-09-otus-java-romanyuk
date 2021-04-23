package ru.otus.vcs.index;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

public class IndexEntry {
    private final Stage stage;
    private final VCSPath path;
    private final Sha1 sha;

    IndexEntry(final Stage stage, final VCSPath path, final Sha1 sha) {
        Contracts.requireNonNullArgument(stage);
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(sha);

        this.stage = stage;
        this.path = path;
        this.sha = sha;
    }

    public static IndexEntry fromLineContent(final String content) {
        final int firstWs = content.indexOf(' ');
        final int lastWs = content.lastIndexOf(' ');
        Contracts.requireThat(firstWs != -1, badLineContent(content) + " No whitespace.");
        Contracts.requireThat(firstWs != lastWs, badLineContent(content) + " No second whitespace.");
        try {
            final int code = Integer.parseInt(content.substring(0, firstWs));
            Contracts.requireThat(
                    code >= 0 && code <= 3,
                    String.format("Bad code %d in line '%s'.", code, content)
            );
            final String path = content.substring(firstWs + 1, lastWs);
            Contracts.requireThat(
                    VCSPath.isValidVCSPathString(path),
                    String.format("Bad path = '%s' in line = '%s'.", path, content)
            );
            final String sha1 = content.substring(lastWs + 1);
            Contracts.requireThat(Sha1.isValidSha1HexString(sha1), badLineContent(content) + " Bad sha1.");
            return new IndexEntry(Stage.fromCode(code), VCSPath.create(path), Sha1.create(sha1));
        } catch (final NumberFormatException ex) {
            throw Contracts.unreachable(badLineContent(content) + " Unable to parse code at the start of hte line.");
        }
    }

    public static IndexEntry newNormalEntry(final VCSPath vcsPath, final Sha1 sha) {
        Contracts.requireNonNullArgument(vcsPath);
        Contracts.requireNonNullArgument(sha);

        return new IndexEntry(Stage.normal, vcsPath, sha);
    }

    public String toLineContent() {
        return stage.getCode() + " " + path + " " + sha.getHexString();
    }

    public Stage getStage() {
        return stage;
    }

    public VCSPath getPath() {
        return path;
    }

    public Sha1 getSha() {
        return sha;
    }

    private static String badLineContent(final String content) {
        return "Bad line content '" + content + "'.";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexEntry that = (IndexEntry) o;
        return stage == that.stage && path.equals(that.path) && sha.equals(that.sha);
    }

    @Override
    public int hashCode() {
        return stage.hashCode() * 31 * 31 + path.hashCode() * 31 + sha.hashCode();
    }

    @Override
    public String toString() {
        return "IndexEntry{" +
                "stage=" + stage +
                ", path=" + path +
                ", sha='" + sha + '\'' +
                '}';
    }
}
