package com.dividezero.stubby.test

import com.dividezero.stubby.test.support.StubbyTest
import com.dividezero.stubby.core.util.OptionUtils
import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonBodyMatchingTest extends FunSuite with StubbyTest with OptionUtils {

  def patternMap = Map("foo" -> "bar")
  def patternStr = """{"foo": "bar"}"""
  
  test("JSON pattern should match JSON request") {
    given(path = "/", body = patternMap, bodyType = "json").respond(Ok).stub

    POST("/", body = patternMap).contentType("application/json").send.assertOk
  }
  
  test("JSON pattern (as string) should match JSON request") {
    given(path = "/", body = patternStr, bodyType = "json").respond(Ok).stub // pattern as a string (not an object)

    POST("/", body = patternMap).contentType("application/json").send.assertOk
  }

  test("JSON pattern type should be guessed when an object") {
    given(path = "/", body = patternMap).respond(Ok).stub // no bodyType specified

    POST("/", body = patternMap).contentType("application/json").send.assertOk
  }
  
  test("JSON pattern should be able to match non-JSON content type request") {
    given(path = "/", body = patternMap, bodyType = "json").respond(Ok).stub

    POST("/", body = patternMap).contentType("text/plain").send.assertOk // not 'application/json'
  }
 
  test("JSON pattern should support non-exact matching") {
    def patternWildcard = Map("foo" -> "b..")
    
    given(path = "/", body = patternWildcard, bodyType = "json").respond(Ok).stub

    POST("/", body = patternMap).contentType("application/json").send.assertOk
  }
  
  test("JSON pattern should not match when incorrect") {
    def pattern = Map("bar" -> "foo")
    
    given(path = "/", body = pattern, bodyType = "json").respond(Ok).stub

    POST("/", body = patternMap).contentType("application/json").send.assertNotFound
  }

}
