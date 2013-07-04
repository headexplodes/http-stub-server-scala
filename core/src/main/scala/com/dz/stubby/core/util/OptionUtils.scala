package com.dz.stubby.core.util

object OptionUtils {
 
  implicit def toOption[T](x:T) : Option[T] = Option(x)

}