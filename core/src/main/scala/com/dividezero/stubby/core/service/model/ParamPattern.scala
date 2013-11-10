package com.dividezero.stubby.core.service.model

import com.dividezero.stubby.core.model.StubParam

case class ParamPattern(val name: String, val pattern: TextPattern) {

  def this(name: String, pattern: String) =
    this(name, new TextPattern(pattern))

  override def toString =
    name + " =~ m/" + pattern + "/"

}
