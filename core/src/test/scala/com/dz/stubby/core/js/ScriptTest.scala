package com.dz.stubby.core.js

import org.scalatest.FunSuite
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubParam
import java.util.ArrayList
import java.util.HashMap
import java.util.Arrays
import com.dz.stubby.core.util.DeepCopyUtils

class ScriptTest extends FunSuite {

  import DeepCopyUtils._

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

  val jsonBodyScala = Map("items" -> List("one", "two"))

  val jsonRequest = request.copy(body = jsonBodyScala)
  val jsonResponse = response.copy(body = jsonBodyScala)

  implicit def createWorld: ScriptWorld = new ScriptWorld(request, response, Some(1234L))

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
    val world = new ScriptWorld(jsonRequest, jsonResponse, Some(1234L))

    assert(executeScript("exchange.request.body.get('items').get(0)")(world) === "one")
    assert(executeScript("exchange.request.body.get('items').get(1)")(world) === "two")
  }

  test("get response JSON body") {
    val world = new ScriptWorld(jsonRequest, jsonResponse, Some(1234L))

    assert(executeScript("exchange.response.body.get('items').get(0)")(world) === "one")
    assert(executeScript("exchange.response.body.get('items').get(1)")(world) === "two")
  }

  test("set request and response JSON body (ensure originals not modified)") {
    type AnyMap = Map[String, _]
    type AnySeq = Seq[_]

    val world = new ScriptWorld(jsonRequest, jsonResponse, Some(1234L))

    executeScript("exchange.request.body.get('items').add('three')")(world) // should have no effect
    executeScript("exchange.response.body.get('items').add('three')")(world)

    val (result, _) = world.result
    val resultBody = result.body.asInstanceOf[AnyMap]("items").asInstanceOf[AnySeq]

    assert(resultBody === List("one", "two", "three"))

    assert(jsonRequest.body === Map("items" -> List("one", "two"))) // ensure original not modified
    assert(jsonResponse.body === Map("items" -> List("one", "two")))
  }

}