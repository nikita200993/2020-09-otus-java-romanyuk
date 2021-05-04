package ru.otus.vcs.newversion.objects;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.ref.Sha1;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static ru.otus.vcs.newversion.utils.Utils.concat;
import static ru.otus.vcs.newversion.utils.Utils.indexOf;
import static ru.otus.vcs.newversion.utils.Utils.utf8;

public abstract class GitObject {

    public static GitObject deserialize(final byte[] bytes) {
        Contracts.requireNonNullArgument(bytes);

        final int firstWsPos = indexOf(bytes, (byte) ' ', 0, bytes.length);
        Contracts.requireThat(firstWsPos != -1, badFormat("No whitespace."));
        final String typeName = utf8(Arrays.copyOfRange(bytes, 0, firstWsPos));
        Contracts.requireThat(ObjectType.isValidName(typeName), badFormat("Invalid type string."));
        final var type = ObjectType.forName(typeName);
        final int nullBytePos = indexOf(bytes, (byte) 0, firstWsPos + 1, bytes.length);
        Contracts.requireThat(nullBytePos != -1, badFormat("No null byte."));
        Contracts.requireThat(
                nullBytePos > firstWsPos,
                badFormat("Null byte must be after first whitespace.")
        );
        final int size = parseSize(utf8(Arrays.copyOfRange(bytes, firstWsPos + 1, nullBytePos)));
        Contracts.requireThat(
                size == bytes.length - nullBytePos - 1,
                badFormat("Actual size is not equal to declared.")
        );
        final byte[] content = Arrays.copyOfRange(bytes, nullBytePos + 1, bytes.length);
        switch (type) {
            case Blob:
                return new Blob(content);
            case Tree:
                return Tree.deserialize(content);
            case Commit:
                return Commit.deserialize(content);
            default:
                throw Contracts.unreachable();
        }
    }

    public abstract ObjectType getType();

    public abstract byte[] serializeContent();

    public final byte[] serialize() {
        final var content = serializeContent();
        final var prefix = (getType().getName() +
                ' ' +
                content.length +
                (char) 0).getBytes(StandardCharsets.UTF_8);
        return concat(prefix, content);
    }

    public Sha1 sha1() {
        return Sha1.hash(serialize());
    }

    private static int parseSize(final String size) {
        try {
            return Integer.parseInt(size);
        } catch (final NumberFormatException ex) {
            throw Contracts.unreachable(badFormat("Can't parse size."));
        }
    }

    private static String badFormat(final String additionalInfo) {
        return "Bad format of object data. " + additionalInfo;
    }
}
