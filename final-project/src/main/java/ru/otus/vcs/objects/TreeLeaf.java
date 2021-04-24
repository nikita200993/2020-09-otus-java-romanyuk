package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSFileName;
import ru.otus.vcs.ref.Sha1;
import ru.otus.vcs.utils.Tuple2;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static ru.otus.vcs.utils.Utils.indexOf;
import static ru.otus.vcs.utils.Utils.utf8;


public class TreeLeaf {

    private final FileType type;
    private final VCSFileName fileName;
    private final Sha1 sha;

    public TreeLeaf(final FileType type, final VCSFileName fileName, final Sha1 sha) {
        this.type = Contracts.ensureNonNullArgument(type);
        this.fileName = Contracts.ensureNonNullArgument(fileName);
        this.sha = Contracts.ensureNonNullArgument(sha);
    }

    public static Tuple2<TreeLeaf, Integer> deserialize(final byte[] entriesList, final int start) {
        final var fileModeAndNewOffset = deserializeFileType(entriesList, start);
        final var pathAndNewOffset = deserializeFileName(
                entriesList,
                fileModeAndNewOffset.second()
        );
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

    static TreeLeaf deserialize(final byte[] treeLeafData) {
        Contracts.requireNonNullArgument(treeLeafData);

        return deserialize(treeLeafData, 0).first();
    }

    public FileType getType() {
        return type;
    }

    public VCSFileName getFileName() {
        return fileName;
    }

    public Sha1 getSha() {
        return sha;
    }

    public byte[] serialize() {
        final var content = type.getFileTypeName()
                + ' '
                + fileName.getName()
                + (char) 0
                + sha.getHexString();
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private static Tuple2<FileType, Integer> deserializeFileType(final byte[] entriesList, final int start) {
        final int end = indexOf(entriesList, (byte) ' ', start, entriesList.length);
        Contracts.requireThat(end != - 1, "No whitespace in tree leaf data.");
        final var fileTypeName = utf8(Arrays.copyOfRange(entriesList, start, end));
        Contracts.requireThat(FileType.isValidFileTypeName(fileTypeName), "Bad file type name " + fileTypeName);
        return new Tuple2<>(FileType.fromFileTypeName(fileTypeName), end + 1);
    }

    private static Tuple2<VCSFileName, Integer> deserializeFileName(final byte[] entriesList, final int start) {
        final int end = indexOf(entriesList, (byte) 0, start, entriesList.length);
        Contracts.requireThat(end != - 1, "No null byte in tree leaf data.");
        final String fileName = utf8(Arrays.copyOfRange(entriesList, start, end));
        Contracts.requireThat(VCSFileName.isValidVCSFileName(fileName), "Bad file name.");
        return new Tuple2<>(VCSFileName.create(fileName), end + 1);
    }

    private static Tuple2<Sha1, Integer> deserializeSha(final byte[] entriesList, final int start) {
        Contracts.requireThat(start + 40 <= entriesList.length, "Bad tree leaf data. Bad sha.");
        final var shaHex = utf8(Arrays.copyOfRange(entriesList, start, start + 40));
        Contracts.requireThat(Sha1.isValidSha1HexString(shaHex), "Bad hex string value = " + shaHex + ".");
        return new Tuple2<>(Sha1.create(shaHex), start + 40);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeLeaf treeLeaf = (TreeLeaf) o;
        return type == treeLeaf.type && fileName.equals(treeLeaf.fileName) && sha.equals(treeLeaf.sha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fileName, sha);
    }
}
