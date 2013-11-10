package com.dividezero.stubby.core.service.model

case class MatchResult(fields: List[MatchField]) extends Ordered[MatchResult] {

  def this(fields: MatchField*) =
    this(fields.toList)
    
  def matches: Boolean =
    fields.forall(_.matchType == MatchType.MATCH)

  def score: Int =
    fields.foldLeft(0)(_ + _.score)

  override def compare(other: MatchResult) =
    -score.compareTo(other.score) // highest score first

}
