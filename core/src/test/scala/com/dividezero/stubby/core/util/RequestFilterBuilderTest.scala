package com.dividezero.stubby.core.util

import scala.collection.mutable.ListBuffer
import org.scalatest.FunSuite
import com.dividezero.stubby.core.model.StubParam
import com.dividezero.stubby.core.model.StubRequest

class RequestFilterBuilderTest extends FunSuite {

  import RequestFilterBuilder._

  test("method") {
    val params = List(StubParam("method", "G.T"))
    assert("G.T" === makeFilter(params).method.get)
  }

  test("path") {
    val params = List(StubParam("path", "/foo/.*"))
    assert("/foo/.*" === makeFilter(params).path.get)
  }

  test("params") {
    val params = List(
      StubParam("param[foo]", "bar1"),
      StubParam("param[foo]", "bar2"))
    assert(List("bar1", "bar2") === makeFilter(params).getParams("foo"))
  }

  test("headers") {
    val params = List(
      StubParam("header[X-Foo]", "bar1"),
      StubParam("header[X-Foo]", "bar2"))
    assert(List("bar1", "bar2") === makeFilter(params).getHeaders("X-Foo"))
  }

}