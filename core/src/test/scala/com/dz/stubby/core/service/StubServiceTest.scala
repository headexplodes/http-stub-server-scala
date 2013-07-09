package com.dz.stubby.core.service

import org.scalatest.FunSuite
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.util.OptionUtils

class StubServiceTest extends FunSuite {

  import OptionUtils._

  val OK = 200
  val CREATED = 201
  val SERVER_ERROR = 500

  def defaultService = {
    val result = new StubService
    result.addResponse(
      StubExchange(
        StubRequest(path = "/foo", method = "G.T"),
        StubResponse(status = OK)))
    result.addResponse(
      StubExchange(
        StubRequest(path = "/foo", method = "GE."), // make sure patterns differ (or they will overwrite eachother)
        StubResponse(status = CREATED)))
    result
  }

  def defaultRequest = // default incoming request
    StubRequest(method = "GET", path = "/foo")

  test("successful match") {
    val result = defaultService.findMatch(defaultRequest)

    assert(result.matchFound)
    assert(result.response.get.status === CREATED) // most recent stubbed first
    assert(result.attempts.size === 1) // ensure attempts returned
  }

  test("match attempt recorded") {
    val service = defaultService
    service.findMatch(defaultRequest)

    val response = service.responses(0)
    assert(response.attempts.size === 1)
    assert(response.attempts(0).matches)
  }

  test("no match") {
    val service = defaultService
    val result = service.findMatch(StubRequest(path = "/not/found"))

    assert(service.responses.nonEmpty)
    assert(!result.matchFound)
  }

  test("delete responses") {
    val service = defaultService
    service.deleteResponses

    assert(!service.findMatch(defaultRequest).matchFound)
  }

  test("delete response") {
    val service = defaultService
    service.deleteResponse(0) // delete first

    val result = service.findMatch(defaultRequest)
    assert(result.matchFound)
    assert(result.response.get.status === OK)
  }

  test("get responses") {
    val service = defaultService

    assert(service.responses(0).exchange.response.status === CREATED) // most recent first
    assert(service.responses(1).exchange.response.status === OK)
  }

  test("requests are recorded") {
    val service = defaultService

    assert(service.findMatch(defaultRequest.copy(path = "/foo")).matchFound)
    assert(!service.findMatch(defaultRequest.copy(path = "/not/found")).matchFound) // ensure even failed matches recorded

    assert(service.requests(0).path.get === "/not/found") // most recent first
    assert(service.requests(1).path.get === "/foo")
  }

  test("delay") {
    val service = new StubService
    service.addResponse(
      StubExchange(
        StubRequest(path = "/foo"),
        StubResponse(status = OK),
        delay = Some(1234)))

    val result = service.findMatch(StubRequest(path = "/foo"))

    assert(result.matchFound)
    assert(result.delay.get === 1234)
  }

  test("script executed") {
    val service = new StubService
    service.addResponse(
      StubExchange(
        StubRequest(path = "/foo"),
        StubResponse(status = OK),
        script = Some("exchange.response.status = 500; exchange.delay = 666; exchange.response.body = exchange.request.path")))

    val result = service.findMatch(StubRequest(path = "/foo"))

    assert(result.matchFound)
    assert(result.delay.get === 666)
    assert(result.response.get.status === SERVER_ERROR)
    assert(result.response.get.body.get === "/foo")
  }

  test("duplicate pattern removed") {
    val service = new StubService

    service.addResponse(
      StubExchange(
        StubRequest(path = "/foo"),
        StubResponse(status = OK)))

    service.addResponse(
      StubExchange(
        StubRequest(path = "/foo"),
        StubResponse(status = CREATED)))

    assert(service.responses.size === 1)
    assert(service.responses(0).exchange.response.status === CREATED) // ensure last stubbed request is kept

  }

  test("request filter empty") {
    val service = defaultService
    service.findMatch(StubRequest(path = "/test"))

    val filter = new StubRequest() // empty filter
    assert(service.findRequests(filter).size === 1)
  }

  test("request filter") {
    val service = defaultService
    service.findMatch(StubRequest(path = "/test"))
    service.findMatch(StubRequest(path = "/test", params = List(StubParam("foo", "bar"))))

    assert(service.requests.size === 2)

    val filter = new StubRequest(params = List(StubParam("foo", "b.r")))
    assert(service.findRequests(filter).size === 1) // should only match one of the requests
  }

  test("request filter with wait, not found") {
    val service = defaultService
    service.findMatch(StubRequest(path = "/test"))

    val filter = new StubRequest(path = "/not/found")
    assert(service.findRequests(filter, 2000).size === 0)
  }

  test("requets filter with wait") {
    val service = defaultService
    service.findMatch(StubRequest(path = "/test1"))

    new Thread(new Runnable {
      def run() {
        try {
          Thread.sleep(1000) // attempt to make 'findMatch' execute after parent thread starts waiting
          service.findMatch(StubRequest(path = "/test2"))
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }).start()

    val filter = StubRequest(path = "/test2") // wait for second request
    val result = service.findRequests(filter, 5000)

    assert(result.size === 1)
    assert(result.head.path.get === "/test2")
  }

}