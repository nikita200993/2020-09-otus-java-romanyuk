package ru.otus.vcs.index;

import ru.otus.utils.Contracts;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.ref.Sha1;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static ru.otus.vcs.utils.Utils.utf8;

/**
 * Simplified version of git index. Just contains list of file paths relative to workdir with stages and hash.
 */
public class Index {

    private final LinkedHashMap<VCSPath, List<IndexEntry>> pathToIndexEntries;

    private Index(LinkedHashMap<VCSPath, List<IndexEntry>> pathToIndexEntries) {
        this.pathToIndexEntries = pathToIndexEntries;
    }

    public static Index deserialize(final byte[] bytes) {
        Contracts.requireNonNullArgument(bytes);

        final var content = utf8(bytes);
        if (content.isBlank()) {
            return new Index(new LinkedHashMap<>());
        }
        final var indexEntries = Arrays.stream(content.split("\n"))
                .map(IndexEntry::fromLineContent)
                .collect(Collectors.toUnmodifiableList());
        return create(indexEntries);
    }

    public static Index create(final List<IndexEntry> indexEntries) {
        Contracts.requireNonNullArgument(indexEntries);

        return new Index(indexEntriesToMap(indexEntries));
    }

    public Index withNewIndexEntry(final VCSPath path, final Sha1 sha) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(sha);

        final var indexEntriesForPath = pathToIndexEntries.get(path);
        if (indexEntriesForPath != null
                && indexEntriesForPath.size() == 1
                && indexEntriesForPath.get(0).getSha().equals(sha)) {
            return this;
        } else {
            final var newMapping = new LinkedHashMap<>(pathToIndexEntries);
            final var newIndexEntry = IndexEntry.newNormalEntry(path, sha);
            newMapping.put(path, List.of(newIndexEntry));
            return new Index(newMapping);
        }
    }

    @Nullable
    public Index withRemovedIndexEntry(final VCSPath path) {
        Contracts.requireNonNullArgument(path);

        final var indexEntriesForPath = pathToIndexEntries.get(path);
        if (indexEntriesForPath == null) {
            return null;
        } else {
            final var newMapping = new LinkedHashMap<>(pathToIndexEntries);
            newMapping.remove(path);
            return new Index(newMapping);
        }
    }

    public boolean isEmpty() {
        return pathToIndexEntries.isEmpty();
    }

    public byte[] serialize() {
        return pathToIndexEntries.values().stream()
                .flatMap(List::stream)
                .map(IndexEntry::toLineContent)
                .collect(joining("\n"))
                .getBytes(StandardCharsets.UTF_8);
    }

    public boolean hasMergeConflict() {
        final var max = pathToIndexEntries.values().stream()
                .map(List::size)
                .max(Integer::compareTo);
        return max.isPresent() && max.get() > 1;
    }

    public Map<VCSPath, Sha1> getPathToSha() {
        Contracts.requireThat(!hasMergeConflict());

        return pathToIndexEntries.entrySet()
                .stream()
                .collect(
                        toUnmodifiableMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().get(0).getSha()
                        )
                );
    }

    public Map<VCSPath, List<IndexEntry>> getPathToIndexEntries() {
        return Collections.unmodifiableMap(pathToIndexEntries);
    }

    public Set<VCSPath> getConflictPaths() {
        return pathToIndexEntries.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    private static LinkedHashMap<VCSPath, List<IndexEntry>> indexEntriesToMap(final List<IndexEntry> entries) {
        final Map<VCSPath, Set<Sha1>> pathToSha = new HashMap<>();
        final Map<VCSPath, Set<Stage>> pathToInteger = new HashMap<>();
        final LinkedHashMap<VCSPath, List<IndexEntry>> result = new LinkedHashMap<>();
        for (final IndexEntry entry : entries) {
            final var correspondingSha = pathToSha.computeIfAbsent(entry.getPath(), (unused) -> new HashSet<>());
            final var correspondingStages = pathToInteger.computeIfAbsent(entry.getPath(), (unused) -> new HashSet<>());
            Contracts.requireThat(
                    correspondingSha.add(entry.getSha()),
                    String.format(
                            "Repetitive sha = %s for the same path = %s.",
                            entry.getSha(),
                            entry.getPath()
                    )
            );
            Contracts.requireThat(
                    correspondingStages.add(entry.getStage()),
                    String.format(
                            "Repetitive stage = %s for the same path = %s.",
                            entry.getStage(),
                            entry.getPath()
                    )
            );
            result.computeIfAbsent(entry.getPath(), (unused) -> new ArrayList<>()).add(entry);
        }
        checkStages(result);
        result.replaceAll((k, v) -> List.copyOf(v));
        return result;
    }

    private static void checkStages(final Map<VCSPath, List<IndexEntry>> pathToIndexEntries) {
        for (final var pathAndEntries : pathToIndexEntries.entrySet()) {
            final VCSPath path = pathAndEntries.getKey();
            final List<IndexEntry> indexEntries = pathAndEntries.getValue();
            Contracts.requireThat(
                    areIndexEntriesConsistent(indexEntries),
                    "Multiple index entries are not consistent for path '" + path
                            + "'. Entries: " + indexEntries
            );
        }
    }

    private static boolean areIndexEntriesConsistent(final List<IndexEntry> indexEntries) {

        if (indexEntries.size() == 1 && indexEntries.get(0).getStage() == Stage.normal) {
            return true;
        } else if (indexEntries.size() == 2) {
            return EnumSet.of(Stage.receiver, Stage.giver).equals(
                    EnumSet.of(indexEntries.get(0).getStage(), indexEntries.get(1).getStage())
            );
        } else if (indexEntries.size() == 3) {
            return EnumSet.of(Stage.base, Stage.receiver, Stage.giver).equals(
                    EnumSet.of(
                            indexEntries.get(0).getStage(),
                            indexEntries.get(1).getStage(),
                            indexEntries.get(2).getStage()
                    )
            );
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return utf8(serialize());
    }
}
