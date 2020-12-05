package ru.otus.json;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// all boxed tests also test primitive, because I pass primitive literal to methods.
public class JsonSerializerTest {

    private final JsonSerializer jsonSerializer = new JsonSerializer();
    private final Gson gson = new Gson();

    @Test
    void testNull() {
        final String jsonString = jsonSerializer.serialize(null);
        Assertions.assertNull(gson.fromJson(jsonString, String.class));
    }

    @Test
    void testString() {
        final String jsonString = jsonSerializer.serialize("abc");
        Assertions.assertEquals("abc", gson.fromJson(jsonString, String.class));
    }

    @Test
    void testBoxedInteger() {
        final String jsonString = jsonSerializer.serialize(10);
        Assertions.assertEquals(10, gson.fromJson(jsonString, Integer.class));
    }

    @Test
    void testBoxedShort() {
        final short aShort = 1;
        final String jsonString = jsonSerializer.serialize(aShort);
        Assertions.assertEquals(aShort, gson.fromJson(jsonString, Short.class));
    }

    @Test
    void testBoxedLong() {
        final String jsonString = jsonSerializer.serialize(10L);
        Assertions.assertEquals(10L, gson.fromJson(jsonString, Long.class));
    }

    @Test
    void testBoxedFloat() {
        final String jsonString = jsonSerializer.serialize(10.5f);
        Assertions.assertEquals(10.5f, gson.fromJson(jsonString, Float.class));
    }

    @Test
    void testBoxedDouble() {
        final String jsonString = jsonSerializer.serialize(10.5);
        Assertions.assertEquals(10.5, gson.fromJson(jsonString, Double.class));
    }

    @Test
    void testBoxedCharacter() {
        final String jsonString = jsonSerializer.serialize('a');
        Assertions.assertEquals('a', gson.fromJson(jsonString, Character.class));
    }

    @Test
    void testBoxedByte() {
        final byte aByte = 1;
        final String jsonString = jsonSerializer.serialize(aByte);
        Assertions.assertEquals(aByte, gson.fromJson(jsonString, Byte.class));
    }

    @Test
    void testBoxedBoolean() {
        final boolean aBool = true;
        final String jsonString = jsonSerializer.serialize(aBool);
        Assertions.assertEquals(aBool, gson.fromJson(jsonString, Boolean.class));
    }

    @Test
    void testArrayOfPrimitive() {
        final var intArray = new int[]{1, 2, 3};
        final String jsonString = jsonSerializer.serialize(intArray);
        Assertions.assertArrayEquals(intArray, gson.fromJson(jsonString, int[].class));
    }

    @Test
    void testArrayOfObjects() {
        final Object[] objectArray = new Object[]{1, 2.0, 3L, "dasd", new String[]{"a", "b"}};
        final String jsonStringActual = jsonSerializer.serialize(objectArray);
        final String jsonStringExpected = gson.toJson(objectArray);
        // here test string representations, because even gson incorrectly restores array element
        // at zero index (1.0 instead of 1)
        Assertions.assertEquals(jsonStringExpected, jsonStringActual);
    }

    @Test
    void testMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put(null, null);
        map.put(null, "a");
        map.put("abc", new int[]{1, 2});
        map.put("a", 2);
        map.put("nestedMap,", Map.of("1", "abc"));
        final String jsonStringActual = jsonSerializer.serialize(map);
        final String jsonStringExpected = gson.toJson(map);
        Assertions.assertEquals(jsonStringExpected, jsonStringActual);
    }

    @Test
    void testCollection() {
        final List<Object> list = List.of("abc", 1, 2L, Map.of("a", 2));
        final String jsonStringActual = jsonSerializer.serialize(list);
        final String jsonStringExpected = gson.toJson(list);
        Assertions.assertEquals(jsonStringExpected, jsonStringActual);
    }

    @Test
    void testThatThrowsIfUnsupportedClassPassed() {
        Assertions.assertThrows(
                SerializationInvalidClassPassed.class,
                () -> jsonSerializer.serialize(new Object()));
    }
}
