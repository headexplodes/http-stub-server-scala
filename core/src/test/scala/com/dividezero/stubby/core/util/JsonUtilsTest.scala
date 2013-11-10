package com.dividezero.stubby.core.util

import org.scalatest.FunSuite
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

case class TestBean(foo: String)

class JsonUtilsTest extends FunSuite {

  val testBean = TestBean("bar")
  val testJson = """{"foo":"bar"}"""

  test("pretty print") {
    expectResult("{\n  \"foo\" : \"bar\"\n}") {
      JsonUtils.prettyPrint(testBean)
    }
  }

  test("serialize string") {
    expectResult(testJson) {
      JsonUtils.serialize(testBean)
    }
  }

  test("serialize stream") {
    val stream = new ByteArrayOutputStream()
    JsonUtils.serialize(stream, testBean)
    expectResult(testJson) {
      stream.toString
    }
  }

  test("deserialize string") {
    expectResult("bar") {
      JsonUtils.deserialize[TestBean](testJson).foo
    }
  }

  test("deserialize stream") {
    val stream = new ByteArrayInputStream(testJson.getBytes)
    expectResult("bar") {
      JsonUtils.deserialize[TestBean](stream).foo
    }
  }

  val primativesStr = """{
        "integer": 1234,
        "decimal": 1.234,
        "string": "foo",
        "boolean": true,
        "empty": null
      }"""

  def primatives = JsonUtils.deserializeObject(primativesStr)
    .asInstanceOf[scala.collection.mutable.Map[String, _]]

  test("deserialize integer value") {
    assert(primatives("integer") === 1234)
  }

  test("deserialize string value") {
    assert(primatives("string") === "foo")
  }

  test("deserialize boolean value") {
    assert(primatives("boolean") === true)
  }

  test("deserialize null value") {
    assert(primatives("empty") === null)
  }

  test("deserialize decimal value") {
    val map = primatives
    assert(map("decimal").isInstanceOf[java.math.BigDecimal])
    assert(map("decimal") === java.math.BigDecimal.valueOf(1.234))
  }

//  test("should deserialize list") {
//    expectResult(List("foo", "bar")) {
//      JsonUtils.deserialize[List[String]]("""["foo", "bar"]""")
//    }
//  }
//
//  test("should deserialize null list as Nil") {
//    expectResult(Nil) {
//      JsonUtils.deserialize[List[String]]("null")
//    }
//  }

}