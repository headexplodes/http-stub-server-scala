package com.dz.stubby.standalone

import unfiltered.request.HttpRequest
import unfiltered.netty.ReceivedMessage
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubParam
import java.net.URI
import org.apache.http.client.utils.URLEncodedUtils
import scala.collection.JavaConversions

object Transformer {

  import JavaConversions._

  def toStubRequest(src: HttpRequest[ReceivedMessage]): StubRequest = {
    val uri = new URI(src.uri)
    StubRequest(
      uri.getPath,
      src.method.toUpperCase, // method should always be upper-case
      parseQuery(uri).map(p => StubParam(p.getName, p.getValue)),
      toStubHeaders(src).toList,
      convertBody(src)
    )
  }
  
  def populateUnfilteredResponse = "TODO"

  def parseQuery(uri: URI) =
    collectionAsScalaIterable(URLEncodedUtils.parse(uri, "UTF-8")).toList

  def convertBody(src: HttpRequest[_]) = {
    "TODO"
  }

  def toStubHeaders(src: HttpRequest[_]): Iterator[StubParam] = {
    for (
      name <- src.headerNames;
      value <- src.headers(name)
    ) yield StubParam(name, value)
  }

}