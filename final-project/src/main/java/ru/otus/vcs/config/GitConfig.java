package ru.otus.vcs.config;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.InnerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class GitConfig {

    public static final ConfigKey<Boolean> BARE_KEY =
            ConfigKey.booleanConfigKey("core.bare", "false");
    public static final ConfigKey<Boolean> FILEMODE_KEY =
            ConfigKey.booleanConfigKey("core.filemode", "false");
    public static final ConfigKey<Integer> REPO_VER_KEY =
            ConfigKey.integerConfigKey("core.repositoryformatversion", "0");
    public static final ConfigKey<String> USER = ConfigKey.stringKey("user", "Nickson");

    private static final Map<String, ConfigKey<?>> allowedKeysByName = Map.of(
            BARE_KEY.name, BARE_KEY,
            FILEMODE_KEY.name, FILEMODE_KEY,
            REPO_VER_KEY.name, REPO_VER_KEY,
            USER.name, USER);

    private final Map<ConfigKey<?>, String> keyValues;

    public GitConfig() {
        keyValues = new HashMap<>();
        fillDefaults(keyValues);
    }

    private GitConfig(final Map<ConfigKey<?>, String> keyValues) {
        this.keyValues = keyValues;
    }

    public <V> V get(final ConfigKey<V> key) {
        final String stringValue = keyValues.get(key);
        if (stringValue == null) {
            return key.parser.apply(key.defaultValue);
        } else {
            return key.parser.apply(stringValue);
        }
    }

    public <V> void put(final ConfigKey<V> key, final V value) {
        Contracts.requireNonNullArgument(key);
        Contracts.requireNonNullArgument(value);

        keyValues.put(key, key.stringConverter.apply(value));
    }

    public static GitConfig create(final Path configPath) {
        try {
            final String content = Files.readString(configPath);
            return createFromString(content);
        } catch (final IOException ex) {
            throw new InnerException(
                    "IO error while reading config file " + configPath + ". " + ex.getMessage(),
                    ex
            );
        }
    }

    static GitConfig createFromString(final String content) {
        Contracts.requireNonNullArgument(content);
        final Map<ConfigKey<?>, String> keyValues = new HashMap<>();
        final String[] lines = content.split(System.lineSeparator());
        for (final var line : lines) {
            if (line.isBlank()) {
                continue;
            }
            final String[] splitLine = line.split("=");
            if (lineIsInvalid(splitLine)) {
                throw new InnerException("Bad line '" + line + "'.");
            }
            final var key = splitLine[0].trim();
            final var value = splitLine[1].trim();
            final ConfigKey<?> configKey = allowedKeysByName.get(key);
            if (configKey == null) {
                throw new InnerException("Bad config key name '" + key + "'.");
            }
            if (!configKey.validator.test(value)) {
                throw new InnerException("Bad value '" + value + "' for key '" + key + "'. "
                        + configKey.hint);
            }
            keyValues.put(configKey, value);
        }
        fillDefaults(keyValues);
        return new GitConfig(keyValues);
    }

    private static boolean isBoolean(final String string) {
        return "true".equals(string) || "false".equals(string);
    }

    private static boolean isInteger(final String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (final NumberFormatException ignore) {
            return false;
        }
    }

    private static void fillDefaults(final Map<ConfigKey<?>, String> keyValues) {
        for (final var configKey : allowedKeysByName.values()) {
            if (!keyValues.containsKey(configKey)) {
                keyValues.put(configKey, configKey.defaultValue);
            }
        }
    }

    private static boolean lineIsInvalid(final String[] splitLine) {
        return splitLine.length != 2 || splitLine[0].isBlank() || splitLine[1].isBlank();
    }

    @Override
    public String toString() {
        final var stringBuilder = new StringBuilder();
        for (final var keyValue : keyValues.entrySet()) {
            stringBuilder.append(keyValue.getKey().name);
            stringBuilder.append(" = ");
            stringBuilder.append(keyValue.getValue());
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    public static class ConfigKey<V> {

        private final String name;
        private final Predicate<String> validator;
        private final String hint;
        private final Function<String, V> parser;
        private final String defaultValue;
        private final Function<V, String> stringConverter;

        private ConfigKey(
                final String name,
                final Predicate<String> validator,
                final String hint,
                final Function<String, V> parser,
                final Function<V, String> stringConverter,
                final String defaultValue) {
            Contracts.requireNonNullArgument(name);
            Contracts.requireNonNullArgument(validator);
            Contracts.requireNonNullArgument(hint);
            Contracts.requireNonNullArgument(parser);
            Contracts.requireNonNullArgument(stringConverter);
            Contracts.requireNonNullArgument(defaultValue);
            Contracts.requireThat(!name.isBlank(), "config key must be non blank");
            Contracts.requireThat(!hint.isBlank(), "hint for config key must be non blank");

            this.name = name;
            this.validator = validator;
            this.hint = hint;
            this.parser = parser;
            this.stringConverter = stringConverter;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        private static ConfigKey<Boolean> booleanConfigKey(final String name, final String defaultValue) {
            Contracts.requireThat(isBoolean(defaultValue));
            return new ConfigKey<>(
                    name,
                    GitConfig::isBoolean,
                    "Value should be 'true' or 'false'",
                    Boolean::parseBoolean,
                    Object::toString,
                    defaultValue
            );
        }

        private static ConfigKey<String> stringKey(final String name, final String defaultValue) {
            return new ConfigKey<>(
                    name,
                    (str) -> true,
                    "no hint",
                    Function.identity(),
                    Object::toString,
                    defaultValue
            );
        }

        private static ConfigKey<Integer> integerConfigKey(final String name, final String defaultValue) {
            Contracts.requireThat(isInteger(defaultValue));
            return new ConfigKey<>(
                    name,
                    GitConfig::isInteger,
                    "Value should be integer",
                    Integer::parseInt,
                    Object::toString,
                    defaultValue
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigKey<?> configKey = (ConfigKey<?>) o;
            return name.equals(configKey.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
