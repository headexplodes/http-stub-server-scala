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
import org.jboss.netty.buffer.ChannelBuffers
import org.apache.http.NameValuePair

object Transformer {

  import JavaConversions._

  def toStubRequest(src: HttpRequest[ReceivedMessage]): StubRequest = {
    val uri = new URI(src.uri)
    StubRequest(
      Some(src.method.toUpperCase), // method should always be upper-case
      Some(uri.getPath),
      parseQuery(uri).map(p => StubParam(p.getName, p.getValue)),
      toStubHeaders(src).toList,
      convertBody(src)
    )
  }

  private def parseQuery(uri: URI): List[NameValuePair] =
    collectionAsScalaIterable(URLEncodedUtils.parse(uri, "UTF-8")).toList

  private def convertBody(src: HttpRequest[ReceivedMessage]): Option[AnyRef] = {
    if (hasBody(src)) {
      Some(IOUtils.toString(src.inputStream))
    } else {
      None // no body
    }
  }
  
  private def hasBody(req: HttpRequest[ReceivedMessage]): Boolean = 
    req.underlying.request.getContent() != ChannelBuffers.EMPTY_BUFFER

  private def toStubHeaders(src: HttpRequest[_]): Iterator[StubParam] = {
    for (
      name <- src.headerNames;
      value <- src.headers(name)
    ) yield StubParam(name, value)
  }

}