package com.dividezero.stubby.test

import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import org.scalatest.exceptions.TestFailedException
import org.scalatest.Assertions
import unfiltered.response.Created
import unfiltered.response.Ok
import unfiltered.response.BadRequest
import unfiltered.response.InternalServerError
import com.dividezero.stubby.core.util.JsonUtils

class GenericClientResponse(response: HttpResponse) extends Assertions with HttpStatuses {

  val body = consumeBody(response)

  def consumeBody(response: HttpResponse): Option[String] = {
    if (response.getEntity != null && response.getEntity.getContent != null) {
      Some(EntityUtils.toString(response.getEntity, "UTF-8")) // releases connection
    } else {
      None
    }
  }

  def hasBody: Boolean = !body.isEmpty

  def status: Int = response.getStatusLine.getStatusCode

  def getHeader(name: String): Option[String] = response.getFirstHeader(name) match {
    case h: Header => Some(h.getValue)
    case _ => None
  }

  def getHeaders(name: String): List[String] =
    response.getHeaders(name) match {
      case a: Array[Header] => a.toList.map(_.getValue)
      case _ => Nil
    }

  def isOk: Boolean = (status == Ok)
  
  def assertStatus(expected: Int): GenericClientResponse = {
    assert(status === expected)
    this
  }

  def assertOk: GenericClientResponse = assertStatus(Ok)
  def assertCreated: GenericClientResponse = assertStatus(Created)
  def assertBadRequest: GenericClientResponse = assertStatus(BadRequest)
  def assertNotFound: GenericClientResponse = assertStatus(NotFound)
  def assertInternalServerError: GenericClientResponse = assertStatus(InternalServerError)

  def assertBody(): GenericClientResponse = {
    if (!hasBody) {
      throw new RuntimeException("Response body expected")
    }
    this
  }

  def asJson[T: Manifest]: T = body match {
    case Some(s) => JsonUtils.deserialize[T](s)
    case None => throw new RuntimeException("Response did not has a body")
  }

  def asText: String = body match {
    case Some(s) => s
    case None => throw new RuntimeException("Response did not has a body")
  }

}