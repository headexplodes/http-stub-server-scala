package com.dividezero.stubby.core.service.model

import org.scalatest.FunSuite
import com.dividezero.stubby.core.util.OptionUtils
import org.scalatest.Matchers

class BodyPatternTest extends FunSuite with Matchers {

  import BodyPattern._
  import OptionUtils._

  object UnknownType

  test("no body type or body => no pattern") {
    fromRequest(None, None) should be(None)
  }

  test("body type but no body => no pattern") {
    fromRequest(None, "json") should be(None)
  }

  test("guess: json (map)") {
    fromRequest(Map("foo" -> "bar"), None) shouldBe a[Option[JsonBodyPattern]]
  }

  test("guess: json (list)") {
    fromRequest(List("foo", "bar"), None) shouldBe a[Option[JsonBodyPattern]]
  }

  test("guess: regular expression") {
    fromRequest("foo.+", None) shouldBe a[Option[RegexBodyPattern]]
  }

  test("guess: unknown") {
    evaluating { fromRequest(UnknownType, None) } should produce[UnexpectedBodyTypeException]
  }

  test("explicit: json") {
    fromRequest("""{"foo":"bar"}""", "json") shouldBe a[Option[JsonBodyPattern]]
  }

  test("explicit: regular expression") {
    fromRequest("foo.+", "regexp")  shouldBe a[Option[RegexBodyPattern]]
  }

  test("explicit: unknown") {
    evaluating { fromRequest("foo", "unknown") } should produce[UnexpectedBodyTypeException]
  }

}