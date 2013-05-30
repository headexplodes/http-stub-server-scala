package com.dz.stubby.core.util

import scala.collection.mutable.ListBuffer
import java.util.regex.Pattern
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubParam

object RequestFilterBuilder {

  val MethodParam = "method"
  val PathParam = "path"
  val ParamPattern = """^param\[(.+)\]$""".r
  val HeaderPattern = """^header\[(.+)\]$""".r

  def makeFilter(params: Seq[StubParam]): StubRequest =
    params.foldLeft(new StubRequest)(addParam)

  private def addParam(filter: StubRequest, param: StubParam): StubRequest =
    param.name match {
      case MethodParam =>
        filter.copy(method = param.value)
      case PathParam =>
        filter.copy(path = param.value)
      case ParamPattern(paramName) =>
        filter.copy(params = filter.params :+ StubParam(paramName, param.value))
      case HeaderPattern(headerName) =>
        filter.copy(headers = filter.headers :+ StubParam(headerName, param.value))
    }

}