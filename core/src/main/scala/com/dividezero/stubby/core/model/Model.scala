package com.dividezero.stubby.core.model

case class StubParam(
  name: String,
  value: String)

trait StubMessage {

  type T <: StubMessage

  val headers: List[StubParam]
  val body: Option[AnyRef]

  def copyWith(headers: List[StubParam] = headers, body: Option[AnyRef] = body): T

  def getHeader(name: String): Option[String] =
    headers.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getHeaders(name: String): Seq[String] =
    headers.filter(_.name.equalsIgnoreCase(name)).map(_.value)

  def addHeader(name: String, value: String): T =
    copyWith(headers :+ StubParam(name, value))
  def removeHeader(name: String): T =
    copyWith(headers.filterNot(_.name.equalsIgnoreCase(name)))
  def setHeader(name: String, value: String): T#T =
    removeHeader(name).addHeader(name, value)

}

case class StubRequest(
    method: Option[String] = None, // optional so we can create filters
    path: Option[String] = None,
    params: List[StubParam] = Nil,
    headers: List[StubParam] = Nil,
    body: Option[AnyRef] = None) extends StubMessage {

  type T = StubRequest

  def getParam(name: String): Option[String] =
    params.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getParams(name: String): Seq[String] =
    params.filter(_.name == name).map(_.value)

  override def copyWith(headers: List[StubParam], body: Option[AnyRef]): StubRequest =
    copy(headers = headers, body = body)

  def nilLists() = copy( // for after Jackson deserialization (there _is_ a better way...)
    params = if (params != null) params else Nil,
    headers = if (headers != null) headers else Nil)
}

case class StubResponse(
    status: Int,
    headers: List[StubParam] = Nil,
    body: Option[AnyRef] = None) extends StubMessage {

  type T = StubResponse

  override def copyWith(headers: List[StubParam], body: Option[AnyRef]): StubResponse =
    copy(headers = headers, body = body)

  def nilLists() = copy( // for after Jackson deserialization (there _is_ a better way...)
    headers = if (headers != null) headers else Nil)
}

case class StubExchange(
    request: StubRequest,
    response: StubResponse,
    delay: Option[Int] = None,
    script: Option[String] = None) {

  def nilLists() = copy( // for after Jackson deserialization (there _is_ a better way...)
    request = request.nilLists,
    response = response.nilLists)
}
