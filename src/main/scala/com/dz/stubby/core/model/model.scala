package com.dz.stubby.core.model

// TODO: use options...

case class StubParam(
  name: String,
  value: String)

abstract class StubMessage(
    val headers: List[StubParam],
    val body: AnyRef) {

  def getHeader(name: String): Option[String] =
    headers.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getHeaders(name: String): Seq[String] =
    headers.filter(_.name.equalsIgnoreCase(name)).map(_.value)
        
}

case class StubRequest(
    val method: String = null,
    val path: String = null,
    val params: List[StubParam] = Nil,
    override val headers: List[StubParam] = Nil,
    override val body: AnyRef = null) extends StubMessage(headers, body) {

  def getParam(name: String): Option[String] =
    params.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getParams(name: String): Seq[String] =
    params.filter(_.name == name).map(_.value)
    
  def addHeader(name: String, value: String): StubRequest =
    this.copy(headers = headers :+ StubParam(name, value))
  def removeHeader(name: String): StubRequest =
    this.copy(headers = headers.filterNot(_.name.equalsIgnoreCase(name)))
  def setHeader(name: String, value: String): StubRequest =
    removeHeader(name).setHeader(name, value)
}

case class StubResponse(
  val status: Int = 0,
  override val headers: List[StubParam] = Nil,
  override val body: AnyRef = null) extends StubMessage(headers, body)

case class StubExchange(
  val request: StubRequest,
  val response: StubResponse,
  val delay: Long = 0,
  val script: String = null)
