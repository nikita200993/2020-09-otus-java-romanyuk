package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;
import ru.otus.vcs.ref.Sha1;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.otus.vcs.utils.Utils.utf8;

public class Commit extends GitObject {

    private static final String TREE_HEADER = "tree";
    private static final String PARENT_HEADER = "parent";
    private static final String AUTHOR_HEADER = "author";

    private final Sha1 treeSha;
    @Nullable
    private final Sha1 firstParentSha;
    @Nullable
    private final Sha1 secondParentSha;
    private final String author;
    private final String message;

    public static Commit deserialize(final byte[] data) {
        Contracts.requireNonNullArgument(data);

        final var content = utf8(data);
        final var lines = content.split("\n");
        Contracts.requireThat(
                lines.length > 3,
                "Should contain at least tree info, author info, blank line and message");
        Sha1 tree = null;
        final List<Sha1> parents = new ArrayList<>();
        String author = null;
        String message = "";
        boolean sawBlankLine = false;
        for (final var line : lines) {
            if (sawBlankLine) {
                message = message + line + "\n";
                continue;
            } else if (line.isEmpty()) {
                sawBlankLine = true;
                continue;
            }
            final int firstWs = line.indexOf(' ');
            Contracts.requireThat(firstWs != -1, badFormat("No whitespace in line '" + line + "'."));
            final String header = line.substring(0, firstWs);
            final String value = line.substring(firstWs + 1);
            switch (header) {
                case TREE_HEADER:
                    Contracts.requireThat(tree == null, badFormat("There was already tree header."));
                    Contracts.requireThat(
                            Sha1.isValidSha1HexString(value),
                            badFormat("Bad sha1 in line = '" + line + "'.")
                    );
                    tree = Sha1.create(value);
                    break;
                case PARENT_HEADER:
                    Contracts.requireThat(
                            Sha1.isValidSha1HexString(value),
                            badFormat("Bad sha1 in line = '" + line + "'.")
                    );
                    parents.add(Sha1.create(value));
                    break;
                case AUTHOR_HEADER:
                    Contracts.requireThat(author == null, badFormat("There was already author header."));
                    Contracts.requireThat(isValidAuthor(value), badFormat("Bad author value = " + value));
                    author = value;
                    break;
                default:
                    throw Contracts.unreachable(badFormat("Bad header in line '" + line + "'."));
            }
        }
        // remove last new line
        message = message.substring(0, message.length() - 1);
        Contracts.requireNonNull(tree, badFormat("There was no tree info."));
        Contracts.requireThat(parents.size() <= 2, badFormat("More than two parents."));
        Contracts.requireNonNull(author, badFormat("There was no author info."));
        Contracts.requireThat(isValidMessage(message), "Message is invalid.");
        final Sha1 firstParent;
        final Sha1 secondParent;
        if (parents.size() == 0) {
            firstParent = null;
            secondParent = null;
        } else if (parents.size() == 1) {
            firstParent = parents.get(0);
            secondParent = null;
        } else {
            firstParent = parents.get(0);
            secondParent = parents.get(1);
        }
        return new Commit(tree, firstParent, secondParent, author, message);
    }

    public Commit(
            final Sha1 treeSha,
            @Nullable final Sha1 firstParentSha,
            @Nullable final Sha1 secondParentSha,
            final String author,
            final String message) {
        Contracts.requireNonNullArgument(treeSha);
        Contracts.forbidThat(firstParentSha == null && secondParentSha != null);
        Contracts.requireNonNullArgument(author);
        Contracts.requireNonNullArgument(message);
        Contracts.requireThat(isValidAuthor(author));
        Contracts.requireThat(isValidMessage(message));

        this.treeSha = Contracts.ensureNonNullArgument(treeSha);
        this.firstParentSha = firstParentSha;
        this.secondParentSha = secondParentSha;
        this.author = author;
        this.message = message;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.Commit;
    }

    @Override
    public byte[] serializeContent() {
        final var strBuilder = new StringBuilder();
        putSha(strBuilder, treeSha, TREE_HEADER);
        putSha(strBuilder, firstParentSha, PARENT_HEADER);
        putSha(strBuilder, secondParentSha, PARENT_HEADER);
        strBuilder.append(AUTHOR_HEADER)
                .append(' ')
                .append(author)
                .append('\n')
                .append('\n')
                .append(message)
                .append('\n');
        return strBuilder.toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    private static void putSha(final StringBuilder stringBuilder, @Nullable final Sha1 sha1, final String headerName) {
        if (sha1 == null) {
            return;
        }
        stringBuilder.append(headerName)
                .append(' ')
                .append(sha1.getHexString())
                .append('\n');
    }

    private static boolean isValidAuthor(final String author) {
        return !author.isBlank();
    }

    private static boolean isValidMessage(final String message) {
        return !message.isBlank();
    }

    private static String badFormat(final String additionalInfo) {
        return "Bad format of commit data. " + additionalInfo;
    }

    public Sha1 getTreeSha() {
        return treeSha;
    }

    @Nullable
    public Sha1 getFirstParentSha() {
        return firstParentSha;
    }

    @Nullable
    public Sha1 getSecondParentSha() {
        return secondParentSha;
    }

    public boolean isFirstCommit() {
        return firstParentSha == null && secondParentSha == null;
    }

    public boolean isMergeCommit() {
        return firstParentSha != null && secondParentSha != null;
    }

    public boolean isLinearCommit() {
        return firstParentSha != null && secondParentSha == null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit commit = (Commit) o;
        return treeSha.equals(commit.treeSha)
                && Objects.equals(firstParentSha, commit.firstParentSha)
                && Objects.equals(secondParentSha, commit.secondParentSha)
                && author.equals(commit.author)
                && message.equals(commit.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(treeSha, firstParentSha, secondParentSha, author, message);
    }
}
