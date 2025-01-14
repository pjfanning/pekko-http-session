package com.github.pjfanning.session.javadsl;

import com.github.pjfanning.session.JValueSessionSerializer$;
import com.github.pjfanning.session.SessionSerializer;
import org.json4s.DefaultFormats$;
import org.json4s.JValue;

/**
 * Wrapper for session transports in com.github.pjfanning.session.JValueSessionSerializer
 */
public final class JwtSessionSerializers {

    public static final DefaultFormats$ DefaultUtcDateFormat = DefaultFormats$.MODULE$;

    public static final SessionSerializer<String, JValue> StringToJValueSessionSerializer = JValueSessionSerializer$.MODULE$.stringToJValueSessionSerializer();
    public static final SessionSerializer<Integer, JValue> IntToJValueSessionSerializer = (SessionSerializer<Integer, JValue>) (SessionSerializer) JValueSessionSerializer$.MODULE$.intToJValueSessionSerializer();
    public static final SessionSerializer<Long, JValue> LongToJValueSessionSerializer = (SessionSerializer<Long, JValue>) (SessionSerializer) JValueSessionSerializer$.MODULE$.longToJValueSessionSerializer();
    public static final SessionSerializer<Float, JValue> FloatToJValueSessionSerializer = (SessionSerializer<Float, JValue>) (SessionSerializer) JValueSessionSerializer$.MODULE$.floatToJValueSessionSerializer();
    public static final SessionSerializer<Double, JValue> DoubleToJValueSessionSerializer = (SessionSerializer<Double, JValue>) (SessionSerializer) JValueSessionSerializer$.MODULE$.doubleToJValueSessionSerializer();

    private JwtSessionSerializers() {
    }

}
