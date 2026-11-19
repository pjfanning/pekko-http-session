package com.github.pjfanning.session.example.serializers;

import com.github.pjfanning.session.example.SomeJavaComplexObject;
import com.github.pjfanning.session.SessionSerializer;
import com.github.pjfanning.session.javadsl.SessionSerializers;

public class MyJavaSingleSessionSerializer {

    /**
     * Stores the session as a single string value. An exception thrown while converting the value back
     * to a session is treated as a deserialization failure.
     */
    public static final SessionSerializer<SomeJavaComplexObject, String> SERIALIZER =
        SessionSerializers.singleValue(SomeJavaComplexObject::getValue, SomeJavaComplexObject::new);

    public static void main(String[] args) {
        String serialized = SERIALIZER.serialize(new SomeJavaComplexObject("abc"));
        System.out.println(serialized + " -> " + SERIALIZER.deserialize(serialized).get().getValue());
    }
}
