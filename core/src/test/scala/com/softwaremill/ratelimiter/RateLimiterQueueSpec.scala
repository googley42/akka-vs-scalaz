package com.softwaremill.ratelimiter

import com.softwaremill.ratelimiter.RateLimiterQueue.{Run, RunAfter}
import org.scalatest.{Matchers, WordSpec}
import scalaz.zio.IO

import scala.collection.immutable.Queue

class RateLimiterQueueSpec extends WordSpec with Matchers {

  "RateLimiterQueue" should {

    "create new using companion object" in {
      val actual: RateLimiterQueue[Nothing] = RateLimiterQueue(1, 1000)

      actual shouldBe RateLimiterQueue(1, 1000, Queue.empty, Queue.empty, scheduled = false)
    }

    "run" in {
      val io1 = IO.sync(println("task1"))
      val io2 = IO.sync(println("task2"))
      val io3 = IO.sync(println("task3"))
      val perMillis = 1000
      val maxRuns = 2
      val three = List(io1, io2, io3).foldLeft[RateLimiterQueue[IO[Nothing, Unit]]](RateLimiterQueue(maxRuns, perMillis))((acc, io) => acc.enqueue(io))
      val startTime = System.currentTimeMillis()

      val (tasks, queue) = three.run(startTime)

      tasks shouldBe List(Run(io1), Run(io2), RunAfter(perMillis))
      queue shouldBe RateLimiterQueue(maxRuns, perMillis, Queue(startTime, startTime), Queue(io3), true)
    }

  }

}
