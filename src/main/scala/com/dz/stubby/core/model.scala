package com.dz.stubby.core

case class StubParam(
  name: String,
  value: String)

class StubMessage(
  val headers: List[StubParam],
  val body: AnyRef)

class StubRequest(
  val method: String,
  val path: String,
  val params: List[StubParam],
  override val headers: List[StubParam],
  override val body: AnyRef) extends StubMessage(headers, body)

class StubResponse(
  val status: Int,
  override val headers: List[StubParam],
  override val body: AnyRef) extends StubMessage(headers, body)

class StubExchange(
  val request: StubRequest,
  val response: StubResponse,
  val delay: Long,
  val script: String)
