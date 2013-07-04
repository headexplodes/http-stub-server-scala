package com.dz.stubby.standalone

import java.net.URI
import scala.collection.JavaConversions
import org.apache.http.client.utils.URLEncodedUtils
import com.dz.stubby.core.model.StubParam
import com.dz.stubby.core.model.StubRequest
import unfiltered.netty.ReceivedMessage
import unfiltered.request.HttpRequest
import org.apache.commons.io.IOUtils
import com.dz.stubby.core.util.OptionUtils

object Transformer {

  import OptionUtils._
  import JavaConversions._

  def toStubRequest(src: HttpRequest[_]): StubRequest = {
    val uri = new URI(src.uri)
    StubRequest(
      src.method.toUpperCase, // method should always be upper-case
      uri.getPath,
      parseQuery(uri).map(p => StubParam(p.getName, p.getValue)),
      toStubHeaders(src).toList,
      convertBody(src)
    )
  }

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