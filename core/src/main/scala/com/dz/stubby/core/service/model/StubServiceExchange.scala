package com.dz.stubby.core.service.model

import scala.collection.mutable.Stack
import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubRequest

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
  override def hashCode = requestPattern.hashCode // hash/equality is based on the request pattern only
  override def equals(obj: Any) = obj match {
    case e: StubServiceExchange => e.requestPattern.equals(requestPattern)
    case _ => false
  }

}