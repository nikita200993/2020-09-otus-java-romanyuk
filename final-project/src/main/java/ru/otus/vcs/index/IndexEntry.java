package ru.otus.vcs.index;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.DeserializationException;
import ru.otus.vcs.naming.VCSPath;

import static ru.otus.vcs.utils.Utils.isSha1;

public class IndexEntry {
    private final Stage stage;
    /**
     * Relative to working dir.
     */
    private final VCSPath path;
    private final String sha;

    IndexEntry(final Stage stage, final VCSPath path, final String sha) {
        Contracts.requireNonNullArgument(stage);
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(sha);
        Contracts.requireThat(isSha1(sha));

        this.stage = stage;
        this.path = path;
        this.sha = sha;
    }

    public static IndexEntry fromLineContent(final String content) {
        final int firstWs = content.indexOf(' ');
        final int lastWs = content.lastIndexOf(' ');
        if (firstWs == -1) {
            throw new DeserializationException("There is no whitespace in line = " + content);
        } else if (firstWs == lastWs) {
            throw new DeserializationException("There should be two whitespaces in line = " + content);
        }
        try {
            final int code = Integer.parseInt(content.substring(0, firstWs));
            if (code < 0 || code > 3) {
                throw new DeserializationException(String.format("Bad code %d in line '%s'.", code, content));
            }
            final String path = content.substring(firstWs + 1, lastWs);
            if (!VCSPath.isValidVCSPathString(path)) {
                throw new DeserializationException(
                        String.format(
                                "Bad path = '%s' in line = '%s'. %s.",
                                path,
                                content,
                                VCSPath.hint()
                        )
                );
            }
            final String sha1 = content.substring(lastWs + 1);
            if (!isSha1(sha1)) {
                throw new DeserializationException(String.format("Bad hash = '%s' in line = '%s'.", sha1, content));
            }
            return new IndexEntry(Stage.fromCode(code), VCSPath.create(path), sha1);
        } catch (final NumberFormatException ex) {
            throw new DeserializationException("Can't parse int at the start of the line = '" + content + "'.", ex);
        }
    }

    public String toLineContent() {
        return stage.getCode() + " " + path + " " + sha;
    }

    public Stage getStage() {
        return stage;
    }

    public VCSPath getPath() {
        return path;
    }

    public String getSha() {
        return sha;
    }

}
