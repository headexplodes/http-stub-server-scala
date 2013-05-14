package com.dz.stubby

import unfiltered.netty._
import unfiltered.request._
import unfiltered.response._

trait MyPlan extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse

object Hello extends MyPlan {
  
  def intent = {

    case req @ Path(Seg("_control" :: "responses" :: Nil)) => req match {
      case GET(_) => ResponseString("GET responses")
      case DELETE(_) => ResponseString("DELETE responses")
      case POST(_) => ResponseString("POST responses")
    }

    case req @ Path(Seg("_control" :: "responses" :: id :: Nil)) => req match {
      case GET(_) => ResponseString("GET response #" + id)
      case DELETE(_) => ResponseString("DELETE response #" + id)
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
    unfiltered.netty.Http(8080).plan(Hello).run()
  }
}
