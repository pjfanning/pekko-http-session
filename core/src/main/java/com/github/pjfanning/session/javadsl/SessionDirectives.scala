package com.github.pjfanning.session.javadsl

import com.github.pjfanning.{session => sess}
import com.github.pjfanning.session.{GetSessionTransport, SessionContinuity, SessionResult, SetSessionTransport}

import java.util.Optional
import java.util.function.Supplier
import org.apache.pekko.http.javadsl.server.Route
import org.apache.pekko.http.javadsl.server.directives.RouteAdapter

import scala.jdk.OptionConverters._

/**
 * Java alternative for com.github.pjfanning.session.SessionDirectives
 */
trait SessionDirectives {

  def session[T](sc: SessionContinuity[T], st: GetSessionTransport, inner: java.util.function.Function[SessionResult[T], Route]): Route = RouteAdapter {
    sess.SessionDirectives.session(sc, st) { sessionResult =>
      inner.apply(sessionResult).asInstanceOf[RouteAdapter].delegate
    }
  }

  def setSession[T](sc: SessionContinuity[T], st: SetSessionTransport, v: T, inner: Supplier[Route]): Route = RouteAdapter {
    sess.SessionDirectives.setSession(sc, st, v) {
      inner.get.asInstanceOf[RouteAdapter].delegate
    }
  }

  def invalidateSession[T](sc: SessionContinuity[T], st: GetSessionTransport, inner: Supplier[Route]): Route = RouteAdapter {
    sess.SessionDirectives.invalidateSession(sc, st) {
      inner.get.asInstanceOf[RouteAdapter].delegate
    }
  }

  def optionalSession[T](sc: SessionContinuity[T], st: GetSessionTransport, inner: java.util.function.Function[Optional[T], Route]): Route = RouteAdapter {
    sess.SessionDirectives.optionalSession(sc, st) { session =>
      inner.apply(session.toJava).asInstanceOf[RouteAdapter].delegate
    }
  }

  def requiredSession[T](sc: SessionContinuity[T], st: GetSessionTransport, inner: java.util.function.Function[T, Route]): Route = RouteAdapter {
    sess.SessionDirectives.requiredSession(sc, st) { session =>
      inner.apply(session).asInstanceOf[RouteAdapter].delegate
    }
  }

  def touchRequiredSession[T](sc: SessionContinuity[T], st: GetSessionTransport, inner: java.util.function.Function[T, Route]): Route = RouteAdapter {
    sess.SessionDirectives.touchRequiredSession(sc, st) { session =>
      inner.apply(session).asInstanceOf[RouteAdapter].delegate
    }
  }

}

object SessionDirectives extends SessionDirectives

