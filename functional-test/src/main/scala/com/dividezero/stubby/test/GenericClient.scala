package com.dividezero.stubby.test

import org.apache.http.client.utils.HttpClientUtils
import java.net.URI
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.entity.{StringEntity, ContentType}
import org.apache.http.client.methods.{HttpUriRequest, HttpGet, HttpDelete, HttpPost}
import java.io.IOException

class GenericClient(baseUri: URI) {

  val MaxConnections = 10

  val httpClient: HttpClient = new DefaultHttpClient(createConnectionManager())

  def createConnectionManager(): ClientConnectionManager = {
    val manager = new PoolingClientConnectionManager()
    manager.setDefaultMaxPerRoute(MaxConnections)
    manager.setMaxTotal(MaxConnections)
    manager
  }

  def close() = HttpClientUtils.closeQuietly(httpClient)

  def makeUri(path: URI): URI = baseUri.resolve(path)
  def makeUri(path: String): URI = baseUri.resolve(path)

  def executePost(path: String, body: String, contentType: ContentType): GenericClientResponse = {
    val request = new HttpPost(makeUri(path))
    request.setEntity(new StringEntity(body, contentType))
    execute(request)
  }

  def executeDelete(path: String): GenericClientResponse = execute(new HttpDelete(makeUri(path)))

  def executeGet(path: String): GenericClientResponse = execute(new HttpGet(makeUri(path)))

  def execute(request: HttpUriRequest): GenericClientResponse = {
    try {
      new GenericClientResponse(httpClient.execute(request)); // consumes & releases connection
    } catch {
      case e: IOException =>
        throw new RuntimeException(s"Error sending '${request.getMethod}' to '${request.getURI}'", e)
    }
  }

}