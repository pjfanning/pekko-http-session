package com.github.pjfanning.session.javadsl

import com.github.pjfanning.session
import com.github.pjfanning.session.{ RefreshTokenData, RefreshTokenLookupResult }
import org.apache.pekko.Done

import java.util.Optional
import java.util.concurrent.CompletionStage
import java.util.function.Supplier
import scala.concurrent.ExecutionContext
import scala.jdk.DurationConverters._
import scala.jdk.FutureConverters._
import scala.jdk.OptionConverters._

/**
 * Java alternative for com.github.pjfanning.session.InMemoryRefreshTokenStorage. Useful for testing; see that class for
 * why it is not recommended for production.
 */
abstract class InMemoryRefreshTokenStorage[T]() extends RefreshTokenStorage[T] {

  private val delegate = new session.InMemoryRefreshTokenStorage[T] {
    override def log(msg: String): Unit = InMemoryRefreshTokenStorage.this.log(msg)
  }

  override def lookup(selector: String): CompletionStage[Optional[RefreshTokenLookupResult[T]]] =
    delegate.lookup(selector).map(_.toJava)(ExecutionContext.parasitic).asJava

  override def store(data: RefreshTokenData[T]): CompletionStage[Done] =
    delegate.store(data).map(_ => Done: Done)(ExecutionContext.parasitic).asJava

  override def remove(selector: String): CompletionStage[Done] =
    delegate.remove(selector).map(_ => Done: Done)(ExecutionContext.parasitic).asJava

  override def schedule[S](after: java.time.Duration, op: Supplier[CompletionStage[S]]): Unit =
    delegate.schedule(after.toScala)(op.get().asScala)

  def log(msg: String): Unit
}
