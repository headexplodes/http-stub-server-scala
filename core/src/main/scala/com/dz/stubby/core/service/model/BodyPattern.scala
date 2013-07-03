package com.dz.stubby.core.service.model

import com.dz.stubby.core.model.StubMessage

trait BodyPattern {
  def matches(request: StubMessage[_]): MatchField
}
