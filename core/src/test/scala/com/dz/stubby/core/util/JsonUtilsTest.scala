package com.dz.stubby.core.util

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

}