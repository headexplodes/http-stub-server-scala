package com.dividezero.stubby.core.model

import org.scalatest.FunSuite
import com.dividezero.stubby.core.util.JsonUtils
import com.dividezero.stubby.core.util.OptionUtils

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
    
  val minimalResponse = StubResponse(200)
  val minimalResponseMap = Map("status" -> 200)

  val testRequetsBody = Map("foo" -> "bar")
  val testRequest = StubRequest("GET", "/foo", List(testParam), List(testParam), testRequetsBody, "testBodyType")
  val testRequestMap = Map(
    "method" -> "GET",
    "path" -> "/foo",
    "headers" -> List(testParamMap),
    "params" -> List(testParamMap),
    "body" -> testRequetsBody,
    "bodyType" -> "testBodyType")
    
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

  test("serialize minimal response") {
    val result = mapper.convertValue[Map[_, _]](minimalResponse)
    assert(result == minimalResponseMap)
  }

  test("deserialize response") {
    val result = mapper.convertValue[StubResponse](testResponseMap)
    assert(result === testResponse)
  }
  
  test("deserialize minimal response") {
    val result = mapper.convertValue[StubResponse](minimalResponseMap).nilLists() // TODO: <HACK/>
    assert(result === minimalResponse)
  }

  test("serialize request") {
    val result = mapper.convertValue[Map[_, _]](testRequest)
    assert(result === testRequestMap)
  }

  test("serialize empty request") {
    val result = mapper.convertValue[Map[_, _]](StubRequest())
    assert(result == Map())
  }

  test("deserialize request") {
    val result = mapper.convertValue[StubRequest](testRequestMap)
    assert(result === testRequest)
  }
  
  test("deserialize empty request") {
    val result = mapper.convertValue[StubRequest](Map()) .nilLists() // TODO: <HACK/>
    assert(result === StubRequest())
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
