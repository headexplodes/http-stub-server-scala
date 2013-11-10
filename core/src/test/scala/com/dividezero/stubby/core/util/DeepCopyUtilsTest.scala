package com.dividezero.stubby.core.util

import org.scalatest.FunSuite
import java.util.Arrays

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

  test("copy list") {
    val value = List("a", "b", "c")

    assert(toJava(value) === new java.util.ArrayList(Arrays.asList("a", "b", "c")))
    assert(toScala(toJava(value)) === List("a", "b", "c"))
  }

  test("copy empty map") {
    val value = Map()

    assert(toJava(value) === new java.util.HashMap())
    assert(toScala(toJava(value)) === Map())
  }

  test("copy map") {
    val value = Map("foo" -> "a", "bar" -> "b")
    val expected = new java.util.HashMap[String, Object]() {
      put("foo", "a")
      put("bar", "b")
    }

    assert(toJava(value) === expected)
    assert(toScala(toJava(value)) === Map("foo" -> "a", "bar" -> "b"))
  }

  type AnyJMap = java.util.Map[String, _]
  type AnyJList = java.util.List[_]
  
  type AnyMap = Map[String, _]
  type AnySeq = Seq[_]
  
  test("copy object graph") {
    val value = Map(
      "foo" -> Map("one" -> 1234),
      "bar" -> List("a", "b", Map("is" -> true)))   

    val asJava = toJava(value)
    val asJavaMap = asJava.asInstanceOf[AnyJMap]

    assert(asJavaMap.get("foo").isInstanceOf[AnyJMap])
    assert(asJavaMap.get("foo").asInstanceOf[AnyJMap].get("one") === 1234)
    
    assert(asJavaMap.get("bar").isInstanceOf[AnyJList])
    assert(asJavaMap.get("bar").asInstanceOf[AnyJList].size === 3)
    assert(asJavaMap.get("bar").asInstanceOf[AnyJList].get(0) === "a")
    assert(asJavaMap.get("bar").asInstanceOf[AnyJList].get(1) === "b")
    
    assert(asJavaMap.get("bar").asInstanceOf[AnyJList].get(2).isInstanceOf[AnyJMap])
    assert(asJavaMap.get("bar").asInstanceOf[AnyJList].get(2).asInstanceOf[AnyJMap].get("is") === true)
    
    val asScala = toScala(asJava)
    val asScalaMap = asScala.asInstanceOf[AnyMap]

    assert(asScalaMap("foo").isInstanceOf[AnyMap])
    assert(asScalaMap("foo").asInstanceOf[AnyMap]("one") === 1234)
    
    assert(asScalaMap("bar").isInstanceOf[AnySeq])
    assert(asScalaMap("bar").asInstanceOf[AnySeq].size === 3)
    assert(asScalaMap("bar").asInstanceOf[AnySeq](0) === "a")
    assert(asScalaMap("bar").asInstanceOf[AnySeq](1) === "b")
    
    assert(asScalaMap("bar").asInstanceOf[AnySeq](2).isInstanceOf[AnyMap])
    assert(asScalaMap("bar").asInstanceOf[AnySeq](2).asInstanceOf[AnyMap]("is") === true)
  }

}