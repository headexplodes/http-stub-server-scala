package com.dz.stubby.core.util

import org.scalatest.FunSuite

class DeepCopyUtilsTest extends FunSuite {

  import DeepCopyUtils._

  test("copy null") {
    assert(toJava(null) === null)
    assert(toScala(null) === null)
  }

  test("copy string") {
    assert(toJava("foo") === "foo")
    assert(toScala("foo") === "foo")
  }

  test("copy boolean") {
    assert(toJava(true) === true)
    assert(toScala(true) === true)
  }

  test("copy integer") {
    assert(toJava(1234) === 1234)
    assert(toScala(1234) === 1234)
  }

  test("copy long") {
    assert(toJava(1234) === 1234)
    assert(toScala(1234) === 1234)
  }

  test("copy double") {
    assert(toJava(1234) === 1234)
    assert(toScala(1234) === 1234)
  }
  
  // TODO: object graphs  

}