package com.dz.stubby.core.service.model

import com.dz.stubby.core.model.StubResponse

class StubServiceResult( // returned by the 'findMatch' method
    val attempts: List[MatchResult],
    val response: StubResponse,
    val delay: Long) {
  
  def this(attempts: List[MatchResult]) = this(attempts, null, 0)
  
  def matchFound(): Boolean = attempts.exists(_.matches)
  
}
