package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.otus.vcs.utils.Utils.concat;

public class Tree extends GitObject {

    public static final String type = "tree";

    private final List<TreeLeaf> leaves;

    Tree(final List<TreeLeaf> leaves) {
        this.leaves = Contracts.ensureNonNullArgument(leaves);
    }

    public static Tree deserialize(final byte[] entriesList) {
        int start = 0;
        final var result = new ArrayList<TreeLeaf>();
        while (start < entriesList.length) {
            final var leafAndOffset = TreeLeaf.deserialize(entriesList, start);
            result.add(leafAndOffset.first());
            start = leafAndOffset.second();
        }
        return new Tree(result);
    }

    @Override
    public byte[] serialize() {
        final byte[] content = serializeContent();
        final byte[] prefix = (type + ' ' + content.length + (char) 0).getBytes(StandardCharsets.UTF_8);
        return concat(prefix, content);
    }

    public List<TreeLeaf> getLeaves() {
        return leaves;
    }

    private byte[] serializeContent() {
        return concat(leaves.stream()
                .map(TreeLeaf::serialize)
                .collect(Collectors.toList())
        );
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
