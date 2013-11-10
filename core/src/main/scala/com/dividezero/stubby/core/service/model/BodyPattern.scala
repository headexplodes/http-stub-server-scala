package com.dividezero.stubby.core.service.model

import com.dividezero.stubby.core.model.StubMessage

trait BodyPattern {
  def matches(request: StubMessage): MatchField
}
