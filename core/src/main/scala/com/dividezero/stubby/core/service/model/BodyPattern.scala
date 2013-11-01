package com.dividezero.stubby.core.service.model

import com.dividezero.stubby.core.model.StubMessage
import com.dividezero.stubby.core.util.JsonUtils

trait BodyPattern {
  def matches(request: StubMessage): MatchField
}

object BodyPattern {

  def fromRequest(body: Option[AnyRef], bodyType: Option[String]): Option[BodyPattern] = {
    (body, bodyType) match {
      case (Some(b), Some(t)) => Some(create(b, t))
      case (Some(b), None) => Some(guess(b))
      case _ => None
    }
  }

  private def create(body: AnyRef, bodyType: String): BodyPattern = bodyType match {
    case "regex" | "regexp" => body match {
      case s: String => new RegexBodyPattern(s)
      case _ => throw new RuntimeException("Body must be a string for 'regexp' type")
    }
    case "json" => body match {
      case str: String => new JsonBodyPattern(JsonUtils.deserializeObject(str))
      case _: collection.Map[_, _] | _: collection.Seq[_] => new JsonBodyPattern(body)
      case _ => throw new RuntimeException("Body must be a object or array for 'json' type")
    }
    //case "jsonpath" => ???
    //case "xpath" => ???
    case _ => throw new RuntimeException("Unknown body type: " + bodyType)
  }

  private def guess(body: AnyRef): BodyPattern = body match {
    case str: String => new RegexBodyPattern(str)
    case coll @ (_: collection.Map[_, _] | _: collection.Seq[_]) => new JsonBodyPattern(body)
    case x @ _ => throw new RuntimeException("Unexpected body type: " + body.getClass)
  }

}