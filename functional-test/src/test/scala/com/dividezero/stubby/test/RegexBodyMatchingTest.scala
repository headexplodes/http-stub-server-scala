package com.dividezero.stubby.test

import com.dividezero.stubby.test.support.StubbyTest
import com.dividezero.stubby.core.util.OptionUtils
import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegexBodyMatchingTest extends FunSuite with StubbyTest with OptionUtils {

  test("Regex body pattern should match 'text/plain' request") {
    given(path = "/", body = "[F]o+", bodyType = "regex").respond(Ok).stub

    POST("/", body = "Foo").contentType("text/plain").send.assertOk
  }

  test("Regex body pattern should match 'application/json' request") {
    given(path = "/", body = "[F]o+", bodyType = "regex").respond(Ok).stub

    POST("/", body = "Foo").contentType("application/json").send.assertOk
  }

  test("String body pattern should be assumed to be a regular expression") {
    given(path = "/", body = "[F]o+").respond(Ok).stub // no bodyType specified

    POST("/", body = "Foo").contentType("text/plain").send.assertOk
  }

  test("Regular expression is a full-match, not partial match") {
    given(path = "/", body = "[F]o+").respond(Ok).stub

    POST("/", body = "Foobar").contentType("text/plain").send.assertNotFound   
    POST("/", body = "Foo").contentType("text/plain").send.assertOk
  }

}
