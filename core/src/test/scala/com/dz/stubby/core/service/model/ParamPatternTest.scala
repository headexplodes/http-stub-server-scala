package com.dz.stubby.core.service.model

import org.scalatest.FunSuite

class ParamPatternTest extends FunSuite {

  val instance1 = new ParamPattern("foo", new TextPattern("bar"))
  val instance2 = new ParamPattern("foo", new TextPattern("bar"))

  test("equality") {
    assert(instance1 === instance2)
  }

  test("hash code") {
    assert(instance1.hashCode === instance2.hashCode)
  }

}