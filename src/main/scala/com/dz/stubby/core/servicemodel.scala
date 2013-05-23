package com.dz.stubby.core

import scala.math.Ordered
import scala.collection.mutable.Stack

object FieldType extends Enumeration("PATH", "METHOD", "QUERY_PARAM", "HEADER", "BODY") {
  type FieldType = Value
  val PATH, METHOD, QUERY_PARAM, HEADER, BODY = Value
}

object MatchType extends Enumeration("NOT_FOUND", "MATCH_FAILURE", "MATCH") {
  type MatchType = Value
  val NOT_FOUND, MATCH_FAILURE, MATCH = Value
}

import MatchType._
import FieldType._

class PartialMatchField(
    val fieldType: FieldType,
    val fieldName: String,
    val expectedValue: Any) { // expected value can be a Pattern, a JSON object etc.

  def asMatch(actualValue: Any) = new MatchField(this, MATCH, actualValue, null)
  def asNotFound = new MatchField(this, NOT_FOUND, null, null)
  def asMatchFailure(actualValue: Any) = new MatchField(this, MATCH_FAILURE, actualValue, null)
  def asMatchFailure(actualValue: Any, message: String) = new MatchField(this, MATCH_FAILURE, actualValue, message)

}

case class MatchField(
  val partial: PartialMatchField,
  val matchType: MatchType,
  val actualValue: Any, // could be string, JSON object etc.
  val message: String)
    extends PartialMatchField(
      partial.fieldType,
      partial.fieldName,
      partial.expectedValue) {

  def score: Int = matchType match { // attempt to give some weight to matches so we can guess 'near misses'
    case NOT_FOUND => 0
    case MATCH_FAILURE => fieldType match {
      case PATH | METHOD => 0 // these guys always exist, so the fact that they're found is unimportant
      case HEADER | QUERY_PARAM | BODY => 1 // if found but didn't match  
    }
    case MATCH => fieldType match {
      case PATH => 5 // path is most important in the match
      case _ => 2
    }
  }

}

class MatchResult(val fields: List[MatchField]) extends Ordered[MatchResult] {
  def matches: Boolean = fields.forall(_.matchType == MATCH)
  def score: Int = fields.foldLeft(0)(_ + _.score)
  def compareTo(other: MatchResult) = -score.compareTo(other.score) // highest score first
}

class StubServiceResult( // returned by the 'findMatch' method
    val attempts: List[MatchResult],
    val response: StubResponse,
    val delay: Int) {
  def this(attempts: List[MatchResult]) = this(attempts, null, 0)
  def matchFound(): Boolean = attempts.exists(_.matches)
}

class StubServiceExchange(val exchange: StubExchange) { // wrap exchange model with some extra runtime info

  val requestPattern: RequestPattern = new RequestPattern(exchange.request)
  val attempts: Stack[MatchResult] = new Stack

  def matches(message: StubRequest): MatchResult = {
    val result = requestPattern.findMatch(message)
    if (result.score >= 5) { // only record attempts that match request path
      attempts.push(result)
    }
    result
  }

  override def toString = requestPattern.toString
  override def hashCode = requestPattern.hashCode(); // hash/equality is based on the request pattern only
  override def equals(obj: Object) = obj match {
    case e: StubServiceExchange => e.requestPattern.equals(requestPattern)
    case _ => false
  }

}