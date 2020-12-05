package ru.otus.json;


import ru.otus.utils.ReflectionUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class JsonSerializer {

    public String serialize(final Object object) {
        final var builder = new StringBuilder();
        serialize(builder, object);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private void serialize(StringBuilder stringBuilder, final Object object) {
        if (object == null) {
            stringBuilder.append("null");
            return;
        }
        final Class<?> clazz = object.getClass();
        if (clazz.equals(String.class) || clazz.equals(Character.class)) {
            serializeCharacterOrString(stringBuilder, object);
        } else if (ReflectionUtils.isBoxedPrimitive(object)) {
            stringBuilder.append(object.toString());
        } else if (object.getClass().isArray()) {
            serializeArray(stringBuilder, object);
        } else if (Map.class.isAssignableFrom(clazz)) {
            serializeMap(stringBuilder, (Map<String, ?>) object);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            final Collection<?> collection = (Collection<?>) object;
            serialize(stringBuilder, collection.toArray());
        } else {
            throw new SerializationInvalidClassPassed("Class is unsupported " + object.getClass());
        }
    }

    private void serializeArray(final StringBuilder stringBuilder, final Object array) {
        stringBuilder.append('[');
        final int length = Array.getLength(array);
        for (int i = 0; i < length - 1; i++) {
            serialize(stringBuilder, Array.get(array, i));
            stringBuilder.append(',');
        }
        serialize(stringBuilder, Array.get(array, length - 1));
        stringBuilder.append(']');
    }

    // only strings are keys in json format
    private void serializeMap(final StringBuilder stringBuilder, final Map<String, ?> stringMap) {
        stringBuilder.append('{');
        for (final var entry : stringMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            // gson behaves like this
            if (value == null && key == null) {
                continue;
            }
            serializeCharacterOrString(stringBuilder, key);
            stringBuilder.append(':');
            serialize(stringBuilder, value);
            stringBuilder.append(',');
        }
        final int length = stringBuilder.length();
        if (stringBuilder.charAt(length - 1) == ',') {
            stringBuilder.deleteCharAt(length - 1);
        }
        stringBuilder.append('}');
    }

    private void serializeCharacterOrString(
            final StringBuilder stringBuilder,
            final Object object) {
        stringBuilder.append('"')
                .append(object)
                .append('"');
    }
}
