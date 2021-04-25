package ru.otus.vcs.objects;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import ru.otus.utils.Contracts;
import ru.otus.vcs.index.Index;
import ru.otus.vcs.index.IndexEntry;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.otus.vcs.utils.Utils.concat;

public class Tree extends GitObject {

    private final List<TreeLeaf> leaves;

    Tree(final List<TreeLeaf> leaves) {
        Contracts.requireNonNullArgument(leaves);
        Contracts.requireThat(leaves.size() > 0);
        checkNoRepeatedFileNames(leaves);

        final var copy = new ArrayList<>(leaves);
        copy.sort(Comparator.comparing(treeLeaf -> treeLeaf.getFileName().getName()));
        this.leaves = copy;
    }

    /**
     * @param index to create from.
     * @return list of trees, where first tree in the list is the root.
     */
    public static List<Tree> createFromIndex(final Index index) {
        Contracts.requireNonNullArgument(index);
        Contracts.forbidThat(index.hasMergeConflict());

        return createFromPathToSha(index.getPathToSha());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<Tree> createFromPathToSha(final Map<VCSPath, Sha1> pathShaMap) {
        Contracts.requireNonNullArgument(pathShaMap);
        Contracts.forbidThat(pathShaMap.isEmpty());

        final MutableGraph<VCSPath> graph = buildGraph(pathShaMap.keySet());
        final var result = new ArrayList<Tree>();
        final var dfsPostOrder = Traverser.forTree(graph).depthFirstPostOrder(VCSPath.root);
        final var collectedLeaves = new HashMap<VCSPath, List<TreeLeaf>>();
        for (final var path : dfsPostOrder) {
            if (!collectedLeaves.containsKey(path)) {
                collectedLeaves.computeIfAbsent(path.getParent(), (unused) -> new ArrayList<>())
                        .add(
                                new TreeLeaf(FileType.Regular, path.getFileName(), pathShaMap.get(path))
                        );
            } else {
                final var leaves = collectedLeaves.get(path);
                final var tree = new Tree(leaves);
                result.add(tree);
                if (path.isRoot()) {
                    continue;
                }
                collectedLeaves.computeIfAbsent(path.getParent(), (unused) -> new ArrayList<>())
                        .add(
                                new TreeLeaf(FileType.Directory, path.getFileName(), tree.sha1())
                        );
            }
        }
        Collections.reverse(result);
        return result;
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

    public Index index(final Function<Sha1, Tree> treeReader) {
        Contracts.requireNonNullArgument(treeReader);

        final var currentDir = VCSPath.root;
        final var visitedTrees = new HashSet<Sha1>();
        final var collectedIndexEntries = new ArrayList<IndexEntry>();
        collectIndexEntries(currentDir, treeReader, collectedIndexEntries, visitedTrees);
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

    private static MutableGraph<VCSPath> buildGraph(final Iterable<VCSPath> iterable) {
        final MutableGraph<VCSPath> graph = GraphBuilder.directed()
                .incidentEdgeOrder(ElementOrder.sorted(Comparator.comparing(VCSPath::toString)))
                .build();
        for (final var path : iterable) {
            var currentPath = path;
            while (!currentPath.isRoot()) {
                graph.putEdge(currentPath.getParent(), currentPath);
                currentPath = currentPath.getParent();
            }
        }
        return graph;
    }

    private void collectIndexEntries(
            final VCSPath currentDir,
            final Function<Sha1, Tree> objectReader,
            final List<IndexEntry> indexEntries,
            final Set<Sha1> visitedTress) {
        Contracts.requireThat(visitedTress.add(sha1()), "Cycle in tree O_o.");

        for (final var leaf : leaves) {
            if (leaf.getType() == FileType.Regular) {
                indexEntries.add(
                        IndexEntry.newNormalEntry(currentDir.resolve(leaf.getFileName()), leaf.getSha())
                );
            } else {
                objectReader.apply(leaf.getSha()).collectIndexEntries(
                        currentDir.resolve(leaf.getFileName()),
                        objectReader,
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
