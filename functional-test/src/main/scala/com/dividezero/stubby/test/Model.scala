package com.dividezero.stubby.test

case class JsonParam(name: String, value: String) {
  def this(pair: (String, String)) = this(pair._1, pair._2)
}

trait JsonMessage {

  type T <: JsonMessage

  val headers: List[JsonParam]
  val body: Option[AnyRef]

  def copyWith(headers: List[JsonParam] = headers, body: Option[AnyRef] = body): T

  def getHeader(name: String): Option[String] =
    headers.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getHeaders(name: String): Seq[String] =
    headers.filter(_.name.equalsIgnoreCase(name)).map(_.value)

  def addHeader(name: String, value: String): T =
    copyWith(headers :+ JsonParam(name, value))
  def removeHeader(name: String): T =
    copyWith(headers.filterNot(_.name.equalsIgnoreCase(name)))
  def setHeader(name: String, value: String): T#T =
    removeHeader(name).addHeader(name, value)

}

case class JsonRequest(
    path: Option[String] = None,
    method: Option[String] = None, // optional so we can create filters
    params: List[JsonParam] = Nil,
    headers: List[JsonParam] = Nil,
    body: Option[AnyRef] = None,
    bodyType: Option[String] = None) extends JsonMessage {

  override type T = JsonRequest

  def getParam(name: String): Option[String] =
    params.find(_.name.equalsIgnoreCase(name)).map(_.value)
  def getParams(name: String): Seq[String] =
    params.filter(_.name == name).map(_.value)

  override def copyWith(headers: List[JsonParam], body: Option[AnyRef]): JsonRequest =
    copy(headers = headers, body = body)

  def nilLists() = copy( // for after Jackson deserialization (there _is_ a better way...)
    params = if (params != null) params else Nil,
    headers = if (headers != null) headers else Nil)
}

case class JsonResponse(
    status: Option[Int] = None,
    headers: List[JsonParam] = Nil,
    body: Option[AnyRef] = None) extends JsonMessage {

  override type T = JsonResponse

  override def copyWith(headers: List[JsonParam], body: Option[AnyRef]): JsonResponse =
    copy(headers = headers, body = body)

  def nilLists() = copy( // for after Jackson deserialization (there _is_ a better way...)
    headers = if (headers != null) headers else Nil)
}

case class JsonExchange(
    request: JsonRequest,
    response: JsonResponse,
    delay: Option[Int] = None,
    script: Option[String] = None) {

  def nilLists() = copy( // for after Jackson deserialization (there _is_ a better way...)
    request = request.nilLists(),
    response = response.nilLists())
}

case class JsonStubbedExchange(exchange: JsonExchange, attempts: List[AnyRef])
