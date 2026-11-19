package com.github.pjfanning.session.example.session;

import com.github.pjfanning.session.SessionSerializer;
import com.github.pjfanning.session.javadsl.SessionSerializers;

public class MyJavaSession {

    /**
     * This session serializer converts a session into a single String value and back. For other ready-to-use
     * serializers, and for sessions stored as multiple values, see com.github.pjfanning.session.javadsl.SessionSerializers.
     */
    private static final SessionSerializer<MyJavaSession, String> serializer =
        SessionSerializers.singleValue(MyJavaSession::getUsername, MyJavaSession::new);

    private final String username;

    public MyJavaSession(String username) {
        this.username = username;
    }

    public static SessionSerializer<MyJavaSession, String> getSerializer() {
        return serializer;
    }

    public String getUsername() {
        return username;
    }
}
