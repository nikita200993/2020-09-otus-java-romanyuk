package ru.otus.vcs.objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.vcs.index.Index;
import ru.otus.vcs.index.IndexEntry;
import ru.otus.vcs.path.VCSFileName;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

import java.util.List;

public class TestIndexFromTree {

    @Test
    void testWithoutNestedDir() {
        final var path1 = VCSPath.create("a");
        final var sha1 = Sha1.hash("a");
        final var path2 = VCSPath.create("b");
        final var sha2 = Sha1.hash("b");
        final var expectedIndex = Index.create(
                List.of(
                        IndexEntry.newNormalEntry(path1, sha1),
                        IndexEntry.newNormalEntry(path2, sha2)
                )
        );
        final var leaf1 = new TreeLeaf(FileType.Regular, path1.getFileName(), sha1);
        final var leaf2 = new TreeLeaf(FileType.Regular, path2.getFileName(), sha2);
        final var actualIndex = new Tree(List.of(leaf1, leaf2)).index((ref) -> {
                    throw new UnsupportedOperationException();
                }
        );
        Assertions.assertThat(actualIndex)
                .isEqualTo(expectedIndex);
    }

    @Test
    void testWithNestedDir() {
        final var file = VCSPath.create("a");
        final var shaForFile = Sha1.hash("a");
        final var dir = VCSPath.create("dir");
        final var nestedFile = VCSFileName.create("nested");
        final var nestedFileSha = Sha1.hash("b");
        final var expectedIndex = Index.create(
                List.of(
                        IndexEntry.newNormalEntry(file, shaForFile),
                        IndexEntry.newNormalEntry(dir.resolve(nestedFile), nestedFileSha)
                )
        );
        final var nestedTree = new Tree(List.of(new TreeLeaf(FileType.Regular, nestedFile, nestedFileSha)));
        final var fileLeaf = new TreeLeaf(FileType.Regular, file.getFileName(), shaForFile);
        final var dirLeaf = new TreeLeaf(FileType.Directory, dir.getFileName(), nestedTree.sha1());
        final var rootTree = new Tree(
                List.of(
                        fileLeaf,
                        dirLeaf
                )
        );
        final var actualIndex = rootTree.index((ref) -> nestedTree);
        Assertions.assertThat(actualIndex)
                .isEqualTo(expectedIndex);
    }
}
