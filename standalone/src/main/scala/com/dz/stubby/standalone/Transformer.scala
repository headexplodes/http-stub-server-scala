package com.dz.stubby.standalone

import java.net.URI
import scala.collection.JavaConversions
import org.apache.http.client.utils.URLEncodedUtils
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest
import unfiltered.netty.ReceivedMessage
import unfiltered.request.HttpRequest
import org.apache.commons.io.IOUtils

object Transformer {

  import JavaConversions._

  def toStubRequest(src: HttpRequest[_]): StubRequest = {
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

  private def parseQuery(uri: URI) =
    collectionAsScalaIterable(URLEncodedUtils.parse(uri, "UTF-8")).toList

  private def convertBody(src: HttpRequest[_]) = {
    val stream = src.inputStream
    if (stream != null) {
      IOUtils.toString(stream)
    } else {
      null // no body
    }
  }

  private def toStubHeaders(src: HttpRequest[_]): Iterator[StubParam] = {
    for (
      name <- src.headerNames;
      value <- src.headers(name)
    ) yield StubParam(name, value)
  }

}