package com.github.pjfanning.session.example

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.apache.pekko.http.scaladsl.server.Directives._
import com.github.pjfanning.session.CsrfDirectives._
import com.github.pjfanning.session.CsrfOptions._
import com.github.pjfanning.session.SessionDirectives._
import com.github.pjfanning.session.{InMemoryRefreshTokenStorage, SessionConfig, SessionManager}
import com.github.pjfanning.session.SessionOptions._
import com.github.pjfanning.session.example.session.MyScalaSession
import com.typesafe.scalalogging.StrictLogging

import scala.io.StdIn

object Example extends App with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("example")

  import system.dispatcher

  val sessionConfig = SessionConfig.default(
    "c05ll3lesrinf39t7mc5h6un6r0c69lgfno69dsak3vabeqamouq4328cuaekros401ajdpkh60rrtpd8ro24rbuqmgtnd1ebag6ljnb65i8a55d482ok7o0nch0bfbe")
  implicit val sessionManager: SessionManager[MyScalaSession] = new SessionManager[MyScalaSession](sessionConfig)
  implicit val refreshTokenStorage: InMemoryRefreshTokenStorage[MyScalaSession] = new InMemoryRefreshTokenStorage[MyScalaSession] {
    def log(msg: String) = logger.info(msg)
  }

  def mySetSession(v: MyScalaSession) = setSession(refreshable, usingCookies, v)

  val myRequiredSession = requiredSession(refreshable, usingCookies)
  val myInvalidateSession = invalidateSession(refreshable, usingCookies)

  val routes =
    path("") {
      redirect("/site/index.html", Found)
    } ~
      hmacTokenCsrfProtection(checkHeader) {
        pathPrefix("api") {
          path("do_login") {
            post {
              entity(as[String]) { body =>
                logger.info(s"Logging in $body")

                mySetSession(MyScalaSession(body)) {
                  setNewCsrfToken(checkHeader) { ctx =>
                    ctx.complete("ok")
                  }
                }
              }
            }
          } ~
            // This should be protected and accessible only when logged in
            path("do_logout") {
              post {
                myRequiredSession { session =>
                  myInvalidateSession { ctx =>
                    logger.info(s"Logging out $session")
                    ctx.complete("ok")
                  }
                }
              }
            } ~
            // This should be protected and accessible only when logged in
            path("current_login") {
              get {
                myRequiredSession { session => ctx =>
                  logger.info("Current session: " + session)
                  ctx.complete(session.username)
                }
              }
            }
        } ~
          pathPrefix("site") {
            getFromResourceDirectory("")
          }
      }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

  println("Server started, press enter to stop. Visit http://localhost:8080 to see the demo.")
  StdIn.readLine()

  import system.dispatcher

  bindingFuture
    .flatMap(_.unbind())
    .onComplete { _ =>
      system.terminate()
      println("Server stopped")
    }
}
