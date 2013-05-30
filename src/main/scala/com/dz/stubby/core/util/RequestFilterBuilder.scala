package com.dz.stubby.core.util

import scala.collection.mutable.ListBuffer
import java.util.regex.Pattern
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubParam

class RequestFilterBuilder { // TODO: make functional? :)

  val MethodParam = "method"
  val PathParam = "path"
  val ParamPattern = """^param\[(.+)\]$""".r
  val HeaderPattern = """^header\[(.+)\]$""".r

  var method: String = null
  var path: String = null
  var params: ListBuffer[StubParam] = new ListBuffer
  var headers: ListBuffer[StubParam] = new ListBuffer

  def getFilter =
    new StubRequest(method, path, params.toList, headers.toList)

  def fromParams(params: Seq[StubParam]): RequestFilterBuilder = {
    params.foreach(p => addParam(p.name, p.value))
    this
  }

  private def addParam(name: String, value: String) =
    name match {
      case MethodParam =>
        method = value
      case PathParam =>
        path = value
      case ParamPattern(paramName) =>
        params += StubParam(paramName, value)
      case HeaderPattern(headerName) =>
        headers += StubParam(headerName, value)
    }

}