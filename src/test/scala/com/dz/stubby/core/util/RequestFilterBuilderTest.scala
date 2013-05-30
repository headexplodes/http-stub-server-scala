package com.dz.stubby.core.util

import scala.collection.mutable.ListBuffer
import org.scalatest.FunSuite
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest

class RequestFilterBuilderTest extends FunSuite {

  test("method") {
    val params = List(StubParam("method", "G.T"))
    val filter = new RequestFilterBuilder().fromParams(params).getFilter

    assert("G.T" === filter.method)
  }

  test("path") {
    val params = List(StubParam("path", "/foo/.*"))
    val filter = new RequestFilterBuilder().fromParams(params).getFilter

    assert("/foo/.*" === filter.path)
  }

  test("params") {
    val params = List(
      StubParam("param[foo]", "bar1"),
      StubParam("param[foo]", "bar2"))
    val filter = new RequestFilterBuilder().fromParams(params).getFilter

    assert(List("bar1", "bar2") === filter.getParams("foo"))
  }

  test("headers") {
    val params = List(
      StubParam("header[X-Foo]", "bar1"),
      StubParam("header[X-Foo]", "bar2"))
    val filter = new RequestFilterBuilder().fromParams(params).getFilter

    assert(List("bar1", "bar2") === filter.getHeaders("X-Foo"))
  }

}