package com.dz.stubby.core

case class StubParam(
  name: String,
  value: String)

class StubMessage(headers: List[StubParam], body: AnyRef)

class StubRequest(
  method: String,
  path: String,
  params: List[StubParam],
  headers: List[StubParam],
  body: AnyRef) extends StubMessage(headers, body)

class StubResponse(
  status: Int,
  headers: List[StubParam],
  body: AnyRef) extends StubMessage(headers, body)

class StubExchange(
  request: StubRequest,
  response: StubResponse,
  delay: Long,
  script: String)

  
  
