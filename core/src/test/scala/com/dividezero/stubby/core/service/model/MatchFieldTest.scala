package com.dividezero.stubby.core.service.model

import org.scalatest.FunSuite

import scala.util.matching.Regex
import FieldType._

class MatchFieldTest extends FunSuite {

  test("Path match") {
    assert(new PartialMatchField(PATH, "path", "/foo").asMatch("/foo").score == 5)
  }
  
  test("Path match failure") {
    assert(new PartialMatchField(PATH, "path", "/foo").asMatchFailure("/bar").score == 0)
  }
    
  test("Other field matches") {
    assert(PartialMatchField(BODY, "body", "foo").asMatch("foo").score == 2)
    assert(PartialMatchField(QUERY_PARAM, "foo", "bar").asMatch("bar").score == 2)
    assert(PartialMatchField(HEADER, "X-Foo", "bar").asMatch("bar").score == 2)
  }
  
  test("Other field match failure") {
    assert(PartialMatchField(QUERY_PARAM, "foo", "bar").asMatchFailure("blah").score == 1)
    assert(PartialMatchField(HEADER, "X-Foo", "bar").asMatchFailure("blah").score == 1)
  }

  test("Ensure equality") {
    val f1 = new PartialMatchField(BODY, "foo", new TextPattern(".*")).asMatchFailure("bar", "message")
    val f2 = new PartialMatchField(BODY, "foo", new TextPattern(".*")).asMatchFailure("bar", "message")
    assert(f1 === f2)
  }

}