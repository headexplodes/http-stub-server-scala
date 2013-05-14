package com.dz.stubby

import unfiltered.netty._
import unfiltered.request._
import unfiltered.response._

class Server {
  
  def getRequests =
  def getRequest(index: Int) = Pass
  def deleteRequest(index: Int) = Pass
  
  def getResponses =  ResponseString("GET responses")
  def getResponse(index: Int) = ResponseString("GET response #" + index)
  def deleteResponses =   ResponseString("DELETE responses")
  def deleteResponse(index: Int) =ResponseString("DELETE response #" + id)
  def addResponse = ResponseString("POST responses")
  
}

class AppPlan(server: Server) extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
  
  def intent = {

    case req @ Path(Seg("_control" :: "responses" :: Nil)) => req match {
      case GET(_) =>
      case DELETE(_) =>
      case POST(_) => 
    }

    case req @ Path(Seg("_control" :: "responses" :: id :: Nil)) => req match {
      case GET(_) => server.getResponse(id)
      case DELETE(_) => server.deleteResponse(id)
    }

    case req @ Path(Seg("_control" :: "requests" :: Nil)) => req match {
      case GET(_) => ResponseString("GET requests")
      case DELETE(_) => ResponseString("GET requests")
    }

    case req @ Path(Seg("_control" :: "requests" :: id :: Nil)) => req match {
      case GET(_) => ResponseString("GET request #" + id)
      case DELETE(_) => ResponseString("DELETE request #" + id)
    }

  }
    
}

object Main {
  def main(args: Array[String]) {
    unfiltered.netty.Http(8080).plan(new AppPlan(new Server)).run()
  }
}
