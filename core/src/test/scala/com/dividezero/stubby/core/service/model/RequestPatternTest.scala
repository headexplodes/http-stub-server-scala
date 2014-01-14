package com.dividezero.stubby.core.service.model

import org.scalatest.FunSuite
import com.dividezero.stubby.core.model.StubRequest
import com.dividezero.stubby.core.model.StubParam
import com.dividezero.stubby.core.service.model.FieldType._
import com.dividezero.stubby.core.util.OptionUtils
import org.scalatest.Matchers

class RequestPatternTest extends FunSuite with Matchers {

  import OptionUtils._

  val stubbedRequest = new StubRequest(
    method = "PO.*",
    path = "/request/.*",
    params = List(StubParam("foo", "b.r")),
    headers = List(StubParam("Content-Type", "text/plain; .+")),
    body = "body .*")

  val incomingRequest = new StubRequest(
    method = "POST",
    path = "/request/path",
    params = List(StubParam("foo", "bar")),
    headers = List(StubParam("Content-Type", "text/plain; charset=UTF-8")),
    body = "body pattern")

  def assertNotFound(fieldType: FieldType, name: String, expected: String)(implicit result: MatchResult) = {
    val expectedField = new PartialMatchField(fieldType, name, expected).asNotFound

    assert(!result.matches)
    result.fields should contain(expectedField)
  }

  def assertMatchFailure(fieldType: FieldType, name: String, expected: String, actual: String)(implicit result: MatchResult) = {
    val expectedField = new PartialMatchField(fieldType, name, expected).asMatchFailure(actual)

    assert(!result.matches)
    result.fields should contain(expectedField)
  }

  def assertMatchSuccess(fieldType: FieldType, name: String, expected: String, actual: String)(implicit result: MatchResult) = {
    val expectedField = new PartialMatchField(fieldType, name, expected).asMatch(actual)

    assert(result.matches)
    result.fields should contain(expectedField)
  }

  test("equality") {
    assert(new RequestPattern(stubbedRequest) === new RequestPattern(stubbedRequest))
  }

  test("hash code") {
    assert(new RequestPattern(stubbedRequest).hashCode === new RequestPattern(stubbedRequest).hashCode)
  }

  test("construct from stubbed request") {
    val pattern = new RequestPattern(stubbedRequest)

    assert(pattern.method.get === "PO.*")
    assert(pattern.path.get === "/request/.*")

    assert(pattern.params.size === 1)
    assert(pattern.params.head.name === "foo")
    assert(pattern.params.head.pattern === "b.r")

    assert(pattern.headers.size === 1)
    assert(pattern.headers.head.name === "Content-Type")
    assert(pattern.headers.head.pattern === "text/plain; .+")

    assert(pattern.body.get === new RegexBodyPattern("body .*"))
  }

  test("construct from empty pattern") {
    val pattern = new RequestPattern(new StubRequest())

    assert(pattern.method.isEmpty)
    assert(pattern.path.isEmpty)
    assert(pattern.body.isEmpty)
    assert(pattern.params.isEmpty)
    assert(pattern.headers.isEmpty)
  }

  test("JSON body pattern map") {
    val body = Map("foo" -> "bar")
    val request = new StubRequest(body = body)
    val pattern = new RequestPattern(request)

    assert(pattern.body.get === new JsonBodyPattern(body))
  }
  
  test("JSON body pattern mutable map") {
    val body = collection.mutable.Map("foo" -> "bar")
    val request = new StubRequest(body = body)
    val pattern = new RequestPattern(request)

    assert(pattern.body.get === new JsonBodyPattern(body))
  }
  
  test("JSON body pattern list") {
    val body = List("foo", "bar")
    val request = new StubRequest(body = body)
    val pattern = new RequestPattern(request)

    assert(pattern.body.get === new JsonBodyPattern(body))
  }
  
  test("JSON body pattern mutable list") {
    val body = collection.mutable.ListBuffer("foo", "bar")
    val request = new StubRequest(body = body)
    val pattern = new RequestPattern(request)

    assert(pattern.body.get === new JsonBodyPattern(body))
  }

