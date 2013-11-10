package com.dividezero.stubby.core.util

import java.util.Collection
import java.util.ArrayList
import scala.collection.JavaConversions

object ListUtils {

  /**
   * Eliminate consecutive elements
   * https://github.com/jrglee/ninety-nine-scala-problems
   */
  def compress[T](list: List[T]): List[T] = list match {
    case head :: next :: tail =>
      if (head == next)
        compress(next :: tail)
      else
        head :: compress(next :: tail)
    case head :: Nil => List(head)
    case Nil => Nil
  }
  
  def compress[T](seq: Seq[T]): List[T] = compress(seq.toList)

}