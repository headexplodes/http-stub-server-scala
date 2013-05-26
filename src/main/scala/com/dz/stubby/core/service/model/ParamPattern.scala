package com.dz.stubby.core.service.model

case class ParamPattern(val name: String, val pattern: TextPattern) {
  
  override def toString = name + " =~ m/" + pattern + "/"

}
