package ru.otus.vcs.objects;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.InnerException;
import ru.otus.vcs.utils.Tuple2;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static ru.otus.vcs.utils.Utils.*;


public class TreeLeaf {

    private final FileMode mode;
    private final Path path;
    private final String sha;

    public TreeLeaf(final FileMode mode, final Path path, final String sha) {
        this.mode = Contracts.ensureNonNullArgument(mode);
        this.path = Contracts.ensureNonNullArgument(path);
        this.sha = Contracts.ensureNonNullArgument(sha);
    }

    public static Tuple2<TreeLeaf, Integer> deserialize(final byte[] entriesList, final int start) {
        final var fileModeAndNewOffset = deserializeFileMode(entriesList, start);
        final var pathAndNewOffset = deserializePath(entriesList, fileModeAndNewOffset.second());
        final var shaAndNewOffset = deserializeSha(entriesList, pathAndNewOffset.second());
        return new Tuple2<>(
                new TreeLeaf(
                        fileModeAndNewOffset.first(),
                        pathAndNewOffset.first(),
                        shaAndNewOffset.first()
                ),
                shaAndNewOffset.second()
        );
    }

    public FileMode getMode() {
        return mode;
    }

    public Path getPath() {
        return path;
    }

    public String getSha() {
        return sha;
    }

    public byte[] serialize() {
        try {
            final byte[] partOne = (Integer.toString(mode.asIntFlags())
                    + ' '
                    + path.toString()
                    + (char) 0).getBytes(StandardCharsets.UTF_8);
            final byte[] binarySha = Hex.decodeHex(sha);
            return concat(partOne, binarySha);
        } catch (final DecoderException ex) {
            throw new InnerException("Can't convert sha1 hex string to binary.", ex);
        }
    }

    static TreeLeaf deserialize(final byte[] data) {
        return deserialize(data, 0).first();
    }

    private static Tuple2<FileMode, Integer> deserializeFileMode(final byte[] entriesList, final int start) {
        final int end = indexOf(entriesList, (byte) ' ', start, entriesList.length);
        if (end == -1) {
            throw new InnerException("Can't deserialize file mode. Failed to find 'space'");
        }
        final var intString = utf8(Arrays.copyOfRange(entriesList, start, end));
        final int flags = Integer.parseInt(intString);
        return new Tuple2<>(FileMode.fromFlags(flags), end + 1);
    }

    private static Tuple2<Path, Integer> deserializePath(final byte[] entriesList, final int start) {
        final int end = indexOf(entriesList, (byte) 0, start, entriesList.length);
        if (end == -1) {
            throw new InnerException("Can't deserialize path. Failed to find null byte");
        }
        final String path = utf8(Arrays.copyOfRange(entriesList, start, end));
        return new Tuple2<>(Path.of(path), end + 1);
    }

    private static Tuple2<String, Integer> deserializeSha(final byte[] entriesList, final int start) {
        if (start + 20 > entriesList.length) {
            throw new InnerException("Can't deserialize sha. There is no available 20 bytes in array.");
        }
        final String sha = new String(Hex.encodeHex(entriesList, start, 20, true));
        return new Tuple2<>(sha, start + 20);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeLeaf treeLeaf = (TreeLeaf) o;
        return mode == treeLeaf.mode && path.equals(treeLeaf.path) && sha.equals(treeLeaf.sha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, path, sha);
    }
}
