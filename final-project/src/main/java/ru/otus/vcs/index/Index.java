package ru.otus.vcs.index;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.DeserializationException;
import ru.otus.vcs.naming.VCSPath;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static ru.otus.vcs.utils.Utils.mapValuesToImmutableMap;
import static ru.otus.vcs.utils.Utils.utf8;

/**
 * Simplified version of git index. Just contains list of file paths relative to workdir with stages and hash.
 */
public class Index {

    private final List<IndexEntry> indexEntries;
    private final Map<VCSPath, List<IndexEntry>> pathToIndexEntries;

    private Index(final List<IndexEntry> indexEntries, Map<VCSPath, List<IndexEntry>> pathToIndexEntries) {
        this.indexEntries = indexEntries;
        this.pathToIndexEntries = pathToIndexEntries;
    }

    public static Index deserialize(final byte[] bytes) {
        Contracts.requireNonNullArgument(bytes);

        final var content = utf8(bytes);
        if (content.isBlank()) {
            return new Index(Collections.emptyList(), Collections.emptyMap());
        }
        final var indexEntries = Arrays.stream(content.split("\n"))
                .map(IndexEntry::fromLineContent)
                .collect(Collectors.toUnmodifiableList());
        return new Index(indexEntries, indexEntriesToMap(indexEntries));
    }

    public static Index create(final List<IndexEntry> indexEntries) {
        return new Index(List.copyOf(indexEntries), indexEntriesToMap(indexEntries));
    }

    public Index withNewIndexEntry(final VCSPath path, final String sha) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(sha);
        Contracts.requireThat(!hasMergeConflict());

        final var indexEntriesForPath = pathToIndexEntries.get(path);

        if (indexEntriesForPath == null) {
            final var newIndexEntry = new IndexEntry(Stage.normal, path, sha);
            final var newIndexEntries = new ArrayList<>(indexEntries);
            newIndexEntries.add(newIndexEntry);
            return new Index(Collections.unmodifiableList(newIndexEntries), indexEntriesToMap(newIndexEntries));
        } else if (indexEntriesForPath.get(0).getSha().equals(sha)) {
            return this;
        } else {
            final var oldIndexEntry = indexEntriesForPath.get(0);
            final var newIndexEntry = new IndexEntry(Stage.normal, path, sha);
            final var newIndexEntries = new ArrayList<>(indexEntries);
            final var indexOfOldIndexEntry = newIndexEntries.indexOf(oldIndexEntry);
            newIndexEntries.set(indexOfOldIndexEntry, newIndexEntry);
            return new Index(Collections.unmodifiableList(newIndexEntries), indexEntriesToMap(newIndexEntries));
        }
    }

    public byte[] serialize() {
        return indexEntries.stream()
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

    public Map<VCSPath, String> getPathToSha() {
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
        return pathToIndexEntries;
    }

    public Set<VCSPath> getConflictPaths() {
        return pathToIndexEntries.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    private static Map<VCSPath, List<IndexEntry>> indexEntriesToMap(final List<IndexEntry> entries) {
        final Map<VCSPath, Set<String>> pathToSha = new HashMap<>();
        final Map<VCSPath, Set<Stage>> pathToInteger = new HashMap<>();
        final Map<VCSPath, List<IndexEntry>> result = new HashMap<>();
        for (final IndexEntry entry : entries) {
            final var correspondingSha = pathToSha.computeIfAbsent(entry.getPath(), (unused) -> new HashSet<>());
            final var correspondingStages = pathToInteger.computeIfAbsent(entry.getPath(), (unused) -> new HashSet<>());
            if (!correspondingSha.add(entry.getSha())) {
                throw new DeserializationException(
                        String.format(
                                "Repetitive sha = %s for the same path = %s.",
                                entry.getSha(),
                                entry.getPath()
                        )
                );
            }
            if (!correspondingStages.add(entry.getStage())) {
                throw new DeserializationException(
                        String.format(
                                "Repetitive stage = %s for the same path = %s.",
                                entry.getStage(),
                                entry.getPath()
                        )
                );
            }
            result.computeIfAbsent(entry.getPath(), (unused) -> new ArrayList<>()).add(entry);
        }
        checkStages(result);
        return mapValuesToImmutableMap(result, List::copyOf);
    }

    private static void checkStages(final Map<VCSPath, List<IndexEntry>> pathToIndexEntries) {
        for (final var pathAndEntries : pathToIndexEntries.entrySet()) {
            final VCSPath path = pathAndEntries.getKey();
            final List<IndexEntry> indexEntries = pathAndEntries.getValue();
            Contracts.requireThat(indexEntries.size() >= 1 && indexEntries.size() <= 3);
            final var stages = indexEntries.stream()
                    .map(IndexEntry::getStage).
                            collect(Collectors.toList());
            if (stages.size() == 1) {
                if (stages.get(0) != Stage.normal) {
                    throw new DeserializationException("Bad index. There is a single entry for path " + path.toString()
                            + " with stage code " + stages.get(0).getCode()
                    );
                }
            } else if (stages.size() == 2 && (stages.contains(Stage.normal) || stages.contains(Stage.base))) {
                throw new DeserializationException("Bad index. There are inconsistent stages = " + stages);
            } else if (stages.contains(Stage.normal)) {
                throw new DeserializationException("Bad index. There are inconsistent stages = " + stages);
            }
        }
    }

    @Override
    public String toString() {
        return utf8(serialize());
    }
}
