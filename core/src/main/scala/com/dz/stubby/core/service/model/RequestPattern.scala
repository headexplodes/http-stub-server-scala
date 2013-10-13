package com.dz.stubby.core.service.model

import scala.util.matching.Regex
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest
import scala.annotation.meta.field
import com.dz.stubby.core.model.StubMessage

object RequestPattern {
  def toPattern(value: Option[String]): Option[TextPattern] = {
    value.map(s => new TextPattern(s))
  }

  def toPattern(params: List[StubParam]): Set[ParamPattern] = {
    params.map(p => new ParamPattern(p.name, p.value)).toSet
  }

  def toBodyPattern(obj: Option[AnyRef]): Option[BodyPattern] = obj.map {
      case str: String => new TextBodyPattern(str)
      case coll @ (_: collection.Map[_, _] | _: collection.Seq[_]) => new JsonBodyPattern(coll)
      case x @ _ => throw new RuntimeException("Unexpected body type: " + x.getClass)
  }
}

case class RequestPattern(
    method: Option[TextPattern],
    path: Option[TextPattern],
    params: Set[ParamPattern],
    headers: Set[ParamPattern],
    body: Option[BodyPattern]) {

  def this(request: StubRequest) = this(
    RequestPattern.toPattern(request.method),
    RequestPattern.toPattern(request.path),
    RequestPattern.toPattern(request.params),
    RequestPattern.toPattern(request.headers),
    RequestPattern.toBodyPattern(request.body))

  def matches(message: StubRequest): MatchResult = {

    def matchMethod: Option[MatchField] = {
      method.map { ptn =>
        val methodField = new PartialMatchField(FieldType.METHOD, "method", method.get)
        message.method match {
          case None => methodField.asNotFound
          case Some(m) => ptn.unapplySeq(m) match {
            case Some(_) => methodField.asMatch(m)
            case None => methodField.asMatchFailure(m)
          }
        }
      }
    }

    def matchPath: Option[MatchField] = {
      path.map { ptn =>
        val pathField = new PartialMatchField(FieldType.PATH, "path", path.get)
        message.path match {
          case None => pathField.asNotFound
          case Some(m) => ptn.unapplySeq(m) match {
            case Some(_) => pathField.asMatch(m)
            case None => pathField.asMatchFailure(m)
          }
        }
      }
    }

    def matchBody: Option[MatchField] = {
      body.map { ptn =>
        val bodyField = new PartialMatchField(FieldType.BODY, "body", "<pattern>")
        message.body match {
          case None => bodyField.asNotFound
          case Some(m) => ptn.matches(message)
        }
      }
    }

    def matchParamValues(values: Seq[String], field: PartialMatchField, pattern: ParamPattern): MatchField = {
      if (values.isEmpty) {
        field.asNotFound
      } else {
        values.find(p => pattern.pattern.matches(p)) match {
          case Some(v) => field.asMatch(v)
          case None => field.asMatchFailure(
            if (values.size > 1) values else values.head) // don't wrap in array if only single value
        }
      }
    }

    def matchParam(pattern: ParamPattern): MatchField = {
      matchParamValues(
        message.getParams(pattern.name), // case sensitive lookup
        new PartialMatchField(FieldType.QUERY_PARAM, pattern.name, pattern.pattern),
        pattern)
    }

    def matchHeader(pattern: ParamPattern): MatchField = {
      matchParamValues(
        message.getHeaders(pattern.name), // case insensitive lookup
        new PartialMatchField(FieldType.HEADER, pattern.name, pattern.pattern),
        pattern)
    }

    def matchParams = {
      params.map(paramPattern => matchParam(paramPattern))
    }

    def matchHeaders = {
      headers.map(headerPattern => matchHeader(headerPattern))
    }

    val fields =
      matchMethod ++
        matchPath ++
        matchParams ++
        matchHeaders ++
        matchBody

    MatchResult(fields.toList)
  }

}
