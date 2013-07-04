package com.dz.stubby.core.js

import java.util.ArrayList
import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubMessage
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.util.DeepCopyUtils
import scala.collection.mutable.Buffer

class ScriptRequest(request: StubRequest) {
  import DeepCopyUtils._

  def getMethod = request.method.get
  def getPath = request.path.get

  def getBody = request.body.map(toJava).orNull

  def getHeaders = toJava(request.headers)
  def getHeaders(name: String) = toJava(request.getHeaders(name))
  def getHeader(name: String) = request.getHeader(name).orNull

  def getParams = toJava(request.params)
  def getParams(name: String) = toJava(request.getParams(name))
  def getParam(name: String) = request.getParam(name).orNull
}

case class ScriptResponse(
    private var status: Int,
    private var body: AnyRef,
    private var headers: Buffer[StubParam]) {

  import DeepCopyUtils._

  def this(response: StubResponse) =
    this(response.status,
      response.body.map(DeepCopyUtils.toJava).orNull, // deep-copy body in to Java classes for JavaScript
      response.headers.toBuffer)

  def getStatus: Int = status
  def setStatus(status: Int): Unit = {
    this.status = status
  }

  def getBody: AnyRef = body
  def setBody(body: AnyRef): Unit = {
    this.body = body
  }

  def getHeaders = toJava(headers) // modifications not persisted
  def getHeaders(name: String) =
    toJava(headers.filter(_.name.equalsIgnoreCase(name)).map(_.value))
  def getHeader(name: String) =
    headers.find(_.name.equalsIgnoreCase(name)).map(_.value).orNull

  def addHeader(name: String, value: String): Unit = {
    headers += StubParam(name, value)
  }
  def removeHeader(name: String): Unit = {
    headers = headers.filterNot(_.name.equalsIgnoreCase(name))
  }
  def setHeader(name: String, value: String): Unit = {
    removeHeader(name)
    headers += StubParam(name, value)
  }

  private def bodyToScala: Option[AnyRef] = 
    if (body != null) Some(toScala(body)) else null
  
  def toStubResponse: StubResponse =
    new StubResponse(status, headers.toList, bodyToScala) // deep-copy body
}

class ScriptWorld(
    private val request: ScriptRequest,
    private val response: ScriptResponse,
    private var delay: Option[Long] = None) {

  def this(request: StubRequest, response: StubResponse, delay: Option[Long]) = this(
    new ScriptRequest(request),
    new ScriptResponse(response),
    delay)

  def getDelay: java.lang.Long = delay.getOrElse(null.asInstanceOf[Long]).longValue
  def setDelay(delay: java.lang.Long): Unit = {
    this.delay = if (delay == null) null else Some(delay)
  }

  def getRequest: ScriptRequest = request
  def getResponse: ScriptResponse = response

  def result: Pair[StubResponse, Option[Long]] = (response.toStubResponse, delay)

}
