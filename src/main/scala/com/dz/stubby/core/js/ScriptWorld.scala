package com.dz.stubby.core.js

import java.util.ArrayList

import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubMessage
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.util.ListUtils.toJavaList
import com.dz.stubby.core.util.ListUtils.toScalaList

abstract class ScriptMessage(message: StubMessage) {
  def getBody: AnyRef = message.body

  def getHeaders: ArrayList[StubParam] = toJavaList(message.headers)
  def getHeaders(name: String): ArrayList[String] = toJavaList(message.getHeaders(name))
  def getHeader(name: String): String = message.getHeader(name).orNull
}

class ScriptRequest(request: StubRequest) extends ScriptMessage(request) {
  def getMethod = request.method
  def getPath = request.path

  def getParams = toJavaList(request.params)
  def getParams(name: String): ArrayList[String] = toJavaList(request.getParams(name))
  def getParam(name: String): String = request.getParam(name).orNull
}

case class ScriptResponse(
    private var status: Int,
    private var body: AnyRef,
    private var headers: ArrayList[StubParam]) {

  def this(response: StubResponse) =
    this(response.status, response.body, toJavaList(response.headers))

  def getStatus: Int = status
  def setStatus(status: Int): Unit = {
    this.status = status
  }

  def getBody: AnyRef = body
  def setBody(body: AnyRef): Unit = {
    this.body = body
  }

  def getHeaders: ArrayList[StubParam] = headers
  def getHeaders(name: String): ArrayList[String] =
    toJavaList(toScalaList(headers).filter(_.name.equalsIgnoreCase(name)).map(_.value))
  def getHeader(name: String): String =
    toScalaList(headers).find(_.name.equalsIgnoreCase(name)).map(_.value).orNull

  def removeHeader(name: String): Unit = {
    headers = toJavaList(toScalaList(headers).filterNot(_.name.equalsIgnoreCase(name)))
  }
  def setHeader(name: String, value: String): Unit = {
    removeHeader(name)
    headers.add(StubParam(name, value))
  }

  def toStubResponse: StubResponse =
    new StubResponse(status, toScalaList(headers), body)
}

class ScriptWorld(
    private val request: ScriptRequest,
    private val response: ScriptResponse,
    private var delay: Option[Long] = None) {

  def this(request: StubRequest, response: StubResponse, delay: Option[Long]) = this( // TODO: need to perform (and test) deep copy of response JSON
    new ScriptRequest(request),
    new ScriptResponse(response),
    delay)

  def getDelay: java.lang.Long = delay.getOrElse(null.asInstanceOf[Long]).longValue
  def setDelay(delay: java.lang.Long): Unit = {
    this.delay = if (delay == null) null else Some(delay)
  }

  def getRequest: ScriptRequest = request
  def getResponse: ScriptResponse = response

  def result: Pair[StubResponse,Option[Long]] = (response.toStubResponse, delay)

}
