package com.dz.stubby.core

import scala.math.Ordered
import scala.collection.mutable.Stack
import com.dz.stubby.core.model.MatchField
import com.dz.stubby.core.model.MatchType._

class MatchResult(val fields: List[MatchField]) extends Ordered[MatchResult] {
  def matches: Boolean = fields.forall(_.matchType == MATCH)
  def score: Int = fields.foldLeft(0)(_ + _.score)
  override def compare(other: MatchResult) = -score.compareTo(other.score) // highest score first
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
    val result = requestPattern.matches(message)
    if (result.score >= 5) { // only record attempts that match request path
      attempts.push(result)
    }
    result
  }

  override def toString = requestPattern.toString
  override def hashCode = requestPattern.hashCode(); // hash/equality is based on the request pattern only
  override def equals(obj: Any) = obj match {
    case e: StubServiceExchange => e.requestPattern.equals(requestPattern)
    case _ => false
  }

}