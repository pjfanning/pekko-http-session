package com.github.pjfanning.session.javadsl;

import com.github.pjfanning.session.MultiValueSessionSerializer;
import com.github.pjfanning.session.SessionSerializer;
import com.github.pjfanning.session.SessionSerializer$;
import com.github.pjfanning.session.SingleValueSessionSerializer;
import com.github.pjfanning.session.converters.MapConverters;
import scala.Function0;
import scala.Function1;
import scala.jdk.javaapi.CollectionConverters;
import scala.util.Try;

import java.util.Map;
import java.util.function.Function;

/**
 * Wrapper for session serializers in com.github.pjfanning.session.SessionSerializer,
 * plus factories for creating session serializers from Java functions.
 */
public final class SessionSerializers {

    public static final SessionSerializer<String, String> StringToStringSessionSerializer = SessionSerializer$.MODULE$.stringToStringSessionSerializer();
    public static final SessionSerializer<Integer, String> IntToStringSessionSerializer = (SessionSerializer<Integer, String>) (SessionSerializer) SessionSerializer$.MODULE$.intToStringSessionSerializer();
    public static final SessionSerializer<Long, String> LongToStringSessionSerializer = (SessionSerializer<Long, String>) (SessionSerializer) SessionSerializer$.MODULE$.longToStringSessionSerializer();
    public static final SessionSerializer<Float, String> FloatToStringSessionSerializer = (SessionSerializer<Float, String>) (SessionSerializer) SessionSerializer$.MODULE$.floatToStringSessionSerializer();
    public static final SessionSerializer<Double, String> DoubleToStringSessionSerializer = (SessionSerializer<Double, String>) (SessionSerializer) SessionSerializer$.MODULE$.doubleToStringSessionSerializer();

    public static final SessionSerializer<Map<String, String>, String> MapToStringSessionSerializer =
        multiValue(Function.identity(), Function.identity());

    private SessionSerializers() {
    }

    /**
     * Creates a serializer for a session that is stored as a single string value.
     *
     * @param toValue   converts the session to a string
     * @param fromValue converts a string back to the session; an exception thrown here is treated as a deserialization failure
     */
    public static <T> SessionSerializer<T, String> singleValue(Function<T, String> toValue, Function<String, T> fromValue) {
        return singleValue(toValue, fromValue, StringToStringSessionSerializer);
    }

    /**
     * Creates a serializer for a session that is stored as a single value, which is serialized with {@code valueSerializer}.
     *
     * @param toValue   converts the session to a value
     * @param fromValue converts a value back to the session; an exception thrown here is treated as a deserialization failure
     */
    public static <T, V> SessionSerializer<T, String> singleValue(Function<T, V> toValue, Function<V, T> fromValue,
                                                                 SessionSerializer<V, String> valueSerializer) {
        return new SingleValueSessionSerializer<T, V>(
            (Function1<T, V>) toValue::apply,
            (Function1<V, Try<T>>) v -> Try.apply((Function0<T>) () -> fromValue.apply(v)),
            valueSerializer
        );
    }

    /**
     * Creates a serializer for a session that is stored as a map of string keys and values.
     *
     * @param toMap   converts the session to a map
     * @param fromMap converts a map back to the session; an exception thrown here is treated as a deserialization failure
     */
    public static <T> SessionSerializer<T, String> multiValue(Function<T, Map<String, String>> toMap,
                                                            Function<Map<String, String>, T> fromMap) {
        return new MultiValueSessionSerializer<T>(
            (Function1<T, scala.collection.immutable.Map<String, String>>) t -> MapConverters.toImmutableMap(toMap.apply(t)),
            (Function1<scala.collection.immutable.Map<String, String>, Try<T>>) m ->
                Try.apply((Function0<T>) () -> fromMap.apply(CollectionConverters.asJava(m)))
        );
    }
}
