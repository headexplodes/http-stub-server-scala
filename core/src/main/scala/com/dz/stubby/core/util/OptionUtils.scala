package com.dz.stubby.core.util

import scala.language.implicitConversions

object OptionUtils {
 
  implicit def toOption[T](x:T) : Option[T] = Option(x)

}