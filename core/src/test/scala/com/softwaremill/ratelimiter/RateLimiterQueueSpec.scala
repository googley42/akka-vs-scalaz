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
      val actal: RateLimiterQueue[Nothing] = RateLimiterQueue(1, 1000)

      actal shouldBe RateLimiterQueue(1, 1000, Queue(), Queue(), scheduled = false)
    }
    "enqueue" in {
      val q1: RateLimiterQueue[IO[Nothing, Unit]] = RateLimiterQueue(1, 1000).enqueue(IO.sync(println("task1")))
      val q2: RateLimiterQueue[IO[Nothing, Unit]] = q1.enqueue(IO.sync(println("task2")))

      val i = 10
    }
    "run" in {
      val io1 = IO.sync(println("task1"))
      val io2 = IO.sync(println("task2"))
      val io3 = IO.sync(println("task3"))

      val three = List(io1, io2, io3).foldLeft[RateLimiterQueue[IO[Nothing, Unit]]](RateLimiterQueue(2, 1000))((acc, io) => acc.enqueue(io))

      val (tasks, queue) = three.run(System.currentTimeMillis())
      tasks shouldBe List(Run(io1), Run(io2), RunAfter(1000))
      println(tasks)
      println(queue)
    }

  }

}
