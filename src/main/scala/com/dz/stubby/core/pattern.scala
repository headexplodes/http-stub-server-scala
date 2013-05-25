package com.dz.stubby.core

import scala.util.matching.Regex
import com.dz.stubby.core.model.FieldType._
import com.dz.stubby.core.model.MatchField._
import com.dz.stubby.core.model._
import java.util.regex.Pattern

class TextPattern(regex: String) extends Regex(regex) {
  
  override def equals(obj: Any) = obj match {
    case r: Regex => pattern.pattern == r.pattern.pattern
    case p: Pattern => pattern.pattern == p.pattern
    case s: String => pattern.pattern == s
    case _ => false
  }
  
}

object RequestPattern {
   val DefaultPattern: TextPattern = new TextPattern(".*")
   
   def toPattern(value: String): TextPattern = if (value != null) new TextPattern(value) else DefaultPattern
   
   def toPattern(params: List[StubParam]): Set[ParamPattern] = {
//        Set<ParamPattern> pattern = new HashSet<ParamPattern>();
//        if (params != null) {
//            for (StubParam param : params) {
//                pattern.add(new ParamPattern(param.getName(), toPattern(param.getValue())));
//            }
//        }
//        return pattern;
     null
    }
   
    def toBodyPattern(obj: Any): BodyPattern = {
//        if (object != null) {
//            if (object instanceof String) {
//                return new TextBodyPattern(object.toString());
//            } else if (object instanceof Map
//                    || object instanceof List) {
//                return new JsonBodyPattern(object);
//            } else {
//                throw new RuntimeException("Unexpected body type: " + object);
//            }
//        } else {
//            return null; // don't match body
//        }
           null
    }
}

class RequestPattern(
    val method: Regex, 
    val path: Regex, 
    val params: Set[ParamPattern], 
    val headers: Set[ParamPattern], 
    val body: BodyPattern) {    
  
    def this(request: StubRequest) = this(
        RequestPattern.toPattern(request.method), 
        RequestPattern.toPattern(request.path), 
        RequestPattern.toPattern(request.params), 
        RequestPattern.toPattern(request.headers), 
        RequestPattern.toBodyPattern(request.body))   
    
    def matches(message: StubRequest): MatchResult = {

//        MatchResult result = new MatchResult();
//
//        MatchField methodField = new MatchField(MatchField.FieldType.METHOD, "method", method);
//        if (method != null) {
//            if (method.matcher(message.getMethod()).matches()) {
//                result.add(methodField.asMatch(message.getMethod()));
//            } else {
//                result.add(methodField.asMatchFailure(message.getMethod()));
//            }
//        }
//
//        MatchField pathField = new MatchField(FieldType.PATH, "path", path);
//        if (path.matcher(message.getPath()).matches()) {
//            result.add(pathField.asMatch(message.getPath()));
//        } else {
//            result.add(pathField.asMatchFailure(message.getPath()));
//        }
//
//        for (ParamPattern paramPattern : params) {
//            result.add(matchParam(message, paramPattern));
//        }
//
//        for (ParamPattern headerPattern : headers) {
//            result.add(matchHeader(message, headerPattern));
//        }
//
//        if (body != null) {
//            if (message.getBody() != null) {
//                result.add(body.matches(message));
//            } else {
//                result.add(new MatchField(FieldType.BODY, "body", "<pattern>").asNotFound());
//            }
//        }
//
//        return result;
            null
      }
//
//    private MatchField matchParam(StubRequest message, ParamPattern pattern) {
//        MatchField field = new MatchField(FieldType.QUERY_PARAM, pattern.getName(), pattern.getPattern());
//        List<String> values = message.getParams(pattern.getName());
//        if (!values.isEmpty()) {
//            for (String value : values) {
//                if (pattern.getPattern().matcher(value).matches()) {
//                    return field.asMatch(value);
//                }
//            }
//            return field.asMatchFailure(values.size() > 1 ? values : values.get(0)); // don't wrap in array if only single value
//        } else {
//            return field.asNotFound();
//        }
//    }
//
//    private MatchField matchHeader(StubMessage message, ParamPattern pattern) {
//        MatchField field = new MatchField(FieldType.HEADER, pattern.getName(), pattern.getPattern());
//        List<String> values = message.getHeaders(pattern.getName()); // case insensitive lookup
//        if (!values.isEmpty()) {
//            for (String value : values) {
//                if (pattern.getPattern().matcher(value).matches()) {
//                    return field.asMatch(value);
//                }
//            }
//            return field.asMatchFailure(values.size() > 1 ? values : values.get(0)); // don't wrap in array if only single value
//        } else {
//            return field.asNotFound();
//        }
//    }


}

class ParamPattern(
    val name: String,
    val pattern: Regex) {
  override def toString = name + " =~ m/" + pattern + "/"
}

trait BodyPattern {
  def matches(request: StubMessage): MatchField
}

case class TextBodyPattern(val pattern: Regex) extends BodyPattern {
    override def matches(request: StubMessage) = {
        val actual = HttpMessageUtils.bodyAsText(request);
        val field = new PartialMatchField(BODY, "body", pattern.pattern);
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

