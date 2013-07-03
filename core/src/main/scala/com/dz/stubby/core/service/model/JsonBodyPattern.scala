package com.dz.stubby.core.service.model

import com.dz.stubby.core.model._
import com.dz.stubby.core.util.HttpMessageUtils
import com.dz.stubby.core.util.JsonUtils
import com.dz.stubby.core.util.ListUtils._

class JsonBodyPattern(val pattern: AnyRef) extends BodyPattern {

  case class MatchResult(matches: Boolean, path: String, reason: String) { // internal match result type
    def message: String = "%s (at '%s')".format(reason, path)
  }

  override def matches(request: StubMessage[_]): MatchField = {
    val expected = JsonUtils.prettyPrint(pattern)
    val actual = JsonUtils.prettyPrint(request.body)
    val result = matchResult(request)
    val field = PartialMatchField(FieldType.BODY, "body", expected)
    if (result.matches) {
      field.asMatch(actual)
    } else {
      field.asMatchFailure(actual, result.message)
    }
  }

  private def matchFailure(reason: String, path: String) = MatchResult(false, path, reason)
  private def matchSuccess() = MatchResult(true, null, null) // message & path ignored for success

  /*
   * For each property that exists in the pattern, ensure a matching
   * property in the request body. All fields in pattern are assumed to
   * be regular expressions. All strings are converted to strings for matching.
   */
  private def matchResult(request: StubMessage[_]): MatchResult = {
    if (HttpMessageUtils.isJson(request)) { // require a JSON body
      matchValue(pattern, HttpMessageUtils.bodyAsJson(request), ""); // root could be any type (eg, an array)
    } else {
      matchFailure("Expected content type: application/json", ".");
    }
  }

  /*
   * For each property in the pattern object, a property in the request must exist with exactly the 
   * same name and it's value must match (see 'matchValue()' for value matching rules). Properties
   * in the request object that are not in the pattern are ignored.
   * 
   * An empty object pattern matches any request object.
   */
  private def matchObject(pattern: collection.Map[String, Any], request: collection.Map[String, Any], path: String): MatchResult = {
    val result =
      for (key <- pattern.keySet) yield {
        val childPath = path + "." + key
        if (request.contains(key)) {
            matchValue(pattern(key), request(key), childPath)
        } else {
            matchFailure("Property '%s' expected".format(key), childPath)
        }
      }

    result.find(!_.matches).getOrElse(matchSuccess()) // empty pattern matches any object
  }

  /*
   * Arrays does not have to match exactly, but the pattern order is important. 
   * For example, pattern [b,d] matches [a,b,c,d] because 'b' and 'd' exist and 'b' appears before 'd'.
   * (see 'matchValue()' for value matching rules)
   * 
   * An empty array pattern matches any request array.
   */
  private def matchArray(pattern: Seq[Any], request: Seq[Any], path: String): MatchResult = {
    val attempts = for ( // attempt to match every pattern element with each request element
      r <- request.zipWithIndex;
      p <- pattern
    ) yield (p, matchValue(p, r._1, path + "[" + r._2 + "]"))

    val matches = attempts.filter(_._2.matches) // filter out unsuccessful matches
    val compressed = compress(matches.map(_._1)) // list the patterns that matched in order

    if (pattern == compressed) { // if all matched, and in correct order
      return matchSuccess()
    } else {
      return matches.map(_._2).find(!_.matches).getOrElse(
          matchFailure("Array elements expected in different order", path)) // just report first failed match
    }
  }

  /*
   * Matching rules:
   *  - A null value in a pattern only matches a null or missing request value
   *  
   *  - A string value in a pattern is treated as a regular expression and can match strings, 
   *    booleans and numbers in the request (they are first converted to strings). 
   *  
   *  - A number value in a pattern can only match numbers in the request and must match exactly.
   *  
   *  - A boolean value in a pattern can only match boolean in the request and must match exactly.
   *  
   *  - An array in a pattern can only match an array in the request. See 'matchArray()' for more detail.
   *    
   *  - An object in the pattern can only match an object in the request. See 'matchObject()' for more detail.
   */
  private def matchValue(pattern: Any, request: Any, path: String): MatchResult = { // TODO: add better debugging information
    pattern match {

      case null => request match {
        case null => matchSuccess() // only match if both are null
        case _ => matchFailure("Expected null value", path)
      }

      case str: String => request match {
        case (_: String | _: Number | _: Boolean) => { // allow regexp to match any scalar value
          if (request.toString().matches(pattern.toString())) { // assume pattern is a regular expression
            matchSuccess()
          } else {
            matchFailure("Expected '%s' to match '%s'".format(request, pattern), path)
          }
        }
        case _ => matchFailure("Scalar value (string, number or boolean) expected", path)
      }

      case num: Number => request match {
        case _: Number => {
          if (pattern.equals(request)) {
            matchSuccess()
          } else {
            matchFailure("Expected %s, was %s".format(pattern, request), path)
          }
        }
        case _ => matchFailure("Number expected", path)
      }

      case bool: Boolean => request match {
        case _: Boolean => {
          if (pattern.equals(request)) {
            matchSuccess()
          } else {
            matchFailure("Expected %s, was %s".format(pattern, request), path)
          }
        }
        case _ => matchFailure("Boolean expected", path)
      }

      case patternList: Seq[_] => request match {
        case requestList: Seq[_] => matchArray(patternList, requestList, path)
        case _ => matchFailure("Array expected", path)

      }

      case patternMap: collection.Map[_, _] => request match {
        case requestMap: collection.Map[_, _] => matchObject(
            patternMap.asInstanceOf[collection.Map[String, Any]],
            requestMap.asInstanceOf[collection.Map[String, Any]], path) // recursively match objects
        case _ => matchFailure("Object expected", path)
      }

      case _ => throw new RuntimeException("Unexpected type in pattern: " + pattern.getClass)
    }
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case p: JsonBodyPattern => p.pattern.equals(pattern)
      case _ => false
    }

  override def hashCode: Int = pattern.hashCode

}