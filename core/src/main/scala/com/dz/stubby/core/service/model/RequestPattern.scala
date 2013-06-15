package com.dz.stubby.core.service.model

import scala.util.matching.Regex
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest
import scala.annotation.meta.field
import com.dz.stubby.core.model.StubMessage

object RequestPattern {
  //val DefaultPattern: TextPattern = new TextPattern(".*")

  def toPattern(value: String): TextPattern = {
    if (value != null) new TextPattern(value) else null
  }

  def toPattern(params: List[StubParam]): Set[ParamPattern] = {
    params.map(p => new ParamPattern(p.name, p.value)).toSet
  }

  def toBodyPattern(obj: AnyRef): BodyPattern = obj match {
    case null => null
    case str: String => new TextBodyPattern(str)
    case coll @ (_: Map[_, _] | _: List[_]) => new JsonBodyPattern(coll)
    case _ => throw new RuntimeException("Unexpected body type: " + obj.getClass)
  }
}

case class RequestPattern(
    val method: TextPattern,
    val path: TextPattern,
    val params: Set[ParamPattern], // TODO: ensure not serialized when empty
    val headers: Set[ParamPattern],
    val body: BodyPattern) {

  def this(request: StubRequest) = this(
    RequestPattern.toPattern(request.method),
    RequestPattern.toPattern(request.path),
    RequestPattern.toPattern(request.params),
    RequestPattern.toPattern(request.headers),
    RequestPattern.toBodyPattern(request.body))

  def matches(message: StubRequest): MatchResult = {

    def matchMethod: Option[MatchField] = {
      val methodField = new PartialMatchField(FieldType.METHOD, "method", method)
      if (method != null && message.method != null) { // not really valid unless incoming message has method, but just to be safe
        if (method.findFirstIn(message.method).nonEmpty) {
          Some(methodField.asMatch(message.method))
        } else {
          Some(methodField.asMatchFailure(message.method))
        }
      } else {
        None
      }
    }

    def matchPath: Option[MatchField] = {
      val pathField = new PartialMatchField(FieldType.PATH, "path", path)
      if (path != null) {
          if (path.findFirstIn(message.path).nonEmpty) { 
            Some(pathField.asMatch(message.path))
          } else {
            Some(pathField.asMatchFailure(message.path))
          }
      } else {
          None
      }
    }

    def matchBody: Option[MatchField] = {
      val bodyField = new PartialMatchField(FieldType.BODY, "body", "<pattern>")
      if (body != null) {
        if (message.body != null) {
          Some(body.matches(message))
        } else {
          Some(bodyField.asNotFound)
        }
      } else {
        None
      }
    }

    def matchParamValues(values: Seq[String], field: PartialMatchField, pattern: ParamPattern): MatchField = {
      if (values.isEmpty) {
        field.asNotFound
      } else {
        values.find(p => pattern.pattern.matches(p)) match {
          case Some(v) => field.asMatch(v)
          case None => field.asMatchFailure(if (values.size > 1) values else values.head) // don't wrap in array if only single value
        }
      }
    }

    def matchParam(pattern: ParamPattern): MatchField = {
      matchParamValues(
        message.getParams(pattern.name), // case insensitive lookup
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
