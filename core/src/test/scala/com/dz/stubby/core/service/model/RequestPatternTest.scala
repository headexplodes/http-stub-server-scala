package com.dz.stubby.core.service.model

import org.scalatest.FunSuite
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.service.model.FieldType._
import org.scalatest.matchers.ShouldMatchers

class RequestPatternTest extends FunSuite with ShouldMatchers {

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

  test("construct from pattern") {
    val pattern = new RequestPattern(stubbedRequest)

    assert(pattern.method === "PO.*")
    assert(pattern.path === "/request/.*")

    assert(pattern.params.size === 1)
    assert(pattern.params.head.name === "foo")
    assert(pattern.params.head.pattern === "b.r")

    assert(pattern.headers.size === 1)
    assert(pattern.headers.head.name === "Content-Type")
    assert(pattern.headers.head.pattern === "text/plain; .+")

    assert(pattern.body === new TextBodyPattern("body .*"))
  }

  test("construct from empty pattern") {
    val pattern = new RequestPattern(new StubRequest())

    assert(pattern.method === null)
    assert(pattern.path === null)
    assert(pattern.body === null)
    assert(pattern.params.isEmpty)
    assert(pattern.headers.isEmpty)
  }

  test("JSON body pattern object") {
    val body = Map("foo" -> "bar")
    val request = new StubRequest(body = body)
    val pattern = new RequestPattern(request)

    assert(pattern.body === new JsonBodyPattern(body))
  }

  test("JSON body pattern list") {
    val body = List("foo" -> "bar")
    val request = new StubRequest(body = body)
    val pattern = new RequestPattern(request)

    assert(pattern.body === new JsonBodyPattern(body))
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
    val incoming = incomingRequest.copy(body = null)
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertNotFound(BODY, "body", "<pattern>")(result)
  }

  test("doesn't match when wrong method") {
    val incoming = incomingRequest.copy(method = "HEAD")
    val result = new RequestPattern(stubbedRequest).matches(incoming)

    assertMatchFailure(METHOD, "method", "PO.*", "HEAD")(result)
  }

}