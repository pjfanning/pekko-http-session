package com.github.pjfanning.session.javadsl;

import com.github.pjfanning.session.RefreshTokenData;
import com.github.pjfanning.session.RefreshTokenLookupResult;
import org.apache.pekko.Done;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Java alternative for com.github.pjfanning.session.RefreshTokenStorage.
 * Use {@link SessionOptions#refreshable} to create a Refreshable session continuity from an implementation.
 */
public interface RefreshTokenStorage<T> {

    CompletionStage<Optional<RefreshTokenLookupResult<T>>> lookup(String selector);

    CompletionStage<Done> store(RefreshTokenData<T> data);

    CompletionStage<Done> remove(String selector);

    <S> void schedule(Duration after, Supplier<CompletionStage<S>> op);

    /**
     * Creates a lookup result, for use in {@link #lookup(String)} implementations.
     */
    static <T> RefreshTokenLookupResult<T> lookupResult(String tokenHash, long expires, Supplier<T> createSession) {
        return RefreshTokenStorageAdapter$.MODULE$.lookupResult(tokenHash, expires, createSession);
    }
}
