package com.dividezero.stubby.core.service.model

import scala.util.matching.Regex
import com.dividezero.stubby.core.util.HttpMessageUtils
import com.dividezero.stubby.core.model.StubMessage

case class RegexBodyPattern(pattern: TextPattern) extends BodyPattern {

  def this(pattern: String) = this(new TextPattern(pattern))

  override def matches(request: StubMessage) = {
    val actual = HttpMessageUtils.bodyAsText(request);
    val field = new PartialMatchField(FieldType.BODY, "body", pattern.pattern.toString)

    pattern.unapplySeq(actual) match { // match pattern against entire body
      case Some(_) => field.asMatch(actual)
      case None => field.asMatchFailure(actual)
    }
  }

}
