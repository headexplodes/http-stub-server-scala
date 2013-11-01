package com.dividezero.stubby.test

import org.apache.http.entity.ContentType
import java.net.URI
import com.dividezero.stubby.core.util.JsonUtils

class Client(baseUri: URI) extends GenericClient(baseUri) {

  def postMessage(message: JsonExchange) =
    executePost("/_control/responses", JsonUtils.serialize(message), ContentType.APPLICATION_JSON).assertOk

  def postMessage(message: String) =
    executePost("/_control/responses", message, ContentType.APPLICATION_JSON).assertOk

  def responses: List[JsonStubbedExchange] =
    executeGet("/_control/responses").assertOk.asJson[List[JsonStubbedExchange]]

  def requests: List[JsonRequest] =
    executeGet("/_control/requests").assertOk.asJson[List[JsonRequest]]

  def findRequests(query: String): List[JsonRequest] =
    executeGet("/_control/requests?" + query).assertOk.asJson[List[JsonRequest]]

  def getResponse(index: Int): JsonStubbedExchange =
    executeGet("/_control/responses/" + index).assertOk.asJson[JsonStubbedExchange]

  def getRequest(index: Int): JsonRequest =
    executeGet("/_control/requests/" + index).assertOk.asJson[JsonRequest]

  def deleteResponses() =
    executeDelete("/_control/responses").assertOk

  def deleteRequests() =
    executeDelete("/_control/requests").assertOk

  def reset() = {
    deleteResponses()
    deleteRequests()
  }

}