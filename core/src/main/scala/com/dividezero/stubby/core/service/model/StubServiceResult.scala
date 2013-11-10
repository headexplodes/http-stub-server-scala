package com.dividezero.stubby.core.service.model

import com.dividezero.stubby.core.model.StubResponse

class StubServiceResult( // returned by the 'findMatch' method
    val attempts: List[MatchResult],
    val response: Option[StubResponse],
    val delay: Option[Int]) {
  
  def this(attempts: List[MatchResult]) = this(attempts, None, None)
  
  def matchFound(): Boolean = attempts.exists(_.matches)
  
}
