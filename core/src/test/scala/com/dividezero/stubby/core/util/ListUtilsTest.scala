package com.dividezero.stubby.core.util

import org.scalatest.FunSuite

class ListUtilsTest extends FunSuite {

  import ListUtils._

  test("compress empty list") {
    assert(compress(List()) === List())
  }

  test("single element list") {
    assert(compress(List("a")) === List("a"))
  }

  test("compress unique list") {
    assert(compress(List("a", "b", "c")) === List("a", "b", "c"))
  }

  test("compress consecutive elements") {
    assert(compress(List("a", "a", "b", "a", "c", "c")) === List("a", "b", "a", "c"))
  }

  test("compress list with all elements same") {
    assert(compress(List("a", "a", "a")) === List("a"))
  }

}