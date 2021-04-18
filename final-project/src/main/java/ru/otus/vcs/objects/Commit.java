package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.InnerException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.otus.vcs.utils.Utils.utf8;

public class Commit extends GitObject {

    public static final String type = "commit";

    private final LinkedHashMap<String, List<String>> keyValues;
    private final String message;

    public static Commit deserialize(final byte[] data) {
        final var content = utf8(data);
        System.out.println(content);
        final var keyValues = new LinkedHashMap<String, List<String>>();
        String message = null;
        int position = 0;
        int valueStart = -1;
        String currentKey = null;
        while (position < content.length()) {
            final int wsPos = content.indexOf(' ', position);
            final int eolPos = content.indexOf('\n', position);
            if (wsPos == -1 || eolPos < wsPos) {
                if (valueStart == -1) {
                    throw new DeserializationException("Bad commit format. Value was not matched before message.");
                }
                message = content.substring(position + 1);
            } else if (wsPos == position && valueStart == -1) {
                throw new DeserializationException("Bad commit format. Error at position " + position);
            } else if (wsPos == position) {
                position = eolPos + 1;
                continue;
            }
            if (valueStart != -1) {
                Contracts.ensureNonNull(currentKey);
                keyValues.computeIfAbsent(currentKey, unused -> new ArrayList<>())
                        .add(
                                content.substring(
                                        valueStart,
                                        position - 1
                                ).replace("\n ", "\n")
                        );
            }
            if (message != null) {
                return new Commit(keyValues, message);
            }
            currentKey = content.substring(position, wsPos);
            valueStart = wsPos + 1;
            position = eolPos + 1;
        }
        throw new DeserializationException("Bad commit format. No blank line met before message.");
    }

    public Commit(final LinkedHashMap<String, List<String>> keyValues, final String message) {
        Contracts.requireNonNullArgument(keyValues);
        Contracts.requireNonNullArgument(message);

        this.keyValues = copyMap(keyValues);
        this.message = message;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public byte[] serializeContent() {
        final var strBuilder = new StringBuilder();
        for (final var entry : keyValues.entrySet()) {
            final var key = entry.getKey();
            final var values = entry.getValue();
            for (final var value : values) {
                strBuilder.append(key)
                        .append(' ')
                        .append(value.replace("\n", " \n"))
                        .append('\n');
            }
        }
        strBuilder.append("\n")
                .append(message);
        return strBuilder.toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    public String getMessage() {
        return message;
    }

    public String getTreeSha() {
        final var tree = keyValues.get(Tree.type);
        if (tree == null || tree.size() != 1) {
            throw new InnerException("There should be single tree in commit.");
        }
        return tree.get(0);
    }

    private static LinkedHashMap<String, List<String>> copyMap(final Map<String, List<String>> orig) {
        final var copy = new LinkedHashMap<>(orig);
        copy.replaceAll((k, v) -> List.copyOf(v));
        return copy;
    }
}
