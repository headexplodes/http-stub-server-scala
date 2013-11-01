package com.dividezero.stubby.core.service.model

import org.scalatest.BeforeAndAfter
import com.dividezero.stubby.core.model.StubRequest
import com.dividezero.stubby.core.model.StubParam
import scala.util.matching.Regex
import org.scalatest.FunSuite
import com.dividezero.stubby.core.util.OptionUtils

class RegexBodyPatternTest extends FunSuite with BeforeAndAfter {

  import OptionUtils._

  implicit val defaultRequest =
    new StubRequest("METHOD", "path", Nil, List(new StubParam("Content-Type", "text/plain")), "foo")

  def assertRequestMatches(patternStr: String)(implicit request: StubRequest) = {
    val pattern = new RegexBodyPattern(new TextPattern(patternStr))

    val result = pattern.matches(request)

    assert(result.fieldType === FieldType.BODY)
    assert(result.matchType === MatchType.MATCH)
  }

  def assertRequestDoesNotMatch(patternStr: String)(implicit request: StubRequest) = {
    val pattern = new RegexBodyPattern(new TextPattern(patternStr))

    val result = pattern.matches(request)

    assert(result.fieldType === FieldType.BODY)
    assert(result.matchType === MatchType.MATCH_FAILURE)
  }

  test("should match non-text content type") {
    val request =
      defaultRequest.copy(headers = List(new StubParam("Content-Type", "application/xml")))

    assertRequestMatches("foo")(request)
  }

  test("matches") {
    assertRequestMatches(".*")
    assertRequestMatches("f[o]+")
  }

  test("matches with groups") {
    assertRequestMatches("(f)(o)(o)")
  }

  test("doesn't match different pattern") {
    assertRequestDoesNotMatch("b..")
  }

  test("doesn't match partial string") {
    assertRequestDoesNotMatch("f")
  }

  test("equality") {
    assert(new RegexBodyPattern(new TextPattern("foo.*")) === new RegexBodyPattern(new TextPattern("foo.*")))
  }

}
