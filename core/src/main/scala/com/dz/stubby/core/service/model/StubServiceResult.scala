package com.dz.stubby.core.service.model

import com.dz.stubby.core.model.StubResponse

class StubServiceResult( // returned by the 'findMatch' method
    val attempts: List[MatchResult],
    val response: Option[StubResponse],
    val delay: Option[Long]) {
  
  def this(attempts: List[MatchResult]) = this(attempts, None, None)
  
  def matchFound(): Boolean = attempts.exists(_.matches)
  
}
