package com.dz.stubby.core.service.model

import org.scalatest.FunSuite
import scala.util.matching.Regex
import org.scalatest.BeforeAndAfter
import org.scalamock.scalatest.MockFactory
import FieldType._
import MatchType._
import scala.collection.immutable.Seq

class MatchResultTest extends FunSuite with BeforeAndAfter {

  val notFoundField = new MatchField(HEADER, "name", "expected", NOT_FOUND, "actual", "message")
  val matchFailureField = new MatchField(HEADER, "name", "expected", MATCH_FAILURE, "actual", "message")
  val matchField = new MatchField(HEADER, "name", "expected", MATCH, "actual", "message")

  before {
    assert(notFoundField.score === 0) // make sure scores are as expected
    assert(matchFailureField.score === 1)
    assert(matchField.score === 2)
  }

  test("test equality") {
    assert(new MatchResult(matchField) === new MatchResult(matchField))
  }

  test("test score added up") {
    val result = new MatchResult(notFoundField, matchFailureField, matchField)

    assert(result.score === 3)
  }

  test("best score first") {
    val result1 = new MatchResult(matchFailureField)
    val result2 = new MatchResult(notFoundField, matchField)

    val sorted = List(result1, result2).sorted

    assert(sorted(0).score === 2)
    assert(sorted(1).score === 1)
  }

  test("matches when all fields match") {
    val result = new MatchResult(matchField)
    assert(result.matches);
  }

  test("doesn't match when not found") {
    val result = new MatchResult(notFoundField)
    assert(!result.matches);
  }

  test("doesn't match when match failed") {
    val result = new MatchResult(matchFailureField)
    assert(!result.matches);
  }

  test("doesn't match when non-matched field exists") {
    val result = new MatchResult(notFoundField, matchField)
    assert(!result.matches);
  }

}