package com.dz.stubby.standalone

import org.apache.commons.io.IOUtils
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.service.JsonServiceInterface
import com.dz.stubby.core.service.NotFoundException
import com.dz.stubby.core.service.StubService
import com.dz.stubby.core.util.JsonUtils
import unfiltered.netty.Http
import unfiltered.netty.ReceivedMessage
import unfiltered.netty.ServerErrorResponse
import unfiltered.netty.cycle
import unfiltered.request.DELETE
import unfiltered.request.GET
import unfiltered.request.HttpRequest
import unfiltered.request.POST
import unfiltered.request.Params
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
import com.dz.stubby.core.util.RequestFilterBuilder
import java.io.File

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

class Server(paths: Seq[File]) {
  import Transformer._

  val service = new StubService
  val jsonService = new JsonServiceInterface(service)
  val fileSource = new FileSource(paths, service, jsonService).loadInitialFiles().watchFolders()

  private def handleNotFound[T >: ResponseFunction[Any]](body: => T): T =
    try {
      body
    } catch {
      case NotFoundException(message) =>
        NotFound ~> ResponseString(message)
    }

  def findRequests(req: HttpRequest[_]) =
    JsonResponse(jsonService.findRequests(createFilter(req)))
  def findRequests(req: HttpRequest[_], wait: Int) =
    JsonResponse(jsonService.findRequests(createFilter(req), wait))

  def getRequest(index: Int) = handleNotFound {
    JsonResponse(jsonService.getRequest(index))
  }

  def deleteRequests() =
    EmptyOk(jsonService.deleteRequests)
  def deleteRequest(index: Int) = handleNotFound {
    EmptyOk(jsonService.deleteRequest(index))
  }

  private def createFilter(req: HttpRequest[_]) =
    RequestFilterBuilder.makeFilter(Transformer.parseQuery(req))

  def getResponses() =
    JsonResponse(jsonService.getResponses)
  def getResponse(index: Int) = handleNotFound {
    JsonResponse(jsonService.getResponse(index))
  }

  def deleteResponses() =
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

object WaitParam extends Params.Extract(
  "wait", Params.first ~> Params.int ~> Params.pred { _ > 0 }
)

class AppPlan(server: Server) extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
  def intent = {
    case req @ Path(Seg("_control" :: "shutdown" :: Nil)) => req match {
      case _ =>
        Main.http.get.stop()
        ResponseString("Shutting Down")
    }
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
      case GET(_) => req match {
        case Params(WaitParam(wait)) => server.findRequests(req, wait)
        case _ => server.findRequests(req)
      }
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

  var http:Option[Http] = None

  def main(args: Array[String]) {
    if (args.length > 0) {
      val paths = parseFileArgs(args.tail)
      val server = new Server(paths)
      Http(args(0).toInt).plan(new AppPlan(server)).beforeStop({ server.fileSource.monitor.stop() }).run()
    } else {
      throw new RuntimeException("Usage: java ... <port>")
    }
  }

  def start(port:Int, paths: List[String]) {
    val server = new Server(paths.flatMap{ n:String => loadFolder(n)})
    http = Some(Http(port).plan(new AppPlan(server)).beforeStop({ server.fileSource.monitor.stop() }))
    http.get.start()
  }

  def parseFileArgs(args: Array[String]) = {
    args.flatMap(loadFolder)
  }

  def loadFolder(name: String) = {
    val folder = new File(name)
    def err = (msg: String) => {
      System.err.println(s"Warning: folder '$name' $msg, skipping")
      None
    }

    if (!folder.exists()) {
      err("does not exist")
    } else if (!folder.isDirectory) {
      err("is not a file")
    } else {
      println(s"Watching folder '$name'...")
      Some(folder)
    }
  }
}