  test("successful match") {
    implicit val result = new RequestPattern(stubbedRequest).matches(incomingRequest)

    assert(result.matches)

    assertMatchSuccess(METHOD, "method", "PO.*", "POST")
    assertMatchSuccess(PATH, "path", "/request/.*", "/request/path")
    assertMatchSuccess(QUERY_PARAM, "foo", "b.r", "bar")
    assertMatchSuccess(HEADER, "Content-Type", "text/plain; .+", "text/plain; charset=UTF-8")
    assertMatchSuccess(BODY, "body", "body .*", "body pattern")
  }

  test("matches with extra params") {
    val incoming = incomingRequest.copy(
      params = incomingRequest.params :+ StubParam("what", "eva"))
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assert(result.matches)
  }
        
  test("matches parameter with groups in pattern") {
    val pattern = stubbedRequest.copy(params = List(StubParam("foo", "(b)(a)r")))
    val result = new RequestPattern(pattern).matches(incomingRequest)
    
    assert(result.matches)
  }

  test("doesn't match partial parameter value") {
    val pattern = stubbedRequest.copy(params = List(StubParam("foo", "b")))
    val result = new RequestPattern(pattern).matches(incomingRequest)

    assert(!result.matches)
  }
  
  test("doesn't match incorrect params") {
    val incoming = incomingRequest.copy(params = List(StubParam("foo", "invalid")))
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(QUERY_PARAM, "foo", "b.r", "invalid")(result)
  }

  test("doesn't match when no parameters") {
    val incoming = incomingRequest.copy(params = Nil)
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertNotFound(QUERY_PARAM, "foo", "b.r")(result)
  }

  test("matches with extra headers") {
    val incoming = incomingRequest.copy(
      headers = incomingRequest.headers :+ StubParam("Content-Type", "application/json"))
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assert(result.matches)
  }
  
  test("matches header value with groups in pattern") {
    val pattern = stubbedRequest.copy(headers = List(StubParam("Content-Type", "(text/plain); (.+)")))
    val result = new RequestPattern(stubbedRequest).matches(incomingRequest)

    assert(result.matches)
  }

  test("doesn't match partial header value") {
    val pattern = stubbedRequest.copy(headers = List(StubParam("Content-Type", "text")))
    val result = new RequestPattern(pattern).matches(incomingRequest)

    assertMatchFailure(HEADER, "Content-Type", "text", "text/plain; charset=UTF-8")(result)
  }

  test("doesn't match incorrect headers") {
    val incoming = incomingRequest.copy(
      headers = List(StubParam("Content-Type", "image/gif")))
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(HEADER, "Content-Type", "text/plain; .+", "image/gif")(result)
  }

  test("doesn't match when no headers") {
    val incoming = incomingRequest.copy(headers = Nil)
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertNotFound(HEADER, "Content-Type", "text/plain; .+")(result)
  }

  test("doesn't match wrong body") {
    val incoming = incomingRequest.copy(body = "wrong body")
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(BODY, "body", "body .*", "wrong body")(result)
  }

  test("doesn't match body when empty") {
    val incoming = incomingRequest.copy(body = None)
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertNotFound(BODY, "body", "<pattern>")(result)
  }
  
  test("matches method pattern with groups") {
    val pattern = stubbedRequest.copy(method = "(PO)(.*)")
    val result = new RequestPattern(pattern).matches(incomingRequest)

    assert(result.matches)
  }
  
  test("doesn't match when wrong method") {
    val incoming = incomingRequest.copy(method = "HEAD")
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(METHOD, "method", "PO.*", "HEAD")(result)
  }
  
  test("doesn't match partial method string") {
    val incoming = incomingRequest.copy(method = "XPOST")
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(METHOD, "method", "PO.*", "XPOST")(result)
  }
  
  test("matches path with groups") {
    val pattern = stubbedRequest.copy(path = "/(request)/(.*)")
    val result = new RequestPattern(pattern).matches(incomingRequest)

    assert(result.matches)
  }
  
  test("doesn't match incorrect path") {
    val incoming = incomingRequest.copy(path = "/invalid")
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(PATH, "path", "/request/.*", "/invalid")(result)
  }
  
  test("doesn't match partial path string") {
    val incoming = incomingRequest.copy(path = "/invalid/request/test")
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(PATH, "path", "/request/.*", "/invalid/request/test")(result)
  }

}