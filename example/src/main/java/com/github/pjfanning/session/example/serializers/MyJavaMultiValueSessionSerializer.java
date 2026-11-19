package com.github.pjfanning.session.example.serializers;

import com.github.pjfanning.session.example.SomeJavaComplexObject;
import com.github.pjfanning.session.SessionSerializer;
import com.github.pjfanning.session.javadsl.SessionSerializers;

import java.util.Collections;

public class MyJavaMultiValueSessionSerializer {

    /**
     * Stores the session as a map of string keys and values. An exception thrown while converting the map back
     * to a session is treated as a deserialization failure.
     */
    public static final SessionSerializer<SomeJavaComplexObject, String> SERIALIZER =
        SessionSerializers.multiValue(
            sjco -> Collections.singletonMap("value", sjco.getValue()),
            map -> new SomeJavaComplexObject(map.get("value"))
        );

    public static void main(String[] args) {
        String serialized = SERIALIZER.serialize(new SomeJavaComplexObject("abc"));
        System.out.println(serialized + " -> " + SERIALIZER.deserialize(serialized).get().getValue());
    }
}
