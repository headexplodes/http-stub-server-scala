package com.dz.stubby.core.util

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite

import com.dz.stubby.core.model.StubMessage
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest

class HttpMessageUtilsTest extends FunSuite with BeforeAndAfter {

  import OptionUtils._
  import HttpMessageUtils._

  test("upper case header") {
    assert("Header" === upperCaseHeader("header"))
    assert("Header-Name" === upperCaseHeader("header-name"))
    assert("X-Header-Name" === upperCaseHeader("x-header-name"))
    assert("-X-Header-Name" === upperCaseHeader("-x-header-name"))
  }

  test("is text: text") {
    val message1 = new StubRequest(headers = List(new StubParam("Content-Type", "text/plain")))
    val message2 = new StubRequest(headers = List(new StubParam("Content-Type", "text/anything; charset=UTF-8")))

    assert(isText(message1))
    assert(isText(message2))
  }

  test("is text: not text") {
    val message = new StubRequest(headers = List(new StubParam("Content-Type", "application/xml")))
    expectResult(false) {
      isText(message)
    }
  }

  test("is text: no header") {
    val message = new StubRequest()
    expectResult(false) {
      isText(message)
    }
  }

  test("is json: json") {
    val message1 = new StubRequest(headers = List(new StubParam("Content-Type", "application/json")))
    val message2 = new StubRequest(headers = List(new StubParam("Content-Type", "application/json; charset=UTF-8")))

    assert(isJson(message1))
    assert(isJson(message2))
  }

  test("is json: not json") {
    val message = new StubRequest(headers = List(new StubParam("Content-Type", "application/xml")))
    expectResult(false) {
      isJson(message)
    }
  }

  test("is json: no header") {
    val message = new StubRequest()
    expectResult(false) {
      isJson(message)
    }
  }

  test("body as text") {
    val message = new StubRequest(body = "text")
    assert(bodyAsText(message) === "text")
  }

  test("body as text: unknown type") {
    val message = new StubRequest(body = List("foo"))
    intercept[RuntimeException] {
      bodyAsText(message)
    }
  }

  test("body as json: string") {
    val message = new StubRequest(body = """["foo","bar"]""")
    assert(bodyAsJson(message) === List("foo", "bar"))
  }

  test("body as json: list") {
    val message = new StubRequest(body = List("foo", "bar"))
    assert(bodyAsJson(message) === List("foo", "bar"))
  }

  test("body as json: map") {
    val message = new StubRequest(body = Map("foo" -> "bar"))
    assert(bodyAsJson(message) === Map("foo" -> "bar"))
  }

  test("body as json: unknown") {
    val message = new StubRequest(body = new Integer(666))
    intercept[RuntimeException] {
      bodyAsJson(message)
    }
  }

}