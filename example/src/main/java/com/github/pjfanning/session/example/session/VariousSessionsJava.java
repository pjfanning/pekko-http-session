package com.github.pjfanning.session.example.session;

import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.dispatch.MessageDispatcher;
import org.apache.pekko.http.javadsl.Http;
import org.apache.pekko.http.javadsl.ServerBinding;
import org.apache.pekko.http.javadsl.server.Route;
import com.github.pjfanning.session.BasicSessionEncoder;
import com.github.pjfanning.session.CheckHeader;
import com.github.pjfanning.session.OneOff;
import com.github.pjfanning.session.SessionConfig;
import com.github.pjfanning.session.SessionEncoder;
import com.github.pjfanning.session.SessionManager;
import com.github.pjfanning.session.SessionResult;
import com.github.pjfanning.session.SessionResult.Corrupt;
import com.github.pjfanning.session.SessionResult.CreatedFromToken;
import com.github.pjfanning.session.SessionResult.Decoded;
import com.github.pjfanning.session.SessionResult.Expired$;
import com.github.pjfanning.session.SessionResult.NoSession$;
import com.github.pjfanning.session.SessionResult.TokenNotFound$;
import com.github.pjfanning.session.SetSessionTransport;
import com.github.pjfanning.session.javadsl.SessionOptions;
import com.github.pjfanning.session.javadsl.HttpSessionAwareDirectives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static com.github.pjfanning.session.javadsl.SessionTransports.CookieST;

public class VariousSessionsJava extends HttpSessionAwareDirectives<MyJavaSession> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariousSessionsJava.class);

    private static final String SECRET = "c05ll3lesrinf39t7mc5h6un6r0c69lgfno69dsak3vabeqamouq4328cuaekros401ajdpkh60rrtpd8ro24rbuqmgtnd1ebag6ljnb65i8a55d482ok7o0nch0bfbe";
    private static final SessionEncoder<MyJavaSession> BASIC_ENCODER = new BasicSessionEncoder<>(MyJavaSession.getSerializer());

    private OneOff<MyJavaSession> oneOff;
    private SetSessionTransport sessionTransport;

    public VariousSessionsJava(MessageDispatcher dispatcher) {
        super(new SessionManager<>(
                SessionConfig.defaultConfig(SECRET),
                BASIC_ENCODER
            )
        );

        oneOff = SessionOptions.oneOff(getSessionManager());
        sessionTransport = CookieST;
    }

    public static void main(String[] args) throws IOException {

        final ActorSystem system = ActorSystem.create("example");
        final Http http = Http.get(system);

        final MessageDispatcher dispatcher = system.dispatchers().lookup("pekko.actor.default-dispatcher");
        final VariousSessionsJava app = new VariousSessionsJava(dispatcher);

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
                        path("secret", () ->
                            get(() -> requiredSession(oneOff, sessionTransport, myJavaSession -> complete("treasure")))
                        ),
                        path("open", () ->
                            get(() -> optionalSession(oneOff, sessionTransport, myJavaSession -> complete("small treasure")))
                        ),
                        path("detail", () -> session(oneOff, sessionTransport, sessionResult -> {
                            if (sessionResult instanceof Decoded) {
                                return complete("decoded");
                            } else if (sessionResult instanceof CreatedFromToken) {
                                return complete("created from token");
                            } else if (NoSession$.MODULE$.equals(sessionResult)) {
                                return complete("no session");
                            } else if (TokenNotFound$.MODULE$.equals(sessionResult)) {
                                return complete("token not found");
                            } else if (Expired$.MODULE$.equals(sessionResult)) {
                                return complete("expired");
                            } else if (((SessionResult<?>) sessionResult) instanceof Corrupt) {
                                return complete("corrupt");
                            }
                            LOGGER.error("Unknown session result: {}", sessionResult);
                            throw new RuntimeException("What's going on here?");
                        }))
                    )
                )
            );
    }
}

