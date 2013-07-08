package com.dz.stubby.core.service.model

import scala.util.matching.Regex
import java.util.regex.Pattern

case class TextPattern(regex: String) extends Regex(regex) {

  override def equals(obj: Any) = obj match {
    case r: Regex => pattern.pattern == r.pattern.pattern
    case p: Pattern => pattern.pattern == p.pattern
    case s: String => pattern.pattern == s
    case _ => false
  }

  def matches(str: String): Boolean =
    anchored.unapplySeq(str).nonEmpty

}