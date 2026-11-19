package com.github.pjfanning.session.javadsl

import com.github.pjfanning.session
import com.github.pjfanning.session.{ RefreshTokenData, RefreshTokenLookupResult }

import java.util.function.Supplier
import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.FutureConverters._
import scala.jdk.OptionConverters._

/**
 * Adapts a Java [[RefreshTokenStorage]] to the Scala [[session.RefreshTokenStorage]] used internally.
 */
private[session] final class RefreshTokenStorageAdapter[T](underlying: RefreshTokenStorage[T])
    extends session.RefreshTokenStorage[T] {

  override def lookup(selector: String): Future[Option[RefreshTokenLookupResult[T]]] =
    underlying.lookup(selector).asScala.map(_.toScala)(ExecutionContext.parasitic)

  override def store(data: RefreshTokenData[T]): Future[Unit] =
    underlying.store(data).asScala.map(_ => ())(ExecutionContext.parasitic)

  override def remove(selector: String): Future[Unit] =
    underlying.remove(selector).asScala.map(_ => ())(ExecutionContext.parasitic)

  override def schedule[S](after: Duration)(op: => Future[S]): Unit =
    underlying.schedule[S](java.time.Duration.ofNanos(after.toNanos), () => op.asJava)
}

private[session] object RefreshTokenStorageAdapter {
  def lookupResult[T](tokenHash: String, expires: Long, createSession: Supplier[T]): RefreshTokenLookupResult[T] =
    RefreshTokenLookupResult[T](tokenHash, expires, () => createSession.get())
}
