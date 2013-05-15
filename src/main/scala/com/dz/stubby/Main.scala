package com.dz.stubby

import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import unfiltered.netty._
import unfiltered.request._
import unfiltered.response._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class Server {
  
  def getRequests = Pass
  def getRequest(index: Int) = Pass
  def deleteRequest(index: Int) = Pass
  
  def getResponses =  ResponseString("GET responses")
  def getResponse(index: Int) = ResponseString("GET response #" + index)
  def deleteResponses =   ResponseString("DELETE responses")
  def deleteResponse(index: Int) =ResponseString("DELETE response #" + index)
  def addResponse = ResponseString("POST responses")
  
}

class AppPlan(server: Server) extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
  
  def intent = {

    case req @ Path(Seg("_control" :: "responses" :: Nil)) => req match {
      case GET(_) => Pass
      case DELETE(_) => Pass
      case POST(_) => Pass
    }

    case req @ Path(Seg("_control" :: "responses" :: id :: Nil)) => req match {
      case GET(_) => server.getResponse(id.toInt)
      case DELETE(_) => server.deleteResponse(id.toInt)
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
    
    //unfiltered.netty.Http(8080).plan(new AppPlan(new Server)).run()
    
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
        
  }
}
