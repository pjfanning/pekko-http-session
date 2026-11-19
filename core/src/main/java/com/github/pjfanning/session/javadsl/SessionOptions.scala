package com.github.pjfanning.session.javadsl

import com.github.pjfanning.session.{ OneOff, Refreshable, SessionManager }

import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext

/**
 * Java alternative for com.github.pjfanning.session.SessionOptions
 */
object SessionOptions {

  def oneOff[T](manager: SessionManager[T]): OneOff[T] = new OneOff[T]()(manager)

  /**
   * @param executor used to run the refresh token handling, e.g. a pekko dispatcher
   */
  def refreshable[T](manager: SessionManager[T], storage: RefreshTokenStorage[T], executor: Executor): Refreshable[T] = {
    val ec = executor match {
      case ec: ExecutionContext => ec
      case other                => ExecutionContext.fromExecutor(other)
    }
    new Refreshable[T]()(manager, new RefreshTokenStorageAdapter[T](storage), ec)
  }
}
