package com.dividezero.stubby.core.util

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class TimeLimitTest extends FunSuite with ShouldMatchers {

  def timer = System.currentTimeMillis

  def time(f: => Any): Long = {
    val start = timer
    f
    timer - start
  }

  def expectDuration(duration: Long)(f: => Any) =
    time(f) should be(duration plusOrMinus 1000) // keep this large due to Travis CI slowness

  test("should return immediately") {
    expectDuration(0) {
      TimeLimit.retry(5000) { r =>
        Some("result")
      }
    }
  }

  test("should wait full duration") {
    expectDuration(2000) {
      TimeLimit.retry(2000) { r =>
        Thread.sleep(100)
        None
      }
    }
  }

  test("should return early") {
    expectDuration(1000) {
      TimeLimit.retry(2000) { r =>
        if (r > 1000) {
          Thread.sleep(100)
          None
        } else {
          Some("result")
        }
      }
    }
  }

  test("should return Some() on success") {
    val result = TimeLimit.retry(300) { r =>
      Some("result")
    }
    assert(result === Some("result"))
  }

  test("should return None when times out") {
    val result = TimeLimit.retry(300) { r =>
      None
    }
    assert(result === None)
  }

}