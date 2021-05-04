package ru.otus.vcs.newversion.index;

import ru.otus.utils.Contracts;
import ru.otus.vcs.newversion.index.diff.Addition;
import ru.otus.vcs.newversion.index.diff.Deletion;
import ru.otus.vcs.newversion.index.diff.Modification;
import ru.otus.vcs.newversion.index.diff.VCSFileChange;
import ru.otus.vcs.newversion.path.VCSFileDesc;
import ru.otus.vcs.newversion.path.VCSPath;
import ru.otus.vcs.newversion.ref.Sha1;
import ru.otus.vcs.newversion.utils.Utils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static ru.otus.vcs.newversion.utils.Utils.utf8;

/**
 * Simplified version of git index. Just contains list of file paths relative to workdir with stages and hash.
 */
public class Index {

    private final LinkedHashMap<VCSPath, List<IndexEntry>> pathToIndexEntries;

    private Index(final LinkedHashMap<VCSPath, List<IndexEntry>> pathToIndexEntries) {
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

    public static Index create(final Path path, final Function<byte[], Sha1> hasher) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(hasher);

        final var realPath = Utils.toReal(path);
        try (var fileWalk = Files.walk(realPath)) {
            final var indexEntries = fileWalk.skip(1)
                    .filter(innerPath -> Files.isRegularFile(innerPath, LinkOption.NOFOLLOW_LINKS))
                    .filter(innerPath -> VCSPath.isValidVCSPath(realPath.relativize(innerPath)))
                    .map(
                            innerPath -> IndexEntry.newNormalEntry(
                                    VCSPath.create(realPath.relativize(innerPath)),
                                    hasher.apply(Utils.readBytes(innerPath))
                            )
                    ).collect(Collectors.toList());
            return Index.create(indexEntries);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Sha1 getSha(final VCSPath path) {
        Contracts.requireNonNullArgument(path);

        final var indexEntries = pathToIndexEntries.get(path);
        Contracts.requireNonNull(indexEntries);
        Contracts.requireThat(indexEntries.size() == 1);
        return indexEntries.get(0).getSha();
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

    public Index withNewConflict(final VCSPath path, final Sha1 receiverSha, final Sha1 giverSha) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(receiverSha);
        Contracts.requireNonNullArgument(giverSha);
        Contracts.forbidThat(path.isRoot());
        Contracts.forbidThat(receiverSha.equals(giverSha));

        final var newIndexEntries = List.of(
                new IndexEntry(Stage.receiver, path, receiverSha),
                new IndexEntry(Stage.giver, path, giverSha)
        );
        final var newMapping = new LinkedHashMap<>(pathToIndexEntries);
        newMapping.put(path, newIndexEntries);
        return new Index(newMapping);
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

    public Index withDroppedConflicts() {
        return create(
                pathToIndexEntries.values()
                        .stream()
                        .filter(list -> list.size() == 1)
                        .map(list -> list.get(0))
                        .collect(Collectors.toList())
        );
    }

    @Nullable
    public Sha1 hashOfPath(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var entries = pathToIndexEntries.get(path);
        if (entries == null) {
            return null;
        }
        Contracts.forbidThat(entries.size() > 1);
        return entries.get(0).getSha();
    }

    public boolean contains(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        return pathToIndexEntries.containsKey(path);
    }

    public boolean inConflict(final VCSPath path) {
        Contracts.requireNonNullArgument(path);
        Contracts.forbidThat(path.isRoot());

        final var entries = pathToIndexEntries.get(path);
        return entries != null && entries.size() > 1;
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

    @Deprecated
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

    public List<VCSFileChange> getDiff(final Index other) {
        Contracts.requireNonNullArgument(other);
        Contracts.forbidThat(hasMergeConflict());
        Contracts.forbidThat(other.hasMergeConflict());

        final var result = new ArrayList<VCSFileChange>();
        for (final var fileDesc : getFileDescriptors()) {
            final var otherIndexEntries = other.pathToIndexEntries.get(fileDesc.getPath());
            if (otherIndexEntries != null) {
                final var otherSha = otherIndexEntries.get(0).getSha();
                if (!otherSha.equals(fileDesc.getSha())) {
                    result.add(new Modification(fileDesc, otherSha));
                }
            } else {
                result.add(new Addition(fileDesc));
            }
        }

        for (final var otherFileDesc : other.getFileDescriptors()) {
            if (!pathToIndexEntries.containsKey(otherFileDesc.getPath())) {
                result.add(new Deletion(otherFileDesc));
            }
        }
        return result;
    }

    public Set<VCSFileDesc> getFileDescriptors() {
        Contracts.forbidThat(hasMergeConflict());

        return pathToIndexEntries.values()
                .stream()
                .flatMap(List::stream)
                .map(indexEntry -> new VCSFileDesc(indexEntry.getPath(), indexEntry.getSha()))
                .collect(toSet());
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

    public List<Modification> getMergeConflicts() {
        final List<Modification> modifications = new ArrayList<>();
        for (final var indexEntries : pathToIndexEntries.values()) {
            if (indexEntries.size() > 1) {
                modifications.add(modificationFromConflictingEntries(indexEntries));
            }
        }
        return modifications;
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

    private static Modification modificationFromConflictingEntries(final List<IndexEntry> indexEntries) {
        Contracts.requireThat(indexEntries.size() > 1);
        final var vcsPath = indexEntries.get(0).getPath();
        Sha1 receiverSha = null;
        Sha1 giverSha = null;
        for (final IndexEntry indexEntry : indexEntries) {
            if (indexEntry.getStage() == Stage.receiver) {
                Contracts.requireThat(receiverSha == null);
                receiverSha = indexEntry.getSha();
            } else if (indexEntry.getStage() == Stage.giver) {
                Contracts.requireThat(giverSha == null);
                giverSha = indexEntry.getSha();
            }
        }
        Contracts.requireNonNull(receiverSha);
        Contracts.requireNonNull(giverSha);
        return new Modification(new VCSFileDesc(vcsPath, giverSha), receiverSha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return pathToIndexEntries.equals(index.pathToIndexEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathToIndexEntries);
    }

    @Override
    public String toString() {
        return utf8(serialize());
    }
}
