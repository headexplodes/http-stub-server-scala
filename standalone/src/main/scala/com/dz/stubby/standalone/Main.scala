package com.dz.stubby.standalone

import com.dz.stubby.core.service.JsonServiceInterface
import com.dz.stubby.core.service.StubService
import unfiltered.netty.Http
import unfiltered.netty.ServerErrorResponse
import unfiltered.netty.cycle
import unfiltered.request.DELETE
import unfiltered.request.GET
import unfiltered.request.HttpRequest
import unfiltered.request.POST
import unfiltered.request.Path
import unfiltered.request.Seg
import unfiltered.response.ComposeResponse
import unfiltered.response.Pass
import unfiltered.response.ResponseString
import unfiltered.response.JsonContent
import unfiltered.response.Ok

case class JsonResponse(json: String)
  extends ComposeResponse(JsonContent ~> ResponseString(json))

case class EmptyOk(any: Unit)
  extends ComposeResponse(Ok)

class Server {
  import Transformer._

  val service = new StubService
  val jsonService = new JsonServiceInterface(service)

  def getRequests =
    JsonResponse(jsonService.getRequests)
  def getRequest(index: Int) =
    JsonResponse(jsonService.getRequest(index))

  def deleteRequests =
    EmptyOk(jsonService.deleteRequests)
  def deleteRequest(index: Int) =
    EmptyOk(jsonService.deleteRequest(index))

  def getResponses =
    JsonResponse(jsonService.getResponses)
  def getResponse(index: Int) =
    JsonResponse(jsonService.getResponse(index))

  def deleteResponses =
    EmptyOk(jsonService.deleteResponses)
  def deleteResponse(index: Int) =
    EmptyOk(jsonService.deleteResponse(index))

  def addResponse(req: HttpRequest[_]) =
    EmptyOk(jsonService.addResponse(req.inputStream))

  def matchRequest(req: HttpRequest[_]) = {
    val result = service.findMatch(toStubRequest(req))
    // TODO: create stub response extending ResponseWriter (see ResponseString)
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
      case GET(_) => server.getRequests
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
    Http(8080).plan(new AppPlan(new Server)).run()
  }
}
