package com.github.pjfanning.session

import java.util.concurrent.{ CountDownLatch, Executors, TimeUnit }

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

class InMemoryRefreshTokenStorageTest extends AnyFlatSpec with Matchers {

  // https://github.com/softwaremill/akka-http-session/issues/48
  "InMemoryRefreshTokenStorage" should "handle concurrent stores and removes" in {
    val storage = new InMemoryRefreshTokenStorage[String] {
      override def log(msg: String): Unit = ()
    }

    val threads = 16
    val tokensPerThread = 2000
    val pool = Executors.newFixedThreadPool(threads)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(pool)
    val start = new CountDownLatch(1)

    try {
      val work = Future.traverse((0 until threads).toList) { t =>
        Future {
          start.await()
          for (i <- 0 until tokensPerThread) {
            val selector = s"$t-$i"
            storage.store(RefreshTokenData(s"session-$selector", selector, s"hash-$selector", Long.MaxValue))
            // remove every other token, as token rotation does
            if (i % 2 == 1) storage.remove(s"$t-${i - 1}")
          }
        }
      }
      start.countDown()
      // an unsynchronized map can lose updates, throw, or loop forever; don't let the latter hang the build
      Await.result(work, 60.seconds)

      storage.store.size shouldBe threads * tokensPerThread / 2
      Await.result(storage.lookup("0-1"), 1.second).map(_.createSession()) shouldBe Some("session-0-1")
      Await.result(storage.lookup("0-0"), 1.second) shouldBe None
    } finally {
      pool.shutdownNow()
      pool.awaitTermination(10, TimeUnit.SECONDS)
    }
  }
}
