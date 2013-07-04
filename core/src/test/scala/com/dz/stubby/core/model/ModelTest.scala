package com.dz.stubby.core.model

import org.scalatest.FunSuite
import com.dz.stubby.core.util.JsonUtils
import com.dz.stubby.core.util.OptionUtils

class ModelTest extends FunSuite {

  import OptionUtils._

  val mapper = JsonUtils.createDefaultMapper

  val testParam = StubParam("foo", "bar")
  val testParamMap = Map("name" -> "foo", "value" -> "bar")

  val testResponse = StubResponse(200, List(testParam), "<html>")
  val testResponseMap = Map(
    "status" -> 200,
    "headers" -> List(testParamMap),
    "body" -> "<html>")

  val testRequetsBody = Map("foo" -> "bar")
  val testRequest = StubRequest("GET", "/foo", List(testParam), List(testParam), testRequetsBody)
  val testRequestMap = Map(
    "method" -> "GET",
    "path" -> "/foo",
    "headers" -> List(testParamMap),
    "params" -> List(testParamMap),
    "body" -> testRequetsBody)

  val testExchange = StubExchange(testRequest, testResponse, Some(1234), Some("script()"))
  val testExchangeMap = Map(
    "request" -> testRequestMap,
    "response" -> testResponseMap,
    "delay" -> 1234,
    "script" -> "script()")

  test("serialize parameter") {
    val result = mapper.convertValue[Map[_, _]](testParam)
    assert(result === testParamMap)
  }

  test("deserialize parameter") {
    val result = mapper.convertValue[StubParam](testParamMap)
    assert(result === testParam)
  }

  test("serialize response") {
    val result = mapper.convertValue[Map[_, _]](testResponse)
    assert(result === testResponseMap)
  }

  test("deserialize response") {
    val result = mapper.convertValue[StubResponse](testResponseMap)
    assert(result === testResponse)
  }

  test("serialize request") {
    val result = mapper.convertValue[Map[_, _]](testRequest)
    assert(result === testRequestMap)
  }

  test("deserialize request") {
    val result = mapper.convertValue[StubRequest](testRequestMap)
    assert(result === testRequest)
  }

  test("serialize exchange") {
    val result = mapper.convertValue[Map[_, _]](testExchange)
    assert(result === testExchangeMap)
  }

  test("deserialize exchange") {
    val result = mapper.convertValue[StubExchange](testExchangeMap)
    assert(result === testExchange)
  }

}
