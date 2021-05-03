package ru.otus.vcs.newversion.objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.vcs.newversion.index.Index;
import ru.otus.vcs.newversion.index.IndexEntry;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.Sha1;

import java.util.List;

public class TestTreeFromIndex {

    @Test
    void testWithoutNestedDir() {
        final var fileName1 = VCSPath.create("1");
        final var sha1 = Sha1.hash("1");
        final var fileName2 = VCSPath.create("2");
        final var sha2 = Sha1.hash("2");
        final var index = Index.create(
                List.of(
                        IndexEntry.newNormalEntry(fileName1, sha1),
                        IndexEntry.newNormalEntry(fileName2, sha2)
                )
        );
        final var tree = new Tree(
                List.of(
                        new TreeLeaf(FileType.Regular, fileName1.getFileName(), sha1),
                        new TreeLeaf(FileType.Regular, fileName2.getFileName(), sha2)
                )
        );
        Assertions.assertThat(Tree.createFromIndex(index).get(0))
                .isEqualTo(tree);
    }

    @Test
    void testWithNestedDir() {
        final var rootFilePath = VCSPath.create("1");
        final var rootFileSha = Sha1.hash("1");
        final var dir = VCSPath.create("2");
        final var nestedFilePath = VCSPath.create("2/3");
        final var nestedFileSha = Sha1.hash("3");
        final var index = Index.create(
                List.of(
                        IndexEntry.newNormalEntry(rootFilePath, rootFileSha),
                        IndexEntry.newNormalEntry(nestedFilePath, nestedFileSha)
                )
        );
        final var nestedTree = new Tree(
                List.of(
                        new TreeLeaf(FileType.Regular, nestedFilePath.getFileName(), nestedFileSha)
                )
        );
        final var tree = new Tree(
                List.of(
                        new TreeLeaf(FileType.Regular, rootFilePath.getFileName(), rootFileSha),
                        new TreeLeaf(FileType.Directory, dir.getFileName(), nestedTree.sha1())
                )
        );
        Assertions.assertThat(Tree.createFromIndex(index))
                .isEqualTo(List.of(tree, nestedTree));
    }
}
