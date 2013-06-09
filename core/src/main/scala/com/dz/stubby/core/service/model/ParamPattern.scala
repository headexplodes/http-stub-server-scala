package com.dz.stubby.core.service.model

import com.dz.stubby.core.model.StubParam

case class ParamPattern(val name: String, val pattern: TextPattern) {

  def this(name: String, pattern: String) =
    this(name, new TextPattern(pattern))

//  def matches(param: StubParam): Boolean =
//    name.equalsIgnoreCase(param.name) && pattern.matches(param.value)
//
//  def matchesIgnoreCase(param: StubParam): Boolean = // ie, for headers
//    name.equals(param.name) && pattern.matches(param.value)
//
//  def matchesName(param: StubParam) =
//    name.equals(param.name)
//
//  def matchesNameIgnoreCase(param: StubParam) =
//    name.equalsIgnoreCase(param.name)

  override def toString =
    name + " =~ m/" + pattern + "/"

}
