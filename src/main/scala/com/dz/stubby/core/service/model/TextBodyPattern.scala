package com.dz.stubby.core.service.model

import scala.util.matching.Regex
import com.dz.stubby.core.util.HttpMessageUtils
import com.dz.stubby.core.model.StubMessage

case class TextBodyPattern(val pattern: TextPattern) extends BodyPattern {
  
    override def matches(request: StubMessage) = {
        val actual = HttpMessageUtils.bodyAsText(request);
        val field = new PartialMatchField(FieldType.BODY, "body", pattern.pattern)
        if (HttpMessageUtils.isText(request)) { // require text body
            pattern.findFirstIn(actual) match { // match pattern against entire body
              case Some(_) => field.asMatch(actual)
              case _ => field.asMatchFailure(actual)
            }
        } else {
            field.asMatchFailure(actual, "Expected content type: text/*")
        }
    }
    
}
