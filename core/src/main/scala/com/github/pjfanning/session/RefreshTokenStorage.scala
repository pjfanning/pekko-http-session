package com.github.pjfanning.session

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait RefreshTokenStorage[T] {
  def lookup(selector: String): Future[Option[RefreshTokenLookupResult[T]]]
  def store(data: RefreshTokenData[T]): Future[Unit]
  def remove(selector: String): Future[Unit]
  def schedule[S](after: Duration)(op: => Future[S]): Unit
}

case class RefreshTokenData[T](forSession: T,
                               selector: String,
                               tokenHash: String,
                               /**
                                 * Timestamp
                                 */
                               expires: Long)

case class RefreshTokenLookupResult[T](tokenHash: String,
                                       /**
                                         * Timestamp
                                         */
                                       expires: Long,
                                       createSession: () => T)

/**
  * Useful for testing. It is safe to use concurrently, but it is not recommended for production:
  *  - tokens are held in memory, so they are lost on restart and are not shared between nodes
  *  - an expired token is only removed if a client presents it again, so abandoned tokens accumulate
  *  - `schedule` runs the operation immediately instead of after the given delay, so a token that is being rotated
  *    is removed straight away rather than remaining valid briefly for concurrent requests using it
  */
trait InMemoryRefreshTokenStorage[T] extends RefreshTokenStorage[T] {
  case class Store(session: T, tokenHash: String, expires: Long)
  // must be thread-safe, as concurrent requests store and remove tokens:
  // https://github.com/softwaremill/akka-http-session/issues/48
  private val _store = TrieMap[String, Store]()

  def store: Map[String, Store] = _store.toMap

  override def lookup(selector: String) = {
    Future.successful {
      val r = _store.get(selector).map(s => RefreshTokenLookupResult[T](s.tokenHash, s.expires, () => s.session))
      log(s"Looking up token for selector: $selector, found: ${r.isDefined}")
      r
    }
  }

  override def store(data: RefreshTokenData[T]) = {
    log(
      s"Storing token for selector: ${data.selector}, user: ${data.forSession}, " +
        s"expires: ${data.expires}, now: ${System.currentTimeMillis()}")
    Future.successful(_store.put(data.selector, Store(data.forSession, data.tokenHash, data.expires)))
  }

  override def remove(selector: String) = {
    log(s"Removing token for selector: $selector")
    Future.successful(_store.remove(selector))
  }

  override def schedule[S](after: Duration)(op: => Future[S]) = {
    log("Running scheduled operation immediately")
    op
    Future.successful(())
  }

  def log(msg: String): Unit
}
