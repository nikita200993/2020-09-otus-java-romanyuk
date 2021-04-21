package ru.otus.vcs.index;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.DeserializationException;
import ru.otus.vcs.naming.VCSPath;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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
        final var content = utf8(bytes);
        final var indexEntries = Arrays.stream(content.split("\n"))
                .map(IndexEntry::fromLineContent)
                .collect(toList());
        return new Index(indexEntries, entryListToMap(indexEntries));
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
                        toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().get(0).getSha()
                        )
                );
    }

    private static Map<VCSPath, List<IndexEntry>> entryListToMap(final List<IndexEntry> entries) {
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
        return result;
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
