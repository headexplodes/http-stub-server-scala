package com.dz.stubby

import unfiltered.request._
import unfiltered.response._

object Main {

  def main(args: Array[String]) {

    val plan = {
      case _ => ResponseString("hello world")
    }
    
    val hello = unfiltered.netty.cycle.Planify(plan)

    unfiltered.netty.Http(8080).plan(hello).run()

  }

}

