package com.dz.stubby.core.util

import org.scalatest.FunSuite

class DeepCopyUtilsTest extends FunSuite {

  import DeepCopyUtils._

  test("copy null") {
    assert(toJava(null) === null)
    assert(toScala(toJava(null)) === null)
  }

  test("copy string") {
    assert(toJava("foo") === "foo")
    assert(toScala(toJava("foo")) === "foo")
  }

  test("copy boolean") {
    val value: java.lang.Boolean = true

    assert(toJava(value) === true)
    assert(toScala(toJava(value)) === true)
  }

  test("copy integer") {
    val value: java.lang.Integer = 1234

    assert(toJava(value) === 1234)
    assert(toScala(toJava(value)) === 1234)
  }

  test("copy long") {
    val value: java.lang.Long = 1234L

    assert(toJava(value) === 1234L)
    assert(toScala(toJava(value)) === 1234L)
  }

  test("copy double") {
    val value: java.lang.Double = 1.234

    assert(toJava(value) === 1.234)
    assert(toScala(toJava(value)) === 1.234)
  }

  test("copy float") {
    val value: java.lang.Double = 1.234

    assert(toJava(value) === 1.234)
    assert(toScala(toJava(value)) === 1.234)
  }

  test("copy BigDecimal") {
    val value = java.math.BigDecimal.valueOf(1.234)

    assert(toJava(value) === value)
    assert(toScala(toJava(value)) === value)
  }
  
  test("copy empty list") {
    val value = List()
    
    assert(toJava(value) === new java.util.ArrayList())
    assert(toScala(toJava(value)) === List())
  }
  
  test("copy empty map") {
    val value = Map()
    
    assert(toJava(value) === new java.util.HashMap())
    assert(toScala(toJava(value)) === Map())
  }
  
  type AnyJMap = java.util.Map[String,_]
  
  test("copy object graph") {
    val value = Map(
        "foo" -> Map("one" -> 1234), 
        "bar" -> List("a", "b", Map("is" -> true)))
    
    val asJava = toJava(value)
    val asJavaMap = asJava.asInstanceOf[AnyJMap]
    
    assert(asJavaMap.get("foo").isInstanceOf[AnyJMap])
        
    val asScala = toScala(asJava)    

  }

}