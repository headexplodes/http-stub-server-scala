package com.dz.stubby.core.model

// TODO: use options... but can Jackson handle them?

case class StubParam(
  name: String,
  value: String)

trait StubHeaders[T <: StubHeaders[T]] {
  val headers: List[StubParam]

  def withHeaders(headers: List[StubParam]): T
  
  def getHeader(name: String): Option[String] =
    headers.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getHeaders(name: String): Seq[String] =
    headers.filter(_.name.equalsIgnoreCase(name)).map(_.value)
        
  def addHeader(name: String, value: String): T =
    withHeaders(headers :+ StubParam(name, value))
  def removeHeader(name: String): T =
    withHeaders(headers.filterNot(_.name.equalsIgnoreCase(name)))
  def setHeader(name: String, value: String): T =
    removeHeader(name).addHeader(name, value)
}

trait StubMessage[T <: StubHeaders[T]] extends StubHeaders[T] {
  val body: AnyRef
}

case class StubRequest(
    val method: String = null,
    val path: String = null,
    val params: List[StubParam] = Nil,
    val headers: List[StubParam] = Nil,
    val body: AnyRef = null) extends StubMessage[StubRequest] {

  def getParam(name: String): Option[String] =
    params.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getParams(name: String): Seq[String] =
    params.filter(_.name == name).map(_.value)

  override def withHeaders(headers: List[StubParam]): StubRequest = 
    copy(headers = headers)
}

case class StubResponse(
  val status: Int = 0,
  val headers: List[StubParam] = Nil,
  val body: AnyRef = null) extends StubMessage[StubResponse] {
  
  override def withHeaders(headers: List[StubParam]): StubResponse = 
    copy(headers = headers)
}

case class StubExchange(
  val request: StubRequest,
  val response: StubResponse,
  val delay: Option[Long] = None,
  val script: Option[String] = None)
