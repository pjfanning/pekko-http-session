package com.github.pjfanning.session.javadsl;

import com.github.pjfanning.session.SessionSerializer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SessionSerializersTest {

    @Test
    public void singleValueRoundTrips() {
        SessionSerializer<Integer, String> serializer = SessionSerializers.singleValue(String::valueOf, Integer::valueOf);
        assertEquals(Integer.valueOf(42), serializer.deserialize(serializer.serialize(42)).get());
    }

    @Test
    public void singleValueWithValueSerializerRoundTrips() {
        SessionSerializer<String, String> serializer = SessionSerializers.singleValue(
            String::length, n -> "x".repeat(n), SessionSerializers.IntToStringSessionSerializer);
        assertEquals("3", serializer.serialize("abc"));
        assertEquals("xxx", serializer.deserialize("3").get());
    }

    @Test
    public void singleValueFromValueExceptionIsAFailure() {
        SessionSerializer<Integer, String> serializer = SessionSerializers.singleValue(String::valueOf, Integer::valueOf);
        assertTrue(serializer.deserialize("not a number").isFailure());
    }

    @Test
    public void multiValueRoundTrips() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "x&y~z");
        SessionSerializer<Map<String, String>, String> serializer = SessionSerializers.MapToStringSessionSerializer;
        assertEquals(map, serializer.deserialize(serializer.serialize(map)).get());
    }

    @Test
    public void multiValueFromMapExceptionIsAFailure() {
        SessionSerializer<String, String> serializer = SessionSerializers.multiValue(
            s -> Map.of("v", s),
            m -> { throw new IllegalArgumentException("bad"); });
        assertTrue(serializer.deserialize(serializer.serialize("abc")).isFailure());
    }
}
