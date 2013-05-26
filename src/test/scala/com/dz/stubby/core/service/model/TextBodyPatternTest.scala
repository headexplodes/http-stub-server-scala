package com.dz.stubby.core.service.model

import org.scalatest.BeforeAndAfter
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubParam
import scala.util.matching.Regex
import org.scalatest.FunSuite

class TextBodyPatternTest extends FunSuite {

  val request = new StubRequest("METHOD", "path", Nil, List(new StubParam("Content-Type", "text/plain")), "foo")

  def assertRequestMatches(patternStr: String) = {
    val pattern = new TextBodyPattern(new Regex(patternStr))

    val result = pattern.matches(request)

    assert(result.fieldType === FieldType.BODY)
    assert(result.matchType === MatchType.MATCH)
  }

  def assertRequestDoesNotMatch(patternStr: String) = {
    val pattern = new TextBodyPattern(new Regex(patternStr))

    val result = pattern.matches(request)

    assert(result.fieldType === FieldType.BODY)
    assert(result.matchType === MatchType.MATCH_FAILURE)
  }

  test("invalid content type") {
    request.setHeader("Content-Type", "application/json")

    assertRequestDoesNotMatch("foo")
  }

  test("matches") {
    assertRequestMatches(".*")
    assertRequestMatches("f[o]+")
  }

  test("doesn't match") {
    assertRequestDoesNotMatch("b..")
  }

  test("equality") {
    assert(new TextBodyPattern(new Regex("foo.*")) === new TextBodyPattern(new Regex("foo.*")))
  }

}
