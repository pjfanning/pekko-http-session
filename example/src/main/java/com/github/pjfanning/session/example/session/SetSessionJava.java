package com.github.pjfanning.session.example.session;

import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.dispatch.MessageDispatcher;
import org.apache.pekko.http.javadsl.Http;
import org.apache.pekko.http.javadsl.ServerBinding;
import org.apache.pekko.http.javadsl.model.HttpResponse;
import org.apache.pekko.http.javadsl.server.Route;
import org.apache.pekko.http.javadsl.unmarshalling.Unmarshaller;
import com.github.pjfanning.session.BasicSessionEncoder;
import com.github.pjfanning.session.CheckHeader;
import com.github.pjfanning.session.javadsl.SessionOptions;
import com.github.pjfanning.session.javadsl.RefreshTokenStorage;
import com.github.pjfanning.session.Refreshable;
import com.github.pjfanning.session.SessionConfig;
import com.github.pjfanning.session.SessionEncoder;
import com.github.pjfanning.session.SessionManager;
import com.github.pjfanning.session.SetSessionTransport;
import com.github.pjfanning.session.javadsl.HttpSessionAwareDirectives;
import com.github.pjfanning.session.javadsl.InMemoryRefreshTokenStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static com.github.pjfanning.session.javadsl.SessionTransports.CookieST;


public class SetSessionJava extends HttpSessionAwareDirectives<MyJavaSession> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetSessionJava.class);

    private static final String SECRET = "c05ll3lesrinf39t7mc5h6un6r0c69lgfno69dsak3vabeqamouq4328cuaekros401ajdpkh60rrtpd8ro24rbuqmgtnd1ebag6ljnb65i8a55d482ok7o0nch0bfbe";
    private static final SessionEncoder<MyJavaSession> BASIC_ENCODER = new BasicSessionEncoder<>(MyJavaSession.getSerializer());

    // in-memory refresh token storage
    private static final RefreshTokenStorage<MyJavaSession> REFRESH_TOKEN_STORAGE = new InMemoryRefreshTokenStorage<MyJavaSession>() {
        @Override
        public void log(String msg) {
            LOGGER.info(msg);
        }
    };

    private Refreshable<MyJavaSession> refreshable;
    private SetSessionTransport sessionTransport;

    public SetSessionJava(MessageDispatcher dispatcher) {
        super(new SessionManager<>(
                SessionConfig.defaultConfig(SECRET),
                BASIC_ENCODER
            )
        );

        refreshable = SessionOptions.refreshable(getSessionManager(), REFRESH_TOKEN_STORAGE, dispatcher);
        sessionTransport = CookieST;
    }

    public static void main(String[] args) throws IOException {

        final ActorSystem system = ActorSystem.create("example");
        final Http http = Http.get(system);

        final MessageDispatcher dispatcher = system.dispatchers().lookup("pekko.actor.default-dispatcher");
        final SetSessionJava app = new SetSessionJava(dispatcher);

        final CompletionStage<ServerBinding> binding = http.newServerAt("localhost", 8080).bind(app.createRoutes());

        System.out.println("Server started, press enter to stop");
        System.in.read();

        binding
            .thenCompose(ServerBinding::unbind)
            .thenAccept(unbound -> system.terminate());
    }

    private Route createRoutes() {
        CheckHeader<MyJavaSession> checkHeader = new CheckHeader<>(getSessionManager());
        return
            concat(
                randomTokenCsrfProtection(checkHeader, () ->
                    concat(
                        path("login", () ->
                            post(() ->
                                entity(Unmarshaller.entityToString(), body -> {
                                        LOGGER.info("Logging in {}", body);
                                        return setSession(refreshable, sessionTransport, new MyJavaSession(body), () ->
                                            setNewCsrfToken(checkHeader, () ->
                                                extractRequestContext(ctx ->
                                                    onSuccess(() -> ctx.completeWith(HttpResponse.create()), routeResult ->
                                                        complete("ok")
                                                    )
                                                )
                                            )
                                        );
                                    }
                                )
                            )
                        )
                    )
                )
            );
    }
}

