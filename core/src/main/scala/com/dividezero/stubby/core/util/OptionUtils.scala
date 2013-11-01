package com.dividezero.stubby.core.util

import scala.language.implicitConversions

object OptionUtils extends OptionUtils

trait OptionUtils {
  implicit def toOption[T](x:T) : Option[T] = Option(x)
}

