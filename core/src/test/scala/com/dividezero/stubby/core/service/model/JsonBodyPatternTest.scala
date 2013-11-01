package com.dividezero.stubby.core.service.model

import org.scalatest.FunSuite
import com.dividezero.stubby.core.util.JsonUtils
import com.dividezero.stubby.core.model.StubMessage
import com.dividezero.stubby.core.model.StubRequest
import com.dividezero.stubby.core.model.StubParam
import com.dividezero.stubby.core.util.OptionUtils

class JsonBodyPatternTest extends FunSuite {

  import OptionUtils._

  def parse(json: String) = JsonUtils.deserializeObject(json)

  def message(bodyJson: String): StubRequest =
    new StubRequest(
      body = parse(bodyJson),
      headers = List(StubParam("Content-Type", "application/json")))

  def makePattern(patternJson: String) = new JsonBodyPattern(parse(patternJson))

  class PartialAssert(patternStr: String) {
    val pattern = makePattern(patternStr)

    def matches(request: String) = {
      val result = pattern.matches(message(request))
      assert(FieldType.BODY === result.fieldType)
      assert(MatchType.MATCH === result.matchType, result.message)
    }

    def doesNotMatch(request: String) = {
      val result = pattern.matches(message(request))
      assert(FieldType.BODY === result.fieldType)
      assert(MatchType.MATCH_FAILURE === result.matchType, result.message)
      assert(result.message != null)
    }
  }

  def assertPattern(pattern: String): PartialAssert = new PartialAssert(pattern)

  test("should match non-JSON content type") {
    val request = message("{}").setHeader("Content-Type", "text/plain")
    assert(MatchType.MATCH === makePattern("{}").matches(request).matchType)
  }

  test("empty pattern") {
    assertPattern("{}").matches("""{}""");
    assertPattern("{}").matches("""{"any":"value"}""")
    assertPattern("[]").matches("""[]""")
    assertPattern("[]").matches("""[{"any":"value"}]""")
  }

  test("array does not match object") {
    assertPattern("[]").doesNotMatch("{}")
  }

  test("object does not match array") {
    assertPattern("{}").doesNotMatch("[]")
  }

  test("simple object match") {
    assertPattern("""{"foo":"bar"}""").matches("""{"foo":"bar"}""")
    assertPattern("""{"foo":"bar"}""").doesNotMatch("""{"foo":"blah"}""")
    assertPattern("""{"foo":"bar"}""").doesNotMatch("""{"foo2":"bar"}""")
    assertPattern("""{"foo":"bar"}""").doesNotMatch("""{}""")
  }

  test("regular expression match") {
    assertPattern("{\"foo\":\".*\"}").matches("{\"foo\":\"bar\"}")
    assertPattern("{\"foo\":\".*\"}").matches("{\"foo\":\"\"}")
    assertPattern("{\"foo\":\".*\"}").doesNotMatch("{}")
    assertPattern("{\"foo\":\"(true|false)\"}").matches("{\"foo\":true}")
    assertPattern("{\"foo\":\"(true|false)\"}").matches("{\"foo\":false}")
    assertPattern("{\"foo\":\"(true|false)\"}").matches("{\"foo\":\"false\"}")
    assertPattern("{\"foo\":\"(true|false)\"}").doesNotMatch("{\"foo\":1}")
    assertPattern("{\"foo\":\"[12]3\"}").matches("{\"foo\":13}")
    assertPattern("{\"foo\":\"[12]3\"}").matches("{\"foo\":23}")
    assertPattern("{\"foo\":\"[12]3\"}").matches("{\"foo\":\"23\"}")
    assertPattern("{\"foo\":\"[12]3\"}").doesNotMatch("{\"foo\":33}")
  }

  test("number match") {
    assertPattern("{\"foo\":123}").matches("{\"foo\":123}");
    assertPattern("{\"foo\":1.23}").matches("{\"foo\":1.23}");
    assertPattern("{\"foo\":1.234}").doesNotMatch("{\"foo\":1.23}");
    assertPattern("{\"foo\":123}").doesNotMatch("{\"foo\":\"123\"}");
  }

  test("BigDecimal for large floating point") {
    assertPattern("{\"foo\":1.11222333444555666777888999}").matches("{\"foo\":1.11222333444555666777888999}")
    assertPattern("{\"foo\":1.11222333444555666777888999}").doesNotMatch("{\"foo\":1.11222333444555666777888998}")
  }

  test("BigInteger for large integers") {
    assertPattern("{\"foo\":111222333444555666777888999}").matches("{\"foo\":111222333444555666777888999}")
    assertPattern("{\"foo\":111222333444555666777888999}").doesNotMatch("{\"foo\":111222333444555666777888998}")
  }

  test("boolean match") {
    assertPattern("{\"foo\":true}").matches("{\"foo\":true}")
    assertPattern("{\"foo\":false}").matches("{\"foo\":false}")
    assertPattern("{\"foo\":false}").doesNotMatch("{\"foo\":\"false\"}")
  }

  test("null values") {
    assertPattern("""{"foo":null}""").matches("""{"foo":null}""")
    assertPattern("""{"foo":null}""").doesNotMatch("""{}""")
    assertPattern("""{"foo":null}""").doesNotMatch("""{"foo":"null"}""")
  }

  test("array matching") {
    assertPattern("[]").matches("[1,2]")
    assertPattern("[1,2]").matches("[1,2]")
    assertPattern("[2,4]").matches("[1,2,3,4]")
    assertPattern("[3,2]").doesNotMatch("[1,2,3,4]") // pattern elements must be found in order
    assertPattern("[{\"foo\":true}]").matches("[{\"foo\":true}]")
    assertPattern("[{\"first\":true},{\"second\":true}]").matches("[{\"first\":true},{\"second\":true}]")
    assertPattern("[{\"first\":true},{\"second\":true}]").doesNotMatch("[{\"second\":true},{\"first\":true}]")
  }

  test("nested matching") {
    assertPattern("{\"foo\":{\"bar\":true}}").matches("{\"foo\":{\"bar\":true}}")
    assertPattern("{\"foo\":{\"bar\":true}}").doesNotMatch("{\"foo\":{\"bar\":false}}")
    assertPattern("{\"foo\":{\"bar\":[]}}").matches("{\"foo\":{\"bar\":[]}}")
    assertPattern("{\"foo\":{\"bar\":[]}}").matches("{\"foo\":{\"bar\":[{}]}}")
    assertPattern("{\"foo\":{\"bar\":[]}}").doesNotMatch("{\"foo\":{\"bar\":{}}}")
    assertPattern("{\"foo\":{\"bar1\":true,\"bar2\":false}}").doesNotMatch("{\"foo\":{\"bar1\":true,\"bar2\":true}}") // second nested property differs
    assertPattern("{\"foo\":{\"bar1\":true,\"bar2\":true}}").matches("{\"foo\":{\"bar1\":true,\"bar2\":true}}") // second nested property is the same
  }

}