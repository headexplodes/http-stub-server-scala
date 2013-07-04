package com.dz.stubby.core.service.model

import org.scalatest.BeforeAndAfter
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubParam
import scala.util.matching.Regex
import org.scalatest.FunSuite
import com.dz.stubby.core.util.OptionUtils

class TextBodyPatternTest extends FunSuite with BeforeAndAfter {

  import OptionUtils._

  implicit val defaultRequest =
    new StubRequest("METHOD", "path", Nil, List(new StubParam("Content-Type", "text/plain")), "foo")

  def assertRequestMatches(patternStr: String)(implicit request: StubRequest) = {
    val pattern = new TextBodyPattern(new TextPattern(patternStr))

    val result = pattern.matches(request)

    assert(result.fieldType === FieldType.BODY)
    assert(result.matchType === MatchType.MATCH)
  }

  def assertRequestDoesNotMatch(patternStr: String)(implicit request: StubRequest) = {
    val pattern = new TextBodyPattern(new TextPattern(patternStr))

    val result = pattern.matches(request)

    assert(result.fieldType === FieldType.BODY)
    assert(result.matchType === MatchType.MATCH_FAILURE)
  }

  test("invalid content type") {
    implicit val request =
      defaultRequest.copy(headers = List(new StubParam("Content-Type", "application/json")))

    assertRequestDoesNotMatch("foo")(request)
  }

  test("matches") {
    assertRequestMatches(".*")
    assertRequestMatches("f[o]+")
  }

  test("doesn't match") {
    assertRequestDoesNotMatch("b..")
  }

  test("equality") {
    assert(new TextBodyPattern(new TextPattern("foo.*")) === new TextBodyPattern(new TextPattern("foo.*")))
  }

}
