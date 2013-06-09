package com.dz.stubby.core.js

import org.scalatest.FunSuite
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubParam
import java.util.ArrayList

class ScriptTest extends FunSuite {

  val request = new StubRequest(
    method = "POST",
    path = "/request/path",
    params = List(StubParam("foo", "bar")),
    headers = List(StubParam("Content-Type", "text/plain")),
    body = "request body")

  val response = new StubResponse(
    status = 200,
    headers = List(StubParam("Content-Type", "application/json")),
    body = "response body")

  class TestBean {
    private val items = new ArrayList[String]
    def getItems: ArrayList[String] = items
  }

  implicit def createWorld: ScriptWorld = new ScriptWorld(request, response, Some(1234L))

  def createWorldWithJsonBodies: ScriptWorld = {
    val bodyBean = new TestBean
    bodyBean.getItems.add("one")
    bodyBean.getItems.add("two")

    val jsonRequest = request.copy(body = bodyBean)
    val jsonResponse = response.copy(body = bodyBean)

    new ScriptWorld(jsonRequest, jsonResponse, Some(1234L))
  }

  def executeScript(script: String)(implicit world: ScriptWorld): Any =
    new Script(script).execute(world)

  test("empty script") {
    executeScript("")
  }

  test("simple expresssion") {
    expectResult(3) {
      executeScript("var a = 1; var b = 2; a + b;")
    }
  }

  test("get delay") {
    expectResult(1234) {
      executeScript("exchange.delay")
    }
  }

  test("set delay") {
    val world = createWorld
    executeScript("exchange.delay = 666")(world)
    expectResult(666) {
      world.getDelay
    }
  }

  test("get request fields") {
    assert(executeScript("exchange.request.method") === "POST")
    assert(executeScript("exchange.request.path") === "/request/path")
    assert(executeScript("exchange.request.getParams('foo').get(0)") === "bar")
    assert(executeScript("exchange.request.getParam('foo')") === "bar")
    assert(executeScript("exchange.request.getHeader('content-type')") === "text/plain") // ensure case insensitive
    assert(executeScript("exchange.request.body") === "request body")
  }

  test("get response fields") {
    assert(executeScript("exchange.response.status") === 200)
    assert(executeScript("exchange.response.getHeader('content-type')") === "application/json") // ensure case insensitive
    assert(executeScript("exchange.response.body") === "response body")
  }

  test("set response fields") {
    val world = createWorld

    executeScript("exchange.response.status = 501")(world)
    assert(world.result._1.status === 501)

    executeScript("exchange.response.setHeader('Content-Type', 'text/xml')")(world)
    assert(world.result._1.getHeader("content-type") === Some("text/xml")) // ensure case insensitive

    executeScript("exchange.response.removeHeader('Content-Type')")(world)
    assert(world.result._1.getHeader("content-type") === None)

    executeScript("exchange.response.body = 'foo'")(world)
    assert(world.result._1.body === "foo")
  }

  test("get request JSON body") {
    val world = createWorldWithJsonBodies

    assert(executeScript("exchange.request.body.items.get(0)")(world) === "one")
    assert(executeScript("exchange.request.body.items.get(1)")(world) === "two")
  }

  ignore("set request JSON body (ensure not possible)") {
    // TODO: How to support JavaScript mutating parsed JSON response
  }

  test("get response JSON body") {
    val world = createWorldWithJsonBodies

    assert(executeScript("exchange.response.body.items.get(0)")(world) === "one")
    assert(executeScript("exchange.response.body.items.get(1)")(world) === "two")
  }

  ignore("set respones JSON body (ensure original not modified)") {
    // TODO: How to support JavaScript mutating parsed JSON response
  }

}