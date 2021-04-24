package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;
import ru.otus.vcs.index.Index;
import ru.otus.vcs.index.IndexEntry;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;
import ru.otus.vcs.repository.GitRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.otus.vcs.utils.Utils.concat;

public class Tree extends GitObject {

    private final List<TreeLeaf> leaves;

    Tree(final List<TreeLeaf> leaves) {
        Contracts.requireNonNullArgument(leaves);
        checkNoRepeatedFileNames(leaves);

        this.leaves = Contracts.ensureNonNullArgument(leaves);
    }

    public static Tree deserialize(final byte[] entriesList) {
        Contracts.requireNonNullArgument(entriesList);

        int start = 0;
        final var result = new ArrayList<TreeLeaf>();
        final var uniqueLeaves = new HashSet<TreeLeaf>();
        while (start < entriesList.length) {
            final var leafAndOffset = TreeLeaf.deserialize(entriesList, start);
            result.add(leafAndOffset.first());
            Contracts.requireThat(uniqueLeaves.add(leafAndOffset.first()), "Repeated file name in tree data");
            start = leafAndOffset.second();
        }
        return new Tree(result);
    }

    Index index(final GitRepository repository) {
        Contracts.requireNonNullArgument(repository);

        final var currentDir = VCSPath.root;
        final var visitedTrees = new HashSet<Sha1>();
        final var collectedIndexEntries = new ArrayList<IndexEntry>();
        collectIndexEntries(currentDir, repository, collectedIndexEntries, visitedTrees);
        return Index.create(collectedIndexEntries);
    }

    public List<TreeLeaf> getLeaves() {
        return leaves;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.Tree;
    }

    @Override
    public byte[] serializeContent() {
        return concat(leaves.stream()
                .map(TreeLeaf::serialize)
                .collect(Collectors.toList())
        );
    }

    private static void checkNoRepeatedFileNames(final List<TreeLeaf> treeLeaves) {
        Contracts.requireThat(treeLeaves.size() == new HashSet<>(treeLeaves).size());
    }

    private void collectIndexEntries(
            final VCSPath currentDir,
            final GitRepository repository,
            final List<IndexEntry> indexEntries,
            final Set<Sha1> visitedTress) {
        Contracts.requireThat(visitedTress.add(sha1()), "Cycle in tree O_o.");

        for (final var leaf : leaves) {
            if (leaf.getType() == FileType.Regular) {
                indexEntries.add(
                        IndexEntry.newNormalEntry(currentDir.resolve(leaf.getFileName()), leaf.getSha())
                );
            } else {
                final var gitObject = repository.readGitObjectOrThrowIfAbsent(leaf.getSha());
                Contracts.requireThat(gitObject instanceof Tree);
                ((Tree) gitObject).collectIndexEntries(
                        currentDir.resolve(leaf.getFileName()),
                        repository,
                        indexEntries,
                        visitedTress
                );
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tree tree = (Tree) o;
        return leaves.equals(tree.leaves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaves);
    }
}
