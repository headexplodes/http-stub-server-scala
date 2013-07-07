package com.dz.stubby.standalone

import org.apache.commons.io.IOUtils
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.handler.codec.http.{HttpResponse=>NHttpResponse}

import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.service.JsonServiceInterface
import com.dz.stubby.core.service.NotFoundException
import com.dz.stubby.core.service.StubService
import com.dz.stubby.core.service.model.StubServiceResult
import com.dz.stubby.core.util.JsonUtils

import unfiltered.netty.Http
import unfiltered.netty.ReceivedMessage
import unfiltered.netty.ServerErrorResponse
import unfiltered.netty.cycle
import unfiltered.request.DELETE
import unfiltered.request.GET
import unfiltered.request.HttpRequest
import unfiltered.request.POST
import unfiltered.request.Path
import unfiltered.request.Seg
import unfiltered.response.ComposeResponse
import unfiltered.response.HttpResponse
import unfiltered.response.JsonContent
import unfiltered.response.NotFound
import unfiltered.response.Ok
import unfiltered.response.Responder
import unfiltered.response.ResponseFunction
import unfiltered.response.ResponseString
import unfiltered.response.ResponseWriter

case class JsonResponse(json: String)
  extends ComposeResponse(JsonContent ~> ResponseString(json))

case class EmptyOk(any: Any)
  extends ComposeResponse(Ok)

case class StubUnfilteredResponse(result: StubResponse) extends Responder[Any] {
  def respond(res: HttpResponse[Any]) {
    val out = res.outputStream
    try {
      res.status(result.status)
      result.headers.foreach {
        h => res.header(h.name, h.value)
      }
      result.body match {
        case Some(body: String) => IOUtils.write(body, out)
        case Some(body: AnyRef) => JsonUtils.serialize(out, body) // assume deserialised JSON (ie, a Map or List)         
        case None =>
      }
    } finally {
      out.close()
    }
  }
}

class Server {
  import Transformer._

  val service = new StubService
  val jsonService = new JsonServiceInterface(service)

  private def handleNotFound[T >: ResponseFunction[Any]](body: => T): T =
    try {
      body
    } catch {
      case NotFoundException(message) =>
        NotFound ~> ResponseString(message)
    }
    
  // TODO: need support for filtering requests...

  def getRequests =
    JsonResponse(jsonService.getRequests)
  def getRequest(index: Int) = handleNotFound {
    JsonResponse(jsonService.getRequest(index))
  }

  def deleteRequests =
    EmptyOk(jsonService.deleteRequests)
  def deleteRequest(index: Int) = handleNotFound {
    EmptyOk(jsonService.deleteRequest(index))
  }

  def getResponses =
    JsonResponse(jsonService.getResponses)
  def getResponse(index: Int) = handleNotFound {
    JsonResponse(jsonService.getResponse(index))
  }

  def deleteResponses =
    EmptyOk(jsonService.deleteResponses)
  def deleteResponse(index: Int) = handleNotFound {
    EmptyOk(jsonService.deleteResponse(index))
  }

  def addResponse(req: HttpRequest[_]) =
    EmptyOk(jsonService.addResponse(req.inputStream))

  def matchRequest(req: HttpRequest[ReceivedMessage]) = {
    val result = service.findMatch(toStubRequest(req))
    if (result.matchFound) {
      result.delay.foreach(t => Thread.sleep(t)) // sleep if delay given
      StubUnfilteredResponse(result.response.get)
    } else {
      NotFound ~> ResponseString("No stubbed response found")
    }
  }
}

class AppPlan(server: Server) extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
  def intent = {
    case req @ Path(Seg("_control" :: "responses" :: Nil)) => req match {
      case GET(_) => server.getResponses
      case DELETE(_) => server.deleteResponses
      case POST(_) => server.addResponse(req)
    }
    case req @ Path(Seg("_control" :: "responses" :: id :: Nil)) => req match {
      case GET(_) => server.getResponse(id.toInt)
      case DELETE(_) => server.deleteResponse(id.toInt)
    }
    case req @ Path(Seg("_control" :: "requests" :: Nil)) => req match {
      case GET(_) => server.getRequests // TODO: need support for filtering requests...
      case DELETE(_) => server.deleteRequests
    }
    case req @ Path(Seg("_control" :: "requests" :: id :: Nil)) => req match {
      case GET(_) => server.getRequest(id.toInt)
      case DELETE(_) => server.deleteRequest(id.toInt)
    }
    case req @ _ => {
      server.matchRequest(req)
    }
  }
}

object Main {
  def main(args: Array[String]) {
    if (args.length > 0) {
      Http(args(0).toInt).plan(new AppPlan(new Server)).run()
    } else {
      throw new RuntimeException("Usage: java ... <port>")
    }
  }
}
