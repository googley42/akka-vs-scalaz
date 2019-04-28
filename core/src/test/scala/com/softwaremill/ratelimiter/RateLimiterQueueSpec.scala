package com.softwaremill.ratelimiter

import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable.Queue
import cats.implicits._
import com.softwaremill.IOInstances._
import com.softwaremill.ratelimiter.RateLimiterQueue.{Run, RunAfter}
import scalaz.zio.IO

class RateLimiterQueueSpec extends WordSpec with Matchers {

  "RateLimiterQueue" should {

    "create a RateLimiterQueue using companion object" in {
      val actual: RateLimiterQueue[Nothing] = RateLimiterQueue(1, 1000)

      actual shouldBe RateLimiterQueue(1, 1000, Queue(), Queue(), scheduled = false)
    }

    "run" in {
      val io1 = IO.sync(println("task1"))
      val io2 = IO.sync(println("task2"))
      val io3 = IO.sync(println("task3"))
      val perMillis = 1000
      val three = List(io1, io2, io3).foldLeft[RateLimiterQueue[IO[Nothing, Unit]]](RateLimiterQueue(maxRuns = 2, perMillis))((acc, io) => acc.enqueue(io))
      val startTime = System.currentTimeMillis()

      val (tasks, queue) = three.run(startTime)

      tasks shouldBe List(Run(io1), Run(io2), RunAfter(perMillis))
      println(tasks)
      println(queue)
    }

  }

}
